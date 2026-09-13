package com.eas.cards2;

/** Parses RGB, RRGGBB or AARRGGBB with one optional leading hash. */
final class HexColor {
    static Integer parse(String text) {
        if (text == null) return null;
        String digits = text.startsWith("#") ? text.substring(1) : text;
        if (!digits.matches("[0-9a-fA-F]+")) return null;
        if (digits.length() == 3) {
            digits = "" + digits.charAt(0) + digits.charAt(0) + digits.charAt(1)
                    + digits.charAt(1) + digits.charAt(2) + digits.charAt(2);
        }
        if (digits.length() != 6 && digits.length() != 8) return null;
        long color = Long.parseLong(digits, 16);
        return (int) (digits.length() == 6 ? color | 0xFF000000L : color);
    }
}
