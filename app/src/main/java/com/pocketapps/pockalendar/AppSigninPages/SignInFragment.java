package com.pocketapps.pockalendar.AppSigninPages;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import com.pocketapps.pockalendar.HomePage.HomeActivity;
import com.pocketapps.pockalendar.R;
import com.pocketapps.pockalendar.UserPreferences.PokalSharedPreferences;
import com.pocketapps.pockalendar.Utils.Utils;

/**
 * Created by chandrima on 12/03/18.
 * SigninFragment is shown on subsequent app starts after the first login (after SplashScreenActivity), IF user sign out / logs out of the app.
 */

public class SignInFragment extends Fragment implements View.OnClickListener {

    private EditText mEmail;
    private EditText mPassword;
    private ImageButton mDoneButton;

    private PokalSharedPreferences mPokalSharedPreferences;

    //-----------------------------------------------------Life cycle Methods--------------------------------------------------//
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sign_in_layout, container, false);

        mPokalSharedPreferences = PokalSharedPreferences.getInstance(getActivity().getApplicationContext());

        mEmail = (EditText) view.findViewById(R.id.email);
        mPassword = (EditText) view.findViewById(R.id.password);
        mDoneButton = (ImageButton) view.findViewById(R.id.signInButton);

        String email = mPokalSharedPreferences.getEmail();
        if (email != null)
            mEmail.setText(email);

        addEventListenersToViews();

        return view;
    }

    //-----------------------------------------------------Event handlers --------------------------------------------------//
    @Override
    public void onClick(View view) {
        if (!testFields())
            return;

        if (verifyUserProfile()) {
            mPokalSharedPreferences.setLockAppWithPassword(false);
            Intent intent = new Intent(getActivity(), HomeActivity.class);
            startActivity(intent);
            getActivity().finish();
            return;
        }
        Utils.buildAlertDialog(getActivity(), getString(R.string.SignInButtonClickAlertMessage_Failed), getString(R.string.SignUpButtonClickAlertTitle), getString(R.string.SignUpButtonClickAlertPositiveButton), null, R.style.AlertDialog);
    }
    //-----------------------------------------------------Private Methods--------------------------------------------------//

    private void addEventListenersToViews() {
        mDoneButton.setOnClickListener(this);
    }

    private boolean testFields() {
        if (Utils.isEmpty(mEmail.getText().toString()) || Utils.isEmpty(mPassword.getText().toString())) {
            Utils.buildAlertDialog(getActivity(), getString(R.string.SignInButtonClickAlertMessage_Incomplete), getString(R.string.SignUpButtonClickAlertTitle), getString(R.string.SignUpButtonClickAlertPositiveButton), null, R.style.AlertDialog);
            return false;
        }

        if (!Utils.isEmail(mEmail.getText().toString())) {
            Utils.buildAlertDialog(getActivity(), getString(R.string.SignUpButtonClickAlertMessage_Email), getString(R.string.SignUpButtonClickAlertTitle), getString(R.string.SignUpButtonClickAlertPositiveButton), null, R.style.AlertDialog);
            return false;
        }
        return true;
    }

    private boolean verifyUserProfile() {
        return mPokalSharedPreferences.verifyUserName(mEmail.getText().toString()) && mPokalSharedPreferences.verifyPassword(mPassword.getText().toString());
    }
}