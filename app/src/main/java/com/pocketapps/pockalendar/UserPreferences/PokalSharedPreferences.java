package com.pocketapps.pockalendar.UserPreferences;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.pocketapps.pockalendar.CalendarItemObjectModel.CalendarItem;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by chandrima on 10/03/18.
 */

public class PokalSharedPreferences {

    private static final String USERNAME = "username";
    private static final String EMAIL = "email";
    private static final String ENCRYPTED_PASSWORD = "encrypted_password";
    private static final String SALT = "salt";
    private static final String PHONE = "phone";
    private static final String DOB = "dob";
    private static final String EMERGENCY_CONTACT_NAME = "emergencycontactname";
    private static final String EMERGENCY_CONTACT_PHONE = "emergencycontactphone";
    private static final String PROFILE_PIC_URI = "profilepicuri";
    private static final String FIRST_LOGIN = "firstlogin";
    private static final String GOOGLE_CALENDAR_SYNC = "googlecalendarsync";
    private static final String LOCK_APP_WITH_PASSWORD = "lockappwithpassword";
    private static final String TEMPERATURE_UNIT = "temperatureunit";
    private static final String WEATHER_FETCH_TIMESTAMP = "weatherfetchtimestamp";
    private static final String WEATHER_FORECAST_TIMESTAMP = "weatherforecasttimestamp";
    private static final String TODAYS_WEATHER = "todaysweather";
    private static final String WEEKLY_WEATHER = "weeklyweather";
    private static final String HOURLY_WEATHER_TOMORROW = "hourlyweathertomorrow";;

    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mEditor;

    private static PokalSharedPreferences sPokalSharedPreferences;

    private SecurePassword mSecurePassword = new SecurePassword();

    //----------------------------------------------------------Constructor---------------------------------------------------//

    private PokalSharedPreferences(Context context) {
        if (mSharedPreferences == null) {
            mSharedPreferences = context.getSharedPreferences(PokalSharedPreferences.class.getSimpleName(), Context.MODE_PRIVATE);
            mEditor = mSharedPreferences.edit();
        }
    }

    //------------------------------------------------------------Singleton----------------------------------------------------//

    public static PokalSharedPreferences getInstance(Context context) {
        if (sPokalSharedPreferences == null) {
            sPokalSharedPreferences = new PokalSharedPreferences(context);
        }
        return sPokalSharedPreferences;
    }

    //-----------------------------------------------------------Getters-----------------------------------------------------//

    public String getUsername() {
        return mSharedPreferences.getString(USERNAME, null);
    }

    public String getEmail() {
        return mSharedPreferences.getString(EMAIL, null);
    }

    public String getPhone() {
        return mSharedPreferences.getString(PHONE, null);
    }

    public String getDob() {
        return mSharedPreferences.getString(DOB, null);
    }

    public String getEmergencyContactName() {
        return mSharedPreferences.getString(EMERGENCY_CONTACT_NAME, null);
    }

    public String getEmergencyContactPhone() {
        return mSharedPreferences.getString(EMERGENCY_CONTACT_PHONE, null);
    }

    public String getUri() {
        return mSharedPreferences.getString(PROFILE_PIC_URI, null);
    }

    public boolean isFirstLogin() {
        return  mSharedPreferences.getBoolean(FIRST_LOGIN, true);
    }

    public boolean isGoogleCalendarSyncOn() {
        return mSharedPreferences.getBoolean(GOOGLE_CALENDAR_SYNC, true);
    }

    public boolean isLockAppWithPasswordOn() {
        return mSharedPreferences.getBoolean(LOCK_APP_WITH_PASSWORD, false);
    }

    public boolean isPreferedTemperatureInFarenheit() {
        return mSharedPreferences.getBoolean(TEMPERATURE_UNIT, true);
    }

    public long getLastWeatherFetchTimeStamp() {
        return mSharedPreferences.getLong(WEATHER_FETCH_TIMESTAMP, 0);
    }

    public long getLastWeatherForecastTimeStamp() {
        return mSharedPreferences.getLong(WEATHER_FORECAST_TIMESTAMP, 0);
    }

