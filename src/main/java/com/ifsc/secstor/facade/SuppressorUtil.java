package com.ifsc.secstor.facade;

public final class SuppressorUtil {

    private SuppressorUtil() {
    }

    public static String suppress() {
        return "*";
    }

    public static String suppressWithLevel(final Object data, final Integer level) {
        if (level < 1) {
            return suppress();
        }

        if (level > data.toString().length()) {
            return suppress();
        }

        StringBuilder toReturn = new StringBuilder();

        for (int i = 0; i < data.toString().length(); i++) {
            if (i < level) {
                toReturn.append(data.toString().charAt(i));
            } else {
                toReturn.append("*");
            }
        }

        return String.valueOf(toReturn);
    }
}
