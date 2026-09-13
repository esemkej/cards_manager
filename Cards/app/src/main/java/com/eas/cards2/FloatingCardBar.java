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
    interface PathNavigation { void accept(int depth); }
    private final RecyclerView list;
    private final View search;
    private final View selection;
    private final View controls;
    private final GlassSurface surface;
    private final SearchCapsule capsule;
    private ValueAnimator selectionEntry;
    private float entryProgress;
    private boolean selectionWanted;
    private float reveal;
    private boolean navigating;
    private float navigationReveal;
    private android.widget.HorizontalScrollView pathStrip;
    private java.util.List<String> pathIds = new java.util.ArrayList<>();
    private boolean pathTransition, pathWanted;
    private float pathProgress;
    private float pathShown;
    private boolean pathTextOnly, showingNewPath;
    private int changedPathChild;
    private android.widget.LinearLayout pendingPathRow;

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
        setClipChildren(false);
        controls.setOutlineProvider(new android.view.ViewOutlineProvider() {
            @Override public void getOutline(View view, Outline outline) {
                outline.setRoundRect(search.getLeft(), search.getTop(), Math.round(searchRight()), search.getBottom(), dp(16));
                outline.setAlpha(.65f);
            }
        });
        controls.setElevation(dp(5));
        observeOverlap = () -> {
            if (!navigating && !list.hasPendingAdapterUpdates() && !list.isComputingLayout()) {
                RecyclerView.LayoutManager layout = list.getLayoutManager();
                View first = layout == null ? null : layout.findViewByPosition(0);
                Integer offset = first != null && list.getChildAdapterPosition(first) == 0
                        ? list.getPaddingTop() - dp(8) - layout.getDecoratedTop(first) : null;
                int firstVisible = layout instanceof androidx.recyclerview.widget.LinearLayoutManager
                        ? ((androidx.recyclerview.widget.LinearLayoutManager) layout).findFirstVisibleItemPosition() : RecyclerView.NO_POSITION;
                setReveal(HeaderScrollState.fromRows(reveal, offset, list.canScrollVertically(-1), firstVisible,
                        list.getItemAnimator() != null && list.getItemAnimator().isRunning(), dp(72)));
            }
            updatePathTransition();
            layoutPath();
            return true;
        };
        surface = new GlassSurface();
        setBackground(surface);
        // Blank areas of the floating controls must not activate a card underneath.
        setClickable(true);
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
    }

    void preparePath(java.util.List<String> ids) {
        if (pathIds.equals(ids)) return;
        navigationReveal = reveal;
        navigating = true;
        list.stopScroll();
        if (list.getLayoutManager() instanceof androidx.recyclerview.widget.LinearLayoutManager) {
            ((androidx.recyclerview.widget.LinearLayoutManager) list.getLayoutManager()).scrollToPositionWithOffset(0, 0);
        }
    }

    void setPath(java.util.List<String> ids, java.util.List<String> names, PathNavigation navigate) {
        if (pathStrip == null) {
            pathStrip = new android.widget.HorizontalScrollView(getContext());
            pathStrip.setHorizontalScrollBarEnabled(false);
            pathStrip.setClipToPadding(false);
            android.graphics.drawable.GradientDrawable paper = new android.graphics.drawable.GradientDrawable();
            paper.setColor(ContextCompat.getColor(getContext(), R.color.app_surface_var));
            paper.setCornerRadius(dp(16));
            pathStrip.setBackground(paper);
            pathStrip.setPadding(dp(8), dp(16), dp(8), 0);
            addView(pathStrip, 0, new FrameLayout.LayoutParams(-1, -2));
            pathStrip.setVisibility(GONE);
        }
        boolean changed = !pathIds.equals(ids);
        if (!changed) return;
        if (pendingPathRow != null) installPendingPath();
        int common = 0;
        while (common < pathIds.size() && common < ids.size() && pathIds.get(common).equals(ids.get(common))) common++;
        pathTextOnly = !pathIds.isEmpty() && !ids.isEmpty();
        changedPathChild = common * 2 + 1; // Root label, then separator/label pairs.
        showingNewPath = false;
        pathIds = new java.util.ArrayList<>(ids);
        pathWanted = !ids.isEmpty();
        pathTransition = true;
        pathProgress = 0;
        if (pathWanted) {
            android.widget.LinearLayout textRow = new android.widget.LinearLayout(getContext());
            textRow.setGravity(android.view.Gravity.CENTER_VERTICAL);
            java.util.List<String> labels = new java.util.ArrayList<>();
            labels.add(getContext().getString(R.string.selection_root)); labels.addAll(names);
            for (int i = 0; i < labels.size(); i++) {
                final int depth = i;
                if (i > 0) {
                    android.widget.TextView separator = pathText(getContext().getString(R.string.path_separator));
                    separator.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
                    textRow.addView(separator);
                }
                android.widget.TextView label = pathText(labels.get(i));
                if (i < labels.size() - 1) {
                    label.setTextColor(ContextCompat.getColor(getContext(), R.color.app_accent));
                    label.setPaintFlags(label.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                    label.setOnClickListener(v -> navigate.accept(depth));
                } else label.setTypeface(null, android.graphics.Typeface.BOLD);
                textRow.addView(label);
            }
            pendingPathRow = textRow;
            android.view.ViewGroup oldRow = pathStrip.getChildCount() == 0 ? null : (android.view.ViewGroup) pathStrip.getChildAt(0);
            if (!pathTextOnly || oldRow == null || oldRow.getChildCount() <= changedPathChild) installPendingPath();
            pathStrip.setVisibility(VISIBLE);
            pathProgress = 0;
            if (pathTextOnly && showingNewPath) setPathTailAlpha(0);
        }
        requestLayout();
        applyPathProgress(pathTextOnly ? 1 : pathWanted ? 0 : 1);
    }

    private void installPendingPath() {
        if (pendingPathRow == null) return;
        pathStrip.removeAllViews(); pathStrip.addView(pendingPathRow);
        pendingPathRow = null; showingNewPath = true;
        pathStrip.post(() -> pathStrip.fullScroll(View.FOCUS_RIGHT));
    }

    private void setPathTailAlpha(float alpha) {
        if (pathStrip.getChildCount() == 0) return;
        android.view.ViewGroup row = (android.view.ViewGroup) pathStrip.getChildAt(0);
        for (int i = 0; i < row.getChildCount(); i++) row.getChildAt(i).setAlpha(i < changedPathChild ? 1 : alpha);
    }

    private android.widget.TextView pathText(String text) {
        android.widget.TextView view = new android.widget.TextView(getContext());
        view.setText(text); view.setTextSize(14); view.setSingleLine(true);
        view.setGravity(android.view.Gravity.CENTER_VERTICAL);
        view.setPadding(dp(4), dp(12), dp(4), dp(12));
        view.setTextColor(ContextCompat.getColor(getContext(), R.color.app_text_dark));
        ((MainActivity) getContext()).applyCurrentTextScale(view);
        return view;
    }

    private void updatePathTransition() {
        if (!pathTransition || pathStrip == null) return;
        RecyclerView.ItemAnimator animator = list.getItemAnimator();
        boolean running = list.hasPendingAdapterUpdates() || list.isComputingLayout()
                || (animator != null && animator.isRunning());
        float visible = 1;
        float outgoingAlpha = 0;
        boolean incoming = false;
        for (int i = 0; i < list.getChildCount(); i++) {
            View child = list.getChildAt(i);
            if (list.getChildAdapterPosition(child) != RecyclerView.NO_POSITION) {
                incoming = true; visible = Math.min(visible, child.getAlpha());
            } else outgoingAlpha = Math.max(outgoingAlpha, child.getAlpha());
        }
        if (navigating) {
            // Collapse from the actual departure state as destination cards appear.
            float destination = incoming ? visible : 0;
            setReveal(HeaderScrollState.duringNavigation(navigationReveal, destination, running));
            if (!running && !list.hasPendingAdapterUpdates()) navigating = false;
        }
        if (pathTextOnly) {
            if (!showingNewPath) {
                setPathTailAlpha(running ? outgoingAlpha : 0);
                if (!running || outgoingAlpha <= .001f) installPendingPath();
            }
            if (showingNewPath) setPathTailAlpha(!running || !incoming ? 1 : visible);
            applyPathProgress(1);
            if (!running) {
                installPendingPath(); setPathTailAlpha(1);
                pathTransition = false; pathTextOnly = false;
            }
            return;
        }
        if (!incoming && running) {
            float outgoing = 0;
            for (int i = 0; i < list.getChildCount(); i++) outgoing = Math.max(outgoing, list.getChildAt(i).getAlpha());
            visible = 1 - outgoing;
        }
        if (!running) visible = 1;
        pathProgress = Math.max(pathProgress, visible);
        applyPathProgress(pathWanted ? pathProgress : 1 - pathProgress);
        if (!running) {
            pathTransition = false;
            if (!pathWanted) { pathStrip.setVisibility(GONE); requestLayout(); }
        }
    }

    private float searchRight() {
        return search.getRight() + (controls.getWidth() - dp(12) - search.getRight()) * reveal;
    }

    private float pathLeft() {
        float searchLeft = controls.getLeft() + search.getLeft();
        float selectedLeft = dp(8 + (8 + 8 * reveal) * entryProgress);
        return searchLeft + (selectedLeft - searchLeft) * entryProgress + dp(12);
    }
    private float pathRight() {
        float right = controls.getLeft() + searchRight();
        float selectedRight = getWidth() - dp(8 + (8 + 8 * reveal) * entryProgress);
        return right + (selectedRight - right) * entryProgress - dp(12);
    }
    private float activeBarBottom() {
        float searchBottom = getPaddingTop() + search.getTop() + search.getMeasuredHeight();
        float selectedBottom = getPaddingTop() + (controls.getMeasuredHeight() + selection.getMeasuredHeight()) / 2f
                - dp(12) * (1 - entryProgress);
        return searchBottom + (selectedBottom - searchBottom) * entryProgress;
    }

    private void applyPathProgress(float progress) {
        if (Math.abs(pathShown - progress) > .0001f) {
            pathShown = progress;
            requestLayout();
        }
        layoutPath();
    }

    private void layoutPath() {
        if (pathStrip == null || pathStrip.getVisibility() == GONE || search.getWidth() == 0) return;
        int left = Math.round(pathLeft());
        int right = Math.round(pathRight());
        int top = Math.round(activeBarBottom() + dp(28) * reveal) - dp(16);
        int height = pathStrip.getMeasuredHeight();
        int width = Math.max(1, right - left);
        if (pathStrip.getMeasuredWidth() != width) {
            pathStrip.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
        }
        pathStrip.layout(left, top, right, top + height);
        pathStrip.getBackground().setAlpha(Math.round(255 - reveal * 45));
        int travel = height;
        pathStrip.setTranslationY(-travel * (1 - pathShown));
        pathStrip.setClipBounds(new Rect(0, Math.round(travel * (1 - pathShown)), width, height));
    }

    @Override protected void onMeasure(int widthSpec, int heightSpec) {
        super.onMeasure(widthSpec, heightSpec);
        if (pathStrip != null && pathStrip.getVisibility() != GONE) {
            pathStrip.setPadding(dp(8), Math.round(dp(16) * (1 - reveal)), dp(8), 0);
            int pathWidth = Math.max(1, Math.round(pathRight() - pathLeft()));
            pathStrip.measure(MeasureSpec.makeMeasureSpec(pathWidth, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        }
        int base = getPaddingTop() + controls.getMeasuredHeight();
        int extra = 0;
        if (pathStrip != null && pathStrip.getVisibility() != GONE) {
            int end = Math.round(activeBarBottom() + dp(28) * reveal) - dp(16)
                    + pathStrip.getMeasuredHeight() + dp(12);
            extra = Math.max(0, end - base);
        }
        setMeasuredDimension(getMeasuredWidth(), base + Math.round(extra * pathShown));
    }
    @Override protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        layoutPath();
        int selectionTop = getPaddingTop() + Math.max(0, (controls.getHeight() - selection.getMeasuredHeight()) / 2);
        selection.layout(selection.getLeft(), selectionTop, selection.getRight(), selectionTop + selection.getMeasuredHeight());
        updateListInset(getHeight() + dp(8));
    }

    private void updateListInset(int inset) {
        int oldInset = list.getPaddingTop();
        if (oldInset == inset) return;
        androidx.recyclerview.widget.LinearLayoutManager manager = list.getLayoutManager() instanceof androidx.recyclerview.widget.LinearLayoutManager
                ? (androidx.recyclerview.widget.LinearLayoutManager) list.getLayoutManager() : null;
        int position = manager == null ? RecyclerView.NO_POSITION : manager.findFirstVisibleItemPosition();
        View first = position == RecyclerView.NO_POSITION ? null : manager.findViewByPosition(position);
        int offset = first == null ? 0 : manager.getDecoratedTop(first) - oldInset;
        list.setPadding(list.getPaddingLeft(), inset, list.getPaddingRight(), list.getPaddingBottom());
        // Preserve the user's position relative to the moving header rather than letting
        // RecyclerView compensate for padding by scrolling the cards underneath it.
        if (navigating && manager != null) manager.scrollToPositionWithOffset(0, 0);
        else if (first != null && !list.hasPendingAdapterUpdates()
                && list.getChildAdapterPosition(first) == position) manager.scrollToPositionWithOffset(position, offset);
        if (list.getParent() instanceof SwipeRefreshLayout) {
            ((SwipeRefreshLayout) list.getParent()).setProgressViewOffset(false, inset, inset + dp(64));
        }
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

    void setSelectionVisible(boolean show) {
        if (selectionWanted == show) return;
        selectionWanted = show;
        if (selectionEntry != null) { selectionEntry.removeAllListeners(); selectionEntry.cancel(); }
        controls.animate().cancel(); selection.animate().cancel();
        controls.setVisibility(VISIBLE); selection.setVisibility(VISIBLE);
        selectionEntry = ValueAnimator.ofFloat(entryProgress, show ? 1f : 0f);
        selectionEntry.setDuration(220);
        selectionEntry.setInterpolator(new DecelerateInterpolator());
        selectionEntry.addUpdateListener(animation -> {
            entryProgress = (float) animation.getAnimatedValue();
            controls.setAlpha(1 - entryProgress);
            selection.setAlpha(entryProgress);
            selection.setTranslationY(-dp(12) * (1 - entryProgress));
            updateSelectionBubble();
            layoutPath(); requestLayout();
        });
        selectionEntry.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override public void onAnimationEnd(android.animation.Animator animator) {
                controls.setVisibility(selectionWanted ? INVISIBLE : VISIBLE);
                selection.setVisibility(selectionWanted ? VISIBLE : GONE);
            }
        });
        selectionEntry.start();
    }

    private void updateSelectionBubble() {
        // Match the search fill; only its background becomes translucent, never the labels.
        Drawable background = selection.getBackground();
        if (background != null) background.setAlpha(Math.round(255 - reveal * 45));
        selection.setElevation(dp(5) - dp(4) * reveal);
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
        controls.setElevation(dp(5) - dp(4) * reveal);
        controls.invalidateOutline();
        requestLayout();
        updateSelectionBubble();
    }

    private final class SearchCapsule extends Drawable {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final RectF shell = new RectF();
        private final int tint = ContextCompat.getColor(getContext(), R.color.app_surface_var);
        private int opacity = 255;

        @Override public void draw(Canvas canvas) {
            if (search.getWidth() == 0 || search.getHeight() == 0) return;
            float right = searchRight();
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
