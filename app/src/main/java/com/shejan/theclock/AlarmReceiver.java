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

        // Create notification channel
        createNotificationChannel(context);

        // Start RingtoneService
        Intent serviceIntent = new Intent(context, RingtoneService.class);
        serviceIntent.putExtra("ALARM_RINGTONE", ringtoneUriString);
        context.startService(serviceIntent);

        // Create Stop PendingIntent
        Intent stopIntent = new Intent(context, AlarmReceiver.class);
        stopIntent.setAction("STOP_RINGTONE");
        PendingIntent stopPendingIntent = PendingIntent.getBroadcast(context, 0, stopIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Create Snooze PendingIntent
        PendingIntent snoozePendingIntent = null;
        if (snoozeEnabled && snoozeTimes > 0) {
            Intent snoozeIntent = new Intent(context, AlarmReceiver.class);
            snoozeIntent.setAction("SNOOZE_RINGTONE");
            snoozeIntent.putExtra("ALARM_ID", alarmId);
            snoozeIntent.putExtra("ALARM_LABEL", label);
            snoozeIntent.putExtra("ALARM_RINGTONE", ringtoneUriString);
            snoozeIntent.putExtra("ALARM_SNOOZE_ENABLED", true);
            snoozeIntent.putExtra("ALARM_SNOOZE_INTERVAL", snoozeInterval);
            snoozeIntent.putExtra("ALARM_SNOOZE_TIMES", snoozeTimes);

            // Use a unique request code for snooze pending intent
            snoozePendingIntent = PendingIntent.getBroadcast(context, 1, snoozeIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        }

        // Build notification
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault());
        String currentTime = sdf.format(new java.util.Date());

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher) // Ensure this icon exists
                .setContentTitle(currentTime)
                .setContentText("Alarm ringing")
                .setPriority(NotificationCompat.PRIORITY_MAX) // MAX for heads-up
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setAutoCancel(true)
                .setOngoing(true) // Make it ongoing so it doesn't just swipe away without stopping sound easily
                .setVibrate(new long[] { 0, 1000, 1000, 1000, 1000 });

        if (snoozePendingIntent != null) {
            builder.addAction(R.drawable.ic_timer, "Snooze", snoozePendingIntent);
        }

        builder.addAction(R.drawable.ic_alarm, "Stop", stopPendingIntent);

        // Full screen intent (optional, for now just notification)
        Intent fullScreenIntent = new Intent(context, MainActivity.class);
        PendingIntent fullScreenPendingIntent = PendingIntent.getActivity(context, 0,
                fullScreenIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        builder.setFullScreenIntent(fullScreenPendingIntent, true);
        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        int notificationId = alarmId != null ? alarmId.hashCode() : 0;
        notificationManager.notify(notificationId, builder.build());
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
