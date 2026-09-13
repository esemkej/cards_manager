package com.eas.cards2;

import android.animation.ValueAnimator;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

/** Fixed controls above a scrolling list, with a scroll-triggered translucent glass surface. */
// Created programmatically with the RecyclerView it observes, never inflated from XML.
@android.annotation.SuppressLint("ViewConstructor")
final class FloatingCardBar extends FrameLayout {
    private final RecyclerView list;
    private final View search;
    private final View selection;
    private final View controls;
    private final GlassSurface surface;
    private final SearchCapsule capsule;
    private ValueAnimator selectionEntry;
    private float entryProgress = 1f;
    private float reveal;
    private final ViewTreeObserver.OnPreDrawListener observeOverlap;

    FloatingCardBar(RecyclerView list, View search, View selection) {
        super(list.getContext());
        this.list = list;
        this.search = search;
        this.selection = selection;
        controls = (View) search.getParent();
        capsule = new SearchCapsule();
        // Paint one expanding shell behind all three controls. Their actual views stay
        // in the same horizontal layout, so input focus and right-aligned buttons survive.
        search.setBackground(null);
        controls.setBackground(capsule);
        observeOverlap = () -> {
            float progress = 0f;
            if (list.getChildCount() > 0 && list.canScrollVertically(-1)) {
                RecyclerView.LayoutManager layout = list.getLayoutManager();
                View first = layout == null ? null : layout.findViewByPosition(0);
                progress = first == null ? 1f : Math.max(0f, Math.min(1f,
                        (getHeight() - layout.getDecoratedTop(first)) / (float) dp(72)));
            }
            // Distance-driven easing: stopping or reversing the finger stops or reverses
            // the entire morph immediately, with no queued scroll animation.
            setReveal(progress * progress * (3f - 2f * progress));
            return true;
        };
        surface = new GlassSurface();
        setBackground(surface);
        // Blank areas of the floating controls must not activate a card underneath.
        setClickable(true);
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
    }

    void setStatusBarInset(int top) {
        // One drawable and one animator cover both the status band and the controls.
        // Padding keeps search and selection actions below clock/camera cutouts.
        if (getPaddingTop() != top) setPadding(0, top, 0, 0);
    }

