package com.pocketapps.pockalendar.HomePage;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pocketapps.pockalendar.HomePage.AutomaticMessaging.EventReminderItemViewHolder;
import com.pocketapps.pockalendar.HomePage.Calendar.CalendarHeaderViewHolder;
import com.pocketapps.pockalendar.HomePage.Calendar.CalendarItemViewHolder;
import com.pocketapps.pockalendar.HomePage.Weather.AllowLocationAccessButtonClickListener;
import com.pocketapps.pockalendar.HomePage.Weather.WeatherItemsViewHolders.WeatherReportViewHolder;
import com.pocketapps.pockalendar.HomePage.Weather.WeatherItemsViewHolders.WeatherReportViewHolder.TemperatureUnit;
import com.pocketapps.pockalendar.HomePage.Weather.WeatherItemsViewHolders.WeatherReportViewHolder.WeatherFetchStatus;
import com.pocketapps.pockalendar.R;
import com.pocketapps.pockalendar.CalendarItemObjectModel.CalendarItem;
import com.pocketapps.pockalendar.SchedulePage.CalendarDetailsPage.OnCalendarItemClickListener;
import com.pocketapps.pockalendar.SendWishesCalendar.SendBirthdayWishesActivity;
import com.pocketapps.pockalendar.SettingsPage.SettingsActivity;
import com.pocketapps.pockalendar.Utils.JsonParser;
import com.pocketapps.pockalendar.Utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by chandrima on 14/03/18.
 */

public class HomeRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements View.OnClickListener, CalendarItemViewHolder.OnCalendarItemViewHolderClickListener {

    private static final int CANNOTFETCHWEATHER = 100;
    private static final int RECEIVEDWEATHER = 101;
    private static final int CALENDARHEADER = 200;
    private static final int NOCALENDARITEMS = 300;
    private static final int RECEIVEDCALENDAR = 301;
    private static final int BIRTHDAYREM = 400;

    private TemperatureUnit mTemperatureUnit = TemperatureUnit.FAHRENHEIT;

    private WeatherFetchStatus mWeatherFetchStatus;
    private HashMap<String, String> mWeatherMap;
    private ArrayList<CalendarItem> mCalendarItems;
    private Context mContext;
    private String temperature;
    private AllowLocationAccessButtonClickListener mListener;
    private boolean mIsPreferedTemperatureInFarenheit;
    private boolean mIsTempUnitClicked = false;
    private OnCalendarItemClickListener mCalendarItemClickListener;

    public HomeRecyclerViewAdapter(HashMap weatherMap, ArrayList<CalendarItem> calendarItems, boolean permitted, Context context, AllowLocationAccessButtonClickListener listener, boolean isPreferedTempUnitFahrenheit, OnCalendarItemClickListener calendarItemClickListener) {
        mContext = context;
        mListener = listener;
        mCalendarItemClickListener = calendarItemClickListener;
        processWeatherData(weatherMap, permitted, isPreferedTempUnitFahrenheit);
        processCalendarData(calendarItems);
    }

    private void processWeatherData(HashMap hashMap, boolean permitted, boolean isPreferedTemperatureInFarenheit) {
        if (!permitted) {
            mWeatherFetchStatus = WeatherFetchStatus.LOC_PERMISSION_DENIED;
            return;
        }

        if (hashMap != null && hashMap.size() != 0) {
            mWeatherMap = hashMap;
            mWeatherFetchStatus = WeatherFetchStatus.CURRENT_WEATHER_RECEIVED;
            mIsPreferedTemperatureInFarenheit = isPreferedTemperatureInFarenheit;
            mTemperatureUnit = mIsPreferedTemperatureInFarenheit ? TemperatureUnit.FAHRENHEIT : TemperatureUnit.CELCIUS;
            return;
        }
        mWeatherFetchStatus = WeatherFetchStatus.NO_WEATHER_DATA;
    }

