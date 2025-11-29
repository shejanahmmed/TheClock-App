package com.shejan.theclock;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class AddAlarmBottomSheet extends BottomSheetDialogFragment {

    private OnAlarmAddedListener listener;

    public interface OnAlarmAddedListener {
        void onAlarmAdded(Alarm alarm);
    }

    public void setOnAlarmAddedListener(OnAlarmAddedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public android.app.Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        com.google.android.material.bottomsheet.BottomSheetDialog dialog = (com.google.android.material.bottomsheet.BottomSheetDialog) super.onCreateDialog(
                savedInstanceState);
        dialog.setOnShowListener(dialogInterface -> {
            com.google.android.material.bottomsheet.BottomSheetDialog d = (com.google.android.material.bottomsheet.BottomSheetDialog) dialogInterface;
            android.widget.FrameLayout bottomSheet = d
                    .findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                bottomSheet.setBackgroundResource(android.R.color.transparent);
                bottomSheet.post(() -> {
                    com.google.android.material.bottomsheet.BottomSheetBehavior<android.view.View> behavior = com.google.android.material.bottomsheet.BottomSheetBehavior
                            .from(bottomSheet);
                    behavior.setState(com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED);
                    behavior.setSkipCollapsed(true);
                    behavior.setPeekHeight(android.content.res.Resources.getSystem().getDisplayMetrics().heightPixels);

                    if (d.getWindow() != null) {
                        d.getWindow().setNavigationBarColor(
                                getContext().getResources().getColor(R.color.bottom_sheet_bg, null));
                    }
                });

                ViewGroup.LayoutParams layoutParams = bottomSheet.getLayoutParams();
                layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
                bottomSheet.setLayoutParams(layoutParams);
            }
        });
        return dialog;
    }

    private String selectedRingtoneUri;
    private androidx.activity.result.ActivityResultLauncher<android.content.Intent> ringtonePickerLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ringtonePickerLauncher = registerForActivityResult(
                new androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null) {
                        android.net.Uri uri = result.getData()
                                .getParcelableExtra(android.media.RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                        if (uri != null) {
                            selectedRingtoneUri = uri.toString();
                            android.media.Ringtone ringtone = android.media.RingtoneManager.getRingtone(getContext(),
                                    uri);
                            String title = ringtone.getTitle(getContext());
                            // Update UI if view is created
                            if (getView() != null) {
                                TextView tvRingtoneName = getView().findViewById(R.id.tv_ringtone_name);
                                if (tvRingtoneName != null) {
                                    tvRingtoneName.setText(title);
                                }
                            }
                        } else {
                            // Silent
                            selectedRingtoneUri = "";
                            if (getView() != null) {
                                TextView tvRingtoneName = getView().findViewById(R.id.tv_ringtone_name);
                                if (tvRingtoneName != null) {
                                    tvRingtoneName.setText("Silent");
                                }
                            }
                        }
                    }
                });
    }

    private boolean snoozeEnabled = true;
    private int snoozeInterval = 5;
    private int snoozeTimes = 3;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_alarm, container, false);

        TextView btnCancel = view.findViewById(R.id.btn_cancel);
        TextView btnDone = view.findViewById(R.id.btn_done);
        TimePicker timePicker = view.findViewById(R.id.timePicker);
        timePicker.setIs24HourView(false);

        TextView btnRingOnce = view.findViewById(R.id.btn_ring_once);
        TextView btnCustom = view.findViewById(R.id.btn_custom);
        android.widget.LinearLayout layoutRepeat = view.findViewById(R.id.layout_repeat);

        android.widget.LinearLayout layoutRingtone = view.findViewById(R.id.layout_ringtone);
        TextView tvRingtoneName = view.findViewById(R.id.tv_ringtone_name);

        android.widget.LinearLayout layoutSnooze = view.findViewById(R.id.layout_snooze);
        TextView tvSnoozeSummary = view.findViewById(R.id.tv_snooze_summary);

        // Default ringtone
        if (selectedRingtoneUri == null) {
            android.net.Uri defaultUri = android.media.RingtoneManager
                    .getDefaultUri(android.media.RingtoneManager.TYPE_ALARM);
            if (defaultUri != null) {
                selectedRingtoneUri = defaultUri.toString();
                android.media.Ringtone ringtone = android.media.RingtoneManager.getRingtone(getContext(), defaultUri);
                if (ringtone != null) {
                    tvRingtoneName.setText(ringtone.getTitle(getContext()));
                }
            }
        }

        layoutRingtone.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(
                    android.media.RingtoneManager.ACTION_RINGTONE_PICKER);
            intent.putExtra(android.media.RingtoneManager.EXTRA_RINGTONE_TYPE,
                    android.media.RingtoneManager.TYPE_ALARM);
            intent.putExtra(android.media.RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Alarm Ringtone");
            intent.putExtra(android.media.RingtoneManager.EXTRA_RINGTONE_EXISTING_URI,
                    selectedRingtoneUri != null && !selectedRingtoneUri.isEmpty()
                            ? android.net.Uri.parse(selectedRingtoneUri)
                            : (android.net.Uri) null);
            ringtonePickerLauncher.launch(intent);
        });

        // Snooze Click Listener
        layoutSnooze.setOnClickListener(v -> {
            SnoozeBottomSheet snoozeSheet = SnoozeBottomSheet.newInstance(snoozeEnabled, snoozeInterval, snoozeTimes);
            snoozeSheet.setOnSnoozeOptionsSelectedListener((enabled, interval, times) -> {
                snoozeEnabled = enabled;
                snoozeInterval = interval;
                snoozeTimes = times;

                if (snoozeEnabled) {
                    tvSnoozeSummary.setText(interval + " minutes, " + times + " times");
                    tvSnoozeSummary.setTextColor(android.graphics.Color.parseColor("#5C7CFA"));
                } else {
                    tvSnoozeSummary.setText("Off");
                    tvSnoozeSummary.setTextColor(getContext().getResources().getColor(android.R.color.white, null));
                }
            });
            snoozeSheet.show(getParentFragmentManager(), "SnoozeBottomSheet");
        });

        // Day TextViews
        TextView tvSun = view.findViewById(R.id.tv_sun);
        TextView tvMon = view.findViewById(R.id.tv_mon);
        TextView tvTue = view.findViewById(R.id.tv_tue);
        TextView tvWed = view.findViewById(R.id.tv_wed);
        TextView tvThu = view.findViewById(R.id.tv_thu);
        TextView tvFri = view.findViewById(R.id.tv_fri);
        TextView tvSat = view.findViewById(R.id.tv_sat);

        // Repeat summary text
        TextView tvRepeatSummary = (TextView) layoutRepeat.getChildAt(1);

        java.util.Set<Integer> selectedDays = new java.util.HashSet<>();

        TextView tvRingIn = view.findViewById(R.id.tv_ring_in);
        android.widget.EditText etAlarmName = view.findViewById(R.id.et_alarm_name);

        // Helper to update "Ring in..." text
        Runnable updateNextAlarmTime = () -> {
            int hour = timePicker.getHour();
            int minute = timePicker.getMinute();
            java.util.Calendar now = java.util.Calendar.getInstance();
            java.util.Calendar alarmTime = java.util.Calendar.getInstance();
            alarmTime.set(java.util.Calendar.HOUR_OF_DAY, hour);
            alarmTime.set(java.util.Calendar.MINUTE, minute);
            alarmTime.set(java.util.Calendar.SECOND, 0);
            alarmTime.set(java.util.Calendar.MILLISECOND, 0);

            if (layoutRepeat.getVisibility() == View.GONE || selectedDays.isEmpty()) {
                // Ring once
                if (alarmTime.before(now)) {
                    alarmTime.add(java.util.Calendar.DAY_OF_YEAR, 1);
                }
            } else {
                // Recurring
                // Find next occurrence
                while (alarmTime.before(now) || !selectedDays.contains(alarmTime.get(java.util.Calendar.DAY_OF_WEEK))) {
                    alarmTime.add(java.util.Calendar.DAY_OF_YEAR, 1);
                    // Safety break to prevent infinite loop if logic fails (though shouldn't with
                    // valid days)
                    if (alarmTime.getTimeInMillis() - now.getTimeInMillis() > 8 * 24 * 60 * 60 * 1000L)
                        break;
                }
            }

            long diff = alarmTime.getTimeInMillis() - now.getTimeInMillis();
            long days = java.util.concurrent.TimeUnit.MILLISECONDS.toDays(diff);
            long hours = java.util.concurrent.TimeUnit.MILLISECONDS.toHours(diff) % 24;
            long minutes = java.util.concurrent.TimeUnit.MILLISECONDS.toMinutes(diff) % 60;

            StringBuilder sb = new StringBuilder("Ring in ");
            if (days > 0) {
                sb.append(days).append(" day").append(days > 1 ? "s" : "");
            } else if (hours > 0) {
                sb.append(hours).append(" hour").append(hours > 1 ? "s" : "");
                if (minutes > 0)
                    sb.append(" ").append(minutes).append(" minute").append(minutes > 1 ? "s" : "");
            } else {
                sb.append(minutes).append(" minute").append(minutes > 1 ? "s" : "");
                if (minutes == 0)
                    sb.append("less than a minute");
            }
            tvRingIn.setText(sb.toString());
        };

        timePicker.setOnTimeChangedListener((view1, hourOfDay, minute) -> updateNextAlarmTime.run());

        // Helper to update UI
        Runnable updateUI = () -> {
            updateDayState(tvSun, java.util.Calendar.SUNDAY, selectedDays);
            updateDayState(tvMon, java.util.Calendar.MONDAY, selectedDays);
            updateDayState(tvTue, java.util.Calendar.TUESDAY, selectedDays);
            updateDayState(tvWed, java.util.Calendar.WEDNESDAY, selectedDays);
            updateDayState(tvThu, java.util.Calendar.THURSDAY, selectedDays);
            updateDayState(tvFri, java.util.Calendar.FRIDAY, selectedDays);
            updateDayState(tvSat, java.util.Calendar.SATURDAY, selectedDays);

            if (selectedDays.isEmpty()) {
                tvRepeatSummary.setText("Ring once");
            } else if (selectedDays.size() == 7) {
                tvRepeatSummary.setText("Every day");
            } else {
                // Build summary string
                java.util.List<Integer> sortedDays = new java.util.ArrayList<>(selectedDays);
                // Sort based on Calendar constants (Sun=1, Mon=2, ...)
                java.util.Collections.sort(sortedDays);

                StringBuilder sb = new StringBuilder();
                for (int day : sortedDays) {
                    if (sb.length() > 0)
                        sb.append(", ");
                    sb.append(getDayName(day));
                }
                tvRepeatSummary.setText(sb.toString());
            }
            updateNextAlarmTime.run();
        };

        // Click listeners for days
        View.OnClickListener dayClickListener = v -> {
            int day = (int) v.getTag();
            if (selectedDays.contains(day)) {
                selectedDays.remove(day);
            } else {
                selectedDays.add(day);
            }
            updateUI.run();
        };

        setupDayButton(tvSun, java.util.Calendar.SUNDAY, dayClickListener);
        setupDayButton(tvMon, java.util.Calendar.MONDAY, dayClickListener);
        setupDayButton(tvTue, java.util.Calendar.TUESDAY, dayClickListener);
        setupDayButton(tvWed, java.util.Calendar.WEDNESDAY, dayClickListener);
        setupDayButton(tvThu, java.util.Calendar.THURSDAY, dayClickListener);
        setupDayButton(tvFri, java.util.Calendar.FRIDAY, dayClickListener);
        setupDayButton(tvSat, java.util.Calendar.SATURDAY, dayClickListener);

        // Initial state
        updateUI.run();

        btnRingOnce.setOnClickListener(v -> {
            btnRingOnce.setBackgroundResource(R.drawable.btn_pill_selected);
            btnCustom.setBackgroundResource(R.drawable.btn_pill_unselected);
            layoutRepeat.setVisibility(View.GONE);
            updateNextAlarmTime.run();
        });

        btnCustom.setOnClickListener(v -> {
            btnRingOnce.setBackgroundResource(R.drawable.btn_pill_unselected);
            btnCustom.setBackgroundResource(R.drawable.btn_pill_selected);
            layoutRepeat.setVisibility(View.VISIBLE);
            updateNextAlarmTime.run();
        });

        btnCancel.setOnClickListener(v -> dismiss());

        btnDone.setOnClickListener(v -> {
            if (listener != null) {
                int hour = timePicker.getHour();
                int minute = timePicker.getMinute();
                Alarm alarm = new Alarm(String.valueOf(System.currentTimeMillis()), hour, minute, true);

                String label = etAlarmName.getText().toString();
                if (!label.isEmpty()) {
                    alarm.setLabel(label);
                }

                if (layoutRepeat.getVisibility() == View.VISIBLE && !selectedDays.isEmpty()) {
                    alarm.setRecurring(true);
                    alarm.setDays(new java.util.ArrayList<>(selectedDays));
                } else {
                    alarm.setRecurring(false);
                    alarm.setDays(new java.util.ArrayList<>());
                }

                if (selectedRingtoneUri != null) {
                    alarm.setRingtoneUri(selectedRingtoneUri);
                }

                alarm.setSnoozeEnabled(snoozeEnabled);
                alarm.setSnoozeInterval(snoozeInterval);
                alarm.setSnoozeTimes(snoozeTimes);

                listener.onAlarmAdded(alarm);
            }
            dismiss();
        });

        return view;
    }

    private void setupDayButton(TextView view, int day, View.OnClickListener listener) {
        view.setTag(day);
        view.setOnClickListener(listener);
    }

    private void updateDayState(TextView view, int day, java.util.Set<Integer> selectedDays) {
        if (selectedDays.contains(day)) {
            view.setBackgroundResource(R.drawable.bg_day_selected);
            view.setTextColor(view.getContext().getResources().getColor(android.R.color.white, null)); // Ensure white
                                                                                                       // text
        } else {
            view.setBackgroundResource(0); // Remove background
            view.setTextColor(view.getContext().getResources().getColor(android.R.color.white, null)); // Keep white
                                                                                                       // text
        }
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
}
