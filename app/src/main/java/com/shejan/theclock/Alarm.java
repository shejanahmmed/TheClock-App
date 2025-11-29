package com.shejan.theclock;

import java.util.ArrayList;
import java.util.List;

public class Alarm {
    private String id;
    private int hour;
    private int minute;
    private boolean isEnabled;
    private String label;
    private boolean isRecurring;
    private List<Integer> days; // Calendar.SUNDAY, etc.
    private boolean isAm; // Helper for display if needed, though usually calculated

    public Alarm(String id, int hour, int minute, boolean isEnabled) {
        this.id = id;
        this.hour = hour;
        this.minute = minute;
        this.isEnabled = isEnabled;
        this.days = new ArrayList<>();
        this.label = "Alarm";
    }

    public String getId() {
        return id;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public boolean isRecurring() {
        return isRecurring;
    }

    public void setRecurring(boolean recurring) {
        isRecurring = recurring;
    }

    public List<Integer> getDays() {
        return days;
    }

    public void setDays(List<Integer> days) {
        this.days = days;
    }

    private String ringtoneUri;

    public String getRingtoneUri() {
        return ringtoneUri;
    }

    public void setRingtoneUri(String ringtoneUri) {
        this.ringtoneUri = ringtoneUri;
    }

    private boolean snoozeEnabled = true;
    private int snoozeInterval = 5; // minutes
    private int snoozeTimes = 3;

    public boolean isSnoozeEnabled() {
        return snoozeEnabled;
    }

    public void setSnoozeEnabled(boolean snoozeEnabled) {
        this.snoozeEnabled = snoozeEnabled;
    }

    public int getSnoozeInterval() {
        return snoozeInterval;
    }

    public void setSnoozeInterval(int snoozeInterval) {
        this.snoozeInterval = snoozeInterval;
    }

    public int getSnoozeTimes() {
        return snoozeTimes;
    }

    public void setSnoozeTimes(int snoozeTimes) {
        this.snoozeTimes = snoozeTimes;
    }
}
