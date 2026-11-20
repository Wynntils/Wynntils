/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.wynn;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Handlers;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.mc.type.Location;
import com.wynntils.utils.type.CardinalDirection;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class LocationUtils {
    // Regex used to parse a coordinate
    // prefix: `([xyz] *[=:]? *)`, matches "x", "x:", "x=", with optional whitespace
    // num: `([+-]?\d{1,6})(?:\.\d+)?`, matches a number with 1-6 digit whole part and discarded decimal part,
    //   allows preceding but not trailing "."
    // sep: `(?: +| *[,/] *|(?=[xyz]))`, matches a separator, which can be either:
    // - 1+ whitespace characters
    // - ',' or '/' with optional whitespace
    // - nothing, as long as the next number begins with a prefix (next char is in [xyz])
    // Complete pattern is `{prefix}?{num}{sep}{prefix}?{num}({sep}{prefix}?{num})?`
    // Prefixes are groups p1-p3, numbers are groups n1-n3, separator is non-capturing, groups p3 and n3 are optional
    private static final Pattern COORDINATE_PATTERN = Pattern.compile(
            "(?<p1>[xyz]\\s*[=:]?\\s*)?(?<n1>[+-]?\\d{1,6})(?:\\.\\d+)?(?:\\s+|\\s*[,/]\\s*|(?=[xyz]))(?<p2>[xyz]\\s*[=:]?\\s*)?(?<n2>[+-]?\\d{1,6})(?:\\.\\d+)?(?:(?:\\s+|\\s*[,/]\\s*|(?=[xyz]))(?<p3>[xyz]\\s*[=:]?\\s*)?(?<n3>[+-]?\\d{1,6})(?:\\.\\d+)?)?",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern STRICT_COORDINATE_PATTERN = Pattern.compile(
            "(?:^|\\s|\\[)\\s*([-+]?\\d{1,6}(?:[\\s,]{0,2}[-+]?\\d{1,3}(?:[\\s,]{0,2}[-+]?\\d{1,6})?)?)\\s*(?:\\]|\\s+|$)");

    private static final String LEFT_BRACKETS = "([{<";
    private static final String RIGHT_BRACKETS = ")]}>";

    private static final int MAX_XZ = 999_999;
    private static final int MAX_Y = 999;

    /**
     * Parses a Location from a string. The string must contain two or three <strong>numbers</strong>,
     * either representing an XYZ coordinate or an XZ coordinate with Y defaulting to 0.
     * Numbers are separated by <strong>separators</strong>, and each number can optionally start with a <strong>prefix</strong>.
     * Optionally, the input can be surrounded by one matching pair of <strong>brackets</strong>.
     * <h4>Number</h4>
     * A decimal number that can be parsed by {@link Integer#parseInt(String)}.
     * Leading periods (".9") are allowed, but trailing periods ("9.") are not. A leading plus ("+") sign is allowed.
     * <h4>Prefix</h4>
     * One of "x=", "x:", or "x" for any of X, Y, or Z, case-insensitive. Whitespace is ignored.
     * Prefixes may be used to specify numbers in any order, such as "y=1, z=2, x=7".
     * However, prefixes must be used for all numbers, or none at all.
     * If no prefixes are used, then Location coordinates are specified in XYZ or XZ order.
     * <h4>Separator</h4>
     * Numbers can be separated by whitespace, commas, or forward slashes. Extra whitespace is ignored.
     * If all numbers have a prefix, then a separator is not needed, such as in "x5y2z-3".
     * <h4>Brackets</h4>
     * The input string may start and end with one pair of matching brackets: "()", "[]", "{}", or "<>".
     * @param locString input string
     * @return parsed location, or empty if the string could not be parsed
     */
    public static Optional<Location> parseFromString(String locString) {
        return parseFromStringInner(locString).filter(LocationUtils::isInRange);
    }

    public static Matcher strictCoordinateMatcher(String str) {
        return STRICT_COORDINATE_PATTERN.matcher(str);
    }

    public static void shareLocation(String target) {
        String locationString =
                "My location is at " + new Location(McUtils.player().blockPosition());

        LocationUtils.sendShareMessage(target, locationString);
    }

    public static void shareCompass(String target, Location compass) {
        String locationString = "My compass is at " + compass;

        LocationUtils.sendShareMessage(target, locationString);
    }

    public static CardinalDirection getCardinalDirection(float yawDegrees) {
        yawDegrees = ((yawDegrees + 180f) % 360f + 360f) % 360f - 180f;
        int index = Math.floorMod((int) Math.floor((yawDegrees + 202.5f) / 45f), 8);
        return CardinalDirection.values()[index];
    }

    private static Optional<Location> parseFromStringInner(String locString) {
        // Shortest possible input is "1,2", reject shorter strings
        if (locString.length() < 3) {
            return Optional.empty();
        }

        // Regex doesn't take into account brackets, any extra chars will cause match to fail
        String insideBrackets = LocationUtils.trimBrackets(locString);
        if (insideBrackets == null) {
            return Optional.empty();
        }
        String trimmed = insideBrackets.trim(); // This trim is necessary

        // Entire trimmed string must match to prevent "a3, 4, 5" from parsing
        Matcher matcher = COORDINATE_PATTERN.matcher(trimmed);
        if (!matcher.matches()) {
            return Optional.empty();
        }
        int coordinateLength = matcher.group("n3") == null ? 2 : 3;

        // Case 1: no prefixes used (ex: "73, 4, -5")
        String prefix1 = matcher.group("p1");
        String prefix2 = matcher.group("p2");
        String prefix3 = matcher.group("p3");
        if (prefix1 == null && prefix2 == null && prefix3 == null) {
            String numStr1 = matcher.group("n1");
            String numStr2 = matcher.group("n2");
            if (coordinateLength == 3) {
                String numStr3 = matcher.group("n3");
                return Optional.of(LocationUtils.parseFromStrings(numStr1, numStr2, numStr3));
            } else {
                return Optional.of(LocationUtils.parseFromStrings(numStr1, null, numStr2));
            }
        }

        // Case 2: all prefixes used (ex: "x=73, y=4, z=-5")
        // If prefixes only used on some numbers, input is ambiguous and rejected
        // X, Y, and Z can be processed in any order
        String xStr = null;
        String yStr = null;
        String zStr = null;
        for (int i = 0; i < coordinateLength; i++) {
            String prefix = matcher.group("p" + (i + 1));
            if (prefix == null) {
                return Optional.empty();
            }
            String numStr = matcher.group("n" + (i + 1));
            // [xyz] is always the first character matched in the prefix group
            char maybeXyz = Character.toLowerCase(prefix.charAt(0));
            switch (maybeXyz) {
                case 'x' -> {
                    if (xStr != null) {
                        return Optional.empty();
                    }
                    xStr = numStr;
                }
                case 'y' -> {
                    if (yStr != null) {
                        return Optional.empty();
                    }
                    yStr = numStr;
                }
                case 'z' -> {
                    if (zStr != null) {
                        return Optional.empty();
                    }
                    zStr = numStr;
                }
                default -> {
                    WynntilsMod.error("Unexpected coordinate prefix: " + maybeXyz);
                    return Optional.empty();
                }
            }
        }
        if (coordinateLength == 2 && yStr != null) {
            return Optional.empty();
        }
        return Optional.of(LocationUtils.parseFromStrings(xStr, yStr, zStr));
    }

    private static Location parseFromStrings(String xStr, String yStr, String zStr) {
        int x = Integer.parseInt(xStr);
        int y = yStr != null ? Integer.parseInt(yStr) : 0;
        int z = Integer.parseInt(zStr);
        return new Location(x, y, z);
    }

    // Trims brackets in the first/last character if they exist, returning empty if brackets are mismatched
    private static String trimBrackets(String str) {
        if (str.length() < 2) {
            return null;
        }
        char first = str.charAt(0);
        char last = str.charAt(str.length() - 1);

        // If first character isn't a bracket, no need to trim brackets
        int bracketId = LEFT_BRACKETS.indexOf(first);
        if (bracketId == -1) {
            return str;
        }
        // Brackets must match
        if (last == RIGHT_BRACKETS.charAt(bracketId)) {
            return str.substring(1, str.length() - 1);
        } else {
            return null;
        }
    }

    private static boolean isInRange(Location location) {
        return Math.abs(location.x) <= MAX_XZ && Math.abs(location.y) <= MAX_Y && Math.abs(location.z) <= MAX_XZ;
    }

    private static void sendShareMessage(String target, String locationString) {
        if (target.equals("guild")) {
            Handlers.Chat.queueChatCommand("g " + locationString);
        } else if (target.equals("party")) {
            Handlers.Chat.queueChatCommand("p " + locationString);
        } else {
            Handlers.Chat.queueChatCommand("msg " + target + " " + locationString);
        }
    }
}
