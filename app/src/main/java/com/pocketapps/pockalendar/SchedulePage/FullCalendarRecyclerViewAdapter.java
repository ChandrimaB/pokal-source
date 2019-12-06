package com.pocketapps.pockalendar.SchedulePage;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pocketapps.pockalendar.HomePage.Calendar.CalendarHeaderViewHolder;
import com.pocketapps.pockalendar.HomePage.Calendar.CalendarItemViewHolder;
import com.pocketapps.pockalendar.R;
import com.pocketapps.pockalendar.CalendarItemObjectModel.CalendarItem;
import com.pocketapps.pockalendar.SchedulePage.CalendarDetailsPage.OnCalendarItemClickListener;
import com.pocketapps.pockalendar.UserPreferences.PokalSharedPreferences;
import com.pocketapps.pockalendar.Utils.Utils;

import java.util.ArrayList;

/**
 * Created by chandrima on 07/04/18.
 */

public class FullCalendarRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements CalendarItemViewHolder.OnCalendarItemViewHolderClickListener {
    private static final int HEADER = 1001;
    private static final int DETAILS = 1002;
    private static final int NOCALENDARITEMS = 1003;
    private static final int NOTE_BEGINNING_INDEX = 23; //excluding the auto date in the note

    private ArrayList<CalendarItem> mPokalCalendarItems = new ArrayList<>();
    private Context mContext;
    private OnCalendarItemClickListener mCalendarItemClickListener;
    private PokalSharedPreferences mPokalSharedPreferences;

    public FullCalendarRecyclerViewAdapter(ArrayList<CalendarItem> pokalCalendarItems, Context context, OnCalendarItemClickListener calendarItemClickListener, PokalSharedPreferences preferences) {
        mPokalCalendarItems = pokalCalendarItems;
        mContext = context;
        mCalendarItemClickListener = calendarItemClickListener;
        mPokalSharedPreferences = preferences;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case HEADER:
                return new CalendarHeaderViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.calendar_items_header, parent, false));
            case NOCALENDARITEMS:
                return new CalendarItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.calendar_nothing_to_show , parent, false), false, this);
            case DETAILS:
                return new CalendarItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.calendar_item_layout, parent, false), true, this);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case HEADER:
                CalendarHeaderViewHolder calendarHeaderViewHolder = (CalendarHeaderViewHolder) holder;
                calendarHeaderViewHolder.mTextView.setText(mContext.getString(R.string.CalendarListHeader));
                break;
            case NOCALENDARITEMS:
                CalendarItemViewHolder calendarItemViewHolderEmpty = (CalendarItemViewHolder) holder;
                calendarItemViewHolderEmpty.noMeeting.setText(mContext.getString(R.string.NoUpcomingMeetings));
                break;
            case DETAILS:
                if (mPokalCalendarItems == null || mPokalCalendarItems.size() == 0)
                    return;
                String type = mPokalCalendarItems.get(position - 1).getType();
                CalendarItemViewHolder calendarItemViewHolder = (CalendarItemViewHolder) holder;
                calendarItemViewHolder.meetingIndicator.setBackground(mContext.getResources().getDrawable(Utils.getCalendarIndicationForType(type)));
                String noteDescription;
                try {
                    noteDescription = mPokalCalendarItems.get(position - 1).getDescription().substring(NOTE_BEGINNING_INDEX).trim();
                } catch (StringIndexOutOfBoundsException e) {
                    noteDescription = mPokalCalendarItems.get(position - 1).getDescription().trim();
                }
                calendarItemViewHolder.subject.setText(type.equals(Utils.POKAL_NOTE) ? noteDescription : mPokalCalendarItems.get(position - 1).getSubject());
                calendarItemViewHolder.date.setVisibility(type.equals(Utils.POKAL_NOTE) ? View.GONE : View.VISIBLE);
                calendarItemViewHolder.endTime.setVisibility(type.equals(Utils.POKAL_NOTE) ? View.GONE : View.VISIBLE);
                calendarItemViewHolder.location.setVisibility(type.equals(Utils.POKAL_NOTE) ? View.GONE : View.VISIBLE);
                calendarItemViewHolder.itemType.setImageResource(Utils.getMeetingTypeDrawable(type));
                calendarItemViewHolder.alarmStatus.setVisibility(type.equals(Utils.POKAL_NOTE) ? View.GONE : View.VISIBLE);
                String date = mPokalCalendarItems.get(position - 1).getDate();
                date = type.equals(Utils.POKAL_NOTE) ? Utils.getDatePart(date) : Utils.getDayAndDateLongFormat(date);
                calendarItemViewHolder.startTime.setText(date);
                if (type.equals(Utils.POKAL_NOTE))
                    return;
                calendarItemViewHolder.date.setText(date);
                String startTime = mPokalCalendarItems.get(position - 1).getStartTime();
                calendarItemViewHolder.startTime.setText(type.equals(Utils.CALENDAR_TYPE_GOOGLE) || type.equals(Utils.CALENDAR_TYPE_POKAL) ? mContext.getString(R.string.MeetingStartTime, startTime) : startTime);
                String endTime = mPokalCalendarItems.get(position - 1).getEndTime();
                if (Utils.isAllDay(startTime)) {
                    calendarItemViewHolder.endTime.setVisibility(View.GONE);
                    calendarItemViewHolder.startTime.setText(startTime);
                } else {
                    calendarItemViewHolder.endTime.setVisibility(View.VISIBLE);
                    calendarItemViewHolder.endTime.setText(endTime);
                }
                String location = mPokalCalendarItems.get(position - 1).getLocation();
                if (Utils.isEmpty(location)) {
                    calendarItemViewHolder.location.setVisibility(View.GONE);
                    return;
                }
                calendarItemViewHolder.location.setText(location);
                break;
        }
    }

    @Override
    public int getItemViewType(int position) {
        switch (position){
            case 0:
                return HEADER;
            default:
                if (mPokalCalendarItems == null || mPokalCalendarItems.size() == 0)
                    return NOCALENDARITEMS;
                return DETAILS;
        }
    }

    @Override
    public int getItemCount() {
        if (mPokalCalendarItems == null || mPokalCalendarItems.size() == 0)
            return 1 + 1;
        return mPokalCalendarItems.size() + 1;
    }

    public void notifyCalendarChanged(ArrayList<CalendarItem> pokalCalendarItems, Context context) {
        mPokalCalendarItems = pokalCalendarItems;
        mContext = context;
        notifyDataSetChanged();
    }

    @Override
    public void onClick(int position) {
        if (mPokalCalendarItems == null || mPokalCalendarItems.size() == 0) {
            mCalendarItemClickListener.onEmptyCalendarClick();
            return;
        }
        mCalendarItemClickListener.onCalendarItemClick(mPokalCalendarItems.get(position - 1));
    }

    public boolean shouldSwipe(int position) {
        if (getItemViewType(position) != DETAILS) {
            return false;
        }

        if (mPokalCalendarItems.get(position - 1).getType().equals(Utils.CALENDAR_TYPE_GOOGLE)) {
            return false;
        }

        return true;
    }

    public void notifyDelete(int position) {
        mPokalSharedPreferences.deleteCalendarItem(mPokalCalendarItems.get(position - 1), mPokalCalendarItems.get(position - 1).getType());
        mPokalCalendarItems.remove(position - 1);
        notifyItemRemoved(position);
    }
}