package com.shejan.theclock;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;

public class WorldClockFragment extends Fragment {

    public WorldClockFragment() {
        // Required empty public constructor
    }

    private ClockView clockView;
    private android.widget.TextView dateTextView;

    @Override
    public void onResume() {
        super.onResume();
        android.content.SharedPreferences prefs = requireContext().getSharedPreferences(
                requireContext().getPackageName() + "_preferences", android.content.Context.MODE_PRIVATE);
        boolean is24Hour = prefs.getBoolean("use_24_hour_format", false);
        if (clockView != null) {
            clockView.set24HourFormat(is24Hour);
        }

        if (dateTextView != null) {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("EEE, MMM d",
                    java.util.Locale.getDefault());
            dateTextView.setText(sdf.format(new java.util.Date()));
        }
    }

    public void updateClockFormat(boolean is24Hour) {
        if (clockView != null) {
            clockView.set24HourFormat(is24Hour);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_world_clock, container, false);
        clockView = view.findViewById(R.id.clock_view);
        dateTextView = view.findViewById(R.id.date_text_view);
        return view;
    }
}
