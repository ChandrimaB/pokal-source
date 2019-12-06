package com.pocketapps.pockalendar.AppSigninPages;

import android.accounts.AccountManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.util.PatternsCompat;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.credentials.HintRequest;
import com.google.android.gms.common.api.GoogleApiClient;
import com.pocketapps.pockalendar.HomePage.HomeActivity;
import com.pocketapps.pockalendar.R;
import com.pocketapps.pockalendar.UserPreferences.PokalSharedPreferences;
import com.pocketapps.pockalendar.Utils.Utils;

import java.sql.Driver;
import java.util.regex.Pattern;

/**
 * Created by chandrima on 12/03/18.
 * This class is responsible for helping user create their very first login to use Pokal.
 * After entering required information, user will sign in first time. Details are saved to the Shared Preferences.
 */

public class CreateLoginFragment extends Fragment implements View.OnClickListener {

    private static final String NAME = "name";
    private static final String EMAIL = "email";
    private static final String GOOGLECALSYNC = "googlecalsync";
    private static final String PHONE = "phone";
    private static final String DOB = "dob";
    private static final String EMERGENCYCONTACT = "emergencycontactname";
    private static final String EMERGENCYPHONE = "emergencyphone";

    private EditText mName;
    private EditText mEmail;
    private CheckBox mGoogleCalendarSync;
    private EditText mPassword;
    private EditText mPhoneNumber;
    private EditText mDateOfBirth;
    private EditText mEmergencyContactName;
    private EditText mEmergencyContactPhoneNumber;
    private ImageButton mDoneButton;

    private PokalSharedPreferences mPokalSharedPreferences;
    private Context mContext;

    //----------------------------------------------------Fragment Lifecycle Methods----------------------------------------//

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.create_login_layout, container,false);

        mContext = getActivity().getApplicationContext();

        mPokalSharedPreferences = PokalSharedPreferences.getInstance(mContext);

        mName = (EditText)view.findViewById(R.id.fullName);
        mEmail = (EditText)view.findViewById(R.id.email);
        mGoogleCalendarSync = (CheckBox)view.findViewById(R.id.googleSync);
        mPassword = (EditText)view.findViewById(R.id.password);
        mPhoneNumber = (EditText)view.findViewById(R.id.phone);
        mDateOfBirth = (EditText)view.findViewById(R.id.dob);
        mEmergencyContactName = (EditText)view.findViewById(R.id.emergencyContactName);
        mEmergencyContactPhoneNumber = (EditText)view.findViewById(R.id.emergencyContactPhone);
        mDoneButton = (ImageButton)view.findViewById(R.id.signUpButton);

        addEventListenersToViews();

        String email = Utils.tryToInitEmailFromGoogleAccount(mContext);
        if (!Utils.isEmpty(email))
            mEmail.setText(email);

        return view;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(NAME, mName.getText().toString());
        outState.putString(EMAIL, mEmail.getText().toString());
        outState.putBoolean(GOOGLECALSYNC, mGoogleCalendarSync.isChecked());
        outState.putString(PHONE, mPhoneNumber.getText().toString());
        outState.putString(DOB, mDateOfBirth.getText().toString());
        outState.putString(EMERGENCYCONTACT, mEmergencyContactName.getText().toString());
        outState.putString(EMERGENCYPHONE, mEmergencyContactPhoneNumber.getText().toString());
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            mName.setText(savedInstanceState.getString(NAME));
            mEmail.setText(savedInstanceState.getString(EMAIL));
            mGoogleCalendarSync.setChecked(savedInstanceState.getBoolean(GOOGLECALSYNC));
            mPhoneNumber.setText(savedInstanceState.getString(PHONE));
            mDateOfBirth.setText(savedInstanceState.getString(DOB));
            mEmergencyContactName.setText(savedInstanceState.getString(EMERGENCYCONTACT));
            mEmergencyContactPhoneNumber.setText(savedInstanceState.getString(EMERGENCYPHONE));
        }
    }

    //-----------------------------------------------Event handlers----------------------------------------------------//

    @Override
    public void onClick(View view) {
        if (!testFields())
            return;
        saveUserProfile();
        Intent intent = new Intent(getActivity(), FirstTimeSignInOptionsActivity.class);
        startActivity(intent);
        getActivity().finish();
    }

    //-----------------------------------------------------Private Methods--------------------------------------------------//
    private void addEventListenersToViews() {
        mDateOfBirth.addTextChangedListener(new Utils.DateOfBirthFormattingTextWatcher());

        mPhoneNumber.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
        mPhoneNumber.setOnClickListener(this);
        mEmergencyContactPhoneNumber.addTextChangedListener(new PhoneNumberFormattingTextWatcher());

        mDoneButton.setOnClickListener(this);
    }

    private boolean testFields() {
        if (Utils.isEmpty(mName.getText().toString()) || Utils.isEmpty(mEmail.getText().toString()) || Utils.isEmpty(mPassword.getText().toString()) || Utils.isEmpty(mPhoneNumber.getText().toString()) || Utils.isEmpty(mDateOfBirth.getText().toString())) {
            Utils.buildAlertDialog(getActivity(), getString(R.string.SignUpButtonClickAlertMessage_Incomplete), getString(R.string.SignUpButtonClickAlertTitle), getString(R.string.SignUpButtonClickAlertPositiveButton), null, R.style.AlertDialog);
            return false;
        }

        if (!Utils.isEmail(mEmail.getText().toString())) {
            Utils.buildAlertDialog(getActivity(), getString(R.string.SignUpButtonClickAlertMessage_Email), getString(R.string.SignUpButtonClickAlertTitle), getString(R.string.SignUpButtonClickAlertPositiveButton), null, R.style.AlertDialog);
            return false;
        }

        if (!Utils.isDate(mDateOfBirth.getText().toString())) {
            Utils.buildAlertDialog(getActivity(), getString(R.string.SignUpButtonClickAlertMessage_Date), getString(R.string.SignUpButtonClickAlertTitle), getString(R.string.SignUpButtonClickAlertPositiveButton), null, R.style.AlertDialog);
            return false;
        }
        return true;
    }

    // Saved in shared preferences
    private void saveUserProfile() {
        mPokalSharedPreferences.setUsername(mName.getText().toString());
        mPokalSharedPreferences.setEmail(mEmail.getText().toString());
        mPokalSharedPreferences.setGoogleCalendarSync(mGoogleCalendarSync.isChecked());
        mPokalSharedPreferences.setEncryptedPassword(mPassword.getText().toString());
        mPokalSharedPreferences.setPhone(mPhoneNumber.getText().toString());
        mPokalSharedPreferences.setDob(mDateOfBirth.getText().toString());
        if (mEmergencyContactName.length() != 0)
            mPokalSharedPreferences.setEmergencyContactName(mEmergencyContactName.getText().toString());
        if (mEmergencyContactPhoneNumber.length() != 0)
            mPokalSharedPreferences.setEmergencyContactPhone(mEmergencyContactPhoneNumber.getText().toString());
        mPokalSharedPreferences.setIsFirstLogin(false);
    }
}
