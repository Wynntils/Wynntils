/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils;

import com.wynntils.mc.objects.CustomColor;
import com.wynntils.mc.utils.McUtils;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.CRC32;
import net.minecraft.client.gui.Font;

public final class StringUtils {
    private static final String[] suffixes = {"", "k", "m", "b", "t"}; // kilo, million, billion, trillion (short scale)
    private static final DecimalFormat fractionalFormat = new DecimalFormat("#.#");

    private static final Pattern STX_PATTERN = Pattern.compile("(\\.?\\d+\\.?\\d*)\\s*(s|stx|stacks)");
    private static final Pattern LE_PATTERN = Pattern.compile("(\\.?\\d+\\.?\\d*)\\s*(l|le)");
    private static final Pattern EB_PATTERN = Pattern.compile("(\\.?\\d+\\.?\\d*)\\s*(b|eb)");
    private static final Pattern K_PATTERN = Pattern.compile("(\\.?\\d+\\.?\\d*)\\s*(k|thousand)");
    private static final Pattern M_PATTERN = Pattern.compile("(\\.?\\d+\\.?\\d*)\\s*(m|million)");
    private static final Pattern E_PATTERN = Pattern.compile("(\\d+)($|\\s|\\s*e|\\s*em)(?![^\\d\\s-])");
    private static final Pattern RAW_PRICE_PATTERN = Pattern.compile("\\d+");

    private static final int stackSize = 64;
    private static final double taxAmount = 1.05;

    private static final Map<String, CustomColor> registeredColors = new HashMap<>();

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
    public static boolean partialMatch(String toMatch, String searchTerm) {
        searchTerm = searchTerm.toLowerCase(Locale.ROOT);
        toMatch = toMatch.toLowerCase(Locale.ROOT);

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

    public static String convertEmeraldPrice(String input) {
        Matcher rawMatcher = RAW_PRICE_PATTERN.matcher(input);
        if (rawMatcher.matches()) return "";

        input = input.toLowerCase(Locale.ROOT);
        long emeralds = 0;

        try {
            // stx
            Matcher stxMatcher = STX_PATTERN.matcher(input);
            while (stxMatcher.find()) {
                emeralds += (long) (Double.parseDouble(stxMatcher.group(1)) * stackSize * stackSize * stackSize);
            }

            // le
            Matcher leMatcher = LE_PATTERN.matcher(input);
            while (leMatcher.find()) {
                emeralds += (long) (Double.parseDouble(leMatcher.group(1)) * stackSize * stackSize);
            }

            // eb
            Matcher ebMatcher = EB_PATTERN.matcher(input);
            while (ebMatcher.find()) {
                emeralds += (long) (Double.parseDouble(ebMatcher.group(1)) * stackSize);
            }
            
            Matcher kMatcher = K_PATTERN.matcher(input);
            while (kMatcher.find()) {
                emeralds += (long) (Double.parseDouble(kMatcher.group(1)) * 1000);
            }

            // eb
            Matcher mMatcher = M_PATTERN.matcher(input);
            while (mMatcher.find()) {
                emeralds += (long) (Double.parseDouble(mMatcher.group(1)) * 1000000);
            }

            // standard numbers/emeralds
            Matcher eMatcher = E_PATTERN.matcher(input);
            while (eMatcher.find()) {
                emeralds += Long.parseLong(eMatcher.group(1));
            }

            // account for tax if flagged
            if (input.contains("-t")) {
                emeralds = Math.round(emeralds / taxAmount);
            }
        } catch (NumberFormatException e) {
            return "";
        }

        return (emeralds > 0) ? String.valueOf(emeralds) : "";
    }

    public static boolean containsIgnoreCase(String string, String string2) {
        return org.apache.commons.lang3.StringUtils.containsIgnoreCase(string, string2);
    }

    /**
     * Generates a Color based in the input string
     * The color will be always the same if the string is the same
     *
     * @param input the input stream
     * @return the color
     */
    public static CustomColor colorFromString(String input) {
        if (registeredColors.containsKey(input)) return registeredColors.get(input);

        CRC32 crc32 = new CRC32();
        crc32.update(input.getBytes(StandardCharsets.UTF_8));

        CustomColor color =
                CustomColor.fromInt(((int) crc32.getValue()) & 0xFFFFFF).withAlpha(255);
        registeredColors.put(input, color);

        return color;
    }

    public static String toSignedString(int value) {
        if (value >= 0) {
            return "+" + value;
        } else {
            return Integer.toString(value);
        }
    }
}
