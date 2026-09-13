package com.eas.cards2;
public final class CardGridSizingTest {
    public static void main(String[] args) {
        int[][] cases = {{320, 1}, {360, 2}, {411, 2}, {600, 3}, {840, 4}, {1200, 5}};
        for (int[] c : cases) if (CardGridSizing.columns(c[0], 1, 0) != c[1]) throw new AssertionError(c[0]);
        if (CardGridSizing.columns(360, 1.5f, 0) != 1) throw new AssertionError("large text");
        if (CardGridSizing.columns(600, 2, 0) != 1) throw new AssertionError("accessibility text");
        if (CardGridSizing.columns(360, 1, 3) != 3) throw new AssertionError("manual override");
        if (CardGridSizing.columns(0, 1, 0) != 1) throw new AssertionError("unmeasured");
        System.out.println("Grid sizing: 10 checks passed");
    }
}
