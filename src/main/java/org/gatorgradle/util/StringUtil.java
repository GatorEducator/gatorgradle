package org.gatorgradle.util;

import java.lang.StringBuilder;

public class StringUtil {
    /**
     * Clamps a string to the provided width, ending with \u2026.
     *
     * @param  str   the string to clamp
     * @param  width the width to clamp it to
     * @return       the clamped string
     */
    public static String clamp(String str, int width) {
        if (str.length() > width) {
            return str.substring(0, width - 1) + "…";
        }
        return str;
    }

    /**
     * Makes provided string be the provided length.
     *
     * @param  str   the string to clamp/pad
     * @param  width the desired length
     * @return       the modified string
     */
    public static String width(String str, int width) {
        if (str.length() > width) {
            return clamp(str, width);
        } else if (str.length() < width) {
            return str + spaces(width - str.length());
        }
        return str;
    }

    /**
     * Generate the provided number of spaces.
     *
     * @param  width the number of spaces to generate
     * @return       a string of spaces
     */
    public static String spaces(int width) {
        StringBuilder spc = new StringBuilder(width);
        for (int i = 0; i < width; i++) {
            spc.append(' ');
        }
        return spc.toString();
    }
}
