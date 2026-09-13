package com.eas.cards2;

import android.animation.ValueAnimator;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.Drawable;
import android.view.*;
import android.view.animation.DecelerateInterpolator;
import android.widget.*;
import androidx.recyclerview.widget.RecyclerView;
import java.util.*;

/** Owns transient selection and gesture state; stored cards are changed only by the host. */
final class CardInteractions extends RecyclerView.SimpleOnItemTouchListener {
    interface Host {
        String idAt(int position);
        boolean folderAt(int position);
        boolean favorite(String id);
        void action(String action, Set<String> ids, String destination);
    }
    final LinkedHashSet<String> selected = new LinkedHashSet<>();
    private final RecyclerView list;
    private final ViewGroup root;
    private final LinearLayout fab;
    private final Host host;
    private final Drawable fabBackground;
    private final HorizontalScrollView bar;
    private final View browsingControls;
    private final FloatingCardBar toolbar;
    private final TextView count;
    private final TextView edit;
    private final TextView favorite;
    private final GestureDetector gestures;
    private float holdX, holdY, fingerX, fingerY;
    private boolean armed, dragging, overTrash, reorderAfter;
    private String anchor, folderTarget, reorderTarget;
    private StackView stack;
    private ValueAnimator gather;
    private final int[] location = new int[2];
    private final Runnable scroll = new Runnable() {
        public void run() {
            if (!dragging) return;
            int edge = dp(64);
            int delta = fingerY < list.getPaddingTop() + edge ? -dp(10) : fingerY > list.getHeight() - edge ? dp(10) : 0;
            if (delta != 0 && !overTrash) { list.scrollBy(0, delta); updateTarget(); }
            list.postOnAnimation(this);
        }
    };

