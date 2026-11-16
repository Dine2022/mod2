package com.concertmod.util;

public final class ConcertColors {
    public static final String[] NAMES = {
            "white", "red", "orange", "yellow", "lime", "green", "cyan", "blue", "purple", "magenta", "pink"
    };
    public static final int[] VALUES = {
            0xFFFFFF, 0xFF2020, 0xFF8000, 0xFFE000, 0x80FF00, 0x00D040, 0x00E0FF, 0x2040FF, 0x8020FF, 0xFF00FF, 0xFF80C0
    };

    private ConcertColors() {
    }

    /** Next preset color after the given one (unknown colors start from white). */
    public static int next(int current) {
        for (int i = 0; i < VALUES.length; i++) {
            if (VALUES[i] == (current & 0xFFFFFF)) {
                return VALUES[(i + 1) % VALUES.length];
            }
        }
        return VALUES[0];
    }

    /** Accepts a preset name, "#RRGGBB", "0xRRGGBB" or "RRGGBB". */
    public static int parse(String s) throws NumberFormatException {
        String t = s.trim().toLowerCase();
        for (int i = 0; i < NAMES.length; i++) {
            if (NAMES[i].equals(t)) {
                return VALUES[i];
            }
        }
        if (t.startsWith("#")) {
            t = t.substring(1);
        } else if (t.startsWith("0x")) {
            t = t.substring(2);
        }
        if (t.length() != 6) {
            throw new NumberFormatException(s);
        }
        return Integer.parseInt(t, 16);
    }

    public static String hex(int rgb) {
        return String.format("#%06X", rgb & 0xFFFFFF);
    }
}
