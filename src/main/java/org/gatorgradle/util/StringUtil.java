package org.gatorgradle.util;

import org.gradle.api.logging.Logger;

import java.lang.StringBuilder;

public class StringUtil {
    public static final String RESET = "\u001B[0m";

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
        return repeat(' ', width);
    }

    /**
     * Generate the provided number of string repetitions.
     *
     * @param  str   the string to repeat
     * @param  times the number of strings to repeat
     * @return       a string of strings
     */
    public static String repeat(String str, int times) {
        StringBuilder spc = new StringBuilder(times * str.length());
        for (int i = 0; i < times; i++) {
            spc.append(str);
        }
        return spc.toString();
    }

    /**
     * Generate the provided number of char repetitions.
     *
     * @param  str   the char to repeat
     * @param  times the number of chars to repeats
     * @return       a string of chars
     */
    public static String repeat(char str, int times) {
        StringBuilder spc = new StringBuilder(times);
        for (int i = 0; i < times; i++) {
            spc.append(str);
        }
        return spc.toString();
    }

    /**
     * Print a string with a border around it.
     *
     * @param text      the text to print
     * @param textCol   the color to use for the text
     * @param borderCol the color to use for the border
     * @param log       the logger to use
     */
    public static void border(String text, String textCol, String borderCol, Logger log) {
        char upleft    = '\u250f'; // upper left corner
        char upright   = '\u2513'; // upper right corner
        char downleft  = '\u2517'; // lower left corner
        char downright = '\u251B'; // lower right corner
        char vert      = '\u2503'; // vertical line
        char horz      = '\u2501'; // horizontal line

        int textLen = text.length();
        text        = textCol + text + RESET;

        String line  = StringUtil.repeat(horz, textLen + 2);
        String above = borderCol + upleft + line + upright + RESET;
        String side  = borderCol + vert + RESET;
        String below = borderCol + downleft + line + downright + RESET;

        log.warn("\n\n\t{}\n\t{} {} {}\n\t{}", above, side, text, side, below);
    }
}
