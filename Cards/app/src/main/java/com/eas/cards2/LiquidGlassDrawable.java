package com.eas.cards2;

import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.view.View;

/** Shared liquid lens for rounded glass panels and caption bubbles. */
class LiquidGlassDrawable extends GradientDrawable {
    private final View host, source;
    private final boolean backgroundOnly;
    private final LiquidGlassBackdrop backdrop = new LiquidGlassBackdrop();
    private final RectF bounds = new RectF();
    private final Path shape = new Path();
    private final Paint rim = new Paint(Paint.ANTI_ALIAS_FLAG);
    private float radius;
    private int opacity = 255;

    LiquidGlassDrawable(View host, View source, boolean backgroundOnly, int[] colors) {
        super(Orientation.TOP_BOTTOM, colors);
        this.host = host;
        this.source = source;
        this.backgroundOnly = backgroundOnly;
    }

    @Override public void setCornerRadius(float radius) {
        this.radius = radius;
        super.setCornerRadius(radius);
    }

    @Override public void setAlpha(int alpha) {
        opacity = alpha;
        super.setAlpha(alpha);
    }

    @Override public void draw(Canvas canvas) {
        if (LiquidGlassBackdrop.isCapturing()) return;
        bounds.set(getBounds());
        shape.reset();
        shape.addRoundRect(bounds, radius, radius, Path.Direction.CW);
        backdrop.draw(canvas, host, source, backgroundOnly, bounds, shape, 1, opacity);
        super.draw(canvas);
        float density = host.getResources().getDisplayMetrics().density;
        bounds.inset(density / 2, density / 2);
        rim.setShader(new LinearGradient(0, bounds.top, bounds.right, bounds.bottom,
                new int[]{0x80FFFFFF, 0x08FFFFFF, 0x38FFFFFF}, null, Shader.TileMode.CLAMP));
        rim.setAlpha(opacity);
        rim.setStyle(Paint.Style.STROKE);
        rim.setStrokeWidth(density);
        canvas.drawRoundRect(bounds, radius, radius, rim);
    }
}
