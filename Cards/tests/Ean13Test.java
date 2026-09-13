package com.eas.cards2;
public final class Ean13Test {
    public static void main(String[] args) {
        String[] valid = {"4006381333931", "5901234123457", "0123456789012"};
        String[] invalid = {"", "400638133393", "40063813339311", "4006381333932",
                "400638133393A", " 4006381333931", "４００６３８１３３３９３１"};
        for (String code : valid) if (!Ean13.isValid(code)) throw new AssertionError(code);
        for (String code : invalid) if (Ean13.isValid(code)) throw new AssertionError(code);
        if (Ean13.isValid(null)) throw new AssertionError("null");
        System.out.println("EAN-13: 11 validation checks passed");
    }
}
