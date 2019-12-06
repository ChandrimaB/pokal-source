package com.pocketapps.pockalendar.SettingsPage;

import android.content.Intent;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;

import com.pocketapps.pockalendar.AppSigninPages.SigninActivity;
import com.pocketapps.pockalendar.R;
import com.pocketapps.pockalendar.UserPreferences.PokalSharedPreferences;

/**
 * Created by chandrima on 13/03/18.
 */

public class SettingsFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener {

    private static final String GOOGLE_CAL_SYNC = "google_calendar_sync";
    private static final String WEATHER_TEMPERATURE_UNIT = "temperature_unit";
    private static final String DELETE_ACCOUNT = "delete_account";

    private SwitchPreference mGoogleCalendarSyncPreference;
    private Preference mDeleteAccountPreference;
    private ListPreference mTemperatureUnitPreference;

    private PokalSharedPreferences mPokalSharedPreferences;

    //-----------------------------------------------------------Life cycle methods--------------------------------------------//
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);

        mPokalSharedPreferences = PokalSharedPreferences.getInstance(getActivity().getApplicationContext());

        mGoogleCalendarSyncPreference = (SwitchPreference) findPreference(GOOGLE_CAL_SYNC);
        mDeleteAccountPreference = findPreference(DELETE_ACCOUNT);
        mTemperatureUnitPreference = (ListPreference) findPreference(WEATHER_TEMPERATURE_UNIT);

        initializePreferenceStates();
        addPreferenceEventListeners();
    }

    //---------------------------------------------------------Event handlers-----------------------------------------------//

    @Override
    public boolean onPreferenceChange(Preference preference, Object o) {
        switch (preference.getKey()) {
            case GOOGLE_CAL_SYNC:
                setGoogleCalendarSyncPreference((Boolean)o);
                break;
            case WEATHER_TEMPERATURE_UNIT:
                setTemperatureUnitPreference((String)o);
                break;
        }
        return true;
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference.getKey().equals(DELETE_ACCOUNT)) {
            deleteAccount();
            return true;
        }
        return false;
    }

    //------------------------------------------------------------private methods----------------------------------------------//

    private void initializePreferenceStates() {
        mGoogleCalendarSyncPreference.setChecked(mPokalSharedPreferences.isGoogleCalendarSyncOn());
        String temperatureUnits[] = getResources().getStringArray(R.array.TemperatureUnitValues);
        mTemperatureUnitPreference.setValue(mPokalSharedPreferences.isPreferedTemperatureInFarenheit() ? temperatureUnits[0] : temperatureUnits[1]);
    }

    private void addPreferenceEventListeners() {
        mGoogleCalendarSyncPreference.setOnPreferenceChangeListener(this);
        mDeleteAccountPreference.setOnPreferenceClickListener(this);
        mTemperatureUnitPreference.setOnPreferenceChangeListener(this);
    }

    private void setGoogleCalendarSyncPreference(boolean shouldSync) {
        mPokalSharedPreferences.setGoogleCalendarSync(shouldSync);
    }

    private void setTemperatureUnitPreference(String chosenUnit) {
        String temperatureUnits[] = getResources().getStringArray(R.array.TemperatureUnitValues);
        mPokalSharedPreferences.setPreferedTemperatureUnit(chosenUnit.contains(temperatureUnits[0])); //parameter boolean isFahrenheit - checking if the new string value contains Fahrenheit
    }

    private void deleteAccount() {
        mPokalSharedPreferences.clearAll();
        Intent intent = new Intent(getActivity(), SigninActivity.class);
        //clear backstack
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        getActivity().finish();
    }
}