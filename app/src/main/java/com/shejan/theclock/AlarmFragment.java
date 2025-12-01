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

import java.util.List;

public class AlarmFragment extends Fragment
        implements AddAlarmBottomSheet.OnAlarmAddedListener, AlarmAdapter.OnAlarmClickListener,
        AlarmAdapter.OnAlarmStatusChangedListener {

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

        // Load alarms from repository
        alarmList = AlarmRepository.loadAlarms(requireContext());

        adapter = new AlarmAdapter(alarmList);
        adapter.setOnAlarmClickListener(this);
        adapter.setOnAlarmStatusChangedListener(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        // Swipe gestures
        androidx.recyclerview.widget.ItemTouchHelper.SimpleCallback simpleCallback = new androidx.recyclerview.widget.ItemTouchHelper.SimpleCallback(
                0, androidx.recyclerview.widget.ItemTouchHelper.LEFT
                        | androidx.recyclerview.widget.ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder,
                    @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getBindingAdapterPosition();
                if (position == RecyclerView.NO_POSITION)
                    return;
                Alarm alarm = alarmList.get(position);

                if (direction == androidx.recyclerview.widget.ItemTouchHelper.LEFT) {
                    // Delete
                    alarmList.remove(position);
                    adapter.notifyItemRemoved(position);
                    AlarmScheduler.cancel(requireContext(), alarm);
                    AlarmRepository.saveAlarms(requireContext(), alarmList);
                    updateNextAlarmText(getView());

                    com.google.android.material.snackbar.Snackbar.make(recyclerView, "Alarm deleted",
                            com.google.android.material.snackbar.Snackbar.LENGTH_LONG)
                            .setAction("Undo", v -> {
                                alarmList.add(position, alarm);
                                adapter.notifyItemInserted(position);
                                AlarmScheduler.schedule(requireContext(), alarm);
                                AlarmRepository.saveAlarms(requireContext(), alarmList);
                                updateNextAlarmText(getView());
                            }).show();
                } else if (direction == androidx.recyclerview.widget.ItemTouchHelper.RIGHT) {
                    // Edit
                    onAlarmClick(alarm);
                    adapter.notifyItemChanged(position); // Reset swipe state
                }
            }

            @Override
            public void onChildDraw(@NonNull android.graphics.Canvas c, @NonNull RecyclerView recyclerView,
                    @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState,
                    boolean isCurrentlyActive) {

                View itemView = viewHolder.itemView;
                android.graphics.Paint p = new android.graphics.Paint();

                // Convert 28dp to pixels for corner radius
                float cornerRadius = android.util.TypedValue.applyDimension(
                        android.util.TypedValue.COMPLEX_UNIT_DIP, 28, getResources().getDisplayMetrics());

                if (dX > 0) {
                    // Swipe Right (Edit)
                    p.setColor(android.graphics.Color.parseColor("#388E3C")); // Green

                    // Draw rounded rect
                    android.graphics.RectF background = new android.graphics.RectF(
                            (float) itemView.getLeft(),
                            (float) itemView.getTop(),
                            dX,
                            (float) itemView.getBottom());
                    c.drawRoundRect(background, cornerRadius, cornerRadius, p);

                    android.graphics.drawable.Drawable icon = androidx.core.content.ContextCompat
                            .getDrawable(requireContext(), R.drawable.ic_edit);
                    if (icon != null) {
                        icon.setTint(android.graphics.Color.WHITE);
                        int margin = (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
                        int top = itemView.getTop() + (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
                        int bottom = top + icon.getIntrinsicHeight();
                        int left = itemView.getLeft() + margin;
                        int right = left + icon.getIntrinsicWidth();

                        icon.setBounds(left, top, right, bottom);
                        icon.draw(c);

                        p.setColor(android.graphics.Color.WHITE);
                        p.setTextSize(40);
                        p.setAntiAlias(true);
                        c.drawText("Edit", right + 20, itemView.getTop() + itemView.getHeight() / 2f + 15, p);
                    }

                } else if (dX < 0) {
                    // Swipe Left (Delete)
                    p.setColor(android.graphics.Color.parseColor("#D32F2F")); // Red

                    // Draw rounded rect
                    android.graphics.RectF background = new android.graphics.RectF(
                            (float) itemView.getRight() + dX,
                            (float) itemView.getTop(),
                            (float) itemView.getRight(),
                            (float) itemView.getBottom());
                    c.drawRoundRect(background, cornerRadius, cornerRadius, p);

                    android.graphics.drawable.Drawable icon = androidx.core.content.ContextCompat
                            .getDrawable(requireContext(), R.drawable.ic_delete);
                    if (icon != null) {
                        icon.setTint(android.graphics.Color.WHITE);
                        int margin = (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
                        int top = itemView.getTop() + (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
                        int bottom = top + icon.getIntrinsicHeight();
                        int right = itemView.getRight() - margin;
                        int left = right - icon.getIntrinsicWidth();

                        icon.setBounds(left, top, right, bottom);
                        icon.draw(c);

                        p.setColor(android.graphics.Color.WHITE);
                        p.setTextSize(40);
                        p.setAntiAlias(true);
                        // Measure text to position correctly
                        float textWidth = p.measureText("Delete");
                        c.drawText("Delete", left - textWidth - 20, itemView.getTop() + itemView.getHeight() / 2f + 15,
                                p);
                    }
                }

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };

        new androidx.recyclerview.widget.ItemTouchHelper(simpleCallback).attachToRecyclerView(recyclerView);

        updateNextAlarmText(view); // Update header based on loaded alarms

        fab.setOnClickListener(v -> {
            AddAlarmBottomSheet bottomSheet = new AddAlarmBottomSheet();
            bottomSheet.setOnAlarmAddedListener(this);
            bottomSheet.show(getParentFragmentManager(), "AddAlarmBottomSheet");
        });

        return view;
    }

    @Override
    public void onAlarmClick(Alarm alarm) {
        AddAlarmBottomSheet bottomSheet = AddAlarmBottomSheet.newInstance(alarm);
        bottomSheet.setOnAlarmAddedListener(this);
        bottomSheet.show(getParentFragmentManager(), "EditAlarmBottomSheet");
    }

    @Override
    public void onAlarmStatusChanged(Alarm alarm) {
        AlarmRepository.saveAlarms(requireContext(), alarmList);
        updateNextAlarmText(getView());
    }

    @Override
    public void onAlarmAdded(Alarm alarm) {
        int index = -1;
        for (int i = 0; i < alarmList.size(); i++) {
            if (alarmList.get(i).getId().equals(alarm.getId())) {
                index = i;
                break;
            }
        }

        if (index != -1) {
            // Update existing alarm
            alarmList.set(index, alarm);
            adapter.notifyItemChanged(index);
        } else {
            // Add new alarm
            alarmList.add(alarm);
            adapter.notifyItemInserted(alarmList.size() - 1);
        }

        AlarmRepository.saveAlarms(requireContext(), alarmList);
        AlarmScheduler.schedule(requireContext(), alarm);
        updateNextAlarmText(getView());
    }

    private void updateNextAlarmText(View view) {
        if (view == null)
            return;
        TextView subtitle = view.findViewById(R.id.header_subtitle);
        TextView emptyView = view.findViewById(R.id.empty_view);

        if (alarmList.isEmpty()) {
            subtitle.setVisibility(View.GONE);
            if (emptyView != null)
                emptyView.setVisibility(View.VISIBLE);
            return;
        } else {
            if (emptyView != null)
                emptyView.setVisibility(View.GONE);
        }

        long minDiff = Long.MAX_VALUE;
        boolean hasEnabledAlarm = false;
        long nowMillis = System.currentTimeMillis();

        for (Alarm alarm : alarmList) {
            if (alarm.isEnabled()) {
                hasEnabledAlarm = true;
                long diff = calculateTimeToNextAlarm(alarm, nowMillis);
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

    private long calculateTimeToNextAlarm(Alarm alarm, long nowMillis) {
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

        return alarmTime.getTimeInMillis() - nowMillis;
    }
}
