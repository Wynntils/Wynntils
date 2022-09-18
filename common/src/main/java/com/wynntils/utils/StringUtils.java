/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils;

import com.wynntils.mc.utils.McUtils;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import net.minecraft.client.gui.Font;

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

    public static String[] wrapTextBySize(String s, int maxPixels) {
        Font font = McUtils.mc().font;
        int spaceSize = font.width(" ");

        String[] stringArray = s.split(" ");
        StringBuilder result = new StringBuilder();
        int length = 0;

        for (String string : stringArray) {
            String[] lines = string.split("\\\\n", -1);
            for (int i = 0; i < lines.length; i++) {
                String line = lines[i];
                if (i > 0 || length + font.width(line) >= maxPixels) {
                    result.append('\n');
                    length = 0;
                }
                if (!line.isEmpty()) {
                    result.append(line).append(' ');
                    length += font.width(line) + spaceSize;
                }
            }
        }

        return result.toString().split("\n");
    }

    /**
     * Creates a new pattern, but replaces all occurrences of '§' in the regex with an expression for detecting any color code
     * @param regex - the expression to be modified and compiled
     * @return a Pattern with the modified regex
     */
    public static Pattern compileCCRegex(String regex) {
        return Pattern.compile(regex.replace("§", "(?:§[0-9a-fklmnor])*"));
    }

    public static String encodeUrl(String url) {
        try {
            return URLEncoder.encode(url, "UTF-8");
        } catch (UnsupportedEncodingException ignored) {
            // will not happen since UTF-8 is part of core charsets
            return null;
        }
    }

    public static String formatAmount(double value) {
        if (value < 0.75) return null;

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
    public static boolean partialMatch(String toMatch, String searchTerm) {
        searchTerm = searchTerm.toLowerCase(Locale.ROOT);
        toMatch = toMatch.toLowerCase(Locale.ROOT);

        int lastMatchIndex = 0;

        for (int i = 0; i < searchTerm.length(); i++) {
            char currentChar = searchTerm.charAt(i);

            int indexOfFirstMatch = toMatch.indexOf(currentChar, lastMatchIndex);

            if (indexOfFirstMatch == -1) {
                return false;
            }

            lastMatchIndex = indexOfFirstMatch;
        }

        return true;
    }

    public static String getMaxFittingText(String text, float maxTextWidth, Font font) {
        String renderedText;
        if (font.width(text) < maxTextWidth) {
            return text;
        } else {
            // This case, the input is too long, only render text that fits, and is closest to cursor
            StringBuilder builder = new StringBuilder();

            int suffixWidth = font.width("...");
            int stringPosition = 0;

            while (font.width(builder.toString()) < maxTextWidth - suffixWidth && stringPosition < text.length()) {
                builder.append(text.charAt(stringPosition));

                stringPosition++;
            }

            builder.append("...");
            renderedText = builder.toString();
        }
        return renderedText;
    }
}