    public HashMap<String, String> getTodaysWeatherFromStore() {
        Gson gson = new Gson();
        String todaysWeatherJson = mSharedPreferences.getString(TODAYS_WEATHER, null);
        HashMap<String, String> todaysWeather = gson.fromJson(todaysWeatherJson,HashMap.class);
        todaysWeather = new HashMap<>(todaysWeather);
        return todaysWeather;
    }

    public ArrayList<HashMap<String, String>> getWeeklyWeatherFromStore() {
        Gson gson = new Gson();
        String weeklyWeatherJson = mSharedPreferences.getString(WEEKLY_WEATHER, null);
        Type type = new TypeToken<ArrayList<HashMap<String, String>>>() {}.getType();
        ArrayList<HashMap<String, String>> weeklyWeather = gson.fromJson(weeklyWeatherJson, type);
        weeklyWeather = new ArrayList<>(weeklyWeather);
        return weeklyWeather;
    }

    public ArrayList<HashMap<String, String>> getTomorrowsDetailedWeatherFromStore() {
        Gson gson = new Gson();
        String weeklyWeatherJson = mSharedPreferences.getString(HOURLY_WEATHER_TOMORROW, null);
        Type type = new TypeToken<ArrayList<HashMap<String, String>>>() {}.getType();
        ArrayList<HashMap<String, String>> hourlyWeather = gson.fromJson(weeklyWeatherJson, type);
        hourlyWeather = new ArrayList<>(hourlyWeather);
        return hourlyWeather;
    }

    public ArrayList<CalendarItem> getCalendarItems(String itemType) {
        Gson gson = new Gson();
        String notes = mSharedPreferences.getString(itemType, null);
        if (notes == null)
            return null;
        Type type = new TypeToken<ArrayList<CalendarItem>>() {}.getType();
        ArrayList<CalendarItem> pokalNotes = gson.fromJson(notes, type);
        pokalNotes = new ArrayList<>(pokalNotes);
        return pokalNotes;
    }

    //-------------------------------------------------------Setters------------------------------------------------------------//

    public void setUsername(String username){
        mEditor.putString(USERNAME, username);
        mEditor.apply();
    }

    public void setEmail(String email){
        mEditor.putString(EMAIL, email);
        mEditor.apply();
    }

    /**To Store a Password

     - Generate a long random salt using a SHA1PRNG.
     - Prepend the salt to the password and hash it with a standard password hashing function.
     - Save both the salt and the hash in the app's shared preference.
     **/
    public void setEncryptedPassword(String password){
        mEditor.putString(ENCRYPTED_PASSWORD, mSecurePassword.getSaltedDigest(password))
                .putString(SALT, mSecurePassword.getSalt())
                .apply();
    }

    public void setPhone(String phone){
        mEditor.putString(PHONE, phone);
        mEditor.apply();
    }

    public void setDob(String dob){
        mEditor.putString(DOB, dob);
        mEditor.apply();
    }

    public void setEmergencyContactName(String emergencyContactName){
        mEditor.putString(EMERGENCY_CONTACT_NAME, emergencyContactName);
        mEditor.apply();
    }

    public void setEmergencyContactPhone(String emergencyContactPhone){
        mEditor.putString(EMERGENCY_CONTACT_PHONE, emergencyContactPhone);
        mEditor.apply();
    }

    public void setProfilePicUri(String stringUri) {
        mEditor.putString(PROFILE_PIC_URI, stringUri);
        mEditor.apply();
    }

    public void setIsFirstLogin(boolean isFirstLogin) {
        mEditor.putBoolean(FIRST_LOGIN, isFirstLogin);
        mEditor.apply();
    }

    public void setGoogleCalendarSync(boolean shouldSync) {
        mEditor.putBoolean(GOOGLE_CALENDAR_SYNC, shouldSync);
        mEditor.apply();
    }

    public void setLockAppWithPassword(boolean shouldLock) {
        mEditor.putBoolean(LOCK_APP_WITH_PASSWORD, shouldLock);
        mEditor.apply();
    }

