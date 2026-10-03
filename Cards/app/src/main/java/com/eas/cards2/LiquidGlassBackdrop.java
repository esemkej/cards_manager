package com.eas.cards2;

import android.graphics.*;
import android.view.View;

/** Samples only the backdrop; the host's controls are painted normally afterwards. */
final class LiquidGlassBackdrop {
    private static final int COLUMNS = 40, ROWS = 20;
    private static boolean capturing;
    private final Canvas capture = new Canvas();
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final float[] vertices = new float[(COLUMNS + 1) * (ROWS + 1) * 2];
    private final int[] hostPosition = new int[2], sourcePosition = new int[2];
    private Bitmap bitmap;
    private int[] pixels, scratch;
    private final RectF meshBounds = new RectF();
    private float meshStrength = -1, meshDensity;

    static boolean isCapturing() { return capturing; }

    void draw(Canvas canvas, View host, View source, boolean backgroundOnly,
              RectF bounds, Path clip, float strength, int alpha) {
        if (capturing || strength <= 0 || bounds.isEmpty() || source.getWidth() == 0) return;
        float density = host.getResources().getDisplayMetrics().density;
        strength *= Math.min(1, Math.min(bounds.width(), bounds.height()) / (24 * density));
        float padding = 24 * density;
        // Blur needs only one sample per two dp. Working at display resolution
        // multiplied CPU pixel work on high-density phones without visible benefit.
        float sample = Math.min(.25f, .5f / density);
        float width = bounds.width() + padding * 2, height = bounds.height() + padding * 2;
        int w = Math.max(1, Math.round(width * sample));
        int h = Math.max(1, Math.round(height * sample));
        if (bitmap == null || bitmap.getWidth() != w || bitmap.getHeight() != h) {
            bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            pixels = new int[w * h];
            scratch = new int[w * h];
        }
        bitmap.eraseColor(Color.TRANSPARENT);
        // Screen coordinates also align a PopupWindow with its source window.
        host.getLocationOnScreen(hostPosition);
        source.getLocationOnScreen(sourcePosition);
        capture.setBitmap(bitmap);
        int saved = capture.save();
        capturing = true;
        try {
            capture.scale(w / width, h / height);
            capture.translate(sourcePosition[0] - hostPosition[0] - bounds.left + padding,
                    sourcePosition[1] - hostPosition[1] - bounds.top + padding);
            if (backgroundOnly) {
                if (source.getBackground() != null) source.getBackground().draw(capture);
            } else source.draw(capture);
        } finally {
            capturing = false;
            capture.restoreToCount(saved);
            capture.setBitmap(null);
        }
        blur(Math.max(1, Math.round(3 * density * sample * strength)));
        if (!meshBounds.equals(bounds) || meshStrength != strength || meshDensity != density) {
            rebuildMesh(bounds, strength, density, padding, width, height);
            meshBounds.set(bounds);
            meshStrength = strength;
            meshDensity = density;
        }
        saved = canvas.save();
        canvas.clipPath(clip);
        paint.setAlpha(alpha);
        canvas.drawBitmapMesh(bitmap, COLUMNS, ROWS, vertices, 0, null, 0, paint);
        canvas.restoreToCount(saved);
    }

    private void rebuildMesh(RectF bounds, float strength, float density,
                             float padding, float width, float height) {
        float centerX = bounds.centerX(), centerY = bounds.centerY();
        float halfW = bounds.width() / 2, halfH = bounds.height() / 2;
        int index = 0;
        for (int row = 0; row <= ROWS; row++) {
            float y = bounds.top - padding + height * row / ROWS;
            for (int column = 0; column <= COLUMNS; column++) {
                float x = bounds.left - padding + width * column / COLUMNS;
                float nx = (x - centerX) / (halfW + padding);
                float ny = (y - centerY) / (halfH + padding);
                float envelope = Math.max(0, (1 - nx * nx) * (1 - ny * ny));
                float edgeX = (float) Math.exp(-Math.pow((Math.abs(x - centerX) - halfW) / (12 * density), 2));
                float edgeY = (float) Math.exp(-Math.pow((Math.abs(y - centerY) - halfH) / (12 * density), 2));
                // Broad lens magnification plus a stronger curved meniscus at the rim.
                // Fixed outer vertices and overscan keep the clipped surface fully covered.
                float dx = (x - centerX) * .065f * envelope
                        + Math.signum(x - centerX) * edgeX * 7 * density * envelope;
                float dy = (y - centerY) * .08f * envelope
                        + Math.signum(y - centerY) * edgeY * 9 * density * envelope;
                dx += Math.sin(ny * Math.PI * 2) * 2 * density * envelope;
                dy += Math.sin(nx * Math.PI * 2) * 2.5f * density * envelope;
                vertices[index++] = x + dx * strength;
                vertices[index++] = y + dy * strength;
            }
        }
    }

    private void blur(int radius) {
        int w = bitmap.getWidth(), h = bitmap.getHeight();
        bitmap.getPixels(pixels, 0, w, 0, 0, w, h);
        // Premultiply before averaging so transparent backdrop edges do not darken.
        for (int i = 0; i < pixels.length; i++) {
            int c = pixels[i], a = Color.alpha(c);
            pixels[i] = Color.argb(a, Color.red(c) * a / 255,
                    Color.green(c) * a / 255, Color.blue(c) * a / 255);
        }
        blurPass(pixels, scratch, w, h, radius, true);
        blurPass(scratch, pixels, w, h, radius, false);
        for (int i = 0; i < pixels.length; i++) {
            int c = pixels[i], a = Color.alpha(c);
            if (a > 0) pixels[i] = Color.argb(a, Math.min(255, Color.red(c) * 255 / a),
                    Math.min(255, Color.green(c) * 255 / a), Math.min(255, Color.blue(c) * 255 / a));
        }
        bitmap.setPixels(pixels, 0, w, 0, 0, w, h);
    }

    private static void blurPass(int[] input, int[] output, int w, int h, int radius, boolean horizontal) {
        int length = horizontal ? w : h, lines = horizontal ? h : w;
        int step = horizontal ? 1 : w, count = radius * 2 + 1;
        for (int line = 0; line < lines; line++) {
            int base = horizontal ? line * w : line;
            int a = 0, r = 0, g = 0, b = 0;
            for (int i = -radius; i <= radius; i++) {
                int c = input[base + Math.max(0, Math.min(length - 1, i)) * step];
                a += Color.alpha(c); r += Color.red(c); g += Color.green(c); b += Color.blue(c);
            }
            for (int i = 0; i < length; i++) {
                output[base + i * step] = Color.argb(a / count, r / count, g / count, b / count);
                int remove = input[base + Math.max(0, i - radius) * step];
                int add = input[base + Math.min(length - 1, i + radius + 1) * step];
                a += Color.alpha(add) - Color.alpha(remove);
                r += Color.red(add) - Color.red(remove);
                g += Color.green(add) - Color.green(remove);
                b += Color.blue(add) - Color.blue(remove);
            }
        }
    }
}
