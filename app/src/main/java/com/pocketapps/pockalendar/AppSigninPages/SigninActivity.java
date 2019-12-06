package com.pocketapps.pockalendar.AppSigninPages;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.pocketapps.pockalendar.R;
import com.pocketapps.pockalendar.UserPreferences.PokalSharedPreferences;

/**
 * Created by chandrima on 01/03/18.
 * Activity that hosts either CreateLoginFragment or SigninFragment
 * based on check - isFirstLogin() - from shared preferences
 */

public class SigninActivity extends AppCompatActivity {

    private static final String CREATE_LOGIN_FRAGMENT_TAG = "CREATELOGIN";
    private static final String SIGNIN_FRAGMENT_TAG = "SIGNIN";

    private PokalSharedPreferences mPokalSharedPreferences;
    CreateLoginFragment mCreateLoginFragment;
    SignInFragment mSignInFragment;
    FragmentManager mFragmentManager;
    FragmentTransaction mFragmentTransaction;

    //-----------------------------------------------------Life cycle Methods--------------------------------------------------//
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.signin_activity);

        // Keep the keyboard from showing up by default
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        mPokalSharedPreferences = PokalSharedPreferences.getInstance(getApplicationContext());

        mFragmentManager = getSupportFragmentManager();
        mFragmentTransaction = mFragmentManager.beginTransaction();

       if (mPokalSharedPreferences.isFirstLogin()) {
           showCreateLoginFragment();
           return;
        }
        showSignInFragment();
    }

    //-----------------------------------------------------Private Methods--------------------------------------------------//
    private void showCreateLoginFragment() {
        // Only add fragment if not already added
        if (mFragmentManager.findFragmentByTag(CREATE_LOGIN_FRAGMENT_TAG) == null) {
            mCreateLoginFragment = new CreateLoginFragment();
            mFragmentTransaction.add(R.id.fragment, mCreateLoginFragment, CREATE_LOGIN_FRAGMENT_TAG);
            mFragmentTransaction.commit();
        }
    }

    private void showSignInFragment() {
        // Only add fragment if not already added
        if (mFragmentManager.findFragmentByTag(SIGNIN_FRAGMENT_TAG) == null) {
            mSignInFragment = new SignInFragment();
            mFragmentTransaction.add(R.id.fragment, mSignInFragment, SIGNIN_FRAGMENT_TAG);
            mFragmentTransaction.commit();
        }
    }
}