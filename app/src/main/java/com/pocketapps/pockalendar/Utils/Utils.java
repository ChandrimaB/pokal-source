package com.pocketapps.pockalendar.Utils;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.ParcelFileDescriptor;
import android.support.v4.app.ActivityCompat;
import android.support.v4.util.PatternsCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataType;
import com.google.api.client.util.DateTime;
import com.pocketapps.pockalendar.GoogleCalendarApi;
import com.pocketapps.pockalendar.R;
import com.pocketapps.pockalendar.WeatherService;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.regex.Pattern;

import pub.devrel.easypermissions.EasyPermissions;

/**
 * Created by chandrima on 06/03/18.
 */

public class Utils {

    private static HashMap<String, Integer> sWeatherIconStore;

    public static class BitmapDecoderParams {
        Resources resources;
        int resId;
        int reqHeight;
        int reqWidth;
        Uri uri;
        Context context;
        BitmapDecoder.OnBitmapDecodeListener onBitmapDecodeListener;

        public BitmapDecoderParams(Resources resources, int resId, int reqHeight, int reqWidth, Uri uri, Context context, BitmapDecoder.OnBitmapDecodeListener onBitmapDecodeListener) {
            this.resources = resources;
            this.resId = resId;
            this.reqHeight = reqHeight;
            this.reqWidth = reqWidth;
            this.uri = uri;
            this.context = context;
            this.onBitmapDecodeListener = onBitmapDecodeListener;
        }
    }

    private static final String DATEDIVIDER = "/";
    private static final String GMAILDOMAIN = "gmail";

    public static final long TENMINUTES = 10 * 60 * 1000;
    public static final long ONEMINUTE = 60 * 1000;
    public static final long SIXTYMINUTES = 60 * 60 * 1000;
    public static final int REQUEST_PERMISSION_LOCATION = 24;
    public static final int REQUEST_ACCOUNT_PICKER = 1000;
    public static final int REQUEST_AUTHORIZATION = 1001;
    public static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    public static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;
    public static final int REQUEST_PERMISSION_ALL = 1004;
    public static final String CALENDAR_TYPE_GOOGLE = "Google Calendar Event";
    public static final String CALENDAR_TYPE_POKAL = "Pokal Calendar Event";
    public static final String POKAL_TASK = "Task";
    public static final String POKAL_NOTE = "Note";
    public static final String INTENTEXTRA = "intentextra";

    /**
     * Formatter to create MM/DD/YYYY mask for string date.
     */
    public static class DateOfBirthFormattingTextWatcher implements TextWatcher {

        private static final int STRLEN_ON_ENTERING_MONTH = 2;
        private static final int STRLEN_ON_ENTERING_DATE = 5;
        private static final int MAX_LENGTH = 10;

        private int mDateLength = 0;
        private StringBuilder mString = new StringBuilder();
        private boolean mShouldCallAfterTextChanged = true;

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            mDateLength = charSequence.length();
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            int currentLength = charSequence.length();
            char currentChar = '1';

            // if user is deleting characters, we do not need to do anything
            if (currentLength < mDateLength || currentLength > MAX_LENGTH) {
                mShouldCallAfterTextChanged = false;
                return;
            }

            if (currentLength > 0) {
                currentChar = charSequence.charAt(i);
            }

            if (currentLength == STRLEN_ON_ENTERING_MONTH || currentLength == STRLEN_ON_ENTERING_DATE) {
                mShouldCallAfterTextChanged = true;
                mString.setLength(0);
                mString.append(charSequence);

                if (currentChar == '/') {
                    mString.insert(currentLength == STRLEN_ON_ENTERING_MONTH ? 0 : 3, "0");
                    return;
                }

                mString.insert(currentLength == STRLEN_ON_ENTERING_MONTH ? STRLEN_ON_ENTERING_MONTH : STRLEN_ON_ENTERING_DATE, DATEDIVIDER);
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {
            if (!mShouldCallAfterTextChanged || mString.length() == 0) {
                return;
            }
            editable.clear();
            editable.append(mString);
            mString.setLength(0);
        }
    }

