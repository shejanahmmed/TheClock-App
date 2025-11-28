package com.shejan.theclock;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Calendar;
import java.util.Locale;

public class ClockView extends View {

    private Paint spikePaint;
    private Paint textPaint;
    private Paint hourPaint;
    private Paint minutePaint;
    private Paint minuteIndicatorPaint;
    private Paint bgPaint;

    private float mWidth;
    private float mHeight;
    private float mClockSize;
    private final Calendar mCalendar = Calendar.getInstance();
    private final RectF boxRect = new RectF();

    public ClockView(Context context) {
        super(context);
        init();
    }

    public ClockView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ClockView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        spikePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        spikePaint.setStyle(Paint.Style.FILL);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD));

        hourPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        hourPaint.setColor(Color.WHITE);
        hourPaint.setTextAlign(Paint.Align.CENTER);
        hourPaint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD));

        minutePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        minutePaint.setColor(Color.WHITE);
        minutePaint.setTextAlign(Paint.Align.LEFT); // Align left for manual positioning
        minutePaint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD));

        minuteIndicatorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        minuteIndicatorPaint.setColor(Color.WHITE);
        minuteIndicatorPaint.setStyle(Paint.Style.STROKE);

        bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setColor(Color.BLACK);
        bgPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;
        // Match Swing: clockSize = 360, window = 600. Ratio ~0.6
        // But we want it to fill screen nicely.
        mClockSize = Math.min(mWidth, mHeight) * 0.65f;
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.BLACK);

        mCalendar.setTimeInMillis(System.currentTimeMillis());
        int hour = mCalendar.get(Calendar.HOUR_OF_DAY);
        int minute = mCalendar.get(Calendar.MINUTE);
        int second = mCalendar.get(Calendar.SECOND);
        int millisecond = mCalendar.get(Calendar.MILLISECOND);

        float centerX = mWidth / 2f;
        float centerY = mHeight / 2f;
        float r = mClockSize / 2f;

        // Calculate smooth values
        float smoothSecond = second + (millisecond / 1000f);
        float smoothMinute = minute + (smoothSecond / 60f);

        // Define common dimensions
        float minutesRadius = r - 100f; // Increased radius further
        float capsuleHeight = 100f;
        float boxHalfHeight = capsuleHeight / 2f;

        // Capsule dimensions
        // Anchor to seconds ring (Right) and shorten from center (Left)
        float capsuleRight = centerX + r;
        float capsuleLeft = centerX + 90f; // Widened even more from left (was 120f)

        // --- 1. Draw Minutes Ring (Inner) ---
        // Drawn first so Capsule Background can cover it
        float minuteGroupRotateDeg = 6.0f * smoothMinute;
        drawSpikesGroup(canvas, centerX, centerY, minutesRadius, minuteGroupRotateDeg, 30f);

        // --- 2. Draw Capsule (Background & Stroke) ---
        // Draw Black Background
        boxRect.set(capsuleLeft, centerY - boxHalfHeight, capsuleRight, centerY + boxHalfHeight);
        canvas.drawRoundRect(boxRect, boxHalfHeight, boxHalfHeight, bgPaint);

        // Draw Stroke
        minuteIndicatorPaint.setStrokeWidth(4f);
        // Left Arc
        canvas.drawArc(capsuleLeft, centerY - boxHalfHeight, capsuleLeft + capsuleHeight, centerY + boxHalfHeight, 90,
                180, false, minuteIndicatorPaint);
        // Top Line
        canvas.drawLine(capsuleLeft + boxHalfHeight, centerY - boxHalfHeight, capsuleRight, centerY - boxHalfHeight,
                minuteIndicatorPaint);
        // Bottom Line
        canvas.drawLine(capsuleLeft + boxHalfHeight, centerY + boxHalfHeight, capsuleRight, centerY + boxHalfHeight,
                minuteIndicatorPaint);

        // --- 3. Draw Seconds Ring (Outer) ---
        // Drawn ON TOP of Capsule
        float secondsGroupRotateDeg = 6.0f * smoothSecond;
        drawSpikesGroup(canvas, centerX, centerY, r, secondsGroupRotateDeg, 35f);

        // --- 4. Draw Hour & Minute Text ---
        // Hour centered
        hourPaint.setTextSize(160f);
        Paint.FontMetrics fm = hourPaint.getFontMetrics();
        float hourOffset = (fm.descent - fm.ascent) / 2f - fm.descent;
        canvas.drawText(String.valueOf(hour), centerX, centerY + hourOffset, hourPaint);

        // Minute Text (aligned with minutes ring)
        minutePaint.setTextSize(70f);
        fm = minutePaint.getFontMetrics();
        float minuteOffset = (fm.descent - fm.ascent) / 2f - fm.descent;

        // Position at minutes radius
        float textX = centerX + (minutesRadius - 50f);

        // Adjust based on user's request (move back right)
        textX -= 65f;

        canvas.drawText(String.format(Locale.US, "%02d", minute), textX, centerY + minuteOffset, minutePaint);

        postInvalidateOnAnimation();
    }

    private void drawSpikesGroup(Canvas canvas, float cx, float cy, float radius, float rotateDeg, float textSize) {
        canvas.save();
        canvas.translate(cx, cy);
        // Rotate the entire group negatively to keep current value at 3 o'clock
        canvas.rotate(-rotateDeg);

        for (int i = 0; i < 60; i++) {
            canvas.save();
            // Rotate to spike position
            canvas.rotate(i * 6f);

            float spikeLength = 15f;
            float spikeHeight = 3f;
            float dialSize = radius - spikeLength;

            if (i % 5 == 0) {
                // Every 5th spike

                // Draw Shadow
                spikePaint.setColor(Color.argb(90, 255, 255, 255));
                canvas.drawRect(dialSize - 10f, -spikeHeight / 2f, dialSize + spikeLength - 10f, spikeHeight / 2f,
                        spikePaint);

                // Draw Spike
                spikePaint.setColor(Color.argb(220, 255, 255, 255));
                canvas.drawRect(dialSize, -spikeHeight / 2f, dialSize + spikeLength, spikeHeight / 2f, spikePaint);

                // Draw Label
                String text = String.valueOf(i);
                float labelDist = dialSize - 30f; // Draw INSIDE the ring

                canvas.save();
                canvas.translate(labelDist, 0);
                // Rotate back so text is upright
                canvas.rotate(-(i * 6f - rotateDeg));

                textPaint.setTextSize(textSize);
                Rect bounds = new Rect();
                textPaint.getTextBounds(text, 0, text.length(), bounds);
                // Center text
                canvas.drawText(text, 0, bounds.height() / 2f, textPaint);

                canvas.restore();

            } else {
                // Normal spike
                spikePaint.setColor(Color.argb(120, 255, 255, 255));
                canvas.drawRect(dialSize + 5f, -1f, dialSize + spikeLength, 1f, spikePaint);
            }

            canvas.restore();
        }
        canvas.restore();
    }
}
