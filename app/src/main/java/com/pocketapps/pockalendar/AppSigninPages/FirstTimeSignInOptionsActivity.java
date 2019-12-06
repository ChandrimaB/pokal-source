package com.pocketapps.pockalendar.AppSigninPages;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.pocketapps.pockalendar.HomePage.HomeActivity;
import com.pocketapps.pockalendar.R;
import com.pocketapps.pockalendar.UserPreferences.PokalSharedPreferences;

/**
 * Created by chandrima on 19/04/18.
 */

public class FirstTimeSignInOptionsActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView mGoogleCalendarSyncInfo;
    private RadioGroup mTemperatureUnitPreference;
    private ImageButton mProceed;
    private PokalSharedPreferences mPokalSharedPreferences;

    private static final String RADIOOPTION = "radiooption";

    //-----------------------------------------------------Life cycle methods--------------------------------------------------//
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.first_time_sign_in_info);
        mPokalSharedPreferences = PokalSharedPreferences.getInstance(getApplicationContext());

        mGoogleCalendarSyncInfo = (TextView) findViewById(R.id.googleSyncInfo);
        mTemperatureUnitPreference = (RadioGroup) findViewById(R.id.temperatureUnitPref);
        // set default checked to Fahrenheit
        mTemperatureUnitPreference.check(R.id.unitFahrenheit);
        mProceed = (ImageButton) findViewById(R.id.allDone);

        String s = mPokalSharedPreferences.isGoogleCalendarSyncOn() ? "chosen" : "not chosen";
        mGoogleCalendarSyncInfo.setText(getString(R.string.GoogleCalendarSyncInfo, s));

        mProceed.setOnClickListener(this);

        if (savedInstanceState != null)
            mTemperatureUnitPreference.check(savedInstanceState.getInt(RADIOOPTION));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(RADIOOPTION, mTemperatureUnitPreference.getCheckedRadioButtonId());
    }

    //--------------------------------------------------Event handling-------------------------------------------------------//
    @Override
    public void onClick(View view) {
        mPokalSharedPreferences.setPreferedTemperatureUnit(mTemperatureUnitPreference.getCheckedRadioButtonId() == R.id.unitFahrenheit ? true : false);
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        finish();
    }
}
