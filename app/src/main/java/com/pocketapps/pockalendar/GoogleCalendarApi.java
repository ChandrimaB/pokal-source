package com.pocketapps.pockalendar;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import com.pocketapps.pockalendar.CalendarItemObjectModel.CalendarItem;
import com.pocketapps.pockalendar.Utils.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by chandrima on 13/03/18.
 */

public class GoogleCalendarApi {
    public static final String TAG = GoogleCalendarApi.class.getSimpleName();
    public static final String ALLDAYEVENT = "alldayevent";
    private static final String SEPARAOTR = ";";

    private GoogleAccountCredential mCredential;
    private static final String[] SCOPES = {CalendarScopes.CALENDAR_READONLY};
    private static GoogleCalendarApi sGoogleCalendarApi;
    ArrayList<GoogleCalendarEventListener> mGoogleCalendarEventListeners = new ArrayList<>();

    public interface GoogleCalendarEventListener {
        void onCalendarItemsReceived(ArrayList<CalendarItem> calendarList);
        void onCalendarItemsReceivedForToday(ArrayList<CalendarItem> calendarList);
        void onUserRecoverableAuthExceptionDuringCalendarFetch(UserRecoverableAuthIOException e);
    }

    private GoogleCalendarApi(Context context) {
        mCredential = GoogleAccountCredential.usingOAuth2(context, Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());
    }

    // Singleton
    public static GoogleCalendarApi getInstance(Context context) {
        if (sGoogleCalendarApi == null) {
            sGoogleCalendarApi = new GoogleCalendarApi(context);
        }
        return sGoogleCalendarApi;
    }

    public void registerListener(GoogleCalendarEventListener listener) {
        mGoogleCalendarEventListeners.add(listener);
    }

    public void deregisterListener(GoogleCalendarEventListener listener) {
        mGoogleCalendarEventListeners.remove(listener);
    }

    public void setCredentialAccountName(String accountName) {
        mCredential.setSelectedAccountName(accountName);
    }

    public Intent getAccountChooserIntent() {
        return mCredential.newChooseAccountIntent();
    }

    public void getResultsFromApi() {
        new MakeRequestTask(mCredential).execute();
    }

    //----------------------------------------------------Private methods----------------------------------------------//

    private class MakeRequestTask extends AsyncTask<Void, Void, ArrayList<CalendarItem>> {
        private com.google.api.services.calendar.Calendar mService = null;

        MakeRequestTask(GoogleAccountCredential credential) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.calendar.Calendar.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("Pokal")
                    .build();
        }
        protected ArrayList<CalendarItem> doInBackground(Void... params) {
            try {
                return getDataFromApi();
            } catch (Exception e) {
                cancel(true);
                if (e instanceof UserRecoverableAuthIOException) {
                    for (GoogleCalendarEventListener googleCalendarEventListener : mGoogleCalendarEventListeners) {
                        googleCalendarEventListener.onUserRecoverableAuthExceptionDuringCalendarFetch((UserRecoverableAuthIOException)e);
                    }
                }
                return null;
            }
        }

        private ArrayList<CalendarItem> getDataFromApi() throws IOException {
            // List the next 10 events from the primary calendar.
            DateTime now = new DateTime(System.currentTimeMillis());
            Events events = mService.events().list("primary")
                    .setMaxResults(10)
                    .setTimeMin(now)
                    .setOrderBy("startTime")
                    .setSingleEvents(true)
                    .execute();
            List<Event> items = events.getItems();
            ArrayList<CalendarItem> meetingList = new ArrayList<>();
            for (Event event : items) {
                CalendarItem calendarItem = new CalendarItem();
                String start = Utils.getTimeFromDateTime(event.getStart().getDateTime());
                String end = Utils.getTimeFromDateTime(event.getEnd().getDateTime());
                if (start == null)
                    // All-day events don't have start times.
                    start = ALLDAYEVENT;

                if (end == null)
                    end = " ";
                calendarItem.setSubject(event.getSummary());
                calendarItem.setStartTime(start);
                calendarItem.setEndTime(end);
                calendarItem.setLocation(event.getLocation());
                calendarItem.setDate(String.valueOf(event.getStart().getDateTime()));
                calendarItem.setType(Utils.CALENDAR_TYPE_GOOGLE);
                String description = Utils.isEmpty(event.getDescription()) ? "No description" : event.getDescription();
                String hangoutLink = Utils.isEmpty(event.getHangoutLink()) ? "No Hangout link found" : "Hangout link: " + event.getHangoutLink();
                String htmlLink = Utils.isEmpty(event.getHtmlLink()) ? "No HTML meeting link found" : "HTML link: " + event.getHtmlLink();
                calendarItem.setDescription(description + SEPARAOTR + hangoutLink + SEPARAOTR + htmlLink);
                calendarItem.setCreator(event.getCreator().getDisplayName() + SEPARAOTR + event.getCreator().getEmail());
                List attendeeList = event.getAttendees();
                if (attendeeList != null) {
                    ArrayList<String> attendees = new ArrayList<>();
                    for (int i = 0; i < attendeeList.size(); i++) {
                        if (!Utils.isEmpty(String.valueOf(attendeeList.get(i))))
                            attendees.add(String.valueOf(attendeeList.get(i)));
                    }
                    if (attendees.size() != 0)
                        calendarItem.setAttendees(attendees);
                }
                List attachmentList = event.getAttachments();
                if (attachmentList != null) {
                    ArrayList<String> attachments = new ArrayList<>();
                    for (int i = 0; i < attachmentList.size(); i++) {
                        if (!Utils.isEmpty(String.valueOf(attachmentList.get(i))))
                            attachments.add(String.valueOf(attachmentList.get(i)));
                    }
                    if (attachments.size() != 0)
                        calendarItem.setAttachments(attachments);
                }
                meetingList.add(calendarItem);
            }
            return meetingList;
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onPostExecute(ArrayList<CalendarItem> output) {
            if (output == null || output.size() == 0) {
                Log.d(TAG,"No results returned.");
            } else {
                Log.d(TAG,"Data retrieved using the Google Calendar API:");

                ArrayList<CalendarItem> laterMeetings = new ArrayList<>();
                ArrayList<CalendarItem> todaysMeetings = new ArrayList<>();
                for(CalendarItem calendarItem : output) {
                    if (Utils.isSameDay(String.valueOf(calendarItem.getDate()), Utils.getCurrentDate())) {
                        todaysMeetings.add(calendarItem);
                        continue;
                    }
                    laterMeetings.add(calendarItem);
                }
                for (GoogleCalendarEventListener googleCalendarEventListener : mGoogleCalendarEventListeners) {
                    googleCalendarEventListener.onCalendarItemsReceived(laterMeetings);
                    googleCalendarEventListener.onCalendarItemsReceivedForToday(todaysMeetings);
                }
            }
        }
    }
}
