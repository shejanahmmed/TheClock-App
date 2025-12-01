package com.shejan.theclock;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

public class AlarmScheduler {

    public static void schedule(Context context, Alarm alarm) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = createAlarmIntent(context, alarm);

        // Use alarm ID hash code as request code to allow multiple alarms
        int requestCode = alarm.getId().hashCode();
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, alarm.getHour());
        calendar.set(Calendar.MINUTE, alarm.getMinute());
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        // If alarm time is in the past, increment day
        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        // Handle recurring alarms logic (simplified for now: find next occurrence)
        if (alarm.isRecurring() && !alarm.getDays().isEmpty()) {
            while (calendar.getTimeInMillis() <= System.currentTimeMillis() ||
                    !alarm.getDays().contains(calendar.get(Calendar.DAY_OF_WEEK))) {
                calendar.add(Calendar.DAY_OF_YEAR, 1);
            }
        }

        Intent showIntent = new Intent(context, MainActivity.class);
        showIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent showOperation = PendingIntent.getActivity(context, 0, showIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        AlarmManager.AlarmClockInfo alarmClockInfo = new AlarmManager.AlarmClockInfo(calendar.getTimeInMillis(),
                showOperation);
        alarmManager.setAlarmClock(alarmClockInfo, pendingIntent);
    }

    private static Intent createAlarmIntent(Context context, Alarm alarm) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("ALARM_ID", alarm.getId());
        intent.putExtra("ALARM_LABEL", alarm.getLabel());
        intent.putExtra("ALARM_RINGTONE", alarm.getRingtoneUri());
        intent.putExtra("ALARM_RECURRING", alarm.isRecurring());
        intent.putExtra("ALARM_SNOOZE_ENABLED", alarm.isSnoozeEnabled());
        intent.putExtra("ALARM_SNOOZE_INTERVAL", alarm.getSnoozeInterval());
        intent.putExtra("ALARM_SNOOZE_TIMES", alarm.getSnoozeTimes());
        return intent;
    }

    public static void cancel(Context context, Alarm alarm) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = createAlarmIntent(context, alarm);
        int requestCode = alarm.getId().hashCode();
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        alarmManager.cancel(pendingIntent);
        pendingIntent.cancel();
    }
}
