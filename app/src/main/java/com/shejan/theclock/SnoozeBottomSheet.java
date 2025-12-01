package com.shejan.theclock;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class SnoozeBottomSheet extends BottomSheetDialogFragment {

    private boolean isSnoozeEnabled;
    private int interval;
    private int times;
    private OnSnoozeOptionsSelectedListener listener;

    public interface OnSnoozeOptionsSelectedListener {
        void onSnoozeOptionsSelected(boolean enabled, int interval, int times);
    }

    public static SnoozeBottomSheet newInstance(boolean enabled, int interval, int times) {
        SnoozeBottomSheet fragment = new SnoozeBottomSheet();
        Bundle args = new Bundle();
        args.putBoolean("enabled", enabled);
        args.putInt("interval", interval);
        args.putInt("times", times);
        fragment.setArguments(args);
        return fragment;
    }

    public void setOnSnoozeOptionsSelectedListener(OnSnoozeOptionsSelectedListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            isSnoozeEnabled = getArguments().getBoolean("enabled");
            interval = getArguments().getInt("interval");
            times = getArguments().getInt("times");
        }
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

                if (d.getWindow() != null) {
                    d.getWindow()
                            .setNavigationBarColor(
                                    requireContext().getResources().getColor(R.color.bottom_sheet_bg, null));
                }
            }
        });
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_snooze, container, false);

        ImageView btnBack = view.findViewById(R.id.btn_back);
        SwitchMaterial switchSnooze = view.findViewById(R.id.switch_snooze);
        LinearLayout cardOptions = view.findViewById(R.id.card_options);
        LinearLayout layoutInterval = view.findViewById(R.id.layout_interval);
        LinearLayout layoutTimes = view.findViewById(R.id.layout_times);
        TextView tvInterval = view.findViewById(R.id.tv_interval);
        TextView tvTimes = view.findViewById(R.id.tv_times);

        // Initialize UI
        switchSnooze.setChecked(isSnoozeEnabled);
        updateOptionsVisibility(cardOptions, isSnoozeEnabled);
        tvInterval.setText(getString(R.string.minutes_format, interval));
        tvTimes.setText(String.valueOf(times));

        btnBack.setOnClickListener(v -> dismiss());

        switchSnooze.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isSnoozeEnabled = isChecked;
            updateOptionsVisibility(cardOptions, isChecked);
            notifyListener();
        });

        layoutInterval.setOnClickListener(v -> showIntervalPopup(v, tvInterval));
        layoutTimes.setOnClickListener(v -> showTimesPopup(v, tvTimes));

        return view;
    }

    private void updateOptionsVisibility(View view, boolean isVisible) {
        view.setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }

    private void showIntervalPopup(View anchor, TextView tvInterval) {
        android.view.ContextThemeWrapper wrapper = new android.view.ContextThemeWrapper(getContext(),
                R.style.Theme_TheClock_PopupMenuWrapper);
        PopupMenu popup = new PopupMenu(wrapper, anchor);
        int[] intervals = { 5, 10, 15, 20, 25, 30 };
        for (int i : intervals) {
            popup.getMenu().add(getString(R.string.minutes_format, i));
        }
        popup.setOnMenuItemClickListener(item -> {
            if (item.getTitle() == null)
                return false;
            String title = item.getTitle().toString();
            interval = Integer.parseInt(title.split(" ")[0]);
            tvInterval.setText(title);
            notifyListener();
            return true;
        });
        popup.show();
    }

    private void showTimesPopup(View anchor, TextView tvTimes) {
        android.view.ContextThemeWrapper wrapper = new android.view.ContextThemeWrapper(getContext(),
                R.style.Theme_TheClock_PopupMenuWrapper);
        PopupMenu popup = new PopupMenu(wrapper, anchor);
        int[] timesOptions = { 3, 5, 10 };
        for (int t : timesOptions) {
            popup.getMenu().add(String.valueOf(t));
        }
        popup.setOnMenuItemClickListener(item -> {
            if (item.getTitle() == null)
                return false;
            times = Integer.parseInt(item.getTitle().toString());
            tvTimes.setText(String.valueOf(times));
            notifyListener();
            return true;
        });
        popup.show();
    }

    private void notifyListener() {
        if (listener != null) {
            listener.onSnoozeOptionsSelected(isSnoozeEnabled, interval, times);
        }
    }
}
