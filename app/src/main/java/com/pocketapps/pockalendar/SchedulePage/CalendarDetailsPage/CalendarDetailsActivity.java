package com.pocketapps.pockalendar.SchedulePage.CalendarDetailsPage;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.pocketapps.pockalendar.R;
import com.pocketapps.pockalendar.CalendarItemObjectModel.CalendarItem;
import com.pocketapps.pockalendar.Utils.Utils;

import java.util.ArrayList;

/**
 * Created by chandrima on 07/04/18.
 */

public class CalendarDetailsActivity extends AppCompatActivity {
    private static final String CALENDAR_DETAIL_TAG = "calendarDetail";
    private static final String NOTE_TAG = "noteTag";
    private static final String TASK_TAG = "tasktag";
    public static final String OPENNOTE = "opennote";
    public static final String OPENTASK = "openTask";
    private FragmentManager mFragmentManager;
    private FragmentTransaction mFragmentTransaction;
    private CalendarDetailsFragment mCalendarDetailsFragment;
    private NoteFragment mNoteFragment;
    private TaskFragment mTaskFragment;
    private Toolbar mToolbar;
    private int mIntExtra = -1;
    private CalendarItem mCalendarItem;

    public interface OnSaveCalendarItemListener {
        void onSaveCalendarItem();
    }

    private ArrayList<OnSaveCalendarItemListener> mOnSaveCalendarItemListeners = new ArrayList<>();
    //--------------------------------------------------------Life cycle methods------------------------------------------//
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calendar_detail_layout);

        mToolbar = findViewById(R.id.toolbarCalendarDetails);
        mFragmentManager = getSupportFragmentManager();
        mFragmentTransaction = mFragmentManager.beginTransaction();

        setUpToolbar();

        if (getIntent() != null) {
            mIntExtra = getIntent().getIntExtra(Utils.INTENTEXTRA, -1);
            mCalendarItem = (CalendarItem) getIntent().getSerializableExtra(OnCalendarItemClickListener.CALENDAR_EXTRA);
        }
        showFragment();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mNoteFragment == null)
            mNoteFragment = (NoteFragment) mFragmentManager.findFragmentByTag(NOTE_TAG);

        if (mTaskFragment == null)
            mTaskFragment = (TaskFragment) mFragmentManager.findFragmentByTag(TASK_TAG);

        mOnSaveCalendarItemListeners.add(mNoteFragment);
        mOnSaveCalendarItemListeners.add(mTaskFragment);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mOnSaveCalendarItemListeners.remove(mNoteFragment);
        mOnSaveCalendarItemListeners.remove(mTaskFragment);
    }

    //--------------------------------------------------------Event handlers------------------------------------------//
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        for (OnSaveCalendarItemListener onSaveCalendarItemListener : mOnSaveCalendarItemListeners) {
            if (onSaveCalendarItemListener != null) {
                onSaveCalendarItemListener.onSaveCalendarItem();
            }
        }
        super.onBackPressed();
    }

    //--------------------------------------------------------Private methods------------------------------------------//
    private void setUpToolbar() {
        setSupportActionBar(mToolbar);
        mToolbar.setBackgroundResource(R.drawable.gradient_colored_background);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(R.string.CalendarDetailsToolbarTitle);
    }

    private void showFragment() {
        if (mCalendarItem != null) {
            if (mCalendarItem.getType().equals(Utils.POKAL_NOTE)) {
                openNote();
                return;
            }
            if (mCalendarItem.getType().equals(Utils.POKAL_TASK)) {
                openTask();
                return;
            }
        }

        if (mIntExtra == -1) {
            if (mFragmentManager.findFragmentByTag(CALENDAR_DETAIL_TAG) == null) {
                mCalendarDetailsFragment = new CalendarDetailsFragment();
                mFragmentTransaction.replace(R.id.fragmentCalendarActivity, mCalendarDetailsFragment, CALENDAR_DETAIL_TAG);
                mFragmentTransaction.commit();
            }
        }

        if (mIntExtra == R.id.note) {
            getSupportActionBar().setTitle(R.string.NewNoteToolbarTitle);
            if (mFragmentManager.findFragmentByTag(NOTE_TAG) == null) {
                mNoteFragment = new NoteFragment();
                mFragmentTransaction.replace(R.id.fragmentCalendarActivity, mNoteFragment, NOTE_TAG);
                mFragmentTransaction.commit();
            }
        }

        if (mIntExtra == R.id.task) {
            getSupportActionBar().setTitle(R.string.NewTaskToolbarTitle);
            if (mFragmentManager.findFragmentByTag(TASK_TAG) == null) {
                mTaskFragment = new TaskFragment();
                mFragmentTransaction.replace(R.id.fragmentCalendarActivity, mTaskFragment, TASK_TAG);
                mFragmentTransaction.commit();
            }
        }
    }

    private void openNote() {
        getSupportActionBar().setTitle(getResources().getString(R.string.ShareNote));
        if (mFragmentManager.findFragmentByTag(NOTE_TAG) == null) {
            mNoteFragment = new NoteFragment();
            Bundle bundle = new Bundle();
            bundle.putSerializable(OPENNOTE, mCalendarItem);
            mNoteFragment.setArguments(bundle);
            mFragmentTransaction.replace(R.id.fragmentCalendarActivity, mNoteFragment, NOTE_TAG);
            mFragmentTransaction.commit();
        }
    }

    private void openTask() {
        getSupportActionBar().setTitle(getResources().getString(R.string.EditTask));
        if (mFragmentManager.findFragmentByTag(TASK_TAG) == null) {
            mTaskFragment = new TaskFragment();
            Bundle bundle = new Bundle();
            bundle.putSerializable(OPENTASK, mCalendarItem);
            mTaskFragment.setArguments(bundle);
            mFragmentTransaction.replace(R.id.fragmentCalendarActivity, mTaskFragment, TASK_TAG);
            mFragmentTransaction.commit();
        }
    }
}
