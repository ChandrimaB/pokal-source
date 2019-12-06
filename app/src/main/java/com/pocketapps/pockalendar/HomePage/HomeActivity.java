package com.pocketapps.pockalendar.HomePage;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.FitnessStatusCodes;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Value;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.request.DataSourcesRequest;
import com.google.android.gms.fitness.request.OnDataPointListener;
import com.google.android.gms.fitness.request.SensorRequest;
import com.google.android.gms.fitness.result.DataReadResponse;
import com.google.android.gms.fitness.result.DataSourcesResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.pocketapps.pockalendar.AppSigninPages.SigninActivity;
import com.pocketapps.pockalendar.GoogleFitApi;
import com.pocketapps.pockalendar.HomePage.ViewPager.HomeViewPagerAdapter;
import com.pocketapps.pockalendar.R;
import com.pocketapps.pockalendar.SettingsPage.SettingsActivity;
import com.pocketapps.pockalendar.UserPreferences.PokalSharedPreferences;
import com.pocketapps.pockalendar.Utils.Utils;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by chandrima on 07/03/18.
 */

public class HomeActivity extends AppCompatActivity implements DrawerLayout.DrawerListener,
        NavigationView.OnNavigationItemSelectedListener,
        View.OnClickListener,
        Utils.BitmapDecoder.OnBitmapDecodeListener,
        SharedPreferences.OnSharedPreferenceChangeListener,
        TabLayout.OnTabSelectedListener,
        GoogleFitApi.GoogleFitListening {

    private static final int REQUEST_CODE = 12;
    private static final int GOOGLE_FITNESS_API_REQUEST_CODE = 101;
    private static final String DOCUMENT_TYPE = "image/*";
    private static final String STEPS = "steps";

    private ImageView mProfilePic;
    private DrawerLayout mDrawerLayout;
    private Toolbar mToolbar;
    private NavigationView mNavigationView;
    private ImageButton mEditProfilePic;
    private TabLayout mTabLayout;
    private TextView mStepCount;

    private Uri mUri;
    private Menu mMenu;
    private MenuItem mUserName, mPhone, mEmergencyName, mEmergencyPhone, mEmergencySection;
    private PokalSharedPreferences mSharedPreferences;
    private FragmentManager mFragmentManager;
    private HomeViewPagerAdapter mHomeViewPagerAdapter;
    private ViewPager mViewPager;
    private GoogleFitApi mGoogleFitApi;

    private String mSteps;

    //--------------------------------------------------Life cycle methods------------------------------------------------//

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_screen_drawer_layout);

        mFragmentManager = getSupportFragmentManager();

        mSharedPreferences = PokalSharedPreferences.getInstance(getApplicationContext());
        mHomeViewPagerAdapter = new HomeViewPagerAdapter(mFragmentManager, getApplicationContext());

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        mToolbar = findViewById(R.id.toolbar);
        mNavigationView = (NavigationView) findViewById(R.id.navView);
        mProfilePic = (ImageView) mNavigationView.getHeaderView(0).findViewById(R.id.profilePic);
        mEditProfilePic = (ImageButton) mNavigationView.getHeaderView(0).findViewById(R.id.editPicture);
        mTabLayout = (TabLayout) findViewById(R.id.tab_layout);
        mViewPager = (ViewPager) findViewById(R.id.homePager);

        mMenu = mNavigationView.getMenu();
        mUserName = mMenu.findItem(R.id.profileName);
        mPhone = mMenu.findItem(R.id.profilePhone);
        mEmergencyName = mMenu.findItem(R.id.emergencyContactName);
        mEmergencyPhone = mMenu.findItem(R.id.emergencyContactPhone);
        mEmergencySection = mMenu.findItem(R.id.emergencySection);

        setUpToolbar();
        setupUserProfileInNavDrawer();
        setUpViewPager();
        addEventListenersToViews();
        showGooglePlayServicesErrorDialog();
        showPermissionDialogs();
        showPermissionDialogForGoogleFitnessApi();

        if (savedInstanceState != null) {
            mSteps = (String) savedInstanceState.get(STEPS);
        }
        mStepCount.setText(savedInstanceState != null ? mSteps : "0");
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleFitApi != null) {
            mGoogleFitApi.connect(this);
        }
    }

    @Override
    protected void onStop() {
        if (mGoogleFitApi != null) {
            mGoogleFitApi.disconnect();
        }
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        if (mViewPager.getCurrentItem() == 0) {
            super.onBackPressed();
        } else {
            mViewPager.setCurrentItem(mViewPager.getCurrentItem() - 1);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (Utils.isEmpty(mSteps))
            return;
        outState.putString(STEPS, mSteps);
    }

    //--------------------------------------------------Event handlers------------------------------------------------//

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDrawerSlide(View drawerView, float slideOffset) {

    }

    @Override
    public void onDrawerOpened(View drawerView) {

    }

    @Override
    public void onDrawerClosed(View drawerView) {

    }

    @Override
    public void onDrawerStateChanged(int newState) {

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.Settings:
                intent = new Intent(HomeActivity.this, SettingsActivity.class);
                startActivity(intent);
                break;
            case R.id.Signout:
                mSharedPreferences.setLockAppWithPassword(true);
                intent = new Intent(this, SigninActivity.class);
                //clear backstack
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
        }
        return true;
    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType(DOCUMENT_TYPE);
        startActivityForResult(intent, REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                mUri = data.getData();
                decodeBitmap();
            }
        }

        if (requestCode == GOOGLE_FITNESS_API_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            mGoogleFitApi = GoogleFitApi.getInstance(getApplicationContext());
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBitmapDecoded(Bitmap profilePic) {
        if (profilePic == null)
            return;
        mProfilePic.setImageBitmap(profilePic);
        mSharedPreferences.setProfilePicUri(mUri.toString());
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {

    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }

    @Override
    public void onConnectionFailure(ConnectionResult connectionResult) {
        if (!connectionResult.hasResolution()) {
            GooglePlayServicesUtil.getErrorDialog(connectionResult.getErrorCode(), this, 0).show();
            return;
        }
        try {
            connectionResult.startResolutionForResult(this, 1);
        } catch (IntentSender.SendIntentException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStepCountAvailable(String stepCount) {
        mSteps = stepCount;
        mStepCount.setText(stepCount);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        sendPermissionUpdateToFragments(requestCode, permissions, grantResults);
    }

    //-----------------------------------------------------private methods------------------------------------------//

    private void setUpToolbar(){
        setSupportActionBar(mToolbar);
        mToolbar.setBackgroundResource(R.drawable.gradient_colored_background);
        View mCustomView = getLayoutInflater().inflate(R.layout.step_count_layout, null);
        //mToolbar.addView(mCustomView);
        mStepCount = (TextView) mToolbar.findViewById(R.id.stepCount);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar,
                R.string.HomeDrawerOpenContentDescription, R.string.HomeDrawerCloseContentDescription);
        mDrawerLayout.addDrawerListener(toggle);
        for (int i = 0; i < mHomeViewPagerAdapter.getCount(); i++) {
            mTabLayout.addTab(mTabLayout.newTab().setIcon(mHomeViewPagerAdapter.getTabIcon(i)));
        }
        mTabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        toggle.syncState();
    }

    private void setStatusBarProperties() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow();
            w.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            w.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
    }

    //Precondition for fetching Latitude and Longitude of device : Location permission is taken from user
    private void showLocationPermissionDialog() {
        if (Utils.needLocationPermission(this) ) {
            ActivityCompat.requestPermissions(this, new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION},
                    Utils.REQUEST_PERMISSION_LOCATION);
        }
    }

    //Precondition for fetching Google calendar data : Google Play Services is installed.
    private void showGooglePlayServicesErrorDialog() {
        if (!Utils.isGooglePlayServicesAvailable(this)) {
            GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
            Dialog dialog = apiAvailability.getErrorDialog(this,
                        Utils.getGooglePlayConnectionStatusCode(this),
                        Utils.REQUEST_GOOGLE_PLAY_SERVICES);
            dialog.show();
        }
    }

    //Precondition for fetching Google calendar data : Contacts permission is taken from user
    private void showGoogleCalendarPermissionDialog() {
        if (Utils.needGoogleCalendarPermissions(this)) {
            ActivityCompat.requestPermissions(this, new String[]{
                            Manifest.permission.GET_ACCOUNTS},
                    Utils.REQUEST_PERMISSION_GET_ACCOUNTS);
        }
    }

    private void showPermissionDialogs() {
        if (Utils.needAllPermissions(this)) {
                ActivityCompat.requestPermissions(this, new String[]{
                                Manifest.permission.GET_ACCOUNTS,
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.READ_CONTACTS,
                                Manifest.permission.WRITE_CONTACTS},
                        Utils.REQUEST_PERMISSION_ALL);
            }
    }

    private void showPermissionDialogForGoogleFitnessApi() {
        FitnessOptions fitnessOptions = Utils.getFitnessOptions();

        GoogleSignInAccount googleSignInAccount = GoogleSignIn.getLastSignedInAccount(this);
        if (!GoogleSignIn.hasPermissions(googleSignInAccount, fitnessOptions)) {
            GoogleSignIn.requestPermissions(this, GOOGLE_FITNESS_API_REQUEST_CODE, googleSignInAccount, fitnessOptions);
            return;
        }

        if (mGoogleFitApi == null)
            mGoogleFitApi = GoogleFitApi.getInstance(getApplicationContext());
    }

    private void decodeBitmap() {
        if (mUri == null)
            return;
        // We will need to use mProfilePic.getMeasuredWidth() and mProfilePic.getMeasuredHeight() since view might not have been
        // drawn yet and getWidth and getHeight may be 0
        mProfilePic.measure(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
        Utils.BitmapDecoderParams bitmapDecoderParams =
                new Utils.BitmapDecoderParams(null, -1, mProfilePic.getMeasuredWidth(), mProfilePic.getMeasuredHeight(), mUri, getApplicationContext(), this);
        Utils.BitmapDecoder bitmapDecoder = new Utils.BitmapDecoder();
        bitmapDecoder.execute(bitmapDecoderParams);
    }

    private void setupUserProfileInNavDrawer() {
        if (mSharedPreferences == null)
            return;
        mUserName.setTitle(mSharedPreferences.getUsername());
        mPhone.setTitle(mSharedPreferences.getPhone());
        setProfilePictureIfAvailable();
        String emerName = mSharedPreferences.getEmergencyContactName();
        String emerPhone = mSharedPreferences.getEmergencyContactPhone();
        if (Utils.isEmpty(emerName) && Utils.isEmpty(emerPhone)) {
            mEmergencySection.setVisible(false);
            return;
        }
        if (Utils.isEmpty(emerName)) {
            mEmergencyName.setVisible(false);
            mEmergencyPhone.setTitle(emerPhone);
            return;
        }

        if (Utils.isEmpty(emerPhone)) {
            mEmergencyPhone.setVisible(false);
            mEmergencyName.setTitle(emerName);
            return;
        }

        mEmergencyName.setTitle(emerName);
        mEmergencyPhone.setTitle(emerPhone);
    }

    private void setUpViewPager() {
        mViewPager.setAdapter(mHomeViewPagerAdapter);
    }

    private void addEventListenersToViews() {
        mDrawerLayout.addDrawerListener(this);
        mNavigationView.setNavigationItemSelectedListener(this);
        mEditProfilePic.setOnClickListener(this);
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabLayout));
        mTabLayout.addOnTabSelectedListener(this);
    }

    private void setProfilePictureIfAvailable() {
        try {
            mUri = Uri.parse(mSharedPreferences.getUri());
        } catch (NullPointerException e) {
            Log.d(HomeActivity.class.getSimpleName() + ":setUserProfile(): ", "Profile picture URI is null");
            return;
        }
        decodeBitmap();
    }

    private void sendPermissionUpdateToFragments(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResult) {
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        if (fragments != null) {
            for (Fragment fragment : fragments) {
                fragment.onRequestPermissionsResult(requestCode, permissions, grantResult);
            }
        }
    }
}
