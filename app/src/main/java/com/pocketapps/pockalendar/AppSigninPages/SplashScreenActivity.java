package com.pocketapps.pockalendar.AppSigninPages;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.pocketapps.pockalendar.HomePage.HomeActivity;
import com.pocketapps.pockalendar.UserPreferences.PokalSharedPreferences;

/**
 * Created by chandrima on 01/02/18.
 * SplashScreen for the app, first screen user sees after app launch.
 * The next screen is chosen based on conditions -
 * IF this is the first login, AnimatedLoadingActivity is shown.
 * IF this is not first login AND user setting for "Lock Pocal" is on(default - off), SigninActivity is shown.
 * IF none of the above, HomeActivity is shown.
 * This Activity has no views/layout.
 */

public class SplashScreenActivity extends AppCompatActivity {

    private PokalSharedPreferences mPokalSharedPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPokalSharedPreferences = PokalSharedPreferences.getInstance(getApplicationContext());

        try {
            Class<?> showActivity = Class.forName(HomeActivity.class.getName());

            if (mPokalSharedPreferences.isFirstLogin()) {
                showActivity = Class.forName(AnimatedLoadingActivity.class.getName());
            }

            if (!mPokalSharedPreferences.isFirstLogin() && mPokalSharedPreferences.isLockAppWithPasswordOn()) {
                showActivity = Class.forName(SigninActivity.class.getName());
            }

            // Close splash screen activity and show AnimatedLoadingActivity as soon as activities can be started
            Intent intent = new Intent(this, showActivity);
            startActivity(intent);
            finish();

        } catch (ClassNotFoundException e) {
            Log.d(AppCompatActivity.class.getSimpleName() + ":onCreate", "Failed to get class reference");
        }
    }
}
