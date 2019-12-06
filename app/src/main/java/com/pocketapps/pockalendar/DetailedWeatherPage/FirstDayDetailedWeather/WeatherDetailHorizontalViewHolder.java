package com.pocketapps.pockalendar.DetailedWeatherPage.FirstDayDetailedWeather;

import android.content.Context;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.pocketapps.pockalendar.R;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by chandrima on 03/04/18.
 */

public class WeatherDetailHorizontalViewHolder extends RecyclerView.ViewHolder {
    public RecyclerView mRecyclerView;
    LinearLayoutManager mLayoutManager;
    DividerItemDecoration mDividerItemDecoration;
    private WeatherDetailsHorizontalRecyclerViewAdapter mAdapter;
    ArrayList<HashMap<String, String>> mTomorrowsDetailedWeatherList = new ArrayList<>();

    public WeatherDetailHorizontalViewHolder(View itemView, Context context, ArrayList<HashMap<String, String>> tomorrowsDetailedWeatherList) {
        super(itemView);
        mRecyclerView = (RecyclerView) itemView;
        mTomorrowsDetailedWeatherList = tomorrowsDetailedWeatherList;
        mLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
        mDividerItemDecoration = new DividerItemDecoration(context, DividerItemDecoration.HORIZONTAL);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.addItemDecoration(mDividerItemDecoration);
        mAdapter = new WeatherDetailsHorizontalRecyclerViewAdapter(mTomorrowsDetailedWeatherList, context);
        mRecyclerView.setAdapter(mAdapter);
    }

    public void notifyWeatherUpdated(ArrayList<HashMap<String, String>> tomorrowsDetailedWeatherList) {
        mTomorrowsDetailedWeatherList = tomorrowsDetailedWeatherList;
        mAdapter.notifyDataSetChanged();
    }
}
