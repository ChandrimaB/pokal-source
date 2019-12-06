package com.pocketapps.pockalendar.SchedulePage;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.pocketapps.pockalendar.GoogleCalendarApi;
import com.pocketapps.pockalendar.R;
import com.pocketapps.pockalendar.SchedulePage.CalendarDetailsPage.CalendarDetailsActivity;
import com.pocketapps.pockalendar.CalendarItemObjectModel.CalendarItem;
import com.pocketapps.pockalendar.SchedulePage.CalendarDetailsPage.OnCalendarItemClickListener;
import com.pocketapps.pockalendar.UserPreferences.PokalSharedPreferences;
import com.pocketapps.pockalendar.Utils.Utils;

import java.util.ArrayList;

/**
 * Created by chandrima on 04/04/18.
 */

public class ScheduleFragment extends Fragment implements GoogleCalendarApi.GoogleCalendarEventListener, OnCalendarItemClickListener {
    private RecyclerView mRecyclerView;
    private FullCalendarRecyclerViewAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ArrayList<CalendarItem> mPokalCalendarItems = new ArrayList<>(0);
    private ArrayList<CalendarItem> mNotesList = new ArrayList<>();
    private ArrayList<CalendarItem> mTaskList = new ArrayList<>();
    private ArrayList<CalendarItem> mGoogleCalendarList = new ArrayList<>();
    private GoogleCalendarApi mGoogleCalendarApi;
    private Context mContext;
    private PokalSharedPreferences mPokalSharedPreferences;

    //--------------------------------------------------------------Life cycle methods------------------------------------------//
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.calendar_data_layout, container, false);
        mContext = getActivity().getApplicationContext();
        mLayoutManager = new LinearLayoutManager(getActivity());
        mGoogleCalendarApi = GoogleCalendarApi.getInstance(mContext);
        mPokalSharedPreferences = PokalSharedPreferences.getInstance(getActivity().getApplicationContext());
        setRetainInstance(true);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRecyclerView = (RecyclerView) getActivity().findViewById(R.id.calendar_recycler_view);
        setUpRecyclerView();
    }

    @Override
    public void onStart() {
        super.onStart();
        mPokalCalendarItems.clear();
        mGoogleCalendarApi.registerListener(this);
        int numberOfItems = mPokalCalendarItems.size();
        supplyGoogleCalendar(mGoogleCalendarList);
        supplyTasksFromSharedPref();
        supplyNotesFromSharedPref();
        if (hasChanged(numberOfItems))
            mAdapter.notifyCalendarChanged(mPokalCalendarItems, mContext);
    }

    @Override
    public void onStop() {
        mGoogleCalendarApi.deregisterListener(this);
        super.onStop();
    }

    //------------------------------------------------------------------Event handlers------------------------------------------//
    @Override
    public void onCalendarItemsReceived(ArrayList<CalendarItem> calendarList) {
        if (calendarList == null || calendarList.size() == 0)
            return;

        mPokalCalendarItems.clear();
        if (mNotesList != null)
            mNotesList.clear();
        supplyGoogleCalendar(calendarList);
        supplyTasksFromSharedPref();
        supplyNotesFromSharedPref();
        mAdapter.notifyCalendarChanged(mPokalCalendarItems, mContext);
    }

    @Override
    public void onCalendarItemsReceivedForToday(ArrayList<CalendarItem> calendarList) {
        //NOTHING TO DO
    }

    @Override
    public void onUserRecoverableAuthExceptionDuringCalendarFetch(UserRecoverableAuthIOException e) {

    }

    @Override
    public void onCalendarItemClick(CalendarItem calendarItem) {
        Intent intent = new Intent(getActivity(), CalendarDetailsActivity.class);
        intent.putExtra(OnCalendarItemClickListener.CALENDAR_EXTRA, calendarItem);
        getActivity().startActivity(intent);
    }

    @Override
    public void onEmptyCalendarClick() {
        
    }

    //------------------------------------------------------------------Private methods------------------------------------------//
    private void setUpRecyclerView() {
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new FullCalendarRecyclerViewAdapter(mPokalCalendarItems, mContext, this, mPokalSharedPreferences);
        mRecyclerView.setAdapter(mAdapter);
        addSwipeToDeleteFeature();
    }

    private void supplyNotesFromSharedPref() {
        if (mNotesList != null && mNotesList.size() != 0)
            mNotesList.clear();
        mNotesList = mPokalSharedPreferences.getCalendarItems(Utils.POKAL_NOTE);
        if (mNotesList == null || mNotesList.size() == 0)
            return;
        mPokalCalendarItems.addAll(mNotesList);
    }

    private void supplyTasksFromSharedPref() {
        if (mTaskList != null && mTaskList.size() != 0)
            mTaskList.clear();
        mTaskList = mPokalSharedPreferences.getCalendarItems(Utils.POKAL_TASK);
        if (mTaskList == null || mTaskList.size() == 0)
            return;
        mPokalCalendarItems.addAll(mTaskList);
    }

    private void supplyGoogleCalendar(ArrayList<CalendarItem> calendarList) {
        mGoogleCalendarList = calendarList;

        if (mPokalCalendarItems == null || mPokalCalendarItems.size() == 0) {
            mPokalCalendarItems.addAll(calendarList);
            return;
        }
        ArrayList<CalendarItem> newList = new ArrayList<>();

        for (CalendarItem item : calendarList) {
            boolean alreadyExists = false;
            for (CalendarItem existingItem : mPokalCalendarItems) {
                if (existingItem.getType().equals(Utils.CALENDAR_TYPE_GOOGLE)) {
                    if (existingItem.equals(item)) {
                        alreadyExists = true;
                        break;
                    }
                }
            }
            if (!alreadyExists)
                newList.add(item);
        }

        mPokalCalendarItems.addAll(newList);
    }

    private void addSwipeToDeleteFeature() {
        SwipeToDeleteController controller = new SwipeToDeleteController(mAdapter, mContext);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(controller);
        itemTouchHelper.attachToRecyclerView(mRecyclerView);
    }

    private boolean hasChanged(int old) {
        return old != mPokalCalendarItems.size();
    }
}
