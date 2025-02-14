/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.beacons.type;

import com.wynntils.core.text.StyledText;
import com.wynntils.utils.colors.CustomColor;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;

public interface BeaconMarkerKind {
    Pattern MARKER_DISTANCE_PATTERN = Pattern.compile("\n(\\d+)m (§[a-z0-9])?(\uE000|\uE001)?");
    Pattern MARKER_COLOR_PATTERN = Pattern.compile("§((?:#)?([a-z0-9]{1,8}))");

    boolean matches(StyledText styledText);

    default Optional<Integer> getDistance(StyledText styledText) {
        Optional<Integer> distanceOpt = Optional.empty();

        Matcher distanceMatcher = styledText.getMatcher(MARKER_DISTANCE_PATTERN);
        if (distanceMatcher.find()) {
            distanceOpt = Optional.of(Integer.parseInt(distanceMatcher.group(1)));
        }

        return distanceOpt;
    }

    default Optional<CustomColor> getCustomColor(StyledText styledText) {
        Optional<CustomColor> colorOpt = Optional.empty();

        Matcher colorMatcher = styledText.getMatcher(MARKER_COLOR_PATTERN);
        if (colorMatcher.find()) {
            String colorStr = colorMatcher.group(1);

            if (colorStr.startsWith("#")) {
                colorOpt = Optional.of(CustomColor.fromHexString(colorMatcher.group(1)));
            } else {
                colorOpt = Optional.of(CustomColor.fromChatFormatting(ChatFormatting.getByCode(colorStr.charAt(0))));
            }
        }

        return colorOpt;
    }
}
