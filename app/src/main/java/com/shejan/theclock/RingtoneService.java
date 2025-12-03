package com.shejan.theclock;

import android.app.Service;
import android.app.PendingIntent;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;

import androidx.annotation.Nullable;

public class RingtoneService extends Service {
    private MediaPlayer mediaPlayer;
    private static final String CHANNEL_ID = "alarm_channel_v3";
    private static final String CHANNEL_NAME = "Alarms";
    public static String currentRingingAlarmId = null;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private android.os.Handler silenceHandler = new android.os.Handler(android.os.Looper.getMainLooper());
    private Runnable silenceRunnable = new Runnable() {
        @Override
        public void run() {
            stopSelf();
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String alarmId = intent.getStringExtra("ALARM_ID");
        currentRingingAlarmId = alarmId;
        String label = intent.getStringExtra("ALARM_LABEL");
        String ringtoneUriString = intent.getStringExtra("ALARM_RINGTONE");
        boolean snoozeEnabled = intent.getBooleanExtra("ALARM_SNOOZE_ENABLED", false);
        int snoozeInterval = intent.getIntExtra("ALARM_SNOOZE_INTERVAL", 5);
        int snoozeTimes = intent.getIntExtra("ALARM_SNOOZE_TIMES", 3);

        // Create notification channel
        createNotificationChannel();

        // Create Stop PendingIntent
        Intent stopIntent = new Intent(this, AlarmReceiver.class);
        stopIntent.setAction("STOP_RINGTONE");
        PendingIntent stopPendingIntent = PendingIntent.getBroadcast(this, 0, stopIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Create Snooze PendingIntent
        PendingIntent snoozePendingIntent = null;
        if (snoozeEnabled && snoozeTimes > 0) {
            Intent snoozeIntent = new Intent(this, AlarmReceiver.class);
            snoozeIntent.setAction("SNOOZE_RINGTONE");
            snoozeIntent.putExtra("ALARM_ID", alarmId);
            snoozeIntent.putExtra("ALARM_LABEL", label);
            snoozeIntent.putExtra("ALARM_RINGTONE", ringtoneUriString);
            snoozeIntent.putExtra("ALARM_SNOOZE_ENABLED", true);
            snoozeIntent.putExtra("ALARM_SNOOZE_INTERVAL", snoozeInterval);
            snoozeIntent.putExtra("ALARM_SNOOZE_TIMES", snoozeTimes);

            // Use a unique request code for snooze pending intent
            snoozePendingIntent = PendingIntent.getBroadcast(this, 1, snoozeIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        }

        // Build notification
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault());
        String currentTime = sdf.format(new java.util.Date());

        androidx.core.app.NotificationCompat.Builder builder = new androidx.core.app.NotificationCompat.Builder(this,
                CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher) // Ensure this icon exists
                .setContentTitle(currentTime)
                .setContentText(label != null && !label.isEmpty() ? label : "Alarm ringing")
                .setPriority(androidx.core.app.NotificationCompat.PRIORITY_MAX) // MAX for heads-up
                .setCategory(androidx.core.app.NotificationCompat.CATEGORY_ALARM)
                .setAutoCancel(false)
                .setOngoing(true)
                .setVibrate(new long[] { 0, 1000, 1000, 1000, 1000 });

        if (snoozePendingIntent != null) {
            builder.addAction(R.drawable.ic_timer, "Snooze", snoozePendingIntent);
        }

        builder.addAction(R.drawable.ic_alarm, "Stop", stopPendingIntent);

        // Full screen intent
        Intent fullScreenIntent = new Intent(this, MainActivity.class);
        PendingIntent fullScreenPendingIntent = PendingIntent.getActivity(this, 0,
                fullScreenIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        builder.setFullScreenIntent(fullScreenPendingIntent, true);

        int notificationId = alarmId != null ? alarmId.hashCode() : 1;
        startForeground(notificationId, builder.build());

        Uri alarmUri;
        if (ringtoneUriString != null && !ringtoneUriString.isEmpty()) {
            alarmUri = Uri.parse(ringtoneUriString);
        } else {
            alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        }

        mediaPlayer = MediaPlayer.create(this, alarmUri);
        if (mediaPlayer == null) {
            // Fallback to default alarm sound
            alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            mediaPlayer = MediaPlayer.create(this, alarmUri);
        }
        if (mediaPlayer == null) {
            // Fallback to notification sound
            alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            mediaPlayer = MediaPlayer.create(this, alarmUri);
        }
        if (mediaPlayer == null) {
            // Fallback to ringtone sound
            alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            mediaPlayer = MediaPlayer.create(this, alarmUri);
        }

        if (mediaPlayer != null) {
            mediaPlayer.setLooping(true);
            mediaPlayer.start();
        }

        // Schedule auto-silence
        android.content.SharedPreferences prefs = getSharedPreferences("Settings", MODE_PRIVATE);
        int silenceAfterMinutes = prefs.getInt("silence_after", 10);
        if (silenceAfterMinutes > 0) {
            silenceHandler.postDelayed(silenceRunnable, silenceAfterMinutes * 60 * 1000L);
        }

        return START_NOT_STICKY;
    }

    private void createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            android.app.NotificationChannel channel = new android.app.NotificationChannel(CHANNEL_ID, CHANNEL_NAME,
                    android.app.NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Channel for Alarm Notifications");
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[] { 0, 1000, 1000, 1000, 1000 });

            android.app.NotificationManager notificationManager = getSystemService(
                    android.app.NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        currentRingingAlarmId = null;
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        if (silenceHandler != null) {
            silenceHandler.removeCallbacks(silenceRunnable);
        }
    }
}
