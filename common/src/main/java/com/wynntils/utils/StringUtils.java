/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;

public final class StringUtils {
    private static final String[] suffixes = {"", "k", "m", "b", "t"}; // kilo, million, billion, trillion (short scale)
    private static final DecimalFormat fractionalFormat = new DecimalFormat("#.#");

    /**
     * Converts a delimited list into a {@link java.util.List} of strings
     *
     * <p>e.g. "1, 2, 3,, 4," for delimiter "," -> returns a list of "1", "2", "3", "4"
     */
    public static List<String> parseStringToList(String input, String delimiter) {
        List<String> result = new ArrayList<>();

        for (String element : input.split(Pattern.quote(delimiter))) {
            result.add(element.strip());
        }

        return result;
    }

    public static List<String> parseStringToList(String input) {
        return parseStringToList(input, ",");
    }

    public static String capitalizeFirst(String input) {
        if (input.isEmpty()) return "";
        return Character.toUpperCase(input.charAt(0)) + input.substring(1);
    }

    public static String uncapitalizeFirst(String input) {
        if (input.isEmpty()) return "";
        return Character.toLowerCase(input.charAt(0)) + input.substring(1);
    }

    public static String capitalized(String input) {
        if (input.isEmpty()) return "";
        return Character.toUpperCase(input.charAt(0)) + input.substring(1).toLowerCase(Locale.ROOT);
    }

    /**
     * Format an integer to have SI suffixes, if it is sufficiently large
     */
    public static String integerToShortString(int count) {
        if (count < 1000) return Integer.toString(count);
        int exp = (int) (Math.log(count) / Math.log(1000));
        DecimalFormat format = new DecimalFormat("0.#");
        String value = format.format(count / Math.pow(1000, exp));
        return String.format("%s%c", value, "kMBTPE".charAt(exp - 1));
    }

    public static String encodeUrl(String url) {
        return URLEncoder.encode(url, StandardCharsets.UTF_8);
    }

    public static String formatAmount(double value) {
        if (value < 0.75) return "0";

        int suffix = 0;
        while (suffix < suffixes.length && value >= 750) {
            value /= 1000;
            ++suffix;
        }

        return fractionalFormat.format(value) + suffixes[suffix];
    }

    /**
     * Matches a string to a specific search term
     */
    public static boolean partialMatch(String toMatchStr, String searchTermStr) {
        String searchTerm = searchTermStr.toLowerCase(Locale.ROOT);
        String toMatch = toMatchStr.toLowerCase(Locale.ROOT);

        int firstIndexToMatch = 0;

        for (int i = 0; i < searchTerm.length(); i++) {
            char currentChar = searchTerm.charAt(i);

            int indexOfFirstMatch = toMatch.indexOf(currentChar, firstIndexToMatch);

            if (indexOfFirstMatch == -1) {
                return false;
            }

            firstIndexToMatch = indexOfFirstMatch + 1;
        }

        return true;
    }

    /** This is slightly less generous in allowing a match than partialMatch,
     * but not as strict as a contains(). It will require all sequences of characters
     * in the searchTerm to be consecutive in the toMatch string as well, until
     * a whitespace is encountered. E.g. "t vo" would match "The void", but "tvo" would not.
     */
    public static boolean initialMatch(String toMatch, String searchTerm) {
        String lookAt = toMatch.toLowerCase(Locale.ROOT);
        String searchFor = searchTerm.strip().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
        // Turn all spaces into .*, including start and end
        String regex = (" " + searchFor + " ").replace(" ", ".*");
        return lookAt.matches(regex);
    }

    // Convenience wrappers due to class name collision
    public static boolean containsIgnoreCase(String str, String searchStr) {
        return org.apache.commons.lang3.StringUtils.containsIgnoreCase(str, searchStr);
    }

    public static String substringBeforeLast(String str, String separator) {
        return org.apache.commons.lang3.StringUtils.substringBeforeLast(str, separator);
    }

    public static String toSignedString(int value) {
        if (value >= 0) {
            return "+" + value;
        } else {
            return Integer.toString(value);
        }
    }

    public static String convertMarkdownToColorCode(String input) {
        return ChatFormatting.RESET + input.replaceFirst("#+\\s+", String.valueOf(ChatFormatting.BOLD));
    }

    /**
     * Returns an arrow (like ⬆) based on the given angle.
     * The angle is expected to be in degrees, with 0 being north, 90 being west, 180 being south, and 270 being east.
     * Throws IllegalArgumentException if the angle is not in the range 0-360 inclusive.
     * @param angle The angle in degrees
     * @return The arrow string
     */
    public static String angleToDirectionArrowString(double angle) {
        if (angle > 360 || angle < 0) {
            throw new IllegalArgumentException("Angle must be in the range 0-360 inclusive");
        }

        if (angle > 22.5 && angle <= 67.5) {
            return "⬉";
        } else if (angle > 67.5 && angle <= 112.5) {
            return "⬅";
        } else if (angle > 112.5 && angle <= 157.5) {
            return "⬋";
        } else if (angle > 157.5 && angle <= 202.5) {
            return "⬇";
        } else if (angle > 202.5 && angle <= 247.5) {
            return "⬊";
        } else if (angle > 247.5 && angle <= 292.5) {
            return "⮕";
        } else if (angle > 292.5 && angle <= 337.5) {
            return "⬈";
        } else { // 337.5 < angle <= 22.5
            return "⬆";
        }
    }
}
