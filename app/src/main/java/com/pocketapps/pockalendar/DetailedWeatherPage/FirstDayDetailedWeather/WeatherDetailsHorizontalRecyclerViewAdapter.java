package com.pocketapps.pockalendar.DetailedWeatherPage.FirstDayDetailedWeather;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pocketapps.pockalendar.DetailedWeatherPage.FirstDayDetailedWeather.WeatherDetailHorizontalItemViewHolder;
import com.pocketapps.pockalendar.R;
import com.pocketapps.pockalendar.Utils.JsonParser;
import com.pocketapps.pockalendar.Utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by chandrima on 03/04/18.
 */

public class WeatherDetailsHorizontalRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<HashMap<String, String>> mDetailedTomorrowWeatherList;
    private Context mContext;

    public WeatherDetailsHorizontalRecyclerViewAdapter(ArrayList<HashMap<String, String>> detailList, Context context) {
        mContext = context;
        mDetailedTomorrowWeatherList = detailList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.weather_details_horizontal_item_layout, parent, false);
        return new WeatherDetailHorizontalItemViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        WeatherDetailHorizontalItemViewHolder weatherDetailHorizontalItemViewHolder = (WeatherDetailHorizontalItemViewHolder) holder;
        weatherDetailHorizontalItemViewHolder.weatherIcon.setImageDrawable(ResourcesCompat.getDrawable(
                mContext.getResources(), Utils.getIcon(mDetailedTomorrowWeatherList.get(position).get(JsonParser.WEATHERCONDITIONSTRING), mDetailedTomorrowWeatherList.get(position).get(JsonParser.WEATHERDATE)), null));
        weatherDetailHorizontalItemViewHolder.temperature.setText(mContext.getString(R.string.WeatherReportCurrentTemperature,
                String.valueOf(Math.round(Double.valueOf(mDetailedTomorrowWeatherList.get(position).get(JsonParser.WEATHERCURRENTTEMPSTRING))))));
        weatherDetailHorizontalItemViewHolder.timeOfTheDay.setText(Utils.getTimeFromDate(mDetailedTomorrowWeatherList.get(position).get(JsonParser.WEATHERDATE)));
        weatherDetailHorizontalItemViewHolder.condition.setText(mDetailedTomorrowWeatherList.get(position).get(JsonParser.WEATHERCONDITIONSTRING));
    }

    @Override
    public int getItemCount() {
        if (mDetailedTomorrowWeatherList == null)
            return 0;
        return mDetailedTomorrowWeatherList.size();
    }
}
