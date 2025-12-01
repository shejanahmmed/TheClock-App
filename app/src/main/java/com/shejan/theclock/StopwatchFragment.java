package com.shejan.theclock;

import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class StopwatchFragment extends Fragment {

    private TextView stopwatchTime;
    private RecyclerView lapsRecyclerView;
    private FloatingActionButton fabPlayPause;
    private FloatingActionButton fabReset;
    private FloatingActionButton fabLap;

    private long startTime = 0L;
    private long timeInMilliseconds = 0L;
    private long timeSwapBuff = 0L;
    private long updateTime = 0L;
    private boolean isRunning = false;

    private final Handler handler = new Handler();
    private final List<String> lapList = new ArrayList<>();
    private LapAdapter lapAdapter;

    public StopwatchFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stopwatch, container, false);

        stopwatchTime = view.findViewById(R.id.stopwatch_time);
        lapsRecyclerView = view.findViewById(R.id.laps_recycler_view);
        fabPlayPause = view.findViewById(R.id.fab_play_pause);
        fabReset = view.findViewById(R.id.fab_reset);
        fabLap = view.findViewById(R.id.fab_lap);

        lapAdapter = new LapAdapter(lapList);
        lapsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        lapsRecyclerView.setAdapter(lapAdapter);

        fabPlayPause.setOnClickListener(v -> {
            if (isRunning) {
                pauseTimer();
            } else {
                startTimer();
            }
        });

        fabReset.setOnClickListener(v -> resetTimer());

        fabLap.setOnClickListener(v -> addLap());

        return view;
    }

    private final Runnable updateTimerThread = new Runnable() {
        public void run() {
            timeInMilliseconds = SystemClock.uptimeMillis() - startTime;
            updateTime = timeSwapBuff + timeInMilliseconds;

            int secs = (int) (updateTime / 1000);
            int mins = secs / 60;
            secs = secs % 60;
            int milliseconds = (int) (updateTime % 1000);

            stopwatchTime.setText(String.format(Locale.getDefault(), "%02d:%02d.%02d", mins, secs, milliseconds / 10));
            handler.postDelayed(this, 10);
        }
    };

    private void startTimer() {
        startTime = SystemClock.uptimeMillis();
        handler.postDelayed(updateTimerThread, 0);
        isRunning = true;
        fabPlayPause.setImageResource(R.drawable.ic_pause);
        fabReset.setVisibility(View.INVISIBLE);
        fabLap.setVisibility(View.VISIBLE);
    }

    private void pauseTimer() {
        timeSwapBuff += timeInMilliseconds;
        handler.removeCallbacks(updateTimerThread);
        isRunning = false;
        fabPlayPause.setImageResource(R.drawable.ic_play);
        fabReset.setVisibility(View.VISIBLE);
        fabLap.setVisibility(View.INVISIBLE);
    }

    private void resetTimer() {
        timeSwapBuff = 0L;
        timeInMilliseconds = 0L;
        updateTime = 0L;
        startTime = 0L;
        stopwatchTime.setText(R.string.stopwatch_initial_time);
        int size = lapList.size();
        lapList.clear();
        lapAdapter.notifyItemRangeRemoved(0, size);
        fabReset.setVisibility(View.INVISIBLE);
        fabLap.setVisibility(View.INVISIBLE);
    }

    private void addLap() {
        int secs = (int) (updateTime / 1000);
        int mins = secs / 60;
        secs = secs % 60;
        int milliseconds = (int) (updateTime % 1000);
        String lapTime = String.format(Locale.getDefault(), "%02d:%02d.%02d", mins, secs, milliseconds / 10);

        lapList.add(0, lapTime); // Add to top
        lapAdapter.notifyItemInserted(0);
        lapsRecyclerView.scrollToPosition(0);
    }

    // Inner Adapter Class
    private static class LapAdapter extends RecyclerView.Adapter<LapAdapter.LapViewHolder> {

        private final List<String> laps;

        public LapAdapter(List<String> laps) {
            this.laps = laps;
        }

        @NonNull
        @Override
        public LapViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_lap, parent, false);
            return new LapViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull LapViewHolder holder, int position) {
            String time = laps.get(position);
            holder.lapNumber.setText(
                    holder.itemView.getContext().getString(R.string.lap_number_format, laps.size() - position));
            holder.lapTime.setText(time);
        }

        @Override
        public int getItemCount() {
            return laps.size();
        }

        static class LapViewHolder extends RecyclerView.ViewHolder {
            TextView lapNumber;
            TextView lapTime;

            public LapViewHolder(@NonNull View itemView) {
                super(itemView);
                lapNumber = itemView.findViewById(R.id.lap_number);
                lapTime = itemView.findViewById(R.id.lap_time);
            }
        }
    }
}
