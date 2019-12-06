package com.pocketapps.pockalendar.HomePage;


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
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;

import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.pocketapps.pockalendar.HomePage.Weather.AllowLocationAccessButtonClickListener;
import com.pocketapps.pockalendar.R;
import com.pocketapps.pockalendar.SchedulePage.CalendarDetailsPage.CalendarDetailsActivity;
import com.pocketapps.pockalendar.CalendarItemObjectModel.CalendarItem;
import com.pocketapps.pockalendar.SchedulePage.CalendarDetailsPage.OnCalendarItemClickListener;
import com.pocketapps.pockalendar.UserPreferences.PokalSharedPreferences;
import com.pocketapps.pockalendar.GoogleCalendarApi;
import com.pocketapps.pockalendar.Utils.Utils;
import com.pocketapps.pockalendar.WeatherService;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by chandrima on 07/03/18.
 */

public class HomeFragment extends Fragment implements AllowLocationAccessButtonClickListener, WeatherService.WeatherServiceListener, LocationListener, GoogleCalendarApi.GoogleCalendarEventListener, OnCalendarItemClickListener, View.OnClickListener, BottomNavigationView.OnNavigationItemSelectedListener {

    private static final String BOTTOMNAVSHOWN = "bottomnavshown";
    private static final int AUTH_CODE_REQUEST_CODE = 381;

    private PokalSharedPreferences mPokalSharedPreferences;
    private GoogleCalendarApi mGoogleCalendarApi;

    private RecyclerView mRecyclerView;
    private HomeRecyclerViewAdapter mAdapter;
    private FloatingActionButton mFloatingActionButton;
    private BottomNavigationView mBottomNavigationView;

    private WeatherService mService;
    private boolean mBound = false;
    private Context mContext;
    private boolean mIsBottomNavAnimationComplete = true;

    private ArrayList<CalendarItem> mGoogleCalendarItems = new ArrayList();
    private HashMap<String, String> mWeatherMap = new HashMap<>();

    //-------------------------------------------------------------Life cycle methods------------------------------------------//

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity().getApplicationContext();
        mPokalSharedPreferences = PokalSharedPreferences.getInstance(mContext);
        mGoogleCalendarApi = GoogleCalendarApi.getInstance(mContext);
        if (mPokalSharedPreferences.isGoogleCalendarSyncOn() && !Utils.needGoogleCalendarPermissions(mContext)) {
            String accountName = mPokalSharedPreferences.getEmail();
            if (accountName != null && Utils.isGmailAccount(accountName)) {
                mGoogleCalendarApi.setCredentialAccountName(accountName);
            }
        }
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.home_screen_layout, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRecyclerView = (RecyclerView)getActivity().findViewById(R.id.home_screen_recycler_view);

        mFloatingActionButton = (FloatingActionButton)getActivity().findViewById(R.id.home_screen_fab);
        mFloatingActionButton.setOnClickListener(this);

        mBottomNavigationView = (BottomNavigationView) getActivity().findViewById(R.id.navigation);
        mBottomNavigationView.setOnNavigationItemSelectedListener(this);

        setUpRecyclerView();

        if (mGoogleCalendarItems == null || mGoogleCalendarItems.size() == 0) {
            if (mPokalSharedPreferences.isGoogleCalendarSyncOn() && !Utils.needGoogleCalendarPermissions(mContext)) {
                mGoogleCalendarApi.getResultsFromApi();
                return;
            }
        }