    CardInteractions(RecyclerView list, ViewGroup root, LinearLayout fab, ViewGroup parent, Host host) {
        this.list = list; this.root = root; this.fab = fab; this.host = host;
        fabBackground = fab.getBackground();
        fab.setContentDescription(list.getContext().getString(R.string.add_item));
        bar = new HorizontalScrollView(list.getContext());
        bar.setHorizontalScrollBarEnabled(false);
        LinearLayout actions = new LinearLayout(list.getContext());
        actions.setGravity(Gravity.CENTER_VERTICAL);
        bar.addView(actions);
        bar.setBackground(Bg.build(bar, list.getResources().getColor(R.color.app_surface_var),
                null, null, 16, null, 0, Color.TRANSPARENT, null));
        bar.setPadding(dp(4), dp(4), dp(4), dp(4));
        bar.setClipToOutline(true);
        count = button(actions, "", () -> {});
        count.setTextColor(list.getResources().getColor(R.color.app_accent));
        count.setTypeface(null, Typeface.BOLD);
        count.setClickable(false); count.setFocusable(false);
        count.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);
        button(actions, list.getContext().getString(R.string.selection_close), this::clear);
        edit = button(actions, list.getContext().getString(R.string.selection_edit), () -> act("edit"));
        button(actions, list.getContext().getString(R.string.selection_move), () -> act("move"));
        favorite = button(actions, list.getContext().getString(R.string.selection_favorite),
                () -> act(allFavorites() ? "unfavorite" : "favorite"));
        button(actions, list.getContext().getString(R.string.delete), () -> act("delete"));
        button(actions, list.getContext().getString(R.string.selection_all), () -> {
            for (int n = 0; n < list.getAdapter().getItemCount(); n++) {
                String id = host.idAt(n); if (id != null) selected.add(id);
            }
            changed();
        });
        // Overlay the browsing controls, preserving geometry while a finger is still down.
        browsingControls = parent.findViewById(R.id.filter_parent);
        int controlsIndex = parent.indexOfChild(browsingControls);
        ViewGroup.LayoutParams controlsLayout = browsingControls.getLayoutParams();
        parent.removeView(browsingControls);
        toolbar = new FloatingCardBar(list, browsingControls.findViewById(R.id.search_bar), bar);
        toolbar.addView(browsingControls, new FrameLayout.LayoutParams(-1, -2));
        FrameLayout.LayoutParams selectionLayout = new FrameLayout.LayoutParams(-1, dp(56), Gravity.CENTER_VERTICAL);
        selectionLayout.leftMargin = dp(16); selectionLayout.rightMargin = dp(16);
        toolbar.addView(bar, selectionLayout);
        parent.addView(toolbar, controlsIndex, controlsLayout);
        bar.setVisibility(View.GONE);
        list.setPadding(list.getPaddingLeft(), list.getPaddingTop(), list.getPaddingRight(), dp(120));
        gestures = new GestureDetector(list.getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override public void onLongPress(MotionEvent e) {
                View child = list.findChildViewUnder(e.getX(), e.getY());
                if (child == null) return;
                String id = host.idAt(list.getChildAdapterPosition(child));
                if (id == null) return;
                anchor = id; selected.add(id); armed = true;
                holdX = e.getX(); holdY = e.getY();
                list.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                changed();
                list.getParent().requestDisallowInterceptTouchEvent(true);
            }
        });
        list.addOnItemTouchListener(this);
        list.addOnChildAttachStateChangeListener(new RecyclerView.OnChildAttachStateChangeListener() {
            public void onChildViewAttachedToWindow(View v) { decorate(v, false); }
            public void onChildViewDetachedFromWindow(View v) { v.setAlpha(1f); }
        });
    }
    private int dp(float n) { return Math.round(n * list.getResources().getDisplayMetrics().density); }
    private TextView button(LinearLayout row, String label, Runnable action) {
        TextView v = new TextView(list.getContext()); v.setText(label); v.setTextSize(14);
        ((MainActivity) list.getContext()).applyCurrentTextScale(v);
        v.setTextColor(list.getResources().getColor(R.color.app_text_dark));
        v.setGravity(Gravity.CENTER); v.setPadding(dp(14), 0, dp(14), 0);
        TypedValueCompat.ripple(v);
        v.setOutlineProvider(new ViewOutlineProvider() {
            @Override public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), dp(12));
            }
        });
        v.setClipToOutline(true);
        v.setOnClickListener(w -> action.run());
        row.addView(v, new LinearLayout.LayoutParams(-2, dp(48))); return v;
    }
    private void act(String action) { host.action(action, new LinkedHashSet<>(selected), null); }
    void setStatusBarInset(int top) { toolbar.setStatusBarInset(top); }
    boolean active() { return !selected.isEmpty(); }
    boolean tap(String id) {
        if (!active()) return false;
        if (id != null) { if (!selected.remove(id)) selected.add(id); changed(); }
        return true;
    }
    void clear() { cancel(); selected.clear(); changed(); }
    void reconcile() {
        Set<String> visible = new HashSet<>();
        for (int i = 0; i < list.getAdapter().getItemCount(); i++) { String id = host.idAt(i); if (id != null) visible.add(id); }
        selected.retainAll(visible); changed();
    }
    private boolean allFavorites() {
        if (selected.isEmpty()) return false;
        for (String id : selected) if (!host.favorite(id)) return false;
        return true;
    }
    private void changed() {
        favorite.setText(allFavorites() ? R.string.selection_unfavorite : R.string.selection_favorite);
        LinearLayout actions = (LinearLayout) bar.getChildAt(0);
        for (int i = 0; i < actions.getChildCount(); i++) {
            ((MainActivity) list.getContext()).applyCurrentTextScale((TextView) actions.getChildAt(i));
        }
        boolean show = active();
        browsingControls.setVisibility(show ? View.INVISIBLE : View.VISIBLE);
        if (show && bar.getVisibility() != View.VISIBLE) {
            toolbar.animateSelectionEntry();
            bar.setVisibility(View.VISIBLE); bar.setAlpha(0); bar.setTranslationY(-dp(12));
            bar.animate().alpha(1).translationY(0).setDuration(180).start();
        } else if (!show) { bar.animate().cancel(); bar.setVisibility(View.GONE); }
        count.setText(list.getResources().getQuantityString(R.plurals.selection_count, selected.size(), selected.size()));
        edit.setVisibility(selected.size() == 1 ? View.VISIBLE : View.GONE);
        ((androidx.swiperefreshlayout.widget.SwipeRefreshLayout) list.getParent()).setEnabled(!show);
        for (int i = 0; i < list.getChildCount(); i++) decorate(list.getChildAt(i), true);
    }
    void decorate(View item, boolean animate) {
        View card = item.findViewById(R.id.parent);
        if (!(card instanceof FrameLayout)) return;
        String id = host.idAt(list.getChildAdapterPosition(item));
        boolean checked = id != null && selected.contains(id);
        View badge = card.findViewById(R.id.selection_badge);
        badge.setVisibility(checked ? View.VISIBLE : View.GONE);
        card.setSelected(checked);
        androidx.core.view.ViewCompat.setStateDescription(card, checked ? list.getContext().getString(R.string.selection_selected) : null);
        float scale = checked ? .95f : 1f;
        card.animate().cancel();
        if (animate) card.animate().scaleX(scale).scaleY(scale).translationZ(checked ? dp(4) : 0).setDuration(160).start();
        else { card.setScaleX(scale); card.setScaleY(scale); card.setTranslationZ(checked ? dp(4) : 0); }
        item.setAlpha(dragging && checked ? .22f : 1f);
    }
    @Override public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
        gestures.onTouchEvent(e);
        if (armed && e.getActionMasked() == MotionEvent.ACTION_MOVE && Math.hypot(e.getX()-holdX, e.getY()-holdY) > ViewConfiguration.get(list.getContext()).getScaledTouchSlop()) {
            start(e); return true;
        }
        if (armed && (e.getActionMasked() == MotionEvent.ACTION_UP || e.getActionMasked() == MotionEvent.ACTION_CANCEL)) {
            armed = false; list.getParent().requestDisallowInterceptTouchEvent(false); return true;
        }
        return dragging;
    }
    @Override public void onTouchEvent(RecyclerView rv, MotionEvent e) {
        if (!dragging) return;
        fingerX = e.getX(); fingerY = e.getY();
        if (e.getActionMasked() == MotionEvent.ACTION_MOVE) { positionStack(); updateTarget(); }
        if (e.getActionMasked() == MotionEvent.ACTION_UP) {
            Set<String> ids = new LinkedHashSet<>(selected);
            String target = folderTarget; String before = reorderTarget; boolean after = reorderAfter; boolean trash = overTrash;
            cancel();
            if (trash) host.action("delete", ids, null);
            else if (target != null) host.action("move", ids, target);
            else if (before != null) host.action(after ? "reorder_after" : "reorder", ids, before);
        } else if (e.getActionMasked() == MotionEvent.ACTION_CANCEL || e.getActionMasked() == MotionEvent.ACTION_POINTER_DOWN) cancel();
    }
    private void start(MotionEvent e) {
        dragging = true; armed = false; fingerX = e.getX(); fingerY = e.getY();
        list.stopScroll();
        stack = new StackView();
        // Capture only visible selected cards, keeping bitmap memory bounded by the viewport.
        for (int i = 0; i < list.getChildCount(); i++) {
            View child = list.getChildAt(i); String id = host.idAt(list.getChildAdapterPosition(child));
            if (id == null || !selected.contains(id) || child.getWidth() == 0 || child.getHeight() == 0) continue;
            Bitmap bitmap = Bitmap.createBitmap(child.getWidth(), child.getHeight(), Bitmap.Config.ARGB_8888);
            child.draw(new Canvas(bitmap));
            child.getLocationInWindow(location); int x = location[0], y = location[1]; root.getLocationInWindow(location);
            Tile tile = new Tile(bitmap, x-location[0], y-location[1]);
            if (id.equals(anchor)) stack.tiles.add(tile); else stack.tiles.add(0, tile);
        }
        root.getOverlay().add(stack); stack.layout(0, 0, root.getWidth(), root.getHeight());
        positionStack();
        gather = ValueAnimator.ofFloat(0, 1); gather.setDuration(230); gather.setInterpolator(new DecelerateInterpolator());
        gather.addUpdateListener(a -> { if (stack != null) { stack.progress = (float)a.getAnimatedValue(); stack.invalidate(); } }); gather.start();
        GradientDrawable trash = new GradientDrawable();
        trash.setColor(list.getResources().getColor(R.color.app_accent_caution)); trash.setCornerRadius(dp(22));
        fab.setBackground(trash);
        ((ImageView) fab.getChildAt(0)).setImageResource(R.drawable.ic_delete);
        fab.setContentDescription(list.getContext().getString(R.string.selection_drop_delete));
        fab.animate().scaleX(1.15f).scaleY(1.15f).setDuration(180).start();
        changed(); updateTarget(); list.postOnAnimation(scroll);
    }
    private void positionStack() {
        list.getLocationInWindow(location); float x = location[0]+fingerX, y = location[1]+fingerY;
        root.getLocationInWindow(location);
        stack.x = x-location[0]; stack.y = y-location[1]; stack.invalidate();
    }
    private void updateTarget() {
        list.getLocationInWindow(location); float x = location[0]+fingerX, y = location[1]+fingerY;
        fab.getLocationInWindow(location);
        boolean hit = x >= location[0]-dp(20) && x <= location[0]+fab.getWidth()+dp(20) && y >= location[1]-dp(20) && y <= location[1]+fab.getHeight()+dp(20);
        if (hit != overTrash) {
            overTrash = hit; fab.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK);
            fab.animate().scaleX(hit ? 1.4f : 1.15f).scaleY(hit ? 1.4f : 1.15f).setDuration(130).start();
        }
        View child = fingerY < list.getPaddingTop() ? null : list.findChildViewUnder(fingerX, fingerY);
        int pos = child == null ? -1 : list.getChildAdapterPosition(child);
        boolean middle = child != null && fingerY > child.getTop() + child.getHeight() * .25f && fingerY < child.getBottom() - child.getHeight() * .25f;
        String next = !hit && middle && host.folderAt(pos) ? host.idAt(pos) : null;
        reorderAfter = child != null && fingerY > child.getTop() + child.getHeight() * .5f;
        reorderTarget = !hit && next == null ? host.idAt(pos) : null;
        if (selected.contains(reorderTarget)) reorderTarget = null;
        if (stack != null) { stack.insertion = null;
            if (reorderTarget != null && child != null) {
                child.getLocationInWindow(location); int left = location[0], top = location[1] + (reorderAfter ? child.getHeight() : 0); root.getLocationInWindow(location);
                stack.insertion = new RectF(left-location[0]+dp(8), top-location[1], left-location[0]+child.getWidth()-dp(8), top-location[1]+dp(3));
            }
            stack.invalidate();
        }
        if (selected.contains(next)) next = null;
        if (!Objects.equals(next, folderTarget)) {
            folderTarget = next;
            for (int i = 0; i < list.getChildCount(); i++) {
                View item = list.getChildAt(i); View card = item.findViewById(R.id.parent);
                if (card != null) { decorate(item, true); if (Objects.equals(folderTarget, host.idAt(list.getChildAdapterPosition(item))) && folderTarget != null) card.animate().scaleX(1.04f).scaleY(1.04f).setDuration(130).start(); }
            }
        }
    }
    void cancel() {
        armed = false; dragging = false; overTrash = false; folderTarget = null; reorderTarget = null;
        list.removeCallbacks(scroll); if (gather != null) gather.cancel();
        if (stack != null) {
            final StackView released = stack; stack = null;
            released.setPivotX(released.x); released.setPivotY(released.y);
            released.animate().alpha(0f).scaleX(.9f).scaleY(.9f).setDuration(160).withEndAction(() -> {
                root.getOverlay().remove(released);
                for (Tile tile : released.tiles) tile.bitmap.recycle();
            }).start();
        }
        fab.setBackground(fabBackground);
        list.getParent().requestDisallowInterceptTouchEvent(false);
        ((ImageView)fab.getChildAt(0)).setImageResource(R.drawable.ic_add);
        fab.setContentDescription(list.getContext().getString(R.string.add_item));
        fab.animate().scaleX(1).scaleY(1).setDuration(180).start();
        for (int i = 0; i < list.getChildCount(); i++) decorate(list.getChildAt(i), true);
        MotionEvent cancellation = MotionEvent.obtain(0, 0, MotionEvent.ACTION_CANCEL, 0, 0, 0);
        gestures.onTouchEvent(cancellation); cancellation.recycle();
    }
    private static final class Tile {
        final Bitmap bitmap; final float x, y;
        Tile(Bitmap bitmap, float x, float y) { this.bitmap = bitmap; this.x=x; this.y=y; }
    }
    private final class StackView extends View {
        final ArrayList<Tile> tiles = new ArrayList<>();
        final int countAtLift = selected.size();
        final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        float x, y, progress;
        RectF insertion;
        StackView() { super(list.getContext()); setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO); }
        @Override protected void onDraw(Canvas canvas) {
            if (insertion != null) { paint.setColor(getResources().getColor(R.color.app_accent)); canvas.drawRoundRect(insertion, dp(2), dp(2), paint); }
            for (int i=0; i<tiles.size(); i++) {
                Tile tile=tiles.get(i); float offset=dp(Math.min(tiles.size()-1-i, 3)*7);
                float tx=x-tile.bitmap.getWidth()/2f+offset, ty=y-tile.bitmap.getHeight()*.65f+offset;
                canvas.drawBitmap(tile.bitmap, tile.x+(tx-tile.x)*progress, tile.y+(ty-tile.y)*progress, paint);
            }
            paint.setColor(getResources().getColor(R.color.app_accent)); canvas.drawCircle(x+dp(42),y-dp(34),dp(19),paint);
            paint.setColor(Color.WHITE); paint.setTextAlign(Paint.Align.CENTER); paint.setTextSize(dp(15));
            canvas.drawText(String.valueOf(countAtLift),x+dp(42),y-dp(29),paint);
        }
    }
    private static final class TypedValueCompat {
        static void ripple(View v) { android.util.TypedValue value=new android.util.TypedValue(); v.getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground,value,true); v.setBackgroundResource(value.resourceId); }
    }
}
