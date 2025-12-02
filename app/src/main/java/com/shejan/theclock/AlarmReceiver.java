package com.shejan.theclock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;

import android.annotation.SuppressLint;
import android.os.Build;
import androidx.core.app.NotificationCompat;

public class AlarmReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "alarm_channel_v3";
    private static final String CHANNEL_NAME = "Alarms";

    @SuppressLint("UseFullScreenIntent")
    @Override
    public void onReceive(Context context, Intent intent) {
        if ("STOP_RINGTONE".equals(intent.getAction())) {
            Intent serviceIntent = new Intent(context, RingtoneService.class);
            context.stopService(serviceIntent);
            return;
        }

        String alarmId = intent.getStringExtra("ALARM_ID");
        String label = intent.getStringExtra("ALARM_LABEL");
        String ringtoneUriString = intent.getStringExtra("ALARM_RINGTONE");

        boolean snoozeEnabled = intent.getBooleanExtra("ALARM_SNOOZE_ENABLED", false);
        int snoozeInterval = intent.getIntExtra("ALARM_SNOOZE_INTERVAL", 5);
        int snoozeTimes = intent.getIntExtra("ALARM_SNOOZE_TIMES", 3);

        if ("SNOOZE_RINGTONE".equals(intent.getAction())) {
            Intent serviceIntent = new Intent(context, RingtoneService.class);
            context.stopService(serviceIntent);

            if (snoozeEnabled && snoozeTimes > 0) {
                java.util.Calendar now = java.util.Calendar.getInstance();
                now.add(java.util.Calendar.MINUTE, snoozeInterval);

                Alarm snoozeAlarm = new Alarm(alarmId, now.get(java.util.Calendar.HOUR_OF_DAY),
                        now.get(java.util.Calendar.MINUTE), true);
                snoozeAlarm.setLabel(label);
                snoozeAlarm.setRingtoneUri(ringtoneUriString);
                snoozeAlarm.setSnoozeEnabled(true);
                snoozeAlarm.setSnoozeInterval(snoozeInterval);
                snoozeAlarm.setSnoozeTimes(snoozeTimes - 1);

                AlarmScheduler.schedule(context, snoozeAlarm);
            }

            NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            nm.cancel(alarmId != null ? alarmId.hashCode() : 0);
            return;
        }

        // Start RingtoneService as Foreground Service
        Intent serviceIntent = new Intent(context, RingtoneService.class);
        serviceIntent.putExtra("ALARM_ID", alarmId);
        serviceIntent.putExtra("ALARM_LABEL", label);
        serviceIntent.putExtra("ALARM_RINGTONE", ringtoneUriString);
        serviceIntent.putExtra("ALARM_SNOOZE_ENABLED", snoozeEnabled);
        serviceIntent.putExtra("ALARM_SNOOZE_INTERVAL", snoozeInterval);
        serviceIntent.putExtra("ALARM_SNOOZE_TIMES", snoozeTimes);

        androidx.core.content.ContextCompat.startForegroundService(context, serviceIntent);

        // Reschedule if recurring
        if (alarmId != null) {
            java.util.List<Alarm> alarms = AlarmRepository.loadAlarms(context);
            for (Alarm alarm : alarms) {
                if (alarm.getId().equals(alarmId) && alarm.isRecurring() && alarm.isEnabled()) {
                    AlarmScheduler.schedule(context, alarm);
                    break;
                }
            }
        }
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Channel for Alarm Notifications");
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[] { 0, 1000, 1000, 1000, 1000 });

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
