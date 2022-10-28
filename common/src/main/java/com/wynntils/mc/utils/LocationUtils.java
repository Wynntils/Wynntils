/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.utils;

import com.wynntils.mc.objects.Location;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LocationUtils {
    private static final Pattern COORDINATE_PATTERN =
            Pattern.compile("(?<x>[-+]?\\d+)([^0-9+-]{1,5}(?<y>[-+]?\\d+))?[^0-9+-]{1,5}(?<z>[-+]?\\d+)");

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
}
