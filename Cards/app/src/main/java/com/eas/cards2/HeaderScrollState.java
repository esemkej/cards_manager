package com.eas.cards2;

/** Ignore transient adapter geometry when deriving scroll-driven header state. */
final class HeaderScrollState {
    static float fromRows(float previous, Integer firstOffset, boolean canScrollUp,
                          int firstVisible, boolean animating, int distance) {
        if (firstOffset != null) {
            float p = Math.max(0, Math.min(1, firstOffset / (float) distance));
            return p * p * (3 - 2 * p);
        }
        if (!canScrollUp) return 0;
        return firstVisible > 0 && !animating ? 1 : previous;
    }
    static float duringNavigation(float start, float visibility, boolean running) {
        return running ? start * (1 - Math.max(0, Math.min(1, visibility))) : 0;
    }
}
