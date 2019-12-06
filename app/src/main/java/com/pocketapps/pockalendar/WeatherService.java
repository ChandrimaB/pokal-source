package com.pocketapps.pockalendar;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.pocketapps.pockalendar.UserPreferences.PokalSharedPreferences;
import com.pocketapps.pockalendar.Utils.JsonParser;
import com.pocketapps.pockalendar.Utils.Utils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by chandrima on 26/03/18.
 */

public class WeatherService extends Service {

    public static final String URLSTRING = "http://api.openweathermap.org/data/2.5/weather?";
    public static final String URLCITYNAME = "q=";
    public static final String URLLATITUDE = "lat=";
    public static final String URLLONGITUDE = "&lon=";
    public static final String OPENWEATHERAPPID = "&APPID=c3669abed779320d0f762e96305ba978";
    public static final String TEMPINFARENHEIGHT = "&units=imperial";
    public static final String TEMPINCELCIUS = "&units=metric";
    public static final String FIVEDAYFORCASTURL = "http://api.openweathermap.org/data/2.5/forecast?";

    public static final String WEATHER_SUNNY = "sunny";
    public static final String WEATHER_RAIN = "rain";
    public static final String WEATHER_MIST = "mist";
    public static final String WEATHER_HAZE = "haze";
    public static final String WEATHER_MIST_DAY = "mistd";
    public static final String WEATHER_HAZE_DAY = "hazed";
    public static final String WEATHER_MIST_NIGHT = "mistn";
    public static final String WEATHER_HAZE_NIGHT = "hazen";
    public static final String WEATHER_STORM = "storm";
    public static final String WEATHER_THUNDER = "thunder";
    public static final String WEATHER_CLEAR = "clear";
    public static final String WEATHER_CLEAR_DAY = "cleard";
    public static final String WEATHER_CLEAR_NIGHT = "clearn";
    public static final String WEATHER_SNOW = "snow";
    public static final String WEATHER_HAIL = "hail";
    public static final String WEATHER_CLOUDY = "cloud";

    private Handler mCurrentTimeUpdateHandler;
    private Timer mFetchCurrentTimeTimer;
    private Handler mWeatherUpdateHandler;
    private Timer mFetchWeatherTimer;
    private Handler mWeatherForecastHandler;
    private Timer mFetchForecastTimer;
    private double mLongitude, mLatitude;
    private LocationManager mLocationManager;
    private WeatherService.WeatherParams mWeatherParams = null;
    private WeatherService.WeatherApi mWeatherApi = null;
    static ArrayList<WeatherServiceListener> mWeatherServiceListener = new ArrayList<>();
    private final IBinder mPokalBinder = new PokalBinder();

    public interface WeatherServiceListener {
        void updateCurrentTime();
        void updateWeatherFromStore();
        void onWeatherUpdateReceived(HashMap hashMap);
        void onWeeklyWeatherReceived(ArrayList arrayList);
    }

    public static class WeatherParams {
        String latitide;
        String longitude;
        boolean isCurrentDayWeather;
        boolean isTempUnitFahrenheit;

        public WeatherParams(String latitide, String longitude, boolean isCurrentDayWeather, boolean isTempUnitFahrenheit) {
            this.latitide = latitide;
            this.longitude = longitude;
            this.isCurrentDayWeather = isCurrentDayWeather;
            this.isTempUnitFahrenheit = isTempUnitFahrenheit;
        }
    }

    //--------------------------------------------------------------Service------------------------------------------------//
    public class PokalBinder extends Binder {
        public WeatherService getService() {
            return WeatherService.this;
        }
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mPokalBinder;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        clear();
    }

    //-----------------------------------------------------Register event listeners---------------------------------------------//
    public void registerListener(WeatherServiceListener weatherServiceListener) {
        mWeatherServiceListener.add(weatherServiceListener);
    }

    public void deregisterListener(WeatherServiceListener weatherServiceListener) {
        mWeatherServiceListener.remove(weatherServiceListener);
    }

    // -------------------------------------------------------Service methods----------------------------------------------------//
    /* Update current time every 1 minute */
    public  void updateCurrentTime() {
        mCurrentTimeUpdateHandler = new Handler();
        mFetchCurrentTimeTimer = new Timer();
        final TimerTask task = new TimerTask() {
            @Override
            public void run() {
                if (mCurrentTimeUpdateHandler == null)
                    return;
                mCurrentTimeUpdateHandler.post(new Runnable() {
                    public void run() {
                        try {
                            for (WeatherServiceListener weatherServiceListener : mWeatherServiceListener)
                                weatherServiceListener.updateCurrentTime();
                        } catch (Exception e) {
                            Log.d("updateCurrentTime:", e.toString());
                        }
                    }
                });
            }
        };
        mFetchCurrentTimeTimer.schedule(task, 0, Utils.ONEMINUTE);
    }

