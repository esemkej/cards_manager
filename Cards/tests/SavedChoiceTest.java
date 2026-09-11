package com.eas.cards2;

public final class SavedChoiceTest {
    public static void main(String[] args) {
        String[] names = {"system", "light", "dark"};
        int[] oldIds = {101, 102, 103};
        int[] newIds = {201, 202, 203};
        check(2, SavedChoice.index("dark", newIds, names));
        int migrated = SavedChoice.index(103, oldIds, names);
        check(2, SavedChoice.index(names[migrated], newIds, names));
        for (Object invalid : new Object[]{null, 103, -1, "deleted_option", true, 2.5}) {
            check(0, SavedChoice.index(invalid, newIds, names));
        }
        check(0, SavedChoice.index(null, new int[]{11, 12, 13}, new String[]{"name", "date", "uses"}));
        check(0, SavedChoice.index(null, new int[]{21, 22}, new String[]{"ascending", "descending"}));
        check(0, SavedChoice.index(null, new int[]{31, 32}, new String[]{"all", "favorites"}));
        check(1, SavedChoice.index("favorites", new int[]{41, 42}, new String[]{"all", "favorites"}));
        System.out.println("Saved-choice migration, defaults, invalid values, and changed-ID checks passed.");
    }

    private static void check(int expected, int actual) {
        if (expected != actual) throw new AssertionError("Expected " + expected + ", got " + actual);
    }
}
