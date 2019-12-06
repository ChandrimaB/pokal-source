package com.pocketapps.pockalendar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.pocketapps.pockalendar.UserPreferences.PokalSharedPreferences;
import com.pocketapps.pockalendar.Utils.Utils;

/**
 * Created by chandrima on 18/04/18.
 */

public class CalendarEventsBroadcastReceiver extends BroadcastReceiver {
    private PokalSharedPreferences mPokalSharedPreferences;
    private GoogleCalendarApi mGoogleCalendarApi;

    @Override
    public void onReceive(Context context, Intent intent) {
        mPokalSharedPreferences = PokalSharedPreferences.getInstance(context);
        mGoogleCalendarApi = GoogleCalendarApi.getInstance(context);
        if (mPokalSharedPreferences.isGoogleCalendarSyncOn() && !Utils.needGoogleCalendarPermissions(context)) {
            mGoogleCalendarApi.getResultsFromApi();
        }
    }
}
