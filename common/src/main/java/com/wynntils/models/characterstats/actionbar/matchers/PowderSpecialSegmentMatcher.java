/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.characterstats.actionbar.matchers;

import com.wynntils.handlers.actionbar.ActionBarSegment;
import com.wynntils.handlers.actionbar.ActionBarSegmentMatcher;
import com.wynntils.models.characterstats.actionbar.segments.PowderSpecialSegment;
import com.wynntils.models.elements.type.Powder;
import com.wynntils.utils.type.Pair;
import java.util.Arrays;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PowderSpecialSegmentMatcher implements ActionBarSegmentMatcher {
    // The start of a powder special segment, a spacer
    private static final String SEGMENT_START = "\uDAFF\uDFF0";

    // The end of a powder special segment, a spacer
    private static final String SEGMENT_END = "\uDAFF\uDFEF";

    // Empty powder special character
    private static final String EMPTY_POWDER_SPECIAL = "\uE110";

    // Characters for powder specials, by type, each having a 10 character range, a character being 10% of the bar
    private static final Map<Powder, Pair<String, String>> POWDER_SPECIAL_MAP = Map.of(
            Powder.AIR,
            Pair.of("\uE120", "\uE129"),
            Powder.EARTH,
            Pair.of("\uE130", "\uE139"),
            Powder.FIRE,
            Pair.of("\uE140", "\uE149"),
            Powder.THUNDER,
            Pair.of("\uE150", "\uE159"),
            Powder.WATER,
            Pair.of("\uE160", "\uE169"));

    private static final Pattern POWDER_SPECIAL_REGEX = Pattern.compile(SEGMENT_START + "["
            + String.join(
                    "",
                    Arrays.stream(Powder.values())
                            .map(POWDER_SPECIAL_MAP::get)
                            .map(pair -> pair.a() + "-" + pair.b())
                            .toArray(String[]::new))
            + EMPTY_POWDER_SPECIAL + "]" + SEGMENT_END);

    @Override
    public ActionBarSegment parse(String actionBar) {
        Matcher matcher = POWDER_SPECIAL_REGEX.matcher(actionBar);
        if (!matcher.find()) return null;
        return new PowderSpecialSegment(matcher.group());
    }
}