        if (mGoogleCalendarItems != null && mGoogleCalendarItems.size() != 0) {
            onCalendarItemsReceivedForToday(mGoogleCalendarItems);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mGoogleCalendarApi.registerListener(this);
        if (!Utils.needLocationPermission(mContext) && !mBound) {
            Intent intent = new Intent(getActivity(), WeatherService.class);
            getActivity().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    public void onStop() {
        if (mBound) {
            mService.deregisterListener(HomeFragment.this);
            mService.clear();
            getActivity().unbindService(mConnection);
            mBound = false;
        }
        mGoogleCalendarApi.deregisterListener(this);
        super.onStop();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(BOTTOMNAVSHOWN, !mFloatingActionButton.isShown());
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            boolean shouldShowBottomNavigation = savedInstanceState.getBoolean(BOTTOMNAVSHOWN);
            mFloatingActionButton.setVisibility(shouldShowBottomNavigation ? View.GONE : View.VISIBLE);
            mBottomNavigationView.setVisibility(shouldShowBottomNavigation ? View.VISIBLE : View.GONE);
        }
    }

    //------------------------------------------------------------------Event handlers------------------------------------------//
    @Override
    public void onWeatherUpdateReceived(HashMap hashMap) {
        mWeatherMap = hashMap;
        notifyAdapterChanged(true, true, false);
        mPokalSharedPreferences.setLastWeatherFetchTimestamp(System.currentTimeMillis());
        mPokalSharedPreferences.saveTodaysWeatherToStore(hashMap);
    }

    @Override
    public void onWeeklyWeatherReceived(ArrayList arrayList) {
        //NOTHING TO DO
    }

    private void onLocationPermissionNotGranted() {
        if (!isAdded())
            return;
        mWeatherMap = null;
        notifyAdapterChanged(false, false, false);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case Utils.REQUEST_PERMISSION_ALL:
                if (grantResults.length == 0) {
                    onLocationPermissionNotGranted();
                    //TODO add onContactsPermissionNotGranted();
                    return;
                }
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showWeather();
                } else {
                    onLocationPermissionNotGranted();
            }

                if (grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    showGoogleCalendar();
                } else {

                }

            case Utils.REQUEST_PERMISSION_LOCATION:
                if (grantResults.length == 0) {
                    onLocationPermissionNotGranted();
                    return;
                }
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showWeather();
                    return;
                }
                onLocationPermissionNotGranted();
            case Utils.REQUEST_PERMISSION_GET_ACCOUNTS:
                if (grantResults.length == 0) {
                    //TODO onContactsPermissionsNotGranted();
                    return;
                }
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showGoogleCalendar();
                    return;
                }
                //TODO onContactsPermissionsNotGranted();

            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onButtonClicked() {

    }

    @Override
    public void updateCurrentTime() {
        mAdapter.notifyItemChanged(0);
    }

    @Override
    public void updateWeatherFromStore() {
        mWeatherMap = mPokalSharedPreferences.getTodaysWeatherFromStore();
        try {
            notifyAdapterChanged(true, true, false);
        } catch (NullPointerException e) {
            forceFetchTodaysWeather();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        forceFetchTodaysWeather();
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
        //NOTHING TO DO
    }

    @Override
    public void onProviderEnabled(String s) {
        //NOTHING TO DO
    }

    @Override
    public void onProviderDisabled(String s) {
        //NOTHING TO DO
    }

    @Override
    public void onCalendarItemsReceived(ArrayList<CalendarItem> calendarList) {
        //NOTHING TO DO
    }

    @Override
    public void onCalendarItemsReceivedForToday(ArrayList<CalendarItem> calendarList) {
        if (!isAdded())
            return;

        mGoogleCalendarItems = calendarList;
        notifyAdapterChanged(true, false, true);
    }

    @Override
    public void onUserRecoverableAuthExceptionDuringCalendarFetch(UserRecoverableAuthIOException e) {
        startActivityForResult(e.getIntent(), AUTH_CODE_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == AUTH_CODE_REQUEST_CODE) {
            if (mGoogleCalendarItems == null || mGoogleCalendarItems.size() == 0) {
                if (mPokalSharedPreferences.isGoogleCalendarSyncOn() && !Utils.needGoogleCalendarPermissions(mContext)) {
                    mGoogleCalendarApi.getResultsFromApi();
                    return;
                }
            }
        }
    }

    @Override
    public void onCalendarItemClick(CalendarItem calendarItem) {
        Intent intent = new Intent(getActivity(), CalendarDetailsActivity.class);
        intent.putExtra(OnCalendarItemClickListener.CALENDAR_EXTRA, calendarItem);
        getActivity().startActivity(intent);
    }

    @Override
    public void onEmptyCalendarClick() {
        if (!mIsBottomNavAnimationComplete)
            return;
        updateFabAndBottomBar();
        mIsBottomNavAnimationComplete = false;
    }

    @Override
    public void onClick(View view) {
        //fab click
        updateFabAndBottomBar();
        mIsBottomNavAnimationComplete = false;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Bundle bundle = new Bundle();
        switch (item.getItemId()) {
            case R.id.meeting:
                bundle.putInt(Utils.INTENTEXTRA, R.id.meeting);
                break;
            case R.id.task:
                bundle.putInt(Utils.INTENTEXTRA, R.id.task);
                break;
            case R.id.note:
                bundle.putInt(Utils.INTENTEXTRA, R.id.note);
                break;
        }
        Intent intent = new Intent(getActivity(), CalendarDetailsActivity.class);
        intent.putExtras(bundle);
        getActivity().startActivity(intent);

        updateFabAndBottomBar();
        return true;
    }

    //------------------------------------------------------------------Private methods------------------------------------------//

    private void setUpRecyclerView() {
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));

       mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
           @Override
           public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
               if (!mIsBottomNavAnimationComplete) {
                   updateFabAndBottomBar();
                   mIsBottomNavAnimationComplete = true;
               }
               super.onScrollStateChanged(recyclerView, newState);
           }
       });
        mAdapter = new HomeRecyclerViewAdapter(new HashMap(0), new ArrayList<CalendarItem>(0),true, getActivity().getApplicationContext(), this, mPokalSharedPreferences.isPreferedTemperatureInFarenheit(), this);
        mRecyclerView.setAdapter(mAdapter);
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
                mService.registerListener(HomeFragment.this);
                mService.updateCurrentTime();
                long lastFetchedAt = mPokalSharedPreferences.getLastWeatherFetchTimeStamp();
                mService.updateWeatherEveryTenMinutes(lastFetchedAt, mPokalSharedPreferences.isPreferedTemperatureInFarenheit());
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mService = null;
            mBound = false;
        }
    };

    private void forceFetchTodaysWeather() {
        if (mBound) {
            mPokalSharedPreferences.clearWeather();
            long lastFetchedAt = mPokalSharedPreferences.getLastWeatherFetchTimeStamp();
            mService.updateWeatherEveryTenMinutes(lastFetchedAt, mPokalSharedPreferences.isPreferedTemperatureInFarenheit());
        }
    }

    private void notifyAdapterChanged(boolean locPermission, boolean hasWeatherChanged, boolean hasCalendarChanged) {
        if (!isAdded())
            return;
        mAdapter.notifyDataSetUpdated(mWeatherMap, mGoogleCalendarItems, locPermission, mContext, this, mPokalSharedPreferences.isPreferedTemperatureInFarenheit(), hasWeatherChanged, hasCalendarChanged);
    }

    private void showWeather() {
        if (mBound) {
            long lastFetchedAt = mPokalSharedPreferences.getLastWeatherFetchTimeStamp();
            mService.updateWeatherEveryTenMinutes(lastFetchedAt, mPokalSharedPreferences.isPreferedTemperatureInFarenheit());
            return;
        }
        Intent intent = new Intent(getActivity(), WeatherService.class);
        getActivity().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    private void showGoogleCalendar() {
        //Precondition for fetching Google calendar data : An account is chosen.
        // IF user has provided a Google email while creating sign in, that account will be used.
        // ELSE the user will be able to choose an account from the dialog.
        String accountName = mPokalSharedPreferences.getEmail();
        if (accountName != null && Utils.isGmailAccount(accountName)) {
            mGoogleCalendarApi.setCredentialAccountName(accountName);
            mGoogleCalendarApi.getResultsFromApi();
            return;
        }
        // Start a dialog from which the user can choose an account
        getActivity().startActivityForResult(mGoogleCalendarApi.getAccountChooserIntent(), Utils.REQUEST_ACCOUNT_PICKER);
    }

    private void updateFabAndBottomBar() {
        if (mFloatingActionButton.isShown()) {
            mFloatingActionButton.hide();
            animateBottomBar(true);
            return;
        }
        animateBottomBar(false);
        mFloatingActionButton.show();
    }

    private void animateBottomBar(boolean shouldShow) {
        AlphaAnimation animation;
        if (shouldShow) {
            animation = new AlphaAnimation(0.1f, 1f);
            animation.setDuration(300);
            animation.setStartOffset(50);
            animation.setFillAfter(true);
            mBottomNavigationView.setVisibility(View.VISIBLE);
            mBottomNavigationView.startAnimation(animation);
            return;
        }
        animation = new AlphaAnimation(1f, 0.1f);
        animation.setDuration(300);
        animation.setStartOffset(50);
        mBottomNavigationView.setVisibility(View.GONE);
        mBottomNavigationView.startAnimation(animation);
    }
}