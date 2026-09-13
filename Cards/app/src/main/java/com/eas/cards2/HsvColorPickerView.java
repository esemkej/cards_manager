package com.eas.cards2;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.view.View;
import android.view.MotionEvent;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.widget.TextView;
import android.widget.EditText;
import android.text.Editable;
import android.text.TextWatcher;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import java.util.Locale;

/** A hue field with native keyboard/accessibility support and direct hex entry. */
public class HsvColorPickerView extends LinearLayout {
    private final float[] hsv = {0f, 1f, 1f};
    private int alpha = 255;
    private EditText preview;
    private TextView randomize;
    private boolean editingHex;
    private HueField hueField;
    private boolean updating;

    public HsvColorPickerView(Context c) { this(c, null); }
    public HsvColorPickerView(Context c, AttributeSet a) { this(c, a, 0); }
    public HsvColorPickerView(Context c, AttributeSet a, int style) {
        super(c, a, style);
        setOrientation(VERTICAL);
        setClipChildren(false);
        preview = new EditText(c);
        preview.setSingleLine(true);
        preview.setSelectAllOnFocus(true);
        preview.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        preview.setFilters(new android.text.InputFilter[]{new android.text.InputFilter.LengthFilter(9)});
        preview.setGravity(Gravity.CENTER);
        preview.setTextSize(18);
        preview.setTypeface(android.graphics.Typeface.MONOSPACE);
        addView(preview, new LayoutParams(-1, dp(64)));

        LinearLayout presets = new LinearLayout(c);
        presets.setGravity(Gravity.CENTER_VERTICAL);
        LayoutParams row = new LayoutParams(-1, dp(56));
        row.topMargin = dp(8);
        addView(presets, row);
        int[] colors = new int[6];
        for (int i = 0; i < colors.length; i++) colors[i] = randomColor();
        for (int i = 0; i < colors.length; i++) {
            final int color = colors[i];
            android.widget.ImageButton swatch = new android.widget.ImageButton(c);
            swatch.setContentDescription(c.getString(R.string.color_preview, String.format(Locale.US, "#%06X", color & 0xFFFFFF)));
            swatch.setBackgroundResource(android.R.color.transparent);
            GradientDrawable circle = shape(new int[]{color, color}, 0, true);
            circle.setShape(GradientDrawable.OVAL);
            circle.setSize(dp(32), dp(32));
            swatch.setImageDrawable(circle);
            swatch.setScaleType(android.widget.ImageView.ScaleType.CENTER_INSIDE);
            swatch.setPadding(dp(4), dp(8), dp(4), dp(8));
            presets.addView(swatch, new LayoutParams(0, -1, 1));
            swatch.setOnClickListener(v -> {
                Color.colorToHSV(color, hsv);
                update();
                swatch.animate().cancel();
                swatch.setScaleX(.88f); swatch.setScaleY(.88f);
                swatch.animate().scaleX(1).scaleY(1).setDuration(180).start();
            });
        }
        hueField = new HueField(c);
        LayoutParams fieldLp = new LayoutParams(-1, dp(180));
        fieldLp.topMargin = dp(8);
        addView(hueField, fieldLp);
        randomize = new TextView(c);
        randomize.setText(R.string.color_randomize);
        randomize.setTextSize(14);
        randomize.setGravity(Gravity.CENTER);
        randomize.setTextColor(theme(R.color.app_text_dark));
        randomize.setMinHeight(dp(48));
        randomize.setPadding(dp(12), dp(8), dp(12), dp(8));
        randomize.setBackground(Bg.build(randomize, theme(R.color.app_surface_var), null, null,
                16, null, 1, theme(R.color.app_stroke), theme(R.color.app_ripple)));
        LayoutParams randomLp = new LayoutParams(-1, -2);
        randomLp.topMargin = dp(12);
        addView(randomize, randomLp);
        randomize.setOnClickListener(v -> setColor(randomColor()));
        preview.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (updating) return;
                String raw = s.toString();
                if (raw.indexOf('#') >= 0) {
                    String normalized = "#" + raw.replace("#", "");
                    if (!normalized.equals(raw)) {
                        preview.setText(normalized);
                        preview.setSelection(normalized.length());
                        return;
                    }
                }
                Integer parsed = HexColor.parse(raw);
                if (parsed == null) return;
                editingHex = true;
                setColor(parsed);
                editingHex = false;
            }
            @Override public void afterTextChanged(Editable text) { }
        });
        preview.setOnFocusChangeListener((v, focused) -> {
            if (!focused) update();
        });
        setColor(randomColor());
    }

    public void applyTextScale(float scale) {
        MainActivity.applyTextScale(preview, scale);
        MainActivity.applyTextScale(randomize, scale);
    }

    public static int randomColor() {
        return Color.HSVToColor(new float[]{java.util.concurrent.ThreadLocalRandom.current().nextFloat() * 360f, 1f, 1f});
    }

    private void update() {
        updating = true;
        int color = getColor();
        preview.setBackground(shape(new int[]{color, color}, 20, true));
        int visibleColor = ColorUtils.compositeColors(color, theme(R.color.app_surface));
        preview.setTextColor(ColorUtils.calculateLuminance(visibleColor) > .4 ? Color.BLACK : Color.WHITE);
        String hex = alpha == 255 ? String.format(Locale.US, "#%06X", color & 0xFFFFFF) : getHex();
        if (!editingHex) preview.setText(hex);
        preview.setContentDescription(getResources().getString(R.string.color_preview, hex));
        hueField.invalidate();
        updating = false;
    }

    private final class HueField extends View {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final RectF field = new RectF();
        private Shader rainbow, shade;
        HueField(Context context) {
            super(context);
            setFocusable(true); setClickable(true);
            setContentDescription(context.getString(R.string.color_field));
            androidx.core.view.ViewCompat.addAccessibilityAction(this, context.getString(R.string.color_brighter),
                    (view, args) -> adjust(0, .05f));
            androidx.core.view.ViewCompat.addAccessibilityAction(this, context.getString(R.string.color_darker),
                    (view, args) -> adjust(0, -.05f));
        }
        @Override protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            field.set(dp(16), dp(16), w - dp(16), h - dp(16));
            rainbow = new LinearGradient(field.left, 0, field.right, 0,
                    new int[]{Color.RED, Color.YELLOW, Color.GREEN, Color.CYAN, Color.BLUE, Color.MAGENTA, Color.RED},
                    null, Shader.TileMode.CLAMP);
            shade = new LinearGradient(0, field.top, 0, field.bottom, Color.TRANSPARENT, Color.BLACK, Shader.TileMode.CLAMP);
        }
        @Override protected void onDraw(Canvas canvas) {
            paint.setStyle(Paint.Style.FILL); paint.setShader(rainbow);
            canvas.drawRoundRect(field, dp(16), dp(16), paint);
            paint.setShader(shade);
            canvas.drawRoundRect(field, dp(16), dp(16), paint);
            paint.setShader(null);
            float x = field.left + hsv[0] / 360f * field.width();
            float y = field.top + (1 - hsv[2]) * field.height();
            paint.setColor(0x55000000);
            canvas.drawCircle(x, y, dp(13), paint);
            paint.setColor(getColor());
            canvas.drawCircle(x, y, dp(10), paint);
            paint.setStyle(Paint.Style.STROKE); paint.setStrokeWidth(dp(3)); paint.setColor(Color.WHITE);
            canvas.drawCircle(x, y, dp(10), paint);
        }
        @Override public boolean onTouchEvent(MotionEvent event) {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_MOVE:
                    getParent().requestDisallowInterceptTouchEvent(true);
                    hsv[0] = Math.max(0, Math.min(1, (event.getX() - field.left) / Math.max(1, field.width()))) * 360;
                    hsv[2] = 1 - Math.max(0, Math.min(1, (event.getY() - field.top) / Math.max(1, field.height())));
                    hsv[1] = 1; alpha = 255; update();
                    return true;
                case MotionEvent.ACTION_UP:
                    performClick();
                    getParent().requestDisallowInterceptTouchEvent(false);
                    return true;
                case MotionEvent.ACTION_CANCEL:
                    getParent().requestDisallowInterceptTouchEvent(false);
                    return true;
            }
            return super.onTouchEvent(event);
        }
        @Override public boolean performClick() { super.performClick(); return true; }
        private boolean adjust(float hue, float brightness) {
            hsv[0] = Math.max(0, Math.min(360, hsv[0] + hue));
            hsv[2] = Math.max(0, Math.min(1, hsv[2] + brightness));
            hsv[1] = 1; alpha = 255; update(); return true;
        }
        @Override public boolean onKeyDown(int key, KeyEvent event) {
            if (key == KeyEvent.KEYCODE_DPAD_LEFT) return adjust(-3, 0);
            if (key == KeyEvent.KEYCODE_DPAD_RIGHT) return adjust(3, 0);
            if (key == KeyEvent.KEYCODE_DPAD_UP) return adjust(0, .05f);
            if (key == KeyEvent.KEYCODE_DPAD_DOWN) return adjust(0, -.05f);
            return super.onKeyDown(key, event);
        }
        @Override public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
            super.onInitializeAccessibilityNodeInfo(info);
            info.setClassName(android.widget.SeekBar.class.getName());
            info.setRangeInfo(AccessibilityNodeInfo.RangeInfo.obtain(AccessibilityNodeInfo.RangeInfo.RANGE_TYPE_FLOAT, 0, 360, hsv[0]));
            info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_FORWARD);
            info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_BACKWARD);
        }
        @Override public boolean performAccessibilityAction(int action, android.os.Bundle args) {
            if (action == AccessibilityNodeInfo.ACTION_SCROLL_FORWARD) return adjust(3, 0);
            if (action == AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD) return adjust(-3, 0);
            return super.performAccessibilityAction(action, args);
        }
    }

    private GradientDrawable shape(int[] colors, int radius, boolean outline) {
        GradientDrawable d = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, colors);
        d.setCornerRadius(dp(radius));
        if (outline) d.setStroke(dp(1), theme(R.color.app_stroke));
        return d;
    }
    private int theme(int id) { return ContextCompat.getColor(getContext(), id); }
    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }
    public void setColor(int color) {
        alpha = Color.alpha(color);
        Color.colorToHSV(color, hsv);
        update();
    }
    public int getColor() { return Color.HSVToColor(alpha, hsv); }
    public String getHex() { return String.format(Locale.US, "#%08X", getColor()); }
}
