package com.pocketapps.pockalendar.SchedulePage.CalendarDetailsPage;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;

import com.pocketapps.pockalendar.R;
import com.pocketapps.pockalendar.CalendarItemObjectModel.CalendarItem;
import com.pocketapps.pockalendar.UserPreferences.PokalSharedPreferences;
import com.pocketapps.pockalendar.Utils.Utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Created by chandrima on 23/04/18.
 */

public class TaskFragment extends Fragment implements NumberPicker.OnValueChangeListener, NumberPicker.Formatter, TimePickerListener, View.OnClickListener, CalendarView.OnDateChangeListener, CalendarDetailsActivity.OnSaveCalendarItemListener {
    private static final String TIMEPICKERTAG = "timePickerTag";
    private static final String REPEATALARM_DEFAULT = "never";
    private static final String REPEATALARM_WEEKLY = "weekly";
    private static final String REPEATALARM_DAILY = "daily";
    private static final String REPEATALARM_WEEKDAYS = "weekdays";
    private static final String REPEATALARM_WEEKENDS = "weekends";
    private static final String DATE = "date";
    private static final String REPEATALARMSTATUS = "repeatalarmstatus";
    private static final String SUBJECT = "subject";
    private static final String DESCRIPTION = "description";
    private static final String TIME = "time";
    private static final String ALARMONOFF = "alarmonoff";
    private static final String SNOOZEMINUTES = "snoozeminutes";

    private NumberPicker mAlarmTimePicker;
    private EditText mSubject;
    private TextView mTime;
    private EditText mDescription;
    private ImageButton mAlarmOnOff;
    private CalendarView mCalendarDate;
    private DialogFragment mTimePickerFragment;
    private TextView mAlarmLabel;
    private RadioGroup mTaskRepeatSchedule;

    private boolean mIsAlarmOn = true;
    private PokalSharedPreferences mPokalSharedPreferences;
    private String mDate;
    private String mAlarmTime;
    private CalendarItem mCalendarItem;
    private long mCalendarRawDate;