    private void processCalendarData(ArrayList<CalendarItem> calendarItems) {
        if (calendarItems != null && calendarItems.size() != 0)
            mCalendarItems = calendarItems;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case CANNOTFETCHWEATHER:
                return new WeatherReportViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.current_weather_no_report_layout, parent, false),
                        mWeatherFetchStatus);
            case RECEIVEDWEATHER:
                return new WeatherReportViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.current_weather_report_layout, parent, false),
                        mWeatherFetchStatus);
            case CALENDARHEADER:
                return new CalendarHeaderViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.calendar_items_header, parent, false));
            case NOCALENDARITEMS:
                return new CalendarItemViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.calendar_nothing_to_show, parent, false), false, this);
            case RECEIVEDCALENDAR:
                return new CalendarItemViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.calendar_item_layout, parent, false), true, this);
            case BIRTHDAYREM:
                return new EventReminderItemViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.events_and_more_layout, parent, false));
        }
        return  null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case CANNOTFETCHWEATHER:
                break;
            case RECEIVEDWEATHER:
                if (mWeatherMap == null || mWeatherMap.size() == 0)
                    return;
                WeatherReportViewHolder weatherReportViewHolder = (WeatherReportViewHolder)holder;
                if (mWeatherFetchStatus == WeatherFetchStatus.CURRENT_WEATHER_RECEIVED) {
                    weatherReportViewHolder.currentTime.setText(Utils.getCurrentTime());
                    weatherReportViewHolder.currentCity.setText(mWeatherMap.get(JsonParser.WEATHERCURRENTCITYSTRING));
                    weatherReportViewHolder.date.setText(Utils.getCurrentDayAndDate());
                    weatherReportViewHolder.temperature.setText(getCurrentTemp());
                    weatherReportViewHolder.temperature.setOnClickListener(this);
                    weatherReportViewHolder.temperatureUnit.setText(mIsTempUnitClicked ? getUnit() : getPreferedUnit());
                    weatherReportViewHolder.temperatureUnit.setOnClickListener(this);
                    weatherReportViewHolder.weatherConditions.setText(getCurrentCondition());
                    weatherReportViewHolder.weatherIcon.setImageDrawable(ResourcesCompat.getDrawable(
                            mContext.getResources(), Utils.getIcon(mWeatherMap.get(JsonParser.WEATHERCONDITIONSTRING), null), null));
                    return;
                }
                if (mWeatherFetchStatus == WeatherFetchStatus.LOC_PERMISSION_DENIED) {
                    weatherReportViewHolder.allowPermission.setVisibility(View.VISIBLE);
                    weatherReportViewHolder.allowPermission.setOnClickListener(this);
                    return;
                }
                weatherReportViewHolder.allowPermission.setVisibility(View.GONE);
                return;
            case CALENDARHEADER:
                //Calendar header, nothing to do
                return;
            case NOCALENDARITEMS:

                break;
            case RECEIVEDCALENDAR:
                if (mCalendarItems == null || mCalendarItems.size() == 0)
                    return;
                CalendarItemViewHolder calendarItemViewHolder = (CalendarItemViewHolder) holder;
                calendarItemViewHolder.meetingIndicator.setBackground(mContext.getResources().getDrawable(Utils.getCalendarIndicationForType(mCalendarItems.get(position - 2).getType())));
                calendarItemViewHolder.subject.setText(mCalendarItems.get(position - 2).getSubject());
                calendarItemViewHolder.date.setVisibility(View.GONE);
                String startTime = mCalendarItems.get(position - 2).getStartTime();
                calendarItemViewHolder.startTime.setText(mContext.getString(R.string.MeetingStartTime, startTime));
                String endTime = mCalendarItems.get(position - 2).getEndTime();
                if (Utils.isAllDay(startTime)) {
                    calendarItemViewHolder.endTime.setVisibility(View.GONE);
                } else {
                    calendarItemViewHolder.endTime.setVisibility(View.VISIBLE);
                    calendarItemViewHolder.endTime.setText(endTime);
                }
                String location = mCalendarItems.get(position - 2).getLocation();
                if (Utils.isEmpty(location)) {
                    calendarItemViewHolder.location.setVisibility(View.INVISIBLE);
                    return;
                }
                calendarItemViewHolder.location.setText(location);
                break;
            case BIRTHDAYREM:
                EventReminderItemViewHolder eventReminderItemViewHolder = (EventReminderItemViewHolder) holder;
                eventReminderItemViewHolder.settingsText.setOnClickListener(this);
                eventReminderItemViewHolder.settingImage.setOnClickListener(this);
                eventReminderItemViewHolder.sendBirthdayText.setOnClickListener(this);
                eventReminderItemViewHolder.sendBirthdayImage.setOnClickListener(this);
        }
    }

    @Override
    public int getItemCount() {
        int calItems = mCalendarItems == null || mCalendarItems.size() == 0 ? 1 : mCalendarItems.size();
        return 1 + 1 + 1 + calItems;
    }

    @Override
    public int getItemViewType(int position) {
        final int birthdayPos = mCalendarItems == null || mCalendarItems.size() == 0 ? 3 : 2+mCalendarItems.size();
        if (position == birthdayPos) {
            return BIRTHDAYREM;
        }
        switch (position) {
            case 0:
                if (mWeatherFetchStatus == WeatherFetchStatus.LOC_PERMISSION_DENIED || mWeatherFetchStatus == WeatherFetchStatus.NO_WEATHER_DATA)
                    return CANNOTFETCHWEATHER;
                return RECEIVEDWEATHER;
            case 1:
                return CALENDARHEADER;
            default:
                if (mCalendarItems == null || mCalendarItems.size() == 0)
                    return NOCALENDARITEMS;
                return RECEIVEDCALENDAR;
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.currentTemperature:
            case R.id.currentTemperatureUnit:
                changeTemperatureUnit();
                break;
            case R.id.locPermissionButton:
                mListener.onButtonClicked();
                break;
            case R.id.settings:
            case R.id.settingsText:
                showSettingsActivity();
                break;
            case R.id.specialReminders:
            case R.id.specialReminderText:
                showBirthdayActivity();
                break;
        }
    }

    @Override
    public void onClick(int position) {
        if (mCalendarItems == null || mCalendarItems.size() == 0) {
            mCalendarItemClickListener.onEmptyCalendarClick();
            return;
        }
        mCalendarItemClickListener.onCalendarItemClick(mCalendarItems.get(position - 2));
    }

    public void notifyDataSetUpdated(HashMap weatherMap, ArrayList<CalendarItem> calendarItems, boolean permitted, Context context, AllowLocationAccessButtonClickListener listener, boolean isPreferedTemperatureInFarenheit, boolean hasWeatherChanged, boolean hasCalendarChanged) {
        mContext = context;
        mListener = listener;
        if (hasWeatherChanged) {
            processWeatherData(weatherMap, permitted, isPreferedTemperatureInFarenheit);
            clearClicks();
        }
        if (hasCalendarChanged)
            processCalendarData(calendarItems);

        notifyDataSetChanged();
    }

    public void clearClicks() {
        mIsTempUnitClicked = false;

    }

    //-----------------------------------------------------------Private Methods---------------------------------------------//

    private void changeTemperatureUnit() {
        mIsTempUnitClicked = true;
        String currentTemp = String.valueOf(Math.round(Double.valueOf(mWeatherMap.get(JsonParser.WEATHERCURRENTTEMPSTRING))));
        if (mTemperatureUnit == TemperatureUnit.FAHRENHEIT) {
            temperature = mContext.getString(R.string.WeatherReportCurrentTemperature, mIsPreferedTemperatureInFarenheit ?
                    Utils.fahrenheitToCelcius(currentTemp) : currentTemp);

            mTemperatureUnit = TemperatureUnit.CELCIUS;
            notifyItemChanged(0);
            return;
        }
        temperature = mContext.getString(R.string.WeatherReportCurrentTemperature, mIsPreferedTemperatureInFarenheit ?
                currentTemp : Utils.celciusToFahrenheit(currentTemp));
        mTemperatureUnit = TemperatureUnit.FAHRENHEIT;
        notifyItemChanged(0);
    }

    private String getCurrentTemp() {
        return !Utils.isEmpty(temperature) ? temperature :
                mContext.getString(R.string.WeatherReportCurrentTemperature,
                        String.valueOf(Math.round(Double.valueOf(mWeatherMap.get(JsonParser.WEATHERCURRENTTEMPSTRING)))));
    }

    private String getUnit() {
        return mTemperatureUnit == TemperatureUnit.FAHRENHEIT ?
                mContext.getString(R.string.WeatherReportTemperatureFahrenheit) :
                mContext.getString(R.string.WeatherReportTemperatureCelcius);
    }

    private String getPreferedUnit() {
        return mIsPreferedTemperatureInFarenheit ? mContext.getString(R.string.WeatherReportTemperatureFahrenheit) : mContext.getString(R.string.WeatherReportTemperatureCelcius);
    }

    private String getCurrentCondition() {
        return mContext.getString(R.string.WeatherReportCurrentCondition,
                mWeatherMap.get(JsonParser.WEATHERCONDITIONSTRING).substring(0, 1).toUpperCase() + mWeatherMap.get(JsonParser.WEATHERCONDITIONSTRING).substring(1));
    }

    private void showSettingsActivity() {
        Intent intent = new Intent(mContext, SettingsActivity.class);
        mContext.startActivity(intent);
    }

    private void showBirthdayActivity() {
        Intent intent = new Intent(mContext, SendBirthdayWishesActivity.class);
        mContext.startActivity(intent);
    }
}