    /**
     * Method to build an alert dialog
     *
     * @param context
     * @param message
     * @param title
     * @param positiveButtonText
     * @param negativeButtonText
     * @param style
     */
    public static void buildAlertDialog(Context context, CharSequence message, CharSequence title, CharSequence positiveButtonText, CharSequence negativeButtonText, int style) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, style);

        builder.setMessage(message)
                .setTitle(title);

        if (positiveButtonText != null) {
            builder.setPositiveButton(positiveButtonText, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });
        }

        if (negativeButtonText != null) {
            builder.setNegativeButton(negativeButtonText, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });
        }

        Dialog dialog = builder.create();
        dialog.show();
        TextView messageView = (TextView) dialog.findViewById(android.R.id.message);
        messageView.setGravity(Gravity.CENTER);
    }

    public static boolean isEmpty(String string) {
        if (string == null)
            return true;
        if (string.equals("null"))
            return true;
        if (string.length() == 0)
            return true;
        return false;
    }

    public static boolean isRealName(String name) {
        return name.matches(".*[a-z].*");
    }

    public static boolean isEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static boolean isGmailAccount(String email) {
        if (isEmail(email)) {
            if (email.contains(GMAILDOMAIN)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isDate(String string) {
        if (isEmpty(string) || string.length() < 10)
            return false;

        String[] dateParts = string.split(DATEDIVIDER);
        if (isValidMonth(dateParts[0])
                && isValidDate(dateParts[0], dateParts[1])
                && isValidYear(dateParts[2])) {
            return true;
        }
        return false;
    }

    /**
     * Decode bitmap to avoid out of memory crashes due to large bitmap using resource id
     */

    public static class BitmapDecoder extends AsyncTask<BitmapDecoderParams, Void, Bitmap> {

        public interface OnBitmapDecodeListener {
            void onBitmapDecoded(Bitmap profilePic);
        }

        OnBitmapDecodeListener onBitmapDecodeListener = null;

        @Override
        protected Bitmap doInBackground(BitmapDecoderParams... bitmapDecoderParams) {
            Resources resources = bitmapDecoderParams[0].resources;
            int resId = bitmapDecoderParams[0].resId;
            int reqHeight = bitmapDecoderParams[0].reqHeight;
            int reqWidth = bitmapDecoderParams[0].reqWidth;
            Uri uri = bitmapDecoderParams[0].uri;
            Context context = bitmapDecoderParams[0].context;
            onBitmapDecodeListener = bitmapDecoderParams[0].onBitmapDecodeListener;

            // Decode bitmap with resource ID
            if (context == null && resources != null && resId != -1) {
                // First decode with inJustDecodeBounds=true to check dimensions without actually loading the bitmap to memory
                final BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeResource(resources, resId, options);

                // Calculate inSampleSize
                options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

                // Decode bitmap with inSampleSize set
                options.inJustDecodeBounds = false;
                return BitmapFactory.decodeResource(resources, resId, options);
            }

            //Decode bitmap with uri
            if (resources == null && context != null && uri != null) {
                ParcelFileDescriptor parcelFileDescriptor = null;

                try {
                    parcelFileDescriptor = context.getContentResolver().openFileDescriptor(uri, "r");
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                if (parcelFileDescriptor == null) {
                    return null;
                }
                FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();

                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);

                options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
                options.inJustDecodeBounds = false;
                Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);

                try {
                    parcelFileDescriptor.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return image;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            if (bitmap == null) {
                Log.d("BitmapDecoder", "Bitmap image returned is null");
            }
            onBitmapDecodeListener.onBitmapDecoded(bitmap);
        }
    }

    public static boolean isDeviceOnline(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        boolean isAvailable = false;
        if (networkInfo != null && networkInfo.isConnected()) {
            isAvailable = true;
        }
        return isAvailable;
    }

    public static String getCurrentTime() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("h:mm a");
        return simpleDateFormat.format(Calendar.getInstance().getTime());
    }

    public static String getCurrentDayAndDate() {
        SimpleDateFormat simpleDateFormat;
        simpleDateFormat = new SimpleDateFormat("EEEE");
        Date d = new Date();
        String dayOfTheWeek = simpleDateFormat.format(d);
        simpleDateFormat = new SimpleDateFormat("MMM, dd");
        String currentDate = simpleDateFormat.format(Calendar.getInstance().getTime());
        return dayOfTheWeek + " " + currentDate;
    }

    public static String getCurrentDate() {
        SimpleDateFormat simpleDateFormat;
        simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return simpleDateFormat.format(Calendar.getInstance().getTime());
    }

    public static String getTomorrowDate() {
        GregorianCalendar gc = new GregorianCalendar();
        gc.add(Calendar.DATE, 1);
        SimpleDateFormat simpleDateFormat;
        simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return simpleDateFormat.format(gc.getTime());
    }

    public static int getIcon(String weather, String date) {
        loadWeatherIcons();

        if (weather.contains(WeatherService.WEATHER_SUNNY))
            return sWeatherIconStore.get(WeatherService.WEATHER_SUNNY);

        if (weather.contains(WeatherService.WEATHER_CLEAR)) {
            if (!isEmpty(date)) {
                if (isDayHour(date))
                    return sWeatherIconStore.get(WeatherService.WEATHER_CLEAR_DAY);
                return sWeatherIconStore.get(WeatherService.WEATHER_CLEAR_NIGHT);
            }
            //Today
            int time = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
            if (time >= 6 && time <= 16)
                return sWeatherIconStore.get(WeatherService.WEATHER_CLEAR_DAY);
            return sWeatherIconStore.get(WeatherService.WEATHER_CLEAR_NIGHT);
        }

        if (weather.contains(WeatherService.WEATHER_SNOW))
            return sWeatherIconStore.get(WeatherService.WEATHER_SNOW);

        if (weather.contains(WeatherService.WEATHER_RAIN))
            return sWeatherIconStore.get(WeatherService.WEATHER_RAIN);

        if (weather.contains(WeatherService.WEATHER_CLOUDY))
            return sWeatherIconStore.get(WeatherService.WEATHER_CLOUDY);

        if (weather.contains(WeatherService.WEATHER_MIST)) {
            if (!isEmpty(date)) {
                if (isDayHour(date))
                    return sWeatherIconStore.get(WeatherService.WEATHER_MIST_DAY);
                return sWeatherIconStore.get(WeatherService.WEATHER_MIST_NIGHT);
            }
            //Today
            int time = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
            if (time >= 6 && time <= 16)
                return sWeatherIconStore.get(WeatherService.WEATHER_MIST_DAY);
            return sWeatherIconStore.get(WeatherService.WEATHER_MIST_NIGHT);
        }

        if (weather.contains(WeatherService.WEATHER_HAZE)) {
            if (!isEmpty(date)) {
                if (isDayHour(date))
                    return sWeatherIconStore.get(WeatherService.WEATHER_HAZE_DAY);
                return sWeatherIconStore.get(WeatherService.WEATHER_HAZE_NIGHT);
            }
            //Today
            int time = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
            if (time >= 6 && time <= 16)
                return sWeatherIconStore.get(WeatherService.WEATHER_HAZE_DAY);
            return sWeatherIconStore.get(WeatherService.WEATHER_HAZE_NIGHT);
        }

        if (weather.contains(WeatherService.WEATHER_HAIL))
            return sWeatherIconStore.get(WeatherService.WEATHER_HAIL);

        if (weather.contains(WeatherService.WEATHER_THUNDER))
            return sWeatherIconStore.get(WeatherService.WEATHER_THUNDER);

        return sWeatherIconStore.get(WeatherService.WEATHER_SUNNY);
    }

    public static String fahrenheitToCelcius(String fahrenheit) {
        float f = Float.parseFloat(fahrenheit);
        return String.valueOf(Math.round((f - 32) * 5 / 9));
    }

    public static String celciusToFahrenheit(String celcius) {
        float c = Float.parseFloat(celcius);
        return String.valueOf(Math.round((c * 9 / 5) + 32));
    }

    public static String getDayAndDateShortFormat(String shortDate) {
        if (isEmpty(shortDate))
            return null;

        Date longDateFormat = null;
        try {
            longDateFormat = new SimpleDateFormat("MM.dd.yyyy", Locale.US).parse(shortDate);
        } catch (ParseException e1) {
            e1.printStackTrace();
        }
        String day = new SimpleDateFormat("EEE").format(longDateFormat);
        String date = new SimpleDateFormat("MMM, dd").format(longDateFormat);
        return day + " " + date;
    }


    public static String getDayAndDate(String longDate) {
        if (isEmpty(longDate))
            return null;

        Date longDateFormat = null;
        try {
            longDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).parse(longDate);
        } catch (ParseException e1) {
            e1.printStackTrace();
        }
        String day = new SimpleDateFormat("EEE").format(longDateFormat);
        String date = new SimpleDateFormat("MMM, dd").format(longDateFormat);
        return day + " " + date;
    }

    public static String getDayAndDateLongFormat(String longDate) {
        if (isEmpty(longDate))
            return null;

        Date longDateFormat = null;
        try {
            longDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX").parse(longDate);
        } catch (ParseException e1) {
            e1.printStackTrace();
            return longDate;
        }
        String day = new SimpleDateFormat("EEE").format(longDateFormat);
        String date = new SimpleDateFormat("MMM, dd").format(longDateFormat);
        return day + " " + date;
    }

    public static String getDatePart(String longDate) {
        if (isEmpty(longDate))
            return null;
        String[] dateParts = longDate.split(" ");
        return dateParts[1] + " " + dateParts[2];
    }

    public static boolean isNoon(String inDate) {
        if (isEmpty(inDate))
            return false;

        Calendar calendar = new GregorianCalendar();
        try {
            Date simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).parse(inDate);
            calendar.setTime(simpleDateFormat);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        int time = calendar.get(Calendar.HOUR_OF_DAY);
        if (time == 12) {
            return true;
        }
        return false;
    }

    public static boolean needLocationPermission(Context context) {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED;
    }

    public static boolean isGooglePlayServicesAvailable(Context context) {
        return getGooglePlayConnectionStatusCode(context) == ConnectionResult.SUCCESS;
    }

    public static int getGooglePlayConnectionStatusCode(Context context) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        return apiAvailability.isGooglePlayServicesAvailable(context);
    }

    public static boolean needGoogleCalendarPermissions(Context context) {
        return !EasyPermissions.hasPermissions(context, Manifest.permission.GET_ACCOUNTS);
    }

    public static boolean needReadContactsPermissions(Context context) {
        return !EasyPermissions.hasPermissions(context, Manifest.permission.READ_CONTACTS);
    }

    public static boolean isSameDay(String firstDate, String secondDate) {
        if (isEmpty(firstDate) || isEmpty(secondDate))
            return false;
        Calendar calendar1 = new GregorianCalendar();
        Calendar calendar2 = new GregorianCalendar();
        try {
            Date one = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(firstDate);
            Date two = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(secondDate);
            calendar1.setTime(one);
            calendar2.setTime(two);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (calendar1.get(Calendar.DATE) == (calendar2.get(Calendar.DATE))) {
            return true;
        }
        return false;
    }

    public static URL createWeatherUrlWithLatitudeLongitude(String latitide, String longitude, boolean isCurrentDayWeather, boolean isTemperatureUnitFahrenheit) throws MalformedURLException {
        String tempUnit = isTemperatureUnitFahrenheit ? WeatherService.TEMPINFARENHEIGHT : WeatherService.TEMPINCELCIUS;
        return isCurrentDayWeather ?
                new URL(WeatherService.URLSTRING + WeatherService.URLLATITUDE + latitide + WeatherService.URLLONGITUDE + longitude + WeatherService.OPENWEATHERAPPID + tempUnit) :
                new URL(WeatherService.FIVEDAYFORCASTURL + WeatherService.URLLATITUDE + latitide + WeatherService.URLLONGITUDE + longitude + WeatherService.OPENWEATHERAPPID + tempUnit);
    }

    public static URL createWeatherUrlWithCity(String city) throws MalformedURLException {
        return new URL(WeatherService.URLSTRING + WeatherService.URLCITYNAME + city + WeatherService.OPENWEATHERAPPID + WeatherService.TEMPINFARENHEIGHT);
    }

    public static boolean hasEnoughTimeElapsedSinceLastFetch(long lastWeatherFetchTime, boolean isCurrentWeatherFetch) {
        long currentTime = System.currentTimeMillis();
        if (isCurrentWeatherFetch)
            return currentTime - lastWeatherFetchTime >= TENMINUTES;
        return currentTime - lastWeatherFetchTime >= SIXTYMINUTES;
    }

    public static String getTimeFromDateTime(DateTime dateTime) {
        if (dateTime == null)
            return null;
        //Extract the time in HH:MM first, then apply format
        String date = dateTime.toString().split("T")[1].substring(0, 5);
        return getTwelveHourDateFromTwentyFourHourFormat(date);
    }

    public static String getTwelveHourDateFromTwentyFourHourFormat(String time) {
        try {
            SimpleDateFormat simpleDateFormat24Hours = new SimpleDateFormat("HH:mm");
            SimpleDateFormat simpleDateFormat12Hours = new SimpleDateFormat("hh:mm a");
            Date date24Hours = simpleDateFormat24Hours.parse(time);
            return simpleDateFormat12Hours.format(date24Hours);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String getTimeFromDate(String date) {
        try {
            Date simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).parse(date);
            SimpleDateFormat simpleDateFormat12Hours = new SimpleDateFormat("hh:mm a");
            return simpleDateFormat12Hours.format(simpleDateFormat);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static boolean isAllDay(String meetingTime) {
        if (isEmpty(meetingTime))
            return false;
        return meetingTime.contains(GoogleCalendarApi.ALLDAYEVENT);
    }

    public static boolean hasData(ArrayList arrayList) {
        if (arrayList == null || arrayList.size() == 0)
            return false;
        return true;
    }

    public static boolean needAllPermissions(Context context) {
        return needLocationPermission(context) && needGoogleCalendarPermissions(context) && needReadContactsPermissions(context);
    }

    public static int getCalendarIndicationForType(String type) {
        switch (type) {
            case CALENDAR_TYPE_POKAL:
                return R.color.calendarItemBackgroundPokal;
            case POKAL_TASK:
                return R.color.calendarItemBackgroundTask;
            case POKAL_NOTE:
                return R.color.calendarItemBackgroundNote;
            default:
                return R.color.calendarItemBackgroundGoogle;
        }
    }

    public static FitnessOptions getFitnessOptions() {
        return FitnessOptions.builder()
                .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                .build();
    }

    public static int getMeetingTypeDrawable(String type) {
        switch (type) {
            case POKAL_NOTE:
                return R.drawable.ic_library_books_24dp;
            case POKAL_TASK:
                return R.drawable.ic_view_list_24dp;
            default:
                return R.drawable.ic_schedule_24dp;
        }
    }

    public static Account tryToInitGoogleAccount(Context context) {
        Pattern emailPattern = PatternsCompat.EMAIL_ADDRESS;
        android.accounts.Account[] accounts = AccountManager.get(context).getAccounts();
        for (android.accounts.Account account : accounts) {
            if (emailPattern.matcher(account.name).matches()) {
                return account;
            }
        }
        return null;
    }

    public static String tryToInitEmailFromGoogleAccount(Context context) {
        Account account = tryToInitGoogleAccount(context);
        if (account != null)
            return account.name;
        return null;
    }

    public static String getStringDateFromCalendar(Calendar calendar) {
        SimpleDateFormat myDateFormat = new SimpleDateFormat("MM.dd.yyyy");
        return myDateFormat.format(calendar.getTime());
    }

    //-----------------------------------------------------Private Methods-------------------------------------------------------//

    private static boolean isValidMonth(String s) {
        if (Integer.parseInt(s) >= 1 && Integer.parseInt(s) <= 12) {
            return true;
        }
        return false;
    }

    private static boolean isValidDate(String s1, String s2) {
        if (isValidMonth(s1) && Integer.parseInt(s1) == 2) {
            if (Integer.parseInt(s2) >= 1 && Integer.parseInt(s2) <= 29) {
                return true;
            }
            return false;
        }

        if (isValidMonth(s1) && Integer.parseInt(s1) % 2 == 0 && Integer.parseInt(s1) != 2) {
            if (Integer.parseInt(s2) >= 1 && Integer.parseInt(s2) <= 30) {
                return true;
            }
            return false;
        }

        if (isValidMonth(s1) && Integer.parseInt(s1) % 2 != 0) {
            if (Integer.parseInt(s2) >= 1 && Integer.parseInt(s2) <= 31) {
                return true;
            }
            return false;
        }
        return false;
    }

    private static boolean isValidYear(String s) {
        if (Integer.parseInt(s) >= 1920 && Integer.parseInt(s) <= 2010) {
            return true;
        }
        return false;
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {

        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    private static void loadWeatherIcons() {
        if (sWeatherIconStore != null && sWeatherIconStore.size() != 0)
            return;
        sWeatherIconStore = new HashMap<>();
        sWeatherIconStore.put(WeatherService.WEATHER_SUNNY, R.drawable.ic_sun);
        sWeatherIconStore.put(WeatherService.WEATHER_RAIN, R.drawable.ic_rain);
        sWeatherIconStore.put(WeatherService.WEATHER_MIST_DAY, R.drawable.ic_cloudy_2);
        sWeatherIconStore.put(WeatherService.WEATHER_HAZE_DAY, R.drawable.ic_cloudy_2);
        sWeatherIconStore.put(WeatherService.WEATHER_MIST_NIGHT, R.drawable.ic_crescent_moon_behind_a_cloud);
        sWeatherIconStore.put(WeatherService.WEATHER_HAZE_NIGHT, R.drawable.ic_crescent_moon_behind_a_cloud);
        sWeatherIconStore.put(WeatherService.WEATHER_STORM, R.drawable.ic_rain);
        sWeatherIconStore.put(WeatherService.WEATHER_THUNDER, R.drawable.ic_flash);
        sWeatherIconStore.put(WeatherService.WEATHER_CLEAR_DAY, R.drawable.ic_sun);
        sWeatherIconStore.put(WeatherService.WEATHER_CLEAR_NIGHT, R.drawable.ic_moon);
        sWeatherIconStore.put(WeatherService.WEATHER_SNOW, R.drawable.ic_snowflake);
        sWeatherIconStore.put(WeatherService.WEATHER_HAIL, R.drawable.ic_snowflake);
        sWeatherIconStore.put(WeatherService.WEATHER_CLOUDY, R.drawable.ic_cloudy);
    }

    private static boolean isDayHour(String inDate) {
        if (isEmpty(inDate))
            return false;

        Calendar calendar = new GregorianCalendar();
        try {
            Date simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).parse(inDate);
            calendar.setTime(simpleDateFormat);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        int time = calendar.get(Calendar.HOUR_OF_DAY);
        if (time >= 6 && time <= 16) {
            return true;
        }
        return false;
    }
}