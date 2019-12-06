package com.pocketapps.pockalendar.SettingsPage;

import android.app.FragmentManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

import com.pocketapps.pockalendar.R;

/**
 * Created by chandrima on 13/03/18.
 */

public class SettingsActivity extends AppCompatActivity {
    private static final String TAG = SettingsActivity.class.getSimpleName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupToolbar();

        FragmentManager fragmentManager = getFragmentManager();
        if (fragmentManager.findFragmentByTag(TAG) == null) {
            fragmentManager.beginTransaction()
                    .replace(android.R.id.content, new SettingsFragment(), TAG)
                    .commit();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void setupToolbar() {
        ActionBar toolbar = getSupportActionBar();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            toolbar.setBackgroundDrawable(getDrawable(R.drawable.gradient_colored_background));
        }
        toolbar.setDisplayHomeAsUpEnabled(true);
        toolbar.setDisplayShowHomeEnabled(true);
    }
}
