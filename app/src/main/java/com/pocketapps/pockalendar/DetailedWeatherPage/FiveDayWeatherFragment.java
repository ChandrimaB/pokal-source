package com.pocketapps.pockalendar.DetailedWeatherPage;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pocketapps.pockalendar.R;
import com.pocketapps.pockalendar.UserPreferences.PokalSharedPreferences;
import com.pocketapps.pockalendar.Utils.JsonParser;
import com.pocketapps.pockalendar.Utils.Utils;
import com.pocketapps.pockalendar.WeatherService;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by chandrima on 22/03/18.
 * Second fragment in HomeActivity's view pager.
 * Shows detailed 5 day weather report.
 */

public class FiveDayWeatherFragment extends Fragment implements LocationListener, WeatherService.WeatherServiceListener {

    private RecyclerView mRecyclerView;
    private FiveDayWeatherRecyclerViewAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ArrayList<HashMap<String, String>> mWeatherLoad;

    private PokalSharedPreferences mPokalSharedPreferences;
    private WeatherService mService;
    private boolean mBound = false;
    private Context mContext;

    //-----------------------------------------------Life cycle Methods----------------------------------------------------//
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.weather_data_layout, container, false);
        mContext = getActivity().getApplicationContext();
        mPokalSharedPreferences = PokalSharedPreferences.getInstance(mContext);
        mLayoutManager = new LinearLayoutManager(mContext);
        setRetainInstance(true);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRecyclerView = (RecyclerView) getActivity().findViewById(R.id.weather_data_recycler_view);
        setUpRecyclerView();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!Utils.needLocationPermission(mContext)) {
            Intent intent = new Intent(getActivity(), WeatherService.class);
            getActivity().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    public void onStop() {
        if (mBound) {
            mService.deregisterListener(FiveDayWeatherFragment.this);
            mService.clear();
            getActivity().unbindService(mConnection);
            mBound = false;
        }
        super.onStop();
    }

    //-----------------------------------------------Event handlers----------------------------------------------------//

    @Override
    public void updateCurrentTime() {
        //NOTHING TO DO
    }

    @Override
    public void updateWeatherFromStore() {
        ArrayList<HashMap<String, String>> weatherFromStore = mPokalSharedPreferences.getWeeklyWeatherFromStore();
        ArrayList<HashMap<String, String>> weatherDetailsFromStore = mPokalSharedPreferences.getTomorrowsDetailedWeatherFromStore();
        if (!Utils.hasData(weatherFromStore) || !Utils.hasData(weatherDetailsFromStore)) {
            forceFetchFiveDayWeatherForecast();
            return;
        }
        notifyWeatherListChanged(weatherFromStore, weatherDetailsFromStore);
    }

    @Override
    public void onWeatherUpdateReceived(HashMap hashMap) {
        //NOTHING TO DO
    }

    public void onLocationPermissionNotGranted() {
        notifyWeatherListChanged(null, null);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case Utils.REQUEST_PERMISSION_ALL:
                if (grantResults.length == 0) {
                    onLocationPermissionNotGranted();
                    return;
                }
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    if (mBound) {
                        long lastFetchedAt = mPokalSharedPreferences.getLastWeatherForecastTimeStamp();
                        mService.updateFiveDayWeatherEveryHour(lastFetchedAt, mPokalSharedPreferences.isPreferedTemperatureInFarenheit());
                        return;
                    }
                    Intent intent = new Intent(getActivity(), WeatherService.class);
                    getActivity().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
                    return;
                }
                onLocationPermissionNotGranted();
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onWeeklyWeatherReceived(ArrayList arrayList) {
        mWeatherLoad = arrayList;
        ArrayList<HashMap<String, String>> refinedArrayList = new ArrayList<>();
        ArrayList<HashMap<String, String>> tomorrowsFrequentDetails = new ArrayList<>();
        for (HashMap hashMap : mWeatherLoad) {
            int index = mWeatherLoad.indexOf(hashMap);
            //get all data for tomorrow
            if (isTomorrow(index)) {
                tomorrowsFrequentDetails.add(mWeatherLoad.get(index));
            }
            if (Utils.isNoon(mWeatherLoad.get(index).get(JsonParser.WEATHERDATE))) {
                refinedArrayList.add(mWeatherLoad.get(index));
            }
        }
        notifyWeatherListChanged(refinedArrayList, tomorrowsFrequentDetails);
        mPokalSharedPreferences.setLastWeatherForecastTimestamp(System.currentTimeMillis());
        mPokalSharedPreferences.saveWeeklyWeatherToStore(refinedArrayList);
        mPokalSharedPreferences.saveTomorrowsDetailedWeatherToStore(tomorrowsFrequentDetails);
    }

    @Override
    public void onLocationChanged(Location location) {
        forceFetchFiveDayWeatherForecast();
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    //-----------------------------------------------Private methods----------------------------------------------------//

    private void setUpRecyclerView() {
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(mLayoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL);
        mRecyclerView.addItemDecoration(dividerItemDecoration);
        mAdapter = new FiveDayWeatherRecyclerViewAdapter(null, null, mContext, mPokalSharedPreferences.isPreferedTemperatureInFarenheit());
        mRecyclerView.setAdapter(mAdapter);
    }

    private boolean isTomorrow(int position) {
        return Utils.isSameDay(mWeatherLoad.get(position).get(JsonParser.WEATHERDATE), Utils.getTomorrowDate());
    }

    private void notifyWeatherListChanged(ArrayList<HashMap<String, String>> arrayList, ArrayList<HashMap<String, String>> arrayListDetails) {
        if (!isAdded())
            return;
        mAdapter.notifyWeatherListDataChanged(arrayList, arrayListDetails, mContext, mPokalSharedPreferences.isPreferedTemperatureInFarenheit());
    }

    private void forceFetchFiveDayWeatherForecast() {
        mPokalSharedPreferences.clearWeather();
        if(mBound) {
            long lastFetchedAt = mPokalSharedPreferences.getLastWeatherForecastTimeStamp();
            mService.updateFiveDayWeatherEveryHour(lastFetchedAt, mPokalSharedPreferences.isPreferedTemperatureInFarenheit());
        }
    }

    //Binding with WeatherService
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            WeatherService.PokalBinder binder = (WeatherService.PokalBinder) service;
            mService = binder.getService();
            mBound = true;
            if (mBound) {
                mService.registerListener(FiveDayWeatherFragment.this);
                long lastFetchedAt = mPokalSharedPreferences.getLastWeatherForecastTimeStamp();
                mService.updateFiveDayWeatherEveryHour(lastFetchedAt, mPokalSharedPreferences.isPreferedTemperatureInFarenheit());
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mService = null;
            mBound = false;
        }
    };
}
