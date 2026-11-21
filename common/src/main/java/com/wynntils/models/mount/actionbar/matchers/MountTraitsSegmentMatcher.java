/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.mount.actionbar.matchers;

import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.actionbar.ActionBarSegment;
import com.wynntils.handlers.actionbar.ActionBarSegmentMatcher;
import com.wynntils.models.mount.actionbar.segments.MountTraitsSegment;
import com.wynntils.utils.type.CappedValue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MountTraitsSegmentMatcher implements ActionBarSegmentMatcher {
    private static final int MAX_TRAIT = 47;

    // Spacer before the energy icon and energy bar
    private static final String ENERGY_SPACER = "\uDB00\uDC0B";
    private static final String ENERGY_ICON = "\uE060";

    // Spacer before the toughness icon
    private static final String TOUGHNESS_ICON_SPACER = "\uDB00\uDC01";
    private static final String TOUGHNESS_ICON = "\uE061";

    // Spacer after the toughness icon and toughness bar
    private static final String TOUGHNESS_END_SPACER = "\uDAFF\uDFE2";
    private static final String TOUGHNESS_BAR_SPACER = "\uDB00\uDC15";

    // Spacer after the energy bar
    private static final String SEGMENT_END = "\uDAFF\uDFEC";

    private static final String ENERGY_BAR_CHARS = "\uE000-\uE02E";
    private static final String TOUGHNESS_BAR_CHARS = "\uE030-\uE05E";

    private static final String ICONS =
            ENERGY_SPACER + ENERGY_ICON + TOUGHNESS_ICON_SPACER + TOUGHNESS_ICON + TOUGHNESS_END_SPACER;

    // Test in MountTraitsSegmentMatcher_MOUNT_TRAITS_PATTERN
    private static final Pattern MOUNT_TRAITS_PATTERN = Pattern.compile("(?:"
            + "(?<icons>" + ICONS + ")"
            + "|" + TOUGHNESS_BAR_SPACER + "(?<toughness>[" + TOUGHNESS_BAR_CHARS + "])" + TOUGHNESS_END_SPACER
            + "|" + ENERGY_SPACER + "(?<energy>[" + ENERGY_BAR_CHARS + "])" + SEGMENT_END
            + "){3}");

    @Override
    public ActionBarSegment parse(StyledText actionBar) {
        Matcher matcher = MOUNT_TRAITS_PATTERN.matcher(actionBar.getStringWithoutFormatting());
        if (!matcher.find()) return null;
        if (matcher.group("icons") == null || matcher.group("energy") == null || matcher.group("toughness") == null) {
            return null;
        }

        return createSegment(matcher);
    }

    private MountTraitsSegment createSegment(Matcher matcher) {
        CappedValue energy =
                new CappedValue(MAX_TRAIT - (matcher.group("energy").codePointAt(0) - '\uE000'), MAX_TRAIT);
        CappedValue toughness =
                new CappedValue(MAX_TRAIT - (matcher.group("toughness").codePointAt(0) - '\uE030'), MAX_TRAIT);
        return new MountTraitsSegment(matcher.group(), matcher.start(), matcher.end(), energy, toughness);
    }
}
