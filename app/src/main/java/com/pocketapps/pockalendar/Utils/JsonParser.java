package com.pocketapps.pockalendar.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by chandrima on 16/03/18.
 */

public class JsonParser {

    public static final String WEATHEROBJECTNAME = "weather";
    public static final String WEATHERCONDITIONOBJNAME = "main";
    public static final String WEATHERCONDITIONSTRING = "description";
    public static final String WEATHERCURRENTTEMPSTRING = "temp";
    public static final String WEATHERCURRENTCITYSTRING = "name";

    public static final String WEATHERMINTEMP = "temp_min";
    public static final String WEATHERMAXTEMP = "temp_max";
    public static final String WEATHERDATE = "dt_txt";
    public static final String WEATHERLIST = "list";

    public static HashMap parseCurrentDayWeatherJson(String jsonString) {
        if (jsonString == null){
            return null;
        }
        HashMap<String, String> weatherMap = new HashMap<>();
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray weather = jsonObject.getJSONArray(WEATHEROBJECTNAME);
            String weatherCondition = "";
            String weatherConditionDetails = "";
            for (int index = 0; index < weather.length(); index++) {
                JSONObject weatherObject = weather.getJSONObject(index);
                weatherCondition += weatherObject.getString(WEATHERCONDITIONOBJNAME);
                weatherConditionDetails += " " + weatherObject.getString(WEATHERCONDITIONSTRING);
            }
            JSONObject temperature = jsonObject.getJSONObject(WEATHERCONDITIONOBJNAME);
            String currentTemperature = temperature.getString(WEATHERCURRENTTEMPSTRING);
            String currentCity = jsonObject.getString(WEATHERCURRENTCITYSTRING);

            weatherMap.put(WEATHERCONDITIONOBJNAME, weatherCondition);
            weatherMap.put(WEATHERCONDITIONSTRING, weatherConditionDetails.trim());
            weatherMap.put(WEATHERCURRENTTEMPSTRING, currentTemperature);
            weatherMap.put(WEATHERCURRENTCITYSTRING, currentCity);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return weatherMap;
    }

    public static ArrayList parseWeeklyForecastJson(String jsonString) {
        if (jsonString == null){
            return null;
        }
        ArrayList<HashMap<String, String>> weeklyWeatherList = new ArrayList<>();

        String date = "";
        String temp = "";
        String tempMin = "";
        String tempMax = "";
        String weatherCondition = "";
        String weatherConditionDetails = "";

        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray jsonArray = jsonObject.getJSONArray(WEATHERLIST);
            for(int index = 0; index < jsonArray.length(); index++) {
                HashMap<String, String> day = new HashMap<>();
                JSONObject weatherObject = jsonArray.getJSONObject(index);
                date = weatherObject.getString(WEATHERDATE);
                JSONObject temperature = weatherObject.getJSONObject(WEATHERCONDITIONOBJNAME);
                temp = temperature.getString(WEATHERCURRENTTEMPSTRING);
                tempMin = temperature.getString(WEATHERMINTEMP);
                tempMax = temperature.getString(WEATHERMAXTEMP);
                JSONArray weatherMap = weatherObject.getJSONArray(WEATHEROBJECTNAME);
                for (int i = 0; i < weatherMap.length(); i++) {
                    JSONObject condition = weatherMap.getJSONObject(i);
                    weatherCondition += condition.getString(WEATHERCONDITIONOBJNAME);
                    weatherConditionDetails += " " + condition.getString(WEATHERCONDITIONSTRING);
                }
                day.put(WEATHERDATE, date);
                day.put(WEATHERCURRENTTEMPSTRING, temp);
                day.put(WEATHERMINTEMP, tempMin);
                day.put(WEATHERMAXTEMP, tempMax);
                day.put(WEATHERCONDITIONOBJNAME, weatherCondition);
                day.put(WEATHERCONDITIONSTRING, weatherConditionDetails.trim());
                weeklyWeatherList.add(index, day);
                weatherCondition = "";
                weatherConditionDetails = "";
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return weeklyWeatherList;
    }
}
