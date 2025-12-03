package com.shejan.theclock;

import android.os.Bundle;
import android.view.MenuItem;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Settings");
        }

        setupSilenceAfter();
        setupSnoozeLength();
        setupAlarmVolume();
        setupStartWeekOn();
        setupTimerSound();
        setupTimerVibration();
        setupScreenSaverNightMode();
        setupChangeDateTime();
    }

    private void setupScreenSaverNightMode() {
        android.widget.LinearLayout nightModeContainer = findViewById(R.id.night_mode_container);
        final androidx.appcompat.widget.SwitchCompat nightModeSwitch = findViewById(R.id.night_mode_switch);

        final android.content.SharedPreferences prefs = getSharedPreferences("Settings", MODE_PRIVATE);
        boolean isNightModeEnabled = prefs.getBoolean("screen_saver_night_mode", false); // Default false
        nightModeSwitch.setChecked(isNightModeEnabled);

        nightModeContainer.setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(android.view.View v) {
                nightModeSwitch.toggle();
                prefs.edit().putBoolean("screen_saver_night_mode", nightModeSwitch.isChecked()).apply();
            }
        });

        nightModeSwitch.setOnCheckedChangeListener(new android.widget.CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(android.widget.CompoundButton buttonView, boolean isChecked) {
                prefs.edit().putBoolean("screen_saver_night_mode", isChecked).apply();
            }
        });
    }

    private void setupTimerVibration() {
        android.widget.LinearLayout timerVibrationContainer = findViewById(R.id.timer_vibration_container);
        final androidx.appcompat.widget.SwitchCompat timerVibrationSwitch = findViewById(R.id.timer_vibration_switch);

        final android.content.SharedPreferences prefs = getSharedPreferences("Settings", MODE_PRIVATE);
        boolean isVibrationEnabled = prefs.getBoolean("timer_vibration", true); // Default true
        timerVibrationSwitch.setChecked(isVibrationEnabled);

        timerVibrationContainer.setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(android.view.View v) {
                timerVibrationSwitch.toggle();
                prefs.edit().putBoolean("timer_vibration", timerVibrationSwitch.isChecked()).apply();
            }
        });

        timerVibrationSwitch.setOnCheckedChangeListener(new android.widget.CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(android.widget.CompoundButton buttonView, boolean isChecked) {
                prefs.edit().putBoolean("timer_vibration", isChecked).apply();
            }
        });
    }

    private void setupTimerSound() {
        android.widget.LinearLayout timerSoundContainer = findViewById(R.id.timer_sound_container);
        final android.widget.TextView timerSoundValue = findViewById(R.id.timer_sound_value);

        final android.content.SharedPreferences prefs = getSharedPreferences("Settings", MODE_PRIVATE);
        String savedValue = prefs.getString("timer_sound", "Beep"); // Default Beep
        timerSoundValue.setText(savedValue);

        timerSoundContainer.setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(android.view.View v) {
                showTimerSoundDialog(timerSoundValue, prefs);
            }
        });
    }

    private void showTimerSoundDialog(final android.widget.TextView textView,
            final android.content.SharedPreferences prefs) {
        final CharSequence[] items = { "Beep", "Alarm", "Notification" };
        String currentValue = prefs.getString("timer_sound", "Beep");
        int checkedItem = 0;

        for (int i = 0; i < items.length; i++) {
            if (items[i].equals(currentValue)) {
                checkedItem = i;
                break;
            }
        }

        new androidx.appcompat.app.AlertDialog.Builder(this, R.style.Theme_TheClock_Dialog)
                .setTitle("Timer sound")
                .setSingleChoiceItems(items, checkedItem, new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(android.content.DialogInterface dialog, int which) {
                        String selectedValue = items[which].toString();
                        prefs.edit().putString("timer_sound", selectedValue).apply();
                        textView.setText(selectedValue);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void setupChangeDateTime() {
        android.widget.LinearLayout changeDateTimeContainer = findViewById(R.id.change_date_time_container);
        changeDateTimeContainer.setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(android.view.View v) {
                startActivity(new android.content.Intent(android.provider.Settings.ACTION_DATE_SETTINGS));
            }
        });
    }

    private void setupStartWeekOn() {
        android.widget.LinearLayout startWeekOnContainer = findViewById(R.id.start_week_on_container);
        final android.widget.TextView startWeekOnValue = findViewById(R.id.start_week_on_value);

        final android.content.SharedPreferences prefs = getSharedPreferences("Settings", MODE_PRIVATE);
        int savedValue = prefs.getInt("start_week_on", java.util.Calendar.SUNDAY); // Default Sunday
        updateStartWeekOnText(startWeekOnValue, savedValue);

        startWeekOnContainer.setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(android.view.View v) {
                showStartWeekOnDialog(startWeekOnValue, prefs);
            }
        });
    }

    private void updateStartWeekOnText(android.widget.TextView textView, int value) {
        switch (value) {
            case java.util.Calendar.SUNDAY:
                textView.setText("Sunday");
                break;
            case java.util.Calendar.MONDAY:
                textView.setText("Monday");
                break;
            case java.util.Calendar.TUESDAY:
                textView.setText("Tuesday");
                break;
            case java.util.Calendar.WEDNESDAY:
                textView.setText("Wednesday");
                break;
            case java.util.Calendar.THURSDAY:
                textView.setText("Thursday");
                break;
            case java.util.Calendar.FRIDAY:
                textView.setText("Friday");
                break;
            case java.util.Calendar.SATURDAY:
                textView.setText("Saturday");
                break;
        }
    }

    private void showStartWeekOnDialog(final android.widget.TextView textView,
            final android.content.SharedPreferences prefs) {
        final CharSequence[] items = { "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday" };
        final int[] values = { java.util.Calendar.SUNDAY, java.util.Calendar.MONDAY, java.util.Calendar.TUESDAY,
                java.util.Calendar.WEDNESDAY, java.util.Calendar.THURSDAY, java.util.Calendar.FRIDAY,
                java.util.Calendar.SATURDAY };

        int currentValue = prefs.getInt("start_week_on", java.util.Calendar.SUNDAY);
        int checkedItem = 0; // Default to Sunday index

        for (int i = 0; i < values.length; i++) {
            if (values[i] == currentValue) {
                checkedItem = i;
                break;
            }
        }

        new androidx.appcompat.app.AlertDialog.Builder(this, R.style.Theme_TheClock_Dialog)
                .setTitle("Start week on")
                .setSingleChoiceItems(items, checkedItem, new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(android.content.DialogInterface dialog, int which) {
                        int selectedValue = values[which];
                        prefs.edit().putInt("start_week_on", selectedValue).apply();
                        updateStartWeekOnText(textView, selectedValue);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void setupSilenceAfter() {
        android.widget.LinearLayout silenceAfterContainer = findViewById(R.id.silence_after_container);
        final android.widget.TextView silenceAfterValue = findViewById(R.id.silence_after_value);

        final android.content.SharedPreferences prefs = getSharedPreferences("Settings", MODE_PRIVATE);
        int savedValue = prefs.getInt("silence_after", 10); // Default 10 minutes
        updateSilenceAfterText(silenceAfterValue, savedValue);

        silenceAfterContainer.setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(android.view.View v) {
                showSilenceAfterDialog(silenceAfterValue, prefs);
            }
        });
    }

    private void setupSnoozeLength() {
        android.widget.LinearLayout snoozeLengthContainer = findViewById(R.id.snooze_length_container);
        final android.widget.TextView snoozeLengthValue = findViewById(R.id.snooze_length_value);

        final android.content.SharedPreferences prefs = getSharedPreferences("Settings", MODE_PRIVATE);
        int savedValue = prefs.getInt("snooze_length", 5); // Default 5 minutes
        updateSnoozeLengthText(snoozeLengthValue, savedValue);

        snoozeLengthContainer.setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(android.view.View v) {
                showSnoozeLengthDialog(snoozeLengthValue, prefs);
            }
        });
    }

    private void setupAlarmVolume() {
        android.widget.SeekBar volumeSeekBar = findViewById(R.id.alarm_volume_seekbar);
        final android.media.AudioManager audioManager = (android.media.AudioManager) getSystemService(AUDIO_SERVICE);

        if (audioManager != null) {
            int maxVolume = audioManager.getStreamMaxVolume(android.media.AudioManager.STREAM_ALARM);

            // Set default volume to full
            audioManager.setStreamVolume(android.media.AudioManager.STREAM_ALARM, maxVolume, 0);
            int currentVolume = audioManager.getStreamVolume(android.media.AudioManager.STREAM_ALARM);

            volumeSeekBar.setMax(maxVolume);
            volumeSeekBar.setProgress(currentVolume);

            volumeSeekBar.setOnSeekBarChangeListener(new android.widget.SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(android.widget.SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser) {
                        audioManager.setStreamVolume(android.media.AudioManager.STREAM_ALARM, progress, 0);
                    }
                }

                @Override
                public void onStartTrackingTouch(android.widget.SeekBar seekBar) {
                }

                @Override
                public void onStopTrackingTouch(android.widget.SeekBar seekBar) {
                }
            });
        }
    }

    private void updateSilenceAfterText(android.widget.TextView textView, int value) {
        if (value == -1) {
            textView.setText("Never");
        } else {
            textView.setText(value + " minutes");
        }
    }

    private void updateSnoozeLengthText(android.widget.TextView textView, int value) {
        textView.setText(value + " minutes");
    }

    private void showSilenceAfterDialog(final android.widget.TextView textView,
            final android.content.SharedPreferences prefs) {
        final CharSequence[] items = { "1 minute", "5 minutes", "10 minutes", "15 minutes", "20 minutes", "25 minutes",
                "30 minutes", "Never" };
        final int[] values = { 1, 5, 10, 15, 20, 25, 30, -1 };

        int currentValue = prefs.getInt("silence_after", 10);
        int checkedItem = 2; // Default to 10 minutes index

        for (int i = 0; i < values.length; i++) {
            if (values[i] == currentValue) {
                checkedItem = i;
                break;
            }
        }

        new androidx.appcompat.app.AlertDialog.Builder(this, R.style.Theme_TheClock_Dialog)
                .setTitle("Silence after")
                .setSingleChoiceItems(items, checkedItem, new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(android.content.DialogInterface dialog, int which) {
                        int selectedValue = values[which];
                        prefs.edit().putInt("silence_after", selectedValue).apply();
                        updateSilenceAfterText(textView, selectedValue);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showSnoozeLengthDialog(final android.widget.TextView textView,
            final android.content.SharedPreferences prefs) {
        final CharSequence[] items = { "1 minute", "2 minutes", "3 minutes", "4 minutes", "5 minutes", "10 minutes",
                "15 minutes", "20 minutes", "25 minutes", "30 minutes" };
        final int[] values = { 1, 2, 3, 4, 5, 10, 15, 20, 25, 30 };

        int currentValue = prefs.getInt("snooze_length", 5);
        int checkedItem = 4; // Default to 5 minutes index

        for (int i = 0; i < values.length; i++) {
            if (values[i] == currentValue) {
                checkedItem = i;
                break;
            }
        }

        new androidx.appcompat.app.AlertDialog.Builder(this, R.style.Theme_TheClock_Dialog)
                .setTitle("Snooze length")
                .setSingleChoiceItems(items, checkedItem, new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(android.content.DialogInterface dialog, int which) {
                        int selectedValue = values[which];
                        prefs.edit().putInt("snooze_length", selectedValue).apply();
                        updateSnoozeLengthText(textView, selectedValue);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
