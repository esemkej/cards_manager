package com.eas.cards;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import java.util.Locale;

public class HsvColorPickerView extends LinearLayout {

    public interface OnColorChangedListener {
        void onColorChanged(int argb, boolean fromUser);
    }

    private final float[] hsv = new float[] { 0f, 1f, 1f }; // h,s,v
    private int alpha = 255;
    private int argb = 0xFFFF0000;

    private OnColorChangedListener listener;

    private ColorWheelView wheel;
    private ValueSliderView valueSlider;

    public HsvColorPickerView(Context context) {
        super(context);
        init(context, null);
    }

    public HsvColorPickerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public HsvColorPickerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context ctx, AttributeSet attrs) {
        setOrientation(VERTICAL);

        wheel = new ColorWheelView(ctx);
        valueSlider = new ValueSliderView(ctx);
        wheel.setElevation(dp(2));
        valueSlider.setElevation(dp(2));

        LayoutParams wheelLp = new LayoutParams(LayoutParams.MATCH_PARENT, 0);
        wheelLp.gravity = android.view.Gravity.CENTER_HORIZONTAL;
        wheelLp.weight = 1f;
        addView(wheel, wheelLp);

        int barH = dp(28);
        LayoutParams barLp = new LayoutParams(LayoutParams.MATCH_PARENT, barH);
        barLp.topMargin = dp(10);
        addView(valueSlider, barLp);

        wheel.setOnHueSatChangedListener(new ColorWheelView.OnHueSatChangedListener() {
            @Override public void onHueSatChanged(float h, float s, boolean fromUser) {
                hsv[0] = h;
                hsv[1] = s;
                update(fromUser);
            }
        });

        valueSlider.setOnValueChangedListener(new ValueSliderView.OnValueChangedListener() {
            @Override public void onValueChanged(float v, boolean fromUser) {
                hsv[2] = v;
                update(fromUser);
            }
        });

        update(false);
    }

    private void update(boolean fromUser) {
        argb = Color.HSVToColor(alpha, hsv);
        wheel.setPreviewColor(argb);

        int fullBrightRgb = Color.HSVToColor(255, new float[]{ hsv[0], hsv[1], 1f });
        valueSlider.setBaseColor(fullBrightRgb);
        valueSlider.setValue(hsv[2]);

        wheel.setHueSat(hsv[0], hsv[1]);

        if (listener != null) listener.onColorChanged(argb, fromUser);
    }

    public void setOnColorChangedListener(OnColorChangedListener l) {
        listener = l;
    }

    /** Set current color (ARGB int: 0xAARRGGBB). */
    public void setColor(int color) {
        alpha = Color.alpha(color);
        Color.colorToHSV(color, hsv);
        update(false);
    }

    /** Get current color as ARGB int (0xAARRGGBB). */
    public int getColor() {
        return argb;
    }

    /** Get current color as hex string: #AARRGGBB */
    public String getHex() {
        return String.format(Locale.US, "#%08X", argb);
    }

    private int dp(int dp) {
        return (int)(dp * getResources().getDisplayMetrics().density + 0.5f);
    }

    // -------------------- Color Wheel --------------------

    public static class ColorWheelView extends View {
        public interface OnHueSatChangedListener {
            void onHueSatChanged(float hue, float sat, boolean fromUser);
        }

        private OnHueSatChangedListener listener;

        private Bitmap wheelBitmap;
        private final Paint bitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint thumbPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint previewFill = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint previewStroke = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint wheelStroke = new Paint(Paint.ANTI_ALIAS_FLAG);
        private int previewColor = 0xFFFFFFFF;

        private float hue = 0f;    // 0..360
        private float sat = 1f;    // 0..1

        private float cx, cy, radius;

        public ColorWheelView(Context c) { super(c); init(); }
        public ColorWheelView(Context c, AttributeSet a) { super(c, a); init(); }

        private void init() {
            thumbPaint.setStyle(Paint.Style.STROKE);
            thumbPaint.setStrokeWidth(4f);
            thumbPaint.setColor(Color.WHITE);
            setFocusable(true);
            previewFill.setStyle(Paint.Style.FILL);
            previewStroke.setStyle(Paint.Style.STROKE);
            previewStroke.setColor(0xFF424242);
            previewStroke.setStrokeWidth(2f * getResources().getDisplayMetrics().density);
            wheelStroke.setStyle(Paint.Style.STROKE);
            wheelStroke.setColor(0xFF424242);
            wheelStroke.setStrokeWidth(2f * getResources().getDisplayMetrics().density);
        }

        public void setOnHueSatChangedListener(OnHueSatChangedListener l) {
            listener = l;
        }

        public void setHueSat(float h, float s) {
            hue = clamp(h, 0f, 360f);
            sat = clamp(s, 0f, 1f);
            invalidate();
        }

        public void setPreviewColor(int argb) {
            previewColor = argb;
            invalidate();
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            int w = MeasureSpec.getSize(widthMeasureSpec);
            int h = MeasureSpec.getSize(heightMeasureSpec);

            if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.UNSPECIFIED) {
                h = w;
            }
            int size = Math.min(w, h);
            setMeasuredDimension(size, size);
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            int size = Math.min(w, h);
            cx = size / 2f;
            cy = size / 2f;
            radius = size / 2f;

            wheelBitmap = makeWheelBitmap(size);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            if (wheelBitmap != null) {
                canvas.drawBitmap(wheelBitmap, 0, 0, bitmapPaint);
            }

            float ang = (float) Math.toRadians(hue);
            float r = sat * radius;
            float x = cx + (float) Math.cos(ang) * r;
            float y = cy + (float) Math.sin(ang) * r;
            float strokeInset = wheelStroke.getStrokeWidth() * 0.5f;

            canvas.drawCircle(cx, cy, radius - strokeInset, wheelStroke);

            float previewRadius = (radius * 0.22f) - (previewStroke.getStrokeWidth() * 0.5f) - (4f *  getResources().getDisplayMetrics().density);

            previewFill.setColor(previewColor);
            previewFill.setAlpha(240);
            canvas.drawCircle(cx, cy, previewRadius, previewFill);
            canvas.drawCircle(cx, cy, previewRadius, previewStroke);

            canvas.drawCircle(x, y, 14f, thumbPaint);
        }

        @Override
        public boolean onTouchEvent(MotionEvent e) {
            float x = e.getX() - cx;
            float y = e.getY() - cy;
            float dist = (float) Math.hypot(x, y);

            float s = clamp(dist / radius, 0f, 1f);
            float ang = (float) Math.atan2(y, x);
            float h = (float) Math.toDegrees(ang);
            if (h < 0) h += 360f;

            switch (e.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_MOVE:
                    hue = h;
                    sat = s;
                    if (listener != null) listener.onHueSatChanged(hue, sat, true);
                    invalidate();
                    getParent().requestDisallowInterceptTouchEvent(true);
                    return true;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    getParent().requestDisallowInterceptTouchEvent(false);
                    return true;
            }
            return super.onTouchEvent(e);
        }

        private Bitmap makeWheelBitmap(int size) {
            Bitmap bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
            int[] px = new int[size * size];

            float r = size / 2f;
            float cx = r;
            float cy = r;

            float[] hsv = new float[] { 0f, 0f, 1f };

            for (int yy = 0; yy < size; yy++) {
                float dy = yy - cy;
                for (int xx = 0; xx < size; xx++) {
                    float dx = xx - cx;
                    float dist = (float) Math.hypot(dx, dy);

                    if (dist <= r) {
                        float sat = dist / r;
                        float ang = (float) Math.atan2(dy, dx);
                        float hue = (float) Math.toDegrees(ang);
                        if (hue < 0) hue += 360f;

                        hsv[0] = hue;
                        hsv[1] = sat;
                        hsv[2] = 1f;

                        px[yy * size + xx] = Color.HSVToColor(hsv);
                    } else {
                        px[yy * size + xx] = 0x00000000;
                    }
                }
            }
            bmp.setPixels(px, 0, size, 0, 0, size, size);
            return bmp;
        }

        private static float clamp(float v, float lo, float hi) {
            return Math.max(lo, Math.min(hi, v));
        }
    }

    // -------------------- Value Slider --------------------

    public static class ValueSliderView extends View {
        public interface OnValueChangedListener {
            void onValueChanged(float value, boolean fromUser);
        }

        private OnValueChangedListener listener;

        private final Paint barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint thumbPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        private int baseColor = Color.RED; // RGB at V=1
        private float value = 1f;          // 0..1

        public ValueSliderView(Context c) { super(c); init(); }
        public ValueSliderView(Context c, AttributeSet a) { super(c, a); init(); }

        private void init() {
            thumbPaint.setStyle(Paint.Style.STROKE);
            thumbPaint.setStrokeWidth(4f);
            thumbPaint.setColor(Color.WHITE);
        }

        public void setOnValueChangedListener(OnValueChangedListener l) {
            listener = l;
        }

        public void setBaseColor(int rgb) {
            baseColor = (rgb | 0xFF000000);
            invalidate();
        }

        public void setValue(float v) {
            value = clamp(v, 0f, 1f);
            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            int w = getWidth();
            int h = getHeight();

            Shader sh = new LinearGradient(
                    0, 0, w, 0,
                    new int[]{ Color.BLACK, baseColor },
                    null,
                    Shader.TileMode.CLAMP
            );
            barPaint.setShader(sh);

            RectF r = new RectF(0, 0, w, h);
            canvas.drawRoundRect(r, h/2f, h/2f, barPaint);

            float x = value * w;
            canvas.drawCircle(x, h/2f, h/2f - 3f, thumbPaint);
        }

        @Override
        public boolean onTouchEvent(MotionEvent e) {
            if (e.getActionMasked() == MotionEvent.ACTION_DOWN ||
                    e.getActionMasked() == MotionEvent.ACTION_MOVE) {

                float v = e.getX() / Math.max(1f, getWidth());
                value = clamp(v, 0f, 1f);

                if (listener != null) listener.onValueChanged(value, true);
                invalidate();
                getParent().requestDisallowInterceptTouchEvent(true);
                return true;
            }
            if (e.getActionMasked() == MotionEvent.ACTION_UP ||
                    e.getActionMasked() == MotionEvent.ACTION_CANCEL) {
                getParent().requestDisallowInterceptTouchEvent(false);
                return true;
            }
            return super.onTouchEvent(e);
        }

        private static float clamp(float v, float lo, float hi) {
            return Math.max(lo, Math.min(hi, v));
        }
    }
}