package com.eas.cards2;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import android.util.TypedValue;
import android.view.View;

public final class Bg {

    private Bg() {}

    // ----- Public API -----

    /** Set background with solid/gradient + optional stroke + optional ripple. */
    public static void apply(View v,
                             Integer solidColor,          // nullable: if null and gradientColors!=null -> gradient
                             int[] gradientColors,        // nullable
                             GradientDrawable.Orientation orientation, // can be null (default TOP_BOTTOM)
                             float cornerDp,              // use -1 to indicate "use cornerRadiiDp instead"
                             float[] cornerRadiiDp,       // nullable, length=8 in dp, overrides cornerDp if not null
                             float strokeDp,
                             Integer strokeColor,         // nullable (treated as transparent)
                             Integer rippleColor          // nullable => no ripple wrapper
    ) {
        Drawable d = build(
                v,
                solidColor,
                gradientColors,
                orientation,
                cornerDp,
                cornerRadiiDp,
                strokeDp,
                strokeColor,
                rippleColor
        );
        v.setBackground(d);
    }

    /** Build drawable only (useful if you need to cache per-item). */
    public static Drawable build(View v,
                                 Integer solidColor,
                                 int[] gradientColors,
                                 GradientDrawable.Orientation orientation,
                                 float cornerDp,
                                 float[] cornerRadiiDp,
                                 float strokeDp,
                                 Integer strokeColor,
                                 Integer rippleColor) {

        GradientDrawable content = new GradientDrawable();

        // Fill: solid or gradient
        if (gradientColors != null && gradientColors.length >= 2) {
            content.setColors(gradientColors);
            content.setOrientation(orientation != null ? orientation : GradientDrawable.Orientation.TOP_BOTTOM);
        } else {
            content.setColor(solidColor != null ? solidColor : Color.TRANSPARENT);
        }

        // Corners
        if (cornerRadiiDp != null && cornerRadiiDp.length == 4) {
            content.setCornerRadii(radii4dpTo8px(v, cornerRadiiDp));
        } else if (cornerDp >= 0f) {
            content.setCornerRadius(dpToPx(v, cornerDp));
        }

        // Stroke (0dp or transparent is fine)
        int strokePx = Math.max(0, Math.round(dpToPx(v, strokeDp)));
        int sColor = (strokeColor != null) ? strokeColor : Color.TRANSPARENT;
        if (strokePx > 0) {
            content.setStroke(strokePx, sColor);
        }

        // Optional ripple wrapper
        if (rippleColor != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ColorStateList ripple = ColorStateList.valueOf(rippleColor);
            // Mask null is okay; ripple will use content outline, but mask improves shape fidelity sometimes.
            return new RippleDrawable(ripple, content, null);
        }

        return content;
    }

    // ----- dp helpers -----

    private static float dpToPx(View v, float dp) {
        return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                v.getResources().getDisplayMetrics()
        );
    }
    
    private static float[] radii4dpTo8px(View v, float[] radii4dp) {
        if (radii4dp == null || radii4dp.length != 4) return null;

        float density = v.getResources().getDisplayMetrics().density;

        float tl = radii4dp[0] * density;
        float tr = radii4dp[1] * density;
        float br = radii4dp[2] * density;
        float bl = radii4dp[3] * density;

        return new float[]{
                tl, tl,   // top-left
                tr, tr,   // top-right
                br, br,   // bottom-right
                bl, bl    // bottom-left
        };
    }

    private static float[] dpArrayToPx(View v, float[] dpRadii) {
        float[] px = new float[dpRadii.length];
        for (int i = 0; i < dpRadii.length; i++) {
            px[i] = dpToPx(v, dpRadii[i]);
        }
        return px;
    }
}