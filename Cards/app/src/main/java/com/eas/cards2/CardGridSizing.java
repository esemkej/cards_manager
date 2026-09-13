package com.eas.cards2;

final class CardGridSizing {
    static int columns(float widthDp, float fontScale, int requested) {
        if (requested > 0) return Math.max(1, Math.min(5, requested));
        float comfortableWidth = 16 + 160 * Math.max(1, fontScale);
        return Math.max(1, Math.min(5, (int) (widthDp / comfortableWidth)));
    }
}
