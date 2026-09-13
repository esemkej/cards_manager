package com.eas.cards2;

/** Strict validation of a complete EAN-13, including its check digit. */
final class Ean13 {
    static boolean isType(String type) {
        return "EAN_13".equals(type) || "EAN13".equals(type);
    }

    static boolean isValid(String code) {
        if (code == null || code.length() != 13) return false;
        int sum = 0;
        for (int i = 0; i < 13; i++) {
            char digit = code.charAt(i);
            if (digit < '0' || digit > '9') return false;
            sum += (digit - '0') * (i % 2 == 0 ? 1 : 3);
        }
        return sum % 10 == 0;
    }
}
