package com.shejan.theclock;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ViewFlipper;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Locale;

public class TimerFragment extends Fragment {

    private ViewFlipper viewFlipper;
    private TextView inputTimeDisplay;
    private TextView countdownDisplay;
    private ProgressBar progressBar;
    private FloatingActionButton fabStart;
    private FloatingActionButton fabPauseResume;

    private String inputString = "";
    private long totalTimeInMillis = 0;
    private long timeLeftInMillis = 0;
    private boolean isTimerRunning = false;
    private CountDownTimer countDownTimer;

    public TimerFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_timer, container, false);

        viewFlipper = view.findViewById(R.id.view_flipper);
        inputTimeDisplay = view.findViewById(R.id.input_time_display);
        countdownDisplay = view.findViewById(R.id.countdown_display);
        progressBar = view.findViewById(R.id.progress_bar);
        fabStart = view.findViewById(R.id.fab_start_timer);
        FloatingActionButton fabStop = view.findViewById(R.id.fab_stop);
        fabPauseResume = view.findViewById(R.id.fab_pause_resume);
        GridLayout keypadGrid = view.findViewById(R.id.keypad_grid);
        ImageButton btnBackspace = view.findViewById(R.id.btn_backspace);

        // Setup Keypad Listeners
        for (int i = 0; i < keypadGrid.getChildCount(); i++) {
            View child = keypadGrid.getChildAt(i);
            if (child instanceof Button) {
                child.setOnClickListener(v -> {
                    String tag = (String) v.getTag();
                    appendInput(tag);
                });
            }
        }

        btnBackspace.setOnClickListener(v -> deleteInput());

        btnBackspace.setOnLongClickListener(v -> {
            clearInput();
            return true;
        });

        fabStart.setOnClickListener(v -> startTimer());
        fabStop.setOnClickListener(v -> stopTimer());
        fabPauseResume.setOnClickListener(v -> {
            if (isTimerRunning) {
                pauseTimer();
            } else {
                resumeTimer();
            }
        });

        updateInputDisplay();

        return view;
    }

    private void appendInput(String digit) {
        if (inputString.length() >= 6)
            return; // Max 6 digits (HHMMSS)
        if (inputString.isEmpty() && digit.equals("0"))
            return; // Prevent leading zeros
        if (inputString.isEmpty() && digit.equals("00"))
            return;

        inputString += digit;
        if (inputString.length() > 6) {
            inputString = inputString.substring(0, 6);
        }
        updateInputDisplay();
    }

    private void deleteInput() {
        if (!inputString.isEmpty()) {
            inputString = inputString.substring(0, inputString.length() - 1);
            updateInputDisplay();
        }
    }

    private void clearInput() {
        inputString = "";
        updateInputDisplay();
    }

    private void updateInputDisplay() {
        long seconds = 0;
        long minutes = 0;
        long hours = 0;

        if (!inputString.isEmpty()) {
            int val = Integer.parseInt(inputString);
            seconds = val % 100;
            minutes = (val / 100) % 100;
            hours = val / 10000;
        }

        inputTimeDisplay.setText(String.format(Locale.getDefault(), "%02dh %02dm %02ds", hours, minutes, seconds));

        if (inputString.isEmpty()) {
            fabStart.setVisibility(View.INVISIBLE);
            inputTimeDisplay.setTextColor(getResources().getColor(android.R.color.darker_gray, null));
        } else {
            fabStart.setVisibility(View.VISIBLE);
            inputTimeDisplay.setTextColor(getResources().getColor(android.R.color.white, null));
        }
    }

    private void startTimer() {
        if (inputString.isEmpty())
            return;

        int val = Integer.parseInt(inputString);
        long seconds = val % 100;
        long minutes = (val / 100) % 100;
        long hours = val / 10000;

        totalTimeInMillis = (hours * 3600 + minutes * 60 + seconds) * 1000;
        if (totalTimeInMillis == 0)
            return;

        timeLeftInMillis = totalTimeInMillis;

        viewFlipper.showNext(); // Switch to countdown view
        resumeTimer();
    }

    private void resumeTimer() {
        countDownTimer = new CountDownTimer(timeLeftInMillis, 10) { // Update every 10ms for smooth progress
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateCountdownUI();
            }

            @Override
            public void onFinish() {
                isTimerRunning = false;
                timeLeftInMillis = 0;
                updateCountdownUI();
                fabPauseResume.setVisibility(View.INVISIBLE);
                // Show alert or play sound
                Toast.makeText(getContext(), "Timer Finished!", Toast.LENGTH_LONG).show();
            }
        }.start();

        isTimerRunning = true;
        fabPauseResume.setImageResource(R.drawable.ic_pause);
        fabPauseResume.setVisibility(View.VISIBLE);
    }

    private void pauseTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        isTimerRunning = false;
        fabPauseResume.setImageResource(R.drawable.ic_play);
    }

    private void stopTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        isTimerRunning = false;
        timeLeftInMillis = 0;
        viewFlipper.showPrevious(); // Switch back to setup view
    }

    private void updateCountdownUI() {
        int hours = (int) (timeLeftInMillis / 1000) / 3600;
        int minutes = (int) ((timeLeftInMillis / 1000) % 3600) / 60;
        int seconds = (int) (timeLeftInMillis / 1000) % 60;

        if (hours > 0) {
            countdownDisplay.setText(String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds));
        } else {
            countdownDisplay.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));
        }

        int progress = (int) ((timeLeftInMillis * 10000) / totalTimeInMillis);
        progressBar.setProgress(progress);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}
