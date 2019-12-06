package com.pocketapps.pockalendar.CalendarItemObjectModel;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by chandrima on 13/04/18.
 */

public class CalendarItem implements Serializable {
    private String date;
    private long calendarDate;
    private String startTime;
    private String endTime;
    private String subject;
    private String description;
    private String type;
    private String creator;
    private ArrayList<String> attendees;
    private String location;
    private ArrayList<String> attachments;
    private boolean alarmOn;
    private String alarmTime;
    private String alarmRepeatSchedule;

    //-----------------------------------------------------------getters------------------------------------------//

    public String getDate() {
        return date;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public String getSubject() {
        return subject;
    }

    public String getDescription() {
        return description;
    }

    public String getType() {
        return type;
    }

    public String getCreator() {
        return creator;
    }

    public ArrayList<String> getAttendees() {
        return attendees;
    }

    public String getLocation() {
        return location;
    }

    public ArrayList<String> getAttachments() {
        return attachments;
    }

    public boolean isAlarmOn() {
        return alarmOn;
    }

    public String getAlarmTime() {
        return alarmTime;
    }

    public String getAlarmRepeatSchedule() {
        return alarmRepeatSchedule;
    }

    public long getCalendarDate() {
        return calendarDate;
    }

    //-----------------------------------------------------------setters------------------------------------------//


    public void setDate(String date) {
        this.date = date;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public void setAttendees(ArrayList<String> attendees) {
        this.attendees = attendees;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setAttachments(ArrayList<String> attachments) {
        this.attachments = attachments;
    }

    public void setAlarmOn(boolean alarmOn) {
        this.alarmOn = alarmOn;
    }

    public void setAlarmTime(String alarmTime) {
        this.alarmTime = alarmTime;
    }

    public void setAlarmRepeatSchedule(String alarmRepeatSchedule) {
        this.alarmRepeatSchedule = alarmRepeatSchedule;
    }

    public void setCalendarDate(long calendarDate) {
        this.calendarDate = calendarDate;
    }

//-------------------------------------------------Other methods--------------------------------------------------//

    @Override
    public boolean equals(Object obj) {
        if (this==obj)
            return true;
        if (this == null)
            return false;
        if (this.getClass() != obj.getClass())
            return false;

        CalendarItem calendarItem = (CalendarItem) obj ;
        boolean isEqual = false;
        if (this.subject != null)
            isEqual = this.subject.equals(calendarItem.getSubject()) && this.date.equals(calendarItem.getDate());
        else
            isEqual = this.description.contains(calendarItem.getDescription()) && this.date.equals(calendarItem.getDate());

        return isEqual;
    }
}
