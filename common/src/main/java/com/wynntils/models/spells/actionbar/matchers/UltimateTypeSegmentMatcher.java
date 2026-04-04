/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.spells.actionbar.matchers;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.actionbar.ActionBarSegment;
import com.wynntils.handlers.actionbar.ActionBarSegmentMatcher;
import com.wynntils.models.spells.actionbar.segments.UltimateSegment;
import com.wynntils.models.spells.type.UltimateInfo;
import com.wynntils.utils.type.CappedValue;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UltimateTypeSegmentMatcher implements ActionBarSegmentMatcher {
    private static final int ULTIMATE_CHARGE_STEPS = 22;

    // The start of an ultimate type segment, a spacer
    private static final String ULTIMATE_SEGMENT_START = "\uDAFF\uDFF0";

    // The end of an ultimate type segment, a spacer
    private static final String ULTIMATE_SEGMENT_END = "\uDAFF\uDFEF";

    // Ultimate type segments per class
    private static final List<String> CLASS_RANGES = List.of(
            "\uE300-\uE315",
            "\uE320-\uE335",
            "\uE340-\uE355",
            "\uE360-\uE375",
            "\uE380-\uE395",
            "\uE3A0-\uE3B5",
            "\uE3C0-\uE3D5",
            "\uE3E0-\uE3F5",
            "\uE400-\uE415",
            "\uE420-\uE435",
            "\uE440-\uE455",
            "\uE460-\uE475",
            "\uE480-\uE495",
            "\uE4A0-\uE4B5",
            "\uE4C0-\uE4D5");

    private static final Pattern SEGMENT_MATCHER = Pattern.compile(
            ULTIMATE_SEGMENT_START + "(?<value>[" + String.join("", CLASS_RANGES) + "])" + ULTIMATE_SEGMENT_END);

    private static final List<Pattern> CLASS_PATTERNS = CLASS_RANGES.stream()
            .map(range -> Pattern.compile("[" + range + "]"))
            .toList();

    private UltimateInfo fromSegmentText(String segmentValue) {
        if (segmentValue.length() != 1) {
            WynntilsMod.warn("Unexpected ultimate meter type segment value length: " + segmentValue);
            return UltimateInfo.EMPTY;
        }

        char ultimateTypeChar = segmentValue.charAt(0);
        for (int i = 0; i < CLASS_PATTERNS.size(); i++) {
            if (CLASS_PATTERNS.get(i).matcher(segmentValue).matches()) {
                // Each class has 3 segments for the ultimate meter type, so we mod by 3 to get the class index
                int classIndex = i % 3;
                String[] range = CLASS_RANGES.get(i).split("-");
                char firstChar = range[0].charAt(0);

                int ultimateType = ultimateTypeChar - firstChar;
                if (ultimateType < 0 || ultimateType >= ULTIMATE_CHARGE_STEPS) {
                    WynntilsMod.warn(
                            "Invalid ultimate type value: " + ultimateType + " from segment value: " + segmentValue);
                    return UltimateInfo.EMPTY;
                }

                return new UltimateInfo(classIndex, new CappedValue(ultimateType, ULTIMATE_CHARGE_STEPS));
            }
        }
        return UltimateInfo.EMPTY;
    }

    @Override
    public ActionBarSegment parse(StyledText actionBar) {
        String actionBarString = actionBar.getStringWithoutFormatting();
        Matcher matcher = SEGMENT_MATCHER.matcher(actionBarString);
        if (!matcher.find()) return null;
        UltimateInfo ultimateInfo = fromSegmentText(matcher.group("value"));
        return new UltimateSegment(matcher.group(), matcher.start(), matcher.end(), ultimateInfo);
    }
}