    /* fetch today's weather every 10 minutes */
    public void updateWeatherEveryTenMinutes(final long lastWeatherFetchTime, final boolean isPreferedTemperatureInFahrenheit) {
        mWeatherUpdateHandler = new Handler();
        mFetchWeatherTimer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                if (mWeatherUpdateHandler == null)
                    return;
                mWeatherUpdateHandler.post(new Runnable() {
                    public void run() {
                        try {
                            if (Utils.hasEnoughTimeElapsedSinceLastFetch(lastWeatherFetchTime, true)) {
                                Log.d("updateWeatherEvTenMins:", " fetching weather");
                                fetchWeatherUpdate(isPreferedTemperatureInFahrenheit);
                                return;
                            }
                            for (WeatherServiceListener weatherServiceListener : mWeatherServiceListener)
                                weatherServiceListener.updateWeatherFromStore();
                        } catch (Exception e) {
                            Log.d("updateWeatherEvTenMins:", e.toString());
                        }
                    }
                });
            }
        };
        mFetchWeatherTimer.schedule(task, 0, Utils.TENMINUTES);
    }

    /* fetch weekly weather every 1 hour */
    public void updateFiveDayWeatherEveryHour(final long lastWeatherFetchTime, final boolean isPreferedTemperatureInFahrenheit) {
        mWeatherForecastHandler = new Handler();
        mFetchForecastTimer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                if (mWeatherForecastHandler == null)
                    return;
                mWeatherForecastHandler.post(new Runnable() {
                    public void run() {
                        try {
                            if (Utils.hasEnoughTimeElapsedSinceLastFetch(lastWeatherFetchTime, false)) {
                                Log.d("updateFivDayWEveryHour:", " fetching weather");
                                fetchFiveDayWeatherForcast(isPreferedTemperatureInFahrenheit);
                                return;
                            }
                            for (WeatherServiceListener weatherServiceListener : mWeatherServiceListener)
                                weatherServiceListener.updateWeatherFromStore();
                        } catch (Exception e) {
                            Log.d("updateFivDayWEveryHour:", e.toString());
                        }
                    }
                });
            }
        };
        mFetchForecastTimer.schedule(task, 0, Utils.SIXTYMINUTES);
    }

    /* Clear resources*/
    public void clear() {
        if (mFetchCurrentTimeTimer != null) {
            mFetchCurrentTimeTimer.purge();
            mFetchCurrentTimeTimer.cancel();
        }
        if (mFetchWeatherTimer != null) {
            mFetchWeatherTimer.purge();
            mFetchWeatherTimer.cancel();
        }
        mCurrentTimeUpdateHandler = null;
        mWeatherUpdateHandler = null;
        if(mWeatherApi != null) {
            mWeatherApi.cancel(true);
        }
    }
//---------------------------------------------------------Async task------------------------------------------------------//
    public static class WeatherApi extends AsyncTask<WeatherParams, Void, String> {
        boolean isCurrentDayWeather;
        boolean isTemperatureUnitFahrenheit;
        @Override
        protected String doInBackground(WeatherParams... weatherParams) {
            String result = "";
            isCurrentDayWeather = weatherParams[0].isCurrentDayWeather;
            isTemperatureUnitFahrenheit = weatherParams[0].isTempUnitFahrenheit;
            HttpURLConnection conn = null;
            try {
                URL url = Utils.createWeatherUrlWithLatitudeLongitude(weatherParams[0].latitide, weatherParams[0].longitude, isCurrentDayWeather, isTemperatureUnitFahrenheit);
                conn = (HttpURLConnection) url.openConnection();
                InputStream in = new BufferedInputStream(conn.getInputStream());
                if (in != null) {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
                    String line = "";

                    while ((line = bufferedReader.readLine()) != null)
                        result += line;
                }
                in.close();
                return result;
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (conn != null)
                    conn.disconnect();
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (isCurrentDayWeather) {
                Log.d("WeatherApi, current day", "Received");
                for(WeatherServiceListener listener : mWeatherServiceListener){
                    listener.onWeatherUpdateReceived(JsonParser.parseCurrentDayWeatherJson(result));
                }
                return;
            }
            Log.d("WeatherApi, weekly", "Received");
            for(WeatherServiceListener listener : mWeatherServiceListener){
                listener.onWeeklyWeatherReceived(JsonParser.parseWeeklyForecastJson(result));
            }
        }
    }

    //------------------------------------------------------private methods--------------------------------------------------//
    @SuppressLint("MissingPermission")
    private void getCoordinates() {
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(getApplicationContext().LOCATION_SERVICE);
        }
        @SuppressLint("MissingPermission") // This is okay because permissions are checked before these methods are even called
        Location location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location == null)
            location = mLocationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
        mLongitude = location.getLongitude();
        mLatitude = location.getLatitude();
    }

    private void fetchWeatherUpdate(boolean isPreferedTemperatureInFarenheit) {
        getCoordinates();
        mWeatherParams = new WeatherService.WeatherParams(Double.toString(mLatitude), Double.toString(mLongitude), true, isPreferedTemperatureInFarenheit);
        mWeatherApi = new WeatherService.WeatherApi();
        mWeatherApi.execute(mWeatherParams);
    }

    private void fetchFiveDayWeatherForcast(boolean isPreferedTemperatureInFahrenheit) {
        getCoordinates();
        mWeatherParams = new WeatherService.WeatherParams(Double.toString(mLatitude), Double.toString(mLongitude), false, isPreferedTemperatureInFahrenheit);
        mWeatherApi = new WeatherService.WeatherApi();
        mWeatherApi.execute(mWeatherParams);
    }
}