    public void setPreferedTemperatureUnit(boolean isFarenheit) {
        mEditor.putBoolean(TEMPERATURE_UNIT, isFarenheit);
        mEditor.apply();
        clearWeather();
    }

    public void setLastWeatherFetchTimestamp(long timestamp) {
        mEditor.putLong(WEATHER_FETCH_TIMESTAMP, timestamp);
        mEditor.apply();
    }

    public void setLastWeatherForecastTimestamp(long timestamp) {
        mEditor.putLong(WEATHER_FORECAST_TIMESTAMP, timestamp);
        mEditor.apply();
    }

    public void saveTodaysWeatherToStore(HashMap<String, String> hashMap) {
        Gson gson = new Gson();
        String jsonWeather = gson.toJson(hashMap);
        mEditor.putString(TODAYS_WEATHER, jsonWeather);
        mEditor.apply();
    }

    public void saveWeeklyWeatherToStore(ArrayList<HashMap<String, String>> arrayList) {
        Gson gson = new Gson();
        String jsonWeather = gson.toJson(arrayList);
        mEditor.putString(WEEKLY_WEATHER, jsonWeather);
        mEditor.apply();
    }

    public void saveTomorrowsDetailedWeatherToStore(ArrayList<HashMap<String, String>> arrayList) {
        Gson gson = new Gson();
        String jsonWeather = gson.toJson(arrayList);
        mEditor.putString(HOURLY_WEATHER_TOMORROW, jsonWeather);
        mEditor.apply();
    }

    public void saveCalendarItemToStore(ArrayList<CalendarItem> arrayList, String itemType) {
        mEditor.remove(itemType);
        mEditor.apply();

        Gson gson = new Gson();
        String jsonNotes = gson.toJson(arrayList);
        mEditor.putString(itemType, jsonNotes);
        mEditor.apply();
    }

    public void deleteCalendarItem(CalendarItem calendarItem, String itemType) {
        ArrayList<CalendarItem> arrayList = getCalendarItems(itemType);
        ArrayList<CalendarItem> arrayListCopy = getCalendarItems(itemType);

        mEditor.remove(itemType);
        mEditor.apply();

        for(CalendarItem c : arrayList) {
            if (c.equals(calendarItem)) {
                arrayListCopy.remove(c);
            }
        }
        if (arrayListCopy != null && arrayListCopy.size() != 0)
            saveCalendarItemToStore(arrayListCopy, itemType);
        arrayList.clear();
        arrayListCopy .clear();
        arrayList = null;
        arrayListCopy = null;
    }
    //-------------------------------------------------------------Other Methods-----------------------------------------------------//
    /** To Validate a Password

     - Retrieve the user's salt and hash from shared preference.
     - Prepend the salt to the given password and hash it using the same hash function.
     - Compare the hash of the given password with the hash from shared preference.
        - If they match, the password is correct.
        - Otherwise, the password is incorrect.
     **/
    public boolean verifyPassword(String password) {
        String salt = "";
        String saltedHash = "";
        String stringToVerify = "";

        salt = mSharedPreferences.getString(SALT, null);
        saltedHash = mSharedPreferences.getString(ENCRYPTED_PASSWORD, null);

        stringToVerify = mSecurePassword.getDigest(password);

        return saltedHash.equals(salt.trim() + "\n" + stringToVerify);
    }

    public boolean verifyUserName(String username) {
        return username.equals(getEmail());
    }

    public void clearAll() {
        mEditor.clear().apply(); //apply to asynchronously commit the changes without blocking ui thread.
    }

    public void clearWeather() {
        mEditor.remove(WEATHER_FORECAST_TIMESTAMP);
        mEditor.remove(WEATHER_FETCH_TIMESTAMP);
        mEditor.remove(WEEKLY_WEATHER);
        mEditor.remove(HOURLY_WEATHER_TOMORROW);
        mEditor.remove(TODAYS_WEATHER);
        mEditor.apply();
    }
}
