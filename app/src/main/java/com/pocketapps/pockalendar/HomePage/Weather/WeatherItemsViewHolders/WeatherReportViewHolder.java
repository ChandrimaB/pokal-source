package com.pocketapps.pockalendar.HomePage.Weather.WeatherItemsViewHolders;

import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by chandrima on 22/03/18.
 */

public class WeatherReportViewHolder extends RecyclerView.ViewHolder {

    public enum WeatherFetchStatus {
        LOC_PERMISSION_DENIED,
        NO_WEATHER_DATA,
        CURRENT_WEATHER_RECEIVED
    }

    public enum TemperatureUnit {
        FAHRENHEIT,
        CELCIUS
    }

    public ImageView weatherIcon;
    public TextView currentTime;
    public TextView currentCity;
    public TextView date;
    public TextView temperature;
    public TextView weatherConditions;
    public TextView temperatureUnit;
    public Button allowPermission;
    public ConstraintLayout mConstraintLayout;

    public WeatherReportViewHolder(View itemView, WeatherFetchStatus weatherFetchStatus) {
        super(itemView);

        mConstraintLayout = (ConstraintLayout)itemView;

        if (weatherFetchStatus == WeatherFetchStatus.LOC_PERMISSION_DENIED || weatherFetchStatus == WeatherFetchStatus.NO_WEATHER_DATA) {
            allowPermission = (Button) mConstraintLayout.getChildAt(1);
            return;
        }
        weatherIcon = (ImageView) mConstraintLayout.getChildAt(0);
        currentTime = (TextView) mConstraintLayout.getChildAt(1);
        currentCity = (TextView)mConstraintLayout.getChildAt(2);
        date = (TextView)mConstraintLayout.getChildAt(3);
        temperature = (TextView)mConstraintLayout.getChildAt(4);
        temperatureUnit = (TextView)mConstraintLayout.getChildAt(5);
        weatherConditions = (TextView)mConstraintLayout.getChildAt(6);
    }
}