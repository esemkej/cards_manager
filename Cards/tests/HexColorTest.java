package com.eas.cards2;
public final class HexColorTest {
    public static void main(String[] args) {
        String[] valid = {"#FFFFFF", "ffffff", "#aBc", "abc", "#80112233", "000000"};
        int[] expected = {0xFFFFFFFF, 0xFFFFFFFF, 0xFFAABBCC, 0xFFAABBCC, 0x80112233, 0xFF000000};
        for (int i = 0; i < valid.length; i++) {
            if (HexColor.parse(valid[i]) == null || HexColor.parse(valid[i]) != expected[i]) throw new AssertionError(valid[i]);
        }
        String[] invalid = {"", "#", "##FFFFFF", "ff#fff", "GGGGGG", "FFFF", "FFFFFFFFF"};
        for (String text : invalid) if (HexColor.parse(text) != null) throw new AssertionError(text);
        System.out.println("Hex color: 13 checks passed");
    }
}
