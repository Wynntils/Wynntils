/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils;

import com.google.common.net.UrlEscapers;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import org.apache.commons.lang3.time.DateFormatUtils;

public final class StringUtils {
    private static final String[] SUFFIXES = {"", "k", "m", "b", "t"}; // kilo, million, billion, trillion (short scale)
    private static final long[] SUFFIX_MULTIPLIERS = {1L, 1_000L, 1_000_000L, 1_000_000_000L, 1_000_000_000_000L};
    private static final DecimalFormat FRACTIONAL_FORMAT = new DecimalFormat("#.#");
    private static final Pattern NONLATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");
    private static final Pattern WHITESPACES = Pattern.compile("\\s+");
    private static final Pattern LINEBREAK = Pattern.compile("\n");
    private static final Pattern MARKDOWN_HEADER_PATTERN = Pattern.compile("#+\\s+");

    /**
     * Converts a delimited list into a {@link java.util.List} of strings
     *
     * <p>e.g. "1, 2, 3, 4," for delimiter "," -> returns a list of "1", "2", "3", "4"
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
    public static String integerToShortString(long count) {
        if (count < 1000) return Long.toString(count);
        int exp = (int) (Math.log(count) / Math.log(1000));
        DecimalFormat format = new DecimalFormat("0.#");
        String value = format.format(count / Math.pow(1000, exp));
        return String.format("%s%c", value, "kMBTPE".charAt(exp - 1));
    }

    /**
     * Parse a string, possible ending with an SI suffix, as an integer.
     */
    public static long parseSuffixedInteger(String numStr) throws NumberFormatException {
        numStr = numStr.toLowerCase(Locale.ROOT);
        for (int i = 1; i < SUFFIXES.length; i++) {
            if (numStr.endsWith(SUFFIXES[i])) {
                double baseValue = Double.parseDouble(numStr.substring(0, numStr.length() - 1));
                return (long) Math.ceil(baseValue * SUFFIX_MULTIPLIERS[i]);
            }
        }
        return Long.parseLong(numStr);
    }

    public static String formatDuration(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long remainingSeconds = seconds % 60;

        if (hours > 0) {
            return String.format("%dh %02dm %02ds", hours, minutes, remainingSeconds);
        } else if (minutes > 0) {
            return String.format("%dm %02ds", minutes, remainingSeconds);
        } else {
            return String.format("%ds", remainingSeconds);
        }
    }

    public static String encodeUrl(String url) {
        return UrlEscapers.urlPathSegmentEscaper().escape(url);
    }

    public static String createSlug(String input) {
        // based on https://stackoverflow.com/a/1657250
        String nowhitespace = WHITESPACE.matcher(input).replaceAll("-");
        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);
        String slug = NONLATIN.matcher(normalized).replaceAll("");
        return slug.toLowerCase(Locale.ENGLISH);
    }

    public static String formatAmount(double value) {
        if (value < 0.75) return "0";

        int suffix = 0;
        while (suffix < SUFFIXES.length && value >= 750) {
            value /= 1000;
            ++suffix;
        }

        return FRACTIONAL_FORMAT.format(value) + SUFFIXES[suffix];
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
        String searchFor =
                WHITESPACES.matcher(searchTerm.strip().toLowerCase(Locale.ROOT)).replaceAll(" ");
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
        return ChatFormatting.RESET
                + MARKDOWN_HEADER_PATTERN.matcher(input).replaceFirst(String.valueOf(ChatFormatting.BOLD));
    }

    public static ByteBuffer decodeBase64(String base64) {
        if (base64 == null) return null;

        return Base64.getDecoder()
                .decode(ByteBuffer.wrap(LINEBREAK.matcher(base64).replaceAll("").getBytes(StandardCharsets.UTF_8)));
    }

    /**
     * Converts a float to a string, but if the float is an integer, it will return the integer as a string
     * @param value the float to convert
     * @return the float as a string
     */
    public static String floatToSimpleString(float value) {
        if (value == (int) value) {
            return Integer.toString((int) value);
        } else {
            return String.format("%.2f", value);
        }
    }

    public static String formatDateTime(long timeMillis) {
        // Format: 2023-01-01 12:00
        return DateFormatUtils.format(timeMillis, "yyyy-MM-dd HH:mm");
    }

    /**
     * Converts a timestamp to a relative time string, e.g. "2 seconds ago", "in 5 minutes"
     * or "now" if the timestamp is within 1 second of the current time.
     */
    public static String getRelativeTimeString(long timestamp) {
        long diffInMillis = timestamp - System.currentTimeMillis();
        if (diffInMillis > -1000 && diffInMillis < 1000) {
            return I18n.get("utils.wynntils.time.now");
        }

        int diffInSeconds = (int) (diffInMillis / 1000);
        int seconds = Math.abs(diffInSeconds);
        int minutes = seconds / 60;
        int hours = minutes / 60;
        int days = hours / 24;

        String timeStr;
        if (seconds < 60) {
            if (seconds == 1) {
                timeStr = I18n.get("utils.wynntils.time.second", seconds);
            } else {
                timeStr = I18n.get("utils.wynntils.time.seconds", seconds);
            }
        } else if (minutes < 60) {
            if (minutes == 1) {
                timeStr = I18n.get("utils.wynntils.time.minute", minutes);
            } else {
                timeStr = I18n.get("utils.wynntils.time.minutes", minutes);
            }
        } else if (hours < 24) {
            if (hours == 1) {
                timeStr = I18n.get("utils.wynntils.time.hour", hours);
            } else {
                timeStr = I18n.get("utils.wynntils.time.hours", hours);
            }
        } else {
            if (days == 1) {
                timeStr = I18n.get("utils.wynntils.time.day", days);
            } else {
                timeStr = I18n.get("utils.wynntils.time.days", days);
            }
        }

        if (diffInSeconds < 0) {
            return I18n.get("utils.wynntils.time.past", timeStr);
        } else {
            return I18n.get("utils.wynntils.time.future", timeStr);
        }
    }

    public static String getAbbreviation(String input) {
        if (input == null || input.isBlank()) return "";

        String[] words = WHITESPACES.split(input.trim());
        StringBuilder abbreviation = new StringBuilder();

        if (words.length == 1) {
            String word = words[0];
            abbreviation.append(word, 0, Math.min(3, word.length()));
        } else {
            for (int i = 0; i < Math.min(3, words.length); i++) {
                String word = words[i];
                if (!word.isEmpty()) {
                    abbreviation.append(word.charAt(0));
                }
            }
        }

        return abbreviation.toString();
    }
}
