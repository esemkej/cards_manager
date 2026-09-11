package com.eas.cards2;

final class SavedChoice {
    private SavedChoice() {}

    static int index(Object value, int[] ids, String[] names) {
        for (int i = 0; i < names.length; i++) {
            if (names[i].equals(value) || (value instanceof Integer && ((Integer) value) == ids[i])) return i;
        }
        return 0;
    }
}
