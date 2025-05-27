/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.wynn;

import com.wynntils.core.components.Handlers;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.mc.type.Location;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class LocationUtils {
    private static final Pattern COORDINATE_PATTERN = Pattern.compile(
            "\\s*(?<x>[-+]?\\d{1,6})(?:\\.\\d+)?([^0-9.+-]{1,5}(?<y>[-+]?\\d{1,3})(?:\\.\\d+)?)?[^0-9.+-]{1,5}(?<z>[-+]?\\d{1,6})(?:\\.\\d+)?\\s*");

    private static final Pattern STRICT_COORDINATE_PATTERN = Pattern.compile(
            "(?:^|\\s|\\[)\\s*([-+]?\\d{1,6}(?:[\\s,]{0,2}[-+]?\\d{1,3}(?:[\\s,]{0,2}[-+]?\\d{1,6})?)?)\\s*(?:\\]|\\s+|$)");

    public static Optional<Location> parseFromString(String locString) {
        Matcher matcher = COORDINATE_PATTERN.matcher(locString);

        if (matcher.matches()) {
            int x = Integer.parseInt(matcher.group("x"));
            String yString = matcher.group("y");
            int y = yString != null ? Integer.parseInt(yString) : 0;
            int z = Integer.parseInt(matcher.group("z"));

            return Optional.of(new Location(x, y, z));
        }

        return Optional.empty();
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

    private static void sendShareMessage(String target, String locationString) {
        if (target.equals("guild")) {
            Handlers.Command.sendCommandImmediately("g " + locationString);
        } else if (target.equals("party")) {
            Handlers.Command.sendCommandImmediately("p " + locationString);
        } else {
            Handlers.Command.sendCommandImmediately("msg " + target + " " + locationString);
        }
    }
}
