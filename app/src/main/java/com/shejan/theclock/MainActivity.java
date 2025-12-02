package com.shejan.theclock;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        com.google.android.material.bottomnavigation.BottomNavigationView bottomNav = findViewById(
                R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(navListener);

        // Set default fragment
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new WorldClockFragment()).commit();
            bottomNav.setSelectedItemId(R.id.navigation_world_clock);
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(
                    android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[] { android.Manifest.permission.POST_NOTIFICATIONS }, 101);
            }
        }

        checkExactAlarmPermission();
    }

    private void checkExactAlarmPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            android.app.AlarmManager alarmManager = (android.app.AlarmManager) getSystemService(
                    android.content.Context.ALARM_SERVICE);
            if (!alarmManager.canScheduleExactAlarms()) {
                new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("Permission Required")
                        .setMessage(
                                "To ensure alarms ring precisely and show the alarm icon, please grant the 'Alarms & reminders' permission.")
                        .setPositiveButton("Grant", (dialog, which) -> {
                            android.content.Intent intent = new android.content.Intent(
                                    android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                            startActivity(intent);
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        }
    }

    private final com.google.android.material.navigation.NavigationBarView.OnItemSelectedListener navListener = item -> {
        androidx.fragment.app.Fragment selectedFragment = null;
        int itemId = item.getItemId();
        if (itemId == R.id.navigation_alarm) {
            selectedFragment = new AlarmFragment();
        } else if (itemId == R.id.navigation_world_clock) {
            selectedFragment = new WorldClockFragment();
        } else if (itemId == R.id.navigation_timer) {
            selectedFragment = new TimerFragment();
        } else if (itemId == R.id.navigation_stopwatch) {
            selectedFragment = new StopwatchFragment();
        }

        if (selectedFragment != null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    selectedFragment).commit();
        }
        return true;
    };

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu, menu);

        android.content.SharedPreferences prefs = getSharedPreferences(getPackageName() + "_preferences", MODE_PRIVATE);
        boolean is24Hour = prefs.getBoolean("use_24_hour_format", false);
        android.view.MenuItem toggleItem = menu.findItem(R.id.action_toggle_format);
        if (toggleItem != null) {
            toggleItem.setChecked(is24Hour);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@androidx.annotation.NonNull android.view.MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_screen_saver) {
            android.widget.Toast.makeText(this, "Screen Saver clicked", android.widget.Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.action_settings) {
            android.content.Intent intent = new android.content.Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_help) {
            android.widget.Toast.makeText(this, "Help clicked", android.widget.Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.action_toggle_format) {
            boolean isChecked = !item.isChecked();
            item.setChecked(isChecked);

            android.content.SharedPreferences prefs = getSharedPreferences(getPackageName() + "_preferences",
                    MODE_PRIVATE);
            prefs.edit().putBoolean("use_24_hour_format", isChecked).apply();

            androidx.fragment.app.Fragment fragment = getSupportFragmentManager()
                    .findFragmentById(R.id.fragment_container);
            if (fragment instanceof WorldClockFragment) {
                ((WorldClockFragment) fragment).updateClockFormat(isChecked);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
