/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.characterstats.actionbar.matchers;

import com.wynntils.core.WynntilsMod;
import com.wynntils.handlers.actionbar.ActionBarSegment;
import com.wynntils.handlers.actionbar.ActionBarSegmentMatcher;
import com.wynntils.models.characterstats.actionbar.segments.PowderSpecialSegment;
import com.wynntils.models.characterstats.type.PowderSpecialInfo;
import com.wynntils.models.elements.type.Powder;
import com.wynntils.utils.type.Pair;
import java.util.Arrays;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PowderSpecialSegmentMatcher implements ActionBarSegmentMatcher {
    // The start of a powder special segment, a spacer
    private static final String SEGMENT_START = "(\uDAFF\uDFEF\uDAFF\uDFFF\u0001|\uDAFF\uDFF0)";

    // The end of a powder special segment, a spacer
    private static final String SEGMENT_END = "(\uDAFF\uDFEE|\uDAFF\uDFEF)";

    // Empty powder special character
    private static final String EMPTY_POWDER_SPECIAL = "\uE010";

    // Characters for powder specials, by type, each having a 10 character range, a character being 10% of the bar
    private static final Map<Powder, Pair<Character, Character>> POWDER_SPECIAL_MAP = Map.of(
            Powder.AIR,
            Pair.of('\uE020', '\uE029'),
            Powder.EARTH,
            Pair.of('\uE030', '\uE039'),
            Powder.FIRE,
            Pair.of('\uE040', '\uE049'),
            Powder.THUNDER,
            Pair.of('\uE050', '\uE059'),
            Powder.WATER,
            Pair.of('\uE060', '\uE069'));

    private static final Pattern POWDER_SPECIAL_REGEX = Pattern.compile(SEGMENT_START + "(?<powder>["
            + String.join(
                    "",
                    Arrays.stream(Powder.values())
                            .map(POWDER_SPECIAL_MAP::get)
                            .map(pair -> pair.a() + "-" + pair.b())
                            .toArray(String[]::new))
            + EMPTY_POWDER_SPECIAL + "])" + SEGMENT_END);

    @Override
    public ActionBarSegment parse(String actionBar) {
        Matcher matcher = POWDER_SPECIAL_REGEX.matcher(actionBar);
        if (!matcher.find()) return null;

        PowderSpecialInfo powderSpecialInfo = getPowderSpecialInfo(matcher.group("powder"));
        return new PowderSpecialSegment(matcher.group(), powderSpecialInfo);
    }

    private PowderSpecialInfo getPowderSpecialInfo(String powderSpecialText) {
        // Handle empty powder special
        if (powderSpecialText.equals(EMPTY_POWDER_SPECIAL)) {
            return PowderSpecialInfo.EMPTY;
        }

        for (Powder powder : Powder.values()) {
            Pair<Character, Character> powderSpecialRange = POWDER_SPECIAL_MAP.get(powder);

            if (powderSpecialRange == null) {
                WynntilsMod.warn("No range found for powder special: " + powder);
                continue;
            }

            // Get the character for the powder special
            char powderSpecialChar = powderSpecialText.charAt(0);

            // Check if this character is in the range for the powder special, and continue if it is not
            if (powderSpecialChar < powderSpecialRange.a() || powderSpecialChar > powderSpecialRange.b()) {
                continue;
            }

            // Get the index of the character in the range
            int powderIndex = powderSpecialChar - powderSpecialRange.a();

            // There are 10 characters in the range, each representing 10% of the bar
            return new PowderSpecialInfo((powderIndex + 1) * 0.1f, powder);
        }

        WynntilsMod.warn("Couldn't find powder special for character: " + powderSpecialText);
        return PowderSpecialInfo.EMPTY;
    }
}
