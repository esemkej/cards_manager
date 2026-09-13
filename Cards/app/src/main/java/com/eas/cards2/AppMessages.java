package com.eas.cards2;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

/** A single, accessible message at a time, placed in the currently visible window. */
final class AppMessages {
    private final Activity activity;
    private final ArrayList<WeakReference<Dialog>> dialogs = new ArrayList<>();
    private final android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());
    private Snackbar current;
    private CharSequence lastText;
    private View lastHost;

    AppMessages(Activity activity) { this.activity = activity; }

    void track(Dialog dialog) {
        dialogs.add(new WeakReference<>(dialog));
        // A message from the underlying window must not linger behind a new sheet.
        dismissCurrent();
        dialog.getWindow().getDecorView().addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override public void onViewAttachedToWindow(View view) {}
            @Override public void onViewDetachedFromWindow(View view) {
                if (lastHost != null && lastHost.getRootView() == view) dismissCurrent();
            }
        });
    }

    void show(@StringRes int message, boolean error) { show(activity.getText(message), error); }

    void show(CharSequence message, boolean error) {
        // Resolve the window after the current click/bind has opened or dismissed its dialog.
        handler.removeCallbacksAndMessages(null);
        handler.post(() -> showNow(message, error));
    }

    private void showNow(CharSequence message, boolean error) {
        if (activity.isFinishing() || activity.isDestroyed()) return;
        View host = activity.findViewById(R.id._coordinator);
        View anchor = activity.findViewById(R.id.fab);
        for (int i = dialogs.size() - 1; i >= 0; i--) {
            Dialog dialog = dialogs.get(i).get();
            if (dialog == null || !dialog.isShowing()) { dialogs.remove(i); continue; }
            host = dialog.findViewById(android.R.id.content);
            anchor = host.findViewById(R.id.save_btn);
            if (anchor == null) anchor = host.findViewById(R.id.positive_txt);
            if (anchor != null && anchor.getParent() instanceof View) anchor = (View) anchor.getParent();
            break;
        }
        if (host == null || !host.isAttachedToWindow()) return;
        if (current != null && current.isShownOrQueued() && host == lastHost && message.equals(lastText)) return;
        dismissCurrent();
        final Snackbar notice = Snackbar.make(host, message, error ? 6500 : 4500);
        lastHost = host; lastText = message; current = notice;
        if (anchor != null && anchor.isShown()) notice.setAnchorView(anchor);
        notice.setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_FADE);
        notice.setTextColor(ContextCompat.getColor(activity, R.color.app_text_dark));
        notice.setActionTextColor(ContextCompat.getColor(activity, R.color.app_accent));
        notice.setAction(R.string.close, view -> notice.dismiss());
        View panel = notice.getView();
        panel.setBackgroundTintList(null);
        panel.setBackground(Bg.build(panel, ContextCompat.getColor(activity, R.color.app_surface_var),
                null, null, 16, null, 1, ContextCompat.getColor(activity, R.color.app_stroke), null));
        panel.setElevation(dp(6));
        ViewGroup.MarginLayoutParams margins = (ViewGroup.MarginLayoutParams) panel.getLayoutParams();
        margins.leftMargin = dp(16); margins.rightMargin = dp(16); margins.bottomMargin = dp(12);
        panel.setLayoutParams(margins);
        TextView text = panel.findViewById(com.google.android.material.R.id.snackbar_text);
        if (activity instanceof MainActivity) {
            ((MainActivity) activity).applyCurrentTextScale(text);
            ((MainActivity) activity).applyCurrentTextScale(panel.findViewById(com.google.android.material.R.id.snackbar_action));
        }
        text.setMaxLines(Integer.MAX_VALUE); text.setEllipsize(null);
        Drawable icon = ContextCompat.getDrawable(activity, R.drawable.ic_info_outlined).mutate();
        icon.setTint(ContextCompat.getColor(activity, error ? R.color.app_accent_caution : R.color.app_accent));
        icon.setBounds(0, 0, dp(20), dp(20));
        text.setCompoundDrawablePadding(dp(12));
        text.setCompoundDrawablesRelative(icon, null, null, null);
        notice.addCallback(new Snackbar.Callback() {
            @Override public void onDismissed(Snackbar snackbar, int event) {
                if (current == snackbar) { current = null; lastHost = null; lastText = null; }
            }
        });
        notice.show();
    }

    void dismiss() {
        handler.removeCallbacksAndMessages(null);
        dismissCurrent();
    }
    private void dismissCurrent() {
        if (current != null) current.dismiss();
        current = null; lastHost = null; lastText = null;
    }
    private int dp(int value) { return Math.round(value * activity.getResources().getDisplayMetrics().density); }
}
