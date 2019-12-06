package com.pocketapps.pockalendar.DetailedWeatherPage.FirstDayDetailedWeather;

import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by chandrima on 03/04/18.
 */

public class WeatherDetailHorizontalItemViewHolder extends RecyclerView.ViewHolder {
    public ImageView weatherIcon;
    public ConstraintLayout mConstraintLayout;
    public TextView timeOfTheDay;
    public TextView condition;
    public TextView temperature;

    public WeatherDetailHorizontalItemViewHolder(View itemView) {
        super(itemView);
        mConstraintLayout = (ConstraintLayout) itemView;
        timeOfTheDay = (TextView) mConstraintLayout.getChildAt(0);
        weatherIcon = (ImageView) mConstraintLayout.getChildAt(1);
        temperature = (TextView) mConstraintLayout.getChildAt(2);
        condition = (TextView) mConstraintLayout.getChildAt(3);
    }
}
