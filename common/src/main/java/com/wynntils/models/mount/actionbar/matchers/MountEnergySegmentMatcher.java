/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.mount.actionbar.matchers;

import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.actionbar.ActionBarSegment;
import com.wynntils.handlers.actionbar.ActionBarSegmentMatcher;
import com.wynntils.models.mount.actionbar.segments.MountEnergySegment;
import com.wynntils.utils.type.CappedValue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MountEnergySegmentMatcher implements ActionBarSegmentMatcher {
    // The start of a mount energy segment, a spacer
    private static final String SEGMENT_START = "\uDB00\uDC08";

    // The end of a mount energy segment, a spacer
    private static final String SEGMENT_END = "\uDAFF\uDFE7";

    private static final int MAX_ENERGY = 48;

    // Full energy character
    private static final String FULL_ENERGY = "\uE000";
    // Empty energy character
    private static final String EMPTY_ENERGY = "\uE02F";

    private static final Pattern MOUNT_ENERGY_REGEX =
            Pattern.compile(SEGMENT_START + "(?<energy>[" + FULL_ENERGY + "-" + EMPTY_ENERGY + "])" + SEGMENT_END);

    @Override
    public ActionBarSegment parse(StyledText actionBar) {
        String actionBarString = actionBar.getStringWithoutFormatting();
        Matcher matcher = MOUNT_ENERGY_REGEX.matcher(actionBarString);
        if (!matcher.find()) return null;

        CappedValue mountEnergy = getMountEnergy(matcher.group("energy"));
        return new MountEnergySegment(matcher.group(), matcher.start(), matcher.end(), mountEnergy);
    }

    private CappedValue getMountEnergy(String mountEnergyText) {
        int codePoint = mountEnergyText.codePointAt(0);
        int energy = EMPTY_ENERGY.codePointAt(0) - codePoint;

        return new CappedValue(energy, MAX_ENERGY);
    }
}
