package com.pocketapps.pockalendar.SchedulePage.CalendarDetailsPage;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.pocketapps.pockalendar.R;
import com.pocketapps.pockalendar.CalendarItemObjectModel.CalendarItem;
import com.pocketapps.pockalendar.Utils.Utils;

import java.util.ArrayList;

/**
 * Created by chandrima on 08/04/18.
 */

public class CalendarDetailsFragment extends Fragment {
    private CalendarItem mCalendarItemDetails = new CalendarItem();
    private TextView mSubject;
    private TextView mMeetingDate;
    private TextView mStartTime;
    private TextView mEndTime;
    private TextView mLocation;
    private TextView mMeetingType;
    private TextView mDescription;
    private TextView mCreator;
    private TextView mParticipants;
    private TextView mAttachments;
    private View mMeetingIndicator;
    private ConstraintLayout mConstraintLayout;

    //--------------------------------------------------------Life cycle methods------------------------------------------//
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.calendar_item_expanded, container, false);
        Intent intent = getActivity().getIntent();
        mCalendarItemDetails = (CalendarItem) intent.getSerializableExtra(OnCalendarItemClickListener.CALENDAR_EXTRA);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        createViews();
        applyBackgroundForItemType(mCalendarItemDetails.getType());
        initViews();
    }

    //--------------------------------------------------------Event handlers------------------------------------------//

    //--------------------------------------------------------Private methods------------------------------------------//
    private void createViews() {
        mConstraintLayout = (ConstraintLayout) getActivity().findViewById(R.id.calendarItem);
        mSubject = (TextView) mConstraintLayout.findViewById(R.id.meetingSubject);
        mMeetingDate = (TextView) mConstraintLayout.findViewById(R.id.meetingDate);
        mStartTime = (TextView) mConstraintLayout.findViewById(R.id.meetingStartTime);
        mEndTime = (TextView) mConstraintLayout.findViewById(R.id.meetingEndTime);
        mLocation = (TextView) mConstraintLayout.findViewById(R.id.meetingPlace);
        mMeetingType = (TextView) getActivity().findViewById(R.id.type);
        mDescription = (TextView) getActivity().findViewById(R.id.description);
        mCreator = (TextView) getActivity().findViewById(R.id.creator);
        mParticipants = (TextView) getActivity().findViewById(R.id.participants);
        mAttachments = (TextView) getActivity().findViewById(R.id.attachments);
        mMeetingIndicator = getActivity().findViewById(R.id.meetingIndicator);
    }

    private void applyBackgroundForItemType(String type) {
        mMeetingIndicator.setBackground(getResources().getDrawable(Utils.getCalendarIndicationForType(type)));
    }

    private void initViews() {
        mSubject.setText(mCalendarItemDetails.getSubject());
        mMeetingDate.setText(Utils.getDayAndDateLongFormat(mCalendarItemDetails.getDate()));
        String startTime = mCalendarItemDetails.getStartTime();
        mStartTime.setText(getString(R.string.MeetingStartTime, startTime));
        if (Utils.isAllDay(startTime)) {
            mEndTime.setVisibility(View.INVISIBLE);
        } else {
            mEndTime.setText(mCalendarItemDetails.getEndTime());
        }
        String location = mCalendarItemDetails.getLocation();
        if (!Utils.isEmpty(location))
            mLocation.setText(location);
        else
            mLocation.setVisibility(View.INVISIBLE);
        mMeetingType.setText(mCalendarItemDetails.getType());
        mDescription.setText(mCalendarItemDetails.getDescription());
        mCreator.setText("Creator: " + mCalendarItemDetails.getCreator());
        ArrayList<String> attendees = mCalendarItemDetails.getAttendees();
        mParticipants.setText(attendees == null || attendees.size() == 0 ? "No attendees" : "Attendees\n" + attendees);
        ArrayList<String> attachments = mCalendarItemDetails.getAttachments();
        mAttachments.setText(attachments == null || attachments.size() == 0 ? "No attachments" : "Attachments\n" + attachments);
    }
}