    //-----------------------------------------------------------Life cycle methods---------------------------------------//

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.new_pokal_task_layout, container, false);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        mPokalSharedPreferences = PokalSharedPreferences.getInstance(getActivity().getApplicationContext());

        Bundle bundle = getArguments();
        if (bundle != null) {
            mCalendarItem = (CalendarItem) bundle.getSerializable(CalendarDetailsActivity.OPENTASK);
        }

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mSubject = (EditText) view.findViewById(R.id.taskSubject);
        mTime = (TextView) view.findViewById(R.id.taskTime);
        mCalendarDate = (CalendarView) view.findViewById(R.id.taskDate);
        mDescription = (EditText) view.findViewById(R.id.taskDescription);
        mAlarmOnOff = (ImageButton) view.findViewById(R.id.taskAlarmButton);
        mAlarmTimePicker = (NumberPicker) view.findViewById(R.id.taskAlarmMinutesBefore);
        mAlarmLabel = (TextView) view.findViewById(R.id.taskAlarmButtonLabel);
        mTaskRepeatSchedule = (RadioGroup) view.findViewById(R.id.taskRepeat);

        mTime.setOnClickListener(this);
        setUpAlarmTimePicker();
        // All dates before current date will be disabled
        mCalendarDate.setMinDate(System.currentTimeMillis() - 1000);
        mCalendarDate.setOnDateChangeListener(this);
        mAlarmOnOff.setOnClickListener(this);

        if(savedInstanceState != null) {
            mCalendarDate.setDate(savedInstanceState.getLong(DATE));
            mCalendarRawDate = savedInstanceState.getLong(DATE);
            mSubject.setText(savedInstanceState.getString(SUBJECT));
            mTime.setText(savedInstanceState.getString(TIME));
            mDescription.setText(savedInstanceState.getString(DESCRIPTION));
            mIsAlarmOn = savedInstanceState.getBoolean(ALARMONOFF);
            updateAlarmToggleState();
            if (mIsAlarmOn)
                mAlarmTimePicker.setValue(savedInstanceState.getInt(SNOOZEMINUTES));
            mTaskRepeatSchedule.check(savedInstanceState.getInt(REPEATALARMSTATUS));
        }

        if (mCalendarItem != null) {
           mCalendarDate.setDate(mCalendarItem.getCalendarDate());
           mSubject.setText(mCalendarItem.getSubject());
           mTime.setText(mCalendarItem.getStartTime());
           mDescription.setText(mCalendarItem.getDescription());
           mIsAlarmOn = mCalendarItem.isAlarmOn();
           updateAlarmToggleState();
           if (mIsAlarmOn)
               mAlarmTimePicker.setValue(Integer.parseInt(mCalendarItem.getAlarmTime().split(" ")[0]));
        }

        // Default date
        mDate = Utils.getCurrentDayAndDate();
        mAlarmLabel.setText(getString(R.string.TaskAlarmLabel, mIsAlarmOn ? "ON" : "OFF"));
        // Default repeat is never
        mTaskRepeatSchedule.check(R.id.taskRepeatNever);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(DATE, mCalendarRawDate);
        outState.putString(SUBJECT, mSubject.getText().toString());
        outState.putString(TIME, mTime.getText().toString());
        outState.putString(DESCRIPTION, mDescription.getText().toString());
        outState.putBoolean(ALARMONOFF, mIsAlarmOn);
        outState.putInt(SNOOZEMINUTES, mAlarmTimePicker.getValue());
        outState.putInt(REPEATALARMSTATUS, mTaskRepeatSchedule.getCheckedRadioButtonId());
    }

    //-------------------------------------------------Event handlers------------------------------------------------------//

    @Override
    public void onValueChange(NumberPicker numberPicker, int i, int i1) {
        mAlarmTime = String.valueOf(numberPicker.getValue());
    }

    @Override
    public String format(int i) {
        return i + " MIN";
    }

    @Override
    public void onTimeSet(String time) {
        ((TimePickerFragment)mTimePickerFragment).unregisterTimePickerListener();
        mTime.setText(time);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.taskTime:
                mTimePickerFragment = new TimePickerFragment();
                ((TimePickerFragment)mTimePickerFragment).registerTimePickerListener(this);
                mTimePickerFragment.show(getActivity().getSupportFragmentManager(), TIMEPICKERTAG);
                break;
            case R.id.taskAlarmButton:
                mIsAlarmOn = !mIsAlarmOn;
                updateAlarmToggleState();
                break;
        }
    }

    @Override
    public void onSelectedDayChange(@NonNull CalendarView calendarView, int year, int month, int dayOfMonth) {
        Calendar calendar = new GregorianCalendar(year, month, dayOfMonth);
        mCalendarRawDate = calendar.getTimeInMillis();
        mDate = Utils.getDayAndDateShortFormat(Utils.getStringDateFromCalendar(calendar));
    }

    @Override
    public void onSaveCalendarItem() {
        if (Utils.isEmpty(mSubject.getText().toString()) || Utils.isEmpty(mTime.getText().toString()))
            return;

        if (mCalendarItem != null) {
            mPokalSharedPreferences.deleteCalendarItem(mCalendarItem, Utils.POKAL_TASK);
        }

        CalendarItem calendarItem = new CalendarItem();
        calendarItem.setDate(mDate);
        calendarItem.setCalendarDate(mCalendarRawDate);
        calendarItem.setType(Utils.POKAL_TASK);
        calendarItem.setSubject(mSubject.getText().toString());
        calendarItem.setDescription(mDescription.getText().toString());
        calendarItem.setStartTime(mTime.getText().toString());
        calendarItem.setAlarmOn(mIsAlarmOn);
        calendarItem.setAlarmTime(mAlarmTime);
        calendarItem.setAlarmRepeatSchedule(getAlarmRepeatSchedule());

        ArrayList<CalendarItem> arrayList = new ArrayList<>();
        if (mPokalSharedPreferences.getCalendarItems(Utils.POKAL_TASK) != null)
            arrayList = mPokalSharedPreferences.getCalendarItems(Utils.POKAL_TASK);
        arrayList.add(calendarItem);

        mPokalSharedPreferences.saveCalendarItemToStore(arrayList, Utils.POKAL_TASK);;
    }

    public static class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {
        private TimePickerListener mTimePickerListener;
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);
            return new TimePickerDialog(getActivity(), this, hour, minute, DateFormat.is24HourFormat(getActivity()));
        }

        @Override
        public void onTimeSet(TimePicker timePicker, int i, int i1) {
            String time = String.valueOf(i) + ":" + String.valueOf(i1);
            time = Utils.getTwelveHourDateFromTwentyFourHourFormat(time);
            mTimePickerListener.onTimeSet(time);
        }

        public void registerTimePickerListener(TimePickerListener listener) {
            mTimePickerListener = listener;
        }

        public void unregisterTimePickerListener() {
            mTimePickerListener = null;
        }
    }
    //-----------------------------------------------------------private methods-----------------------------------------//
    private void setUpAlarmTimePicker() {
        mAlarmTimePicker.setMinValue(5);
        mAlarmTimePicker.setMaxValue(30);
        mAlarmTimePicker.setFormatter(this);
        mAlarmTimePicker.setOnValueChangedListener(this);
    }

    private String getAlarmRepeatSchedule() {
        switch(mTaskRepeatSchedule.getCheckedRadioButtonId()) {
            case R.id.taskRepeatWeekly:
                return REPEATALARM_WEEKLY;
            case R.id.taskRepeatDaily:
                return REPEATALARM_DAILY;
            case R.id.taskRepeatWeekdays:
                return REPEATALARM_WEEKDAYS;
            case R.id.taskRepeatWeekends:
                return REPEATALARM_WEEKENDS;
            default:
                return REPEATALARM_DEFAULT;
        }
    }

    private void updateAlarmToggleState() {
        mAlarmOnOff.setImageDrawable(mIsAlarmOn ? getResources().getDrawable(R.drawable.ic_alarm_on_black_24dp) : getResources().getDrawable(R.drawable.ic_alarm_off_black_24dp));
        mAlarmTimePicker.setVisibility(mIsAlarmOn ? View.VISIBLE : View.INVISIBLE);
        mAlarmLabel.setText(getString(R.string.TaskAlarmLabel, mIsAlarmOn ? "ON" : "OFF"));
    }
}
