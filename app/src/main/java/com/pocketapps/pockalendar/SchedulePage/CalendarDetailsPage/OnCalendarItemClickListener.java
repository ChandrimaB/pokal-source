package com.pocketapps.pockalendar.SchedulePage.CalendarDetailsPage;

import com.pocketapps.pockalendar.CalendarItemObjectModel.CalendarItem;

/**
 * Created by chandrima on 08/04/18.
 */

public interface OnCalendarItemClickListener {
    public static final String CALENDAR_EXTRA = "calendar_extra";
    void onCalendarItemClick(CalendarItem calendarItem);
    void onEmptyCalendarClick();
}
