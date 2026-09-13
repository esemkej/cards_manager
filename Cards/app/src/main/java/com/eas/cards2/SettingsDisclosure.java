package com.eas.cards2;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import androidx.core.view.ViewCompat;

/** Animate only this section's measured height, leaving sheet drag/scroll state alone. */
final class SettingsDisclosure {
    private ValueAnimator animator;
    private boolean expanded;
    SettingsDisclosure(View header, View body, View arrow) {
        header.setOnClickListener(v -> {
            expanded = !expanded;
            if (animator != null) { animator.removeAllListeners(); animator.cancel(); }
            int start = body.getVisibility() == View.GONE ? 0 : body.getHeight();
            body.setVisibility(View.VISIBLE);
            body.measure(View.MeasureSpec.makeMeasureSpec(((View) body.getParent()).getWidth()
                    - ((View) body.getParent()).getPaddingLeft() - ((View) body.getParent()).getPaddingRight(), View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            int end = expanded ? body.getMeasuredHeight() : 0;
            ViewGroup.LayoutParams lp = body.getLayoutParams(); lp.height = start; body.setLayoutParams(lp);
            float fromRotation = arrow.getRotation();
            animator = ValueAnimator.ofInt(start, end);
            animator.setDuration(240); animator.setInterpolator(new DecelerateInterpolator());
            animator.addUpdateListener(a -> {
                lp.height = (int) a.getAnimatedValue(); body.setLayoutParams(lp);
                arrow.setRotation(fromRotation + ((expanded ? 180 : 0) - fromRotation) * a.getAnimatedFraction());
            });
            animator.addListener(new AnimatorListenerAdapter() {
                @Override public void onAnimationEnd(Animator a) {
                    body.setVisibility(expanded ? View.VISIBLE : View.GONE);
                    lp.height = ViewGroup.LayoutParams.WRAP_CONTENT; body.setLayoutParams(lp);
                }
            });
            ViewCompat.setStateDescription(header, header.getContext().getString(expanded ? R.string.expanded : R.string.collapsed));
            animator.start();
        });
        ViewCompat.setStateDescription(header, header.getContext().getString(R.string.collapsed));
        header.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            public void onViewAttachedToWindow(View v) { }
            public void onViewDetachedFromWindow(View v) {
                if (animator != null) { animator.removeAllListeners(); animator.cancel(); }
            }
        });
    }
}
