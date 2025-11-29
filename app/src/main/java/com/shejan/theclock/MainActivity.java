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
        }
        return super.onOptionsItemSelected(item);
    }
}
