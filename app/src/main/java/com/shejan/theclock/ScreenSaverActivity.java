package com.shejan.theclock;

import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import androidx.appcompat.app.AppCompatActivity;

public class ScreenSaverActivity extends AppCompatActivity {

        private ClockView clockView;
        private android.view.GestureDetector gestureDetector;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);

                // Hide status bar and navigation bar for immersive mode
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                                WindowManager.LayoutParams.FLAG_FULLSCREEN);
                getWindow().getDecorView().setSystemUiVisibility(
                                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                                                | View.SYSTEM_UI_FLAG_FULLSCREEN);

                // Keep screen on
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

                setContentView(R.layout.activity_screen_saver);

                clockView = findViewById(R.id.clock_view);

                gestureDetector = new android.view.GestureDetector(this,
                                new android.view.GestureDetector.SimpleOnGestureListener() {
                                        @Override
                                        public boolean onDoubleTap(android.view.MotionEvent e) {
                                                finish();
                                                return true;
                                        }

                                        @Override
                                        public boolean onDown(android.view.MotionEvent e) {
                                                return true;
                                        }
                                });

                View rootView = findViewById(android.R.id.content);
                rootView.setKeepScreenOn(true); // Ensure screen stays awake
                rootView.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, android.view.MotionEvent event) {
                                return gestureDetector.onTouchEvent(event);
                        }
                });
        }

        @Override
        protected void onResume() {
                super.onResume();

                // Hide UI again in case it reappeared
                getWindow().getDecorView().setSystemUiVisibility(
                                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                                                | View.SYSTEM_UI_FLAG_FULLSCREEN);

                android.content.SharedPreferences defaultPrefs = getSharedPreferences(
                                getPackageName() + "_preferences", android.content.Context.MODE_PRIVATE);
                boolean is24Hour = defaultPrefs.getBoolean("use_24_hour_format", false);

                android.content.SharedPreferences settingsPrefs = getSharedPreferences(
                                "Settings", android.content.Context.MODE_PRIVATE);
                boolean isNightMode = settingsPrefs.getBoolean("screen_saver_night_mode", false);

                if (clockView != null) {
                        clockView.set24HourFormat(is24Hour);
                        clockView.setAlpha(isNightMode ? 0.4f : 1.0f);
                }
        }
}
