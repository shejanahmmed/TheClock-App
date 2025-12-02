package com.shejan.theclock;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import java.util.Calendar;
import java.util.Locale;

public class ClockWidgetDrawer {

    private Paint timePaint;
    private Paint datePaint;
    private final Calendar mCalendar = Calendar.getInstance();
    private boolean is24HourFormat = false;

    public ClockWidgetDrawer(Context context, int color) {
        init(context, color);
        SharedPreferences prefs = context.getSharedPreferences("com.shejan.theclock_preferences", Context.MODE_PRIVATE);
        is24HourFormat = prefs.getBoolean("use_24_hour_format", false);
    }

    // Default constructor for backward compatibility if needed, or update callers
    public ClockWidgetDrawer(Context context) {
        this(context, Color.WHITE);
    }

    private void init(Context context, int color) {
        Typeface gugiTypeface;
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                gugiTypeface = context.getResources().getFont(R.font.gugi);
            } else {
                // Fallback for older versions if needed, or use ResourcesCompat
                gugiTypeface = androidx.core.content.res.ResourcesCompat.getFont(context, R.font.gugi);
            }
        } catch (Exception e) {
            e.printStackTrace();
            gugiTypeface = Typeface.DEFAULT;
        }

        timePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        timePaint.setColor(color);
        timePaint.setStyle(Paint.Style.STROKE);
        timePaint.setStrokeWidth(5f); // Adjust for outline thickness
        timePaint.setTextAlign(Paint.Align.LEFT);
        timePaint.setTypeface(gugiTypeface);

        datePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        datePaint.setColor(color);
        datePaint.setStyle(Paint.Style.FILL);
        datePaint.setTextAlign(Paint.Align.LEFT);
        datePaint.setTypeface(gugiTypeface);
    }

    public void draw(Canvas canvas, int w, int h) {
        mCalendar.setTimeInMillis(System.currentTimeMillis());

        // Scale logic
        float minDim = Math.min(w, h);
        float scale = minDim / 400f; // Reference size 400px

        timePaint.setTextSize(180f * scale);
        timePaint.setStrokeWidth(4f * scale);

        datePaint.setTextSize(40f * scale);

        // Format Text
        int hour = mCalendar.get(Calendar.HOUR_OF_DAY);
        int minute = mCalendar.get(Calendar.MINUTE);

        if (!is24HourFormat) {
            if (hour > 12)
                hour -= 12;
            if (hour == 0)
                hour = 12;
        }

        String timeText = String.format(Locale.US, "%02d:%02d", hour, minute);

        String dayOfWeek = mCalendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.US);
        int dayOfMonth = mCalendar.get(Calendar.DAY_OF_MONTH);
        String dateText = String.format(Locale.US, "%s %d", dayOfWeek, dayOfMonth);

        // Measure
        float timeWidth = timePaint.measureText(timeText);
        float dateWidth = datePaint.measureText(dateText);

        // Calculate positions to center the block
        float maxWidth = Math.max(timeWidth, dateWidth);
        float startX = (w - maxWidth) / 2f;
        float centerY = h / 2f;

        // Draw Date
        // Position date above time
        float timeHeight = timePaint.descent() - timePaint.ascent();
        float dateHeight = datePaint.descent() - datePaint.ascent();
        float gap = 20f * scale;
        float totalHeight = timeHeight + dateHeight + gap;

        float currentY = (h - totalHeight) / 2f + dateHeight; // Top of date baseline roughly

        // Adjust for baseline
        canvas.drawText(dateText, startX, currentY, datePaint);

        // Draw Time
        // Move down by gap + time ascent (ascent is negative)
        // currentY is baseline of date.
        // Top of time is currentY + gap? No, baseline of time is currentY + gap +
        // timeHeight (roughly)
        // Let's use exact metrics
        float timeBaseline = currentY + gap - timePaint.ascent(); // -ascent is positive height

        canvas.drawText(timeText, startX, timeBaseline, timePaint);

        // Draw colon dots filled? The image shows "14:47" with outline numbers but the
        // colon might be filled or outline.
        // Standard font colon will be outlined if style is stroke.
        // If we want filled colon, we'd need to draw it separately.
        // For now, simple outline is safer and matches "same font style".
    }
}
