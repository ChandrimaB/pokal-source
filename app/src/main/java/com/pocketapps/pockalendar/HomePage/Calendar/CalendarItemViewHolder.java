package com.pocketapps.pockalendar.HomePage.Calendar;

import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.pocketapps.pockalendar.HomePage.Weather.WeatherItemsViewHolders.WeatherReportViewHolder;
import com.pocketapps.pockalendar.R;
import com.pocketapps.pockalendar.SchedulePage.CalendarDetailsPage.OnCalendarItemClickListener;

/**
 * Created by chandrima on 29/03/18.
 */

public class CalendarItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

    public ImageView itemType;
    public TextView subject;
    public TextView startTime;
    public TextView endTime;
    public TextView date;
    public TextView location;
    public ImageButton alarmStatus;
    public View meetingIndicator;
    public ConstraintLayout mConstraintLayout;
    public TextView noMeeting;
    public OnCalendarItemViewHolderClickListener mCalendarItemViewHolderClickListener;

    @Override
    public void onClick(View view) {
        mCalendarItemViewHolderClickListener.onClick(getAdapterPosition());
    }


    public interface OnCalendarItemViewHolderClickListener {
        void onClick(int position);
    }

    public CalendarItemViewHolder(View itemView, boolean hasData, OnCalendarItemViewHolderClickListener listener) {
        super(itemView);
        mCalendarItemViewHolderClickListener = listener;
        if (!hasData) {
            mConstraintLayout = (ConstraintLayout) itemView;
            mConstraintLayout.setOnClickListener(this);
            noMeeting = (TextView)mConstraintLayout.getChildAt(0);
            return;
        }

        mConstraintLayout = (ConstraintLayout)itemView;
        mConstraintLayout.setOnClickListener(this);
        itemType = (ImageView) mConstraintLayout.getChildAt(0);
        startTime = (TextView) mConstraintLayout.getChildAt(1);
        endTime = (TextView) mConstraintLayout.getChildAt(2);
        meetingIndicator = mConstraintLayout.getChildAt(3);
        subject = (TextView) mConstraintLayout.getChildAt(4);
        date = (TextView) mConstraintLayout.getChildAt(5);
        location = (TextView) mConstraintLayout.getChildAt(6);
        alarmStatus = (ImageButton) mConstraintLayout.getChildAt(7);
    }
}
