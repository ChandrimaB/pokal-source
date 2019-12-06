package com.pocketapps.pockalendar.DetailedWeatherPage;

import android.content.Context;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pocketapps.pockalendar.DetailedWeatherPage.FirstDayDetailedWeather.WeatherDetailHorizontalViewHolder;
import com.pocketapps.pockalendar.DetailedWeatherPage.FirstDayDetailedWeather.WeatherDetailsHorizontalRecyclerViewAdapter;
import com.pocketapps.pockalendar.HomePage.Weather.WeatherItemsViewHolders.WeatherMoreLinkViewHolder;
import com.pocketapps.pockalendar.HomePage.Weather.WeatherItemsViewHolders.WeatherReportHeaderViewHolder;
import com.pocketapps.pockalendar.HomePage.Weather.WeatherItemsViewHolders.WeatherReportViewHolder;
import com.pocketapps.pockalendar.R;
import com.pocketapps.pockalendar.UserPreferences.PokalSharedPreferences;
import com.pocketapps.pockalendar.Utils.JsonParser;
import com.pocketapps.pockalendar.Utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by chandrima on 22/03/18.
 */

public class FiveDayWeatherRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final int HEADER = 100;
    public static final int NEXTDAYWEATHERDETAILS = 200;
    public static final int TOMORROWHEADER = 300;
    public static final int WEEKLYFORECAST = 400;
    public static final int MORELINK = 500;

    private ArrayList<HashMap<String, String>> mWeatherList = new ArrayList<>();
    private ArrayList<HashMap<String, String>> mTomorrowsDetailedWeatherList = new ArrayList<>();

    private Context mContext;
    private boolean mIsPreferedTemperatureInFarenheit;

    public FiveDayWeatherRecyclerViewAdapter(ArrayList arrayList, ArrayList arrayListDetails, Context context, boolean isPreferedTemperatureInFarenheit) {
        mWeatherList = arrayList;
        mTomorrowsDetailedWeatherList = arrayListDetails;
        mContext = context;
        mIsPreferedTemperatureInFarenheit = isPreferedTemperatureInFarenheit;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = null;
        switch (viewType) {
            case HEADER:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.weather_forecast_header, parent, false);
                return new WeatherReportHeaderViewHolder(v);
            case TOMORROWHEADER:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.weather_forecast_header, parent, false);
                return new WeatherReportHeaderViewHolder(v);
            case NEXTDAYWEATHERDETAILS:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.weather_details_horizontal_recycler_view, parent, false);
                return new WeatherDetailHorizontalViewHolder(v, mContext, mTomorrowsDetailedWeatherList);
            case WEEKLYFORECAST:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.current_weather_report_layout, parent, false);
                return new WeatherReportViewHolder(v, WeatherReportViewHolder.WeatherFetchStatus.CURRENT_WEATHER_RECEIVED);
            case MORELINK:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.open_weather_map_link, parent, false);
                return new WeatherMoreLinkViewHolder(v);
            default:
                return null;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        WeatherReportHeaderViewHolder weatherReportHeaderViewHolder;
        switch (getItemViewType(position)) {
            case HEADER:
                weatherReportHeaderViewHolder = (WeatherReportHeaderViewHolder) holder;
                if (mWeatherList == null) {
                    if (Utils.needLocationPermission(mContext)) {
                        weatherReportHeaderViewHolder.allowAccessButton.setVisibility(View.VISIBLE);
                        return;
                    }
                }
                weatherReportHeaderViewHolder.allowAccessButton.setVisibility(View.GONE);
                break;
            case TOMORROWHEADER:
                weatherReportHeaderViewHolder = (WeatherReportHeaderViewHolder) holder;
                weatherReportHeaderViewHolder.headerText.setText(mContext.getString(R.string.TomorrowsDetails));
                weatherReportHeaderViewHolder.allowAccessButton.setVisibility(View.GONE);
                weatherReportHeaderViewHolder.headerText.setGravity(Gravity.LEFT);
                weatherReportHeaderViewHolder.headerText.setBackgroundColor(mContext.getResources().getColor(R.color.weatherDetailHeader));
                break;
            case NEXTDAYWEATHERDETAILS:
                if (mTomorrowsDetailedWeatherList == null || mTomorrowsDetailedWeatherList.size() == 0)
                    return;
                WeatherDetailHorizontalViewHolder weatherDetailHorizontalViewHolder = (WeatherDetailHorizontalViewHolder) holder;
                weatherDetailHorizontalViewHolder.notifyWeatherUpdated(mTomorrowsDetailedWeatherList);
                break;
            case WEEKLYFORECAST:
                if (mWeatherList == null || mWeatherList.size() == 0)
                    return;
                WeatherReportViewHolder weatherReportViewHolder = (WeatherReportViewHolder) holder;
                weatherReportViewHolder.currentTime.setText(mWeatherList.get(getActualPosition(position)).get(JsonParser.WEATHERCONDITIONSTRING).substring(0, 1).toUpperCase() + mWeatherList.get(getActualPosition(position)).get(JsonParser.WEATHERCONDITIONSTRING).substring(1));
                weatherReportViewHolder.currentCity.setVisibility(View.GONE);
                weatherReportViewHolder.date.setText(Utils.getDayAndDate(mWeatherList.get(getActualPosition(position)).get(JsonParser.WEATHERDATE)));
                weatherReportViewHolder.temperature.setText(mContext.getString(R.string.WeatherReportCurrentTemperature,
                        String.valueOf(Math.round(Double.valueOf(mWeatherList.get(getActualPosition(position)).get(JsonParser.WEATHERCURRENTTEMPSTRING))))));
                weatherReportViewHolder.temperatureUnit.setText(mIsPreferedTemperatureInFarenheit ? mContext.getString(R.string.WeatherReportTemperatureFahrenheitPrefered) : mContext.getString((R.string.WeatherReportTemperatureCelciusPrefered)));
                weatherReportViewHolder.weatherConditions.setVisibility(View.GONE);
                weatherReportViewHolder.weatherIcon.setImageDrawable(ResourcesCompat.getDrawable(
                        mContext.getResources(), Utils.getIcon(mWeatherList.get(getActualPosition(position)).get(JsonParser.WEATHERCONDITIONSTRING), mWeatherList.get(getActualPosition(position)).get(JsonParser.WEATHERDATE)), null));
                break;
        }
    }

    @Override
    public int getItemCount() {
        if (mWeatherList == null)
            return 1; // only header needs to be shown if there is no weather data
        return mWeatherList.size() + 4; // all items from weatherlist and headers + 1st day's detailed weather + step count+openweathermap link
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0)
            return HEADER;
        if (position == 2)
            return TOMORROWHEADER;
        if (position == 3)
            return NEXTDAYWEATHERDETAILS;
        if (position == 8)
            return MORELINK;
        else
            return WEEKLYFORECAST;
    }

    public void notifyWeatherListDataChanged(ArrayList arrayList, ArrayList arrayListDetails, Context context, boolean isPreferedTemperatureInFarenheit) {
        mWeatherList = arrayList;
        mTomorrowsDetailedWeatherList = arrayListDetails;
        mIsPreferedTemperatureInFarenheit = isPreferedTemperatureInFarenheit;
        mContext = context;
        notifyDataSetChanged();
    }

    private int getActualPosition(int position) {
        if (position == 1) {
            return position - 1; //For the first day in weekly forecast to be shown at positon 1 of recyclerview, after header at position 0 - but first item of arraylist
        }
        return position - 3; //For the rest of the weekly forecast items, shown after header - position 0, 1 day weather and details + header - positions 1 and 2 - but 2nd item onward in arraylist
    }
}
