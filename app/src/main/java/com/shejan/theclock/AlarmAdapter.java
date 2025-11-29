package com.shejan.theclock;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

public class AlarmAdapter extends RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder> {

    private List<Alarm> alarmList;

    public AlarmAdapter(List<Alarm> alarmList) {
        this.alarmList = alarmList;
    }

    @NonNull
    @Override
    public AlarmViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_alarm, parent, false);
        return new AlarmViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlarmViewHolder holder, int position) {
        Alarm alarm = alarmList.get(position);

        // Format time
        String amPm = alarm.getHour() >= 12 ? "PM" : "AM";
        int hour = alarm.getHour() > 12 ? alarm.getHour() - 12 : alarm.getHour();
        if (hour == 0)
            hour = 12;

        holder.timeTextView.setText(String.format(Locale.getDefault(), "%02d:%02d", hour, alarm.getMinute()));
        holder.amPmTextView.setText(amPm);
        // Format label
        StringBuilder sb = new StringBuilder();
        if (!alarm.isRecurring() || alarm.getDays().isEmpty()) {
            sb.append("Ring once");
        } else if (alarm.getDays().size() == 7) {
            sb.append("Every day");
        } else {
            // Sort days
            List<Integer> days = new java.util.ArrayList<>(alarm.getDays());
            java.util.Collections.sort(days);
            for (int i = 0; i < days.size(); i++) {
                if (i > 0)
                    sb.append(", ");
                sb.append(getDayName(days.get(i)));
            }
        }

        if (alarm.getLabel() != null && !alarm.getLabel().isEmpty()) {
            sb.append(" | ").append(alarm.getLabel());
        }

        holder.labelTextView.setText(sb.toString());
        holder.alarmSwitch.setChecked(alarm.isEnabled());

        holder.alarmSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            alarm.setEnabled(isChecked);
        });
    }

    private String getDayName(int day) {
        switch (day) {
            case java.util.Calendar.SUNDAY:
                return "Sun";
            case java.util.Calendar.MONDAY:
                return "Mon";
            case java.util.Calendar.TUESDAY:
                return "Tue";
            case java.util.Calendar.WEDNESDAY:
                return "Wed";
            case java.util.Calendar.THURSDAY:
                return "Thu";
            case java.util.Calendar.FRIDAY:
                return "Fri";
            case java.util.Calendar.SATURDAY:
                return "Sat";
            default:
                return "";
        }
    }

    @Override
    public int getItemCount() {
        return alarmList.size();
    }

    public static class AlarmViewHolder extends RecyclerView.ViewHolder {
        TextView timeTextView;
        TextView amPmTextView;
        TextView labelTextView;
        Switch alarmSwitch;

        public AlarmViewHolder(@NonNull View itemView) {
            super(itemView);
            timeTextView = itemView.findViewById(R.id.alarm_time);
            amPmTextView = itemView.findViewById(R.id.alarm_ampm);
            labelTextView = itemView.findViewById(R.id.alarm_label);
            alarmSwitch = itemView.findViewById(R.id.alarm_switch);
        }
    }
}
