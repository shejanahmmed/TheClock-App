package com.shejan.theclock;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class RingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        showOnLockScreen();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ring);

        // UI Components
        TextView textCurrentTime = findViewById(R.id.text_current_time);
        TextView textAlarmLabel = findViewById(R.id.text_alarm_label);
        Button btnSnooze = findViewById(R.id.btn_snooze);
        Button btnStop = findViewById(R.id.btn_stop);

        // Get Data from Intent
        String alarmId = getIntent().getStringExtra("ALARM_ID");
        String label = getIntent().getStringExtra("ALARM_LABEL");
        String ringtoneUri = getIntent().getStringExtra("ALARM_RINGTONE");
        boolean snoozeEnabled = getIntent().getBooleanExtra("ALARM_SNOOZE_ENABLED", false);
        int snoozeInterval = getIntent().getIntExtra("ALARM_SNOOZE_INTERVAL", 5);
        int snoozeTimes = getIntent().getIntExtra("ALARM_SNOOZE_TIMES", 3);

        // Set Data
        if (label != null && !label.isEmpty()) {
            textAlarmLabel.setText(label);
        } else {
            textAlarmLabel.setText("Alarm");
        }

        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.getDefault());
        textCurrentTime.setText(sdf.format(new Date()));

        // Buttons
        btnStop.setOnClickListener(v -> {
            stopAlarm(alarmId);
            finish();
        });

        if (snoozeEnabled && snoozeTimes > 0) {
            btnSnooze.setVisibility(android.view.View.VISIBLE);
            btnSnooze.setOnClickListener(v -> {
                snoozeAlarm(alarmId, label, ringtoneUri, snoozeInterval, snoozeTimes);
                finish();
            });
        } else {
            btnSnooze.setVisibility(android.view.View.GONE);
        }
    }

    private void stopAlarm(String alarmId) {
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.setAction("STOP_RINGTONE");
        intent.putExtra("ALARM_ID", alarmId);
        sendBroadcast(intent);
    }

    private void snoozeAlarm(String alarmId, String label, String ringtoneUri, int interval, int times) {
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.setAction("SNOOZE_RINGTONE");
        intent.putExtra("ALARM_ID", alarmId);
        intent.putExtra("ALARM_LABEL", label);
        intent.putExtra("ALARM_RINGTONE", ringtoneUri);
        intent.putExtra("ALARM_SNOOZE_ENABLED", true);
        intent.putExtra("ALARM_SNOOZE_INTERVAL", interval);
        intent.putExtra("ALARM_SNOOZE_TIMES", times);
        sendBroadcast(intent);
    }

    private void showOnLockScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
            KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
            if (keyguardManager != null) {
                keyguardManager.requestDismissKeyguard(this, null);
            }
        } else {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        }
    }
}