    @Override protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        getViewTreeObserver().addOnPreDrawListener(observeOverlap);
    }

    @Override protected void onDetachedFromWindow() {
        getViewTreeObserver().removeOnPreDrawListener(observeOverlap);
        if (selectionEntry != null) selectionEntry.cancel();
        super.onDetachedFromWindow();
    }

    @Override protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);
        int inset = height + dp(8);
        if (list.getPaddingTop() != inset) {
            list.setPadding(list.getPaddingLeft(), inset, list.getPaddingRight(), list.getPaddingBottom());
            if (list.getParent() instanceof SwipeRefreshLayout) {
                ((SwipeRefreshLayout) list.getParent()).setProgressViewOffset(false, inset, inset + dp(64));
            }
        }
    }

    void animateSelectionEntry() {
        if (selectionEntry != null) selectionEntry.cancel();
        entryProgress = 0f;
        updateSelectionBubble();
        selectionEntry = ValueAnimator.ofFloat(0f, 1f);
        selectionEntry.setDuration(220);
        selectionEntry.setInterpolator(new DecelerateInterpolator());
        selectionEntry.addUpdateListener(animation -> {
            entryProgress = (float) animation.getAnimatedValue();
            updateSelectionBubble();
        });
        selectionEntry.start();
    }

    private void updateSelectionBubble() {
        // Match the search fill; only its background becomes translucent, never the labels.
        Drawable background = selection.getBackground();
        if (background != null) background.setAlpha(Math.round(255 - reveal * 45));
        FrameLayout.LayoutParams layout = (FrameLayout.LayoutParams) selection.getLayoutParams();
        if (layout == null) return;
        // Genuine width changes create the squeeze without distorting text or touch targets.
        int margin = dp(8 + (8 + 8 * reveal) * entryProgress);
        if (layout.leftMargin != margin || layout.rightMargin != margin) {
            layout.leftMargin = margin; layout.rightMargin = margin;
            selection.setLayoutParams(layout);
        }
    }

    private void setReveal(float value) {
        if (Math.abs(reveal - value) < .0001f) return;
        reveal = value;
        surface.update(reveal);
        setElevation(dp(8) * reveal);
        invalidateOutline();
        // Draw the browsing controls inward alongside the selection bubble.
        FrameLayout.LayoutParams layout = (FrameLayout.LayoutParams) controls.getLayoutParams();
        int margin = dp(8 * reveal);
        if (layout != null && (layout.leftMargin != margin || layout.rightMargin != margin)) {
            layout.leftMargin = margin; layout.rightMargin = margin;
            controls.setLayoutParams(layout);
        }
        capsule.invalidateSelf();
        updateSelectionBubble();
    }

    private final class SearchCapsule extends Drawable {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final RectF shell = new RectF();
        private final int tint = ContextCompat.getColor(getContext(), R.color.app_surface_var);
        private int opacity = 255;

        @Override public void draw(Canvas canvas) {
            if (search.getWidth() == 0 || search.getHeight() == 0) return;
            float farEdge = controls.getWidth() - dp(12);
            float right = search.getRight() + (farEdge - search.getRight()) * reveal;
            shell.set(search.getLeft(), search.getTop(), right, search.getBottom());
            paint.setColor(tint);
            paint.setAlpha(Math.round((255 - reveal * 45) * opacity / 255f));
            canvas.drawRoundRect(shell, dp(16), dp(16), paint);
        }
        @Override public void setAlpha(int alpha) { opacity = alpha; invalidateSelf(); }
        @Override public void setColorFilter(ColorFilter filter) { paint.setColorFilter(filter); invalidateSelf(); }
        @Override public int getOpacity() { return PixelFormat.TRANSLUCENT; }
    }

    private int dp(float value) { return Math.round(value * getResources().getDisplayMetrics().density); }

    private final class GlassSurface extends Drawable {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final RectF bounds = new RectF();
        private final Path edge = new Path();
        private final Path shape = new Path();
        private final int rest = ContextCompat.getColor(getContext(), R.color.app_bg);
        private final int tint = ContextCompat.getColor(getContext(), R.color.app_surface_var);
        private final int stroke = ContextCompat.getColor(getContext(), R.color.app_stroke);
        private float amount;
        private int opacity = 255;
        private LinearGradient glass;

        void update(float amount) { this.amount = amount; rebuild(); invalidateSelf(); }
        @Override protected void onBoundsChange(Rect rect) { bounds.set(rect); rebuild(); }
        private void rebuild() {
            int top = mix(rest, tint, amount * .45f, Math.round((255 - 38 * amount) * opacity / 255f));
            int bottom = mix(rest, tint, amount, Math.round((255 - 72 * amount) * opacity / 255f));
            glass = new LinearGradient(0, bounds.top, 0, Math.max(1, bounds.bottom), top, bottom, Shader.TileMode.CLAMP);
            float radius = dp(22) * amount;
            shape.reset();
            shape.addRoundRect(bounds, new float[]{0, 0, 0, 0, radius, radius, radius, radius}, Path.Direction.CW);
            edge.reset();
            edge.moveTo(bounds.left, bounds.bottom - radius);
            edge.quadTo(bounds.left, bounds.bottom, bounds.left + radius, bounds.bottom);
            edge.lineTo(bounds.right - radius, bounds.bottom);
            edge.quadTo(bounds.right, bounds.bottom, bounds.right, bounds.bottom - radius);
        }
        @Override public void draw(Canvas canvas) {
            paint.setShader(glass); paint.setStyle(Paint.Style.FILL);
            // Only the lower corners soften; the status-bar edge stays continuous.
            canvas.drawPath(shape, paint);
            paint.setShader(null);
            paint.setColor(mix(stroke, Color.WHITE, .2f, Math.round(110 * amount * opacity / 255f)));
            paint.setStyle(Paint.Style.STROKE); paint.setStrokeWidth(dp(1));
            canvas.drawPath(edge, paint);
        }
        @Override public void getOutline(Outline outline) {
            outline.setRoundRect(getBounds(), dp(22) * amount);
            outline.setAlpha(amount * .65f);
        }
        @Override public void setAlpha(int alpha) { opacity = alpha; rebuild(); invalidateSelf(); }
        @Override public void setColorFilter(ColorFilter filter) { paint.setColorFilter(filter); invalidateSelf(); }
        @Override public int getOpacity() { return PixelFormat.TRANSLUCENT; }
        private int mix(int from, int to, float fraction, int alpha) {
            return Color.argb(alpha, Math.round(Color.red(from) + (Color.red(to) - Color.red(from)) * fraction),
                    Math.round(Color.green(from) + (Color.green(to) - Color.green(from)) * fraction),
                    Math.round(Color.blue(from) + (Color.blue(to) - Color.blue(from)) * fraction));
        }
    }
}
