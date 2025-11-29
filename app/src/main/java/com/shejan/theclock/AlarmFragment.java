package com.shejan.theclock;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class AlarmFragment extends Fragment implements AddAlarmBottomSheet.OnAlarmAddedListener {

    private RecyclerView recyclerView;
    private AlarmAdapter adapter;
    private List<Alarm> alarmList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_alarm, container, false);

        recyclerView = view.findViewById(R.id.alarms_recycler_view);
        FloatingActionButton fab = view.findViewById(R.id.fab_add_alarm);

        alarmList = new ArrayList<>();
        // Add some dummy data for testing if needed, or keep empty
        // alarmList.add(new Alarm("1", 10, 0, true));

        adapter = new AlarmAdapter(alarmList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        fab.setOnClickListener(v -> {
            AddAlarmBottomSheet bottomSheet = new AddAlarmBottomSheet();
            bottomSheet.setOnAlarmAddedListener(this);
            bottomSheet.show(getParentFragmentManager(), "AddAlarmBottomSheet");
        });

        return view;
    }

    @Override
    public void onAlarmAdded(Alarm alarm) {
        alarmList.add(alarm);
        adapter.notifyItemInserted(alarmList.size() - 1);
        updateNextAlarmText();
    }

    private void updateNextAlarmText() {
        TextView subtitle = getView().findViewById(R.id.header_subtitle);
        if (alarmList.isEmpty()) {
            subtitle.setVisibility(View.GONE);
            return;
        }

        long minDiff = Long.MAX_VALUE;
        boolean hasEnabledAlarm = false;
        java.util.Calendar now = java.util.Calendar.getInstance();
        long nowMillis = now.getTimeInMillis();

        for (Alarm alarm : alarmList) {
            if (alarm.isEnabled()) {
                hasEnabledAlarm = true;
                java.util.Calendar alarmTime = java.util.Calendar.getInstance();
                alarmTime.set(java.util.Calendar.HOUR_OF_DAY, alarm.getHour());
                alarmTime.set(java.util.Calendar.MINUTE, alarm.getMinute());
                alarmTime.set(java.util.Calendar.SECOND, 0);
                alarmTime.set(java.util.Calendar.MILLISECOND, 0);

                if (alarm.isRecurring() && !alarm.getDays().isEmpty()) {
                    while (alarmTime.getTimeInMillis() <= nowMillis
                            || !alarm.getDays().contains(alarmTime.get(java.util.Calendar.DAY_OF_WEEK))) {
                        alarmTime.add(java.util.Calendar.DAY_OF_YEAR, 1);
                        // Safety break
                        if (alarmTime.getTimeInMillis() - nowMillis > 8 * 24 * 60 * 60 * 1000L)
                            break;
                    }
                } else {
                    if (alarmTime.getTimeInMillis() <= nowMillis) {
                        alarmTime.add(java.util.Calendar.DAY_OF_YEAR, 1);
                    }
                }

                long diff = alarmTime.getTimeInMillis() - nowMillis;
                if (diff < minDiff) {
                    minDiff = diff;
                }
            }
        }

        if (hasEnabledAlarm && minDiff != Long.MAX_VALUE) {
            long hours = java.util.concurrent.TimeUnit.MILLISECONDS.toHours(minDiff);
            long minutes = java.util.concurrent.TimeUnit.MILLISECONDS.toMinutes(minDiff) % 60;

            String text;
            if (hours > 0) {
                text = String.format(java.util.Locale.getDefault(), "Ring in %d hours %d minutes", hours, minutes);
            } else {
                text = String.format(java.util.Locale.getDefault(), "Ring in %d minutes", minutes);
            }

            subtitle.setText(text);
            subtitle.setVisibility(View.VISIBLE);
        } else {
            subtitle.setVisibility(View.GONE);
        }
    }
}
