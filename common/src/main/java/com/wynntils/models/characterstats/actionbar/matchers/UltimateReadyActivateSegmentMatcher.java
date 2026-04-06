/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.characterstats.actionbar.matchers;

import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.actionbar.ActionBarSegment;
import com.wynntils.handlers.actionbar.ActionBarSegmentMatcher;
import com.wynntils.models.characterstats.actionbar.segments.UltimateReadyActivateSegment;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UltimateReadyActivateSegmentMatcher implements ActionBarSegmentMatcher {
    // The start of an ultimate ready segment, a spacer
    private static final String ULTIMATE_SEGMENT_START = "\uDAFF\uDFFA";

    // The end of an ultimate ready segment, a spacer
    private static final String ULTIMATE_SEGMENT_END = "\uDAFF\uDFF6";

    private static final String PRESS_F = "\uE4E0";

    private static final Pattern ULTIMATE_READY_SEGMENT_PATTERN =
            Pattern.compile(ULTIMATE_SEGMENT_START + PRESS_F + ULTIMATE_SEGMENT_END);

    @Override
    public ActionBarSegment parse(StyledText actionBar) {
        String actionBarString = actionBar.getStringWithoutFormatting();
        Matcher matcher = ULTIMATE_READY_SEGMENT_PATTERN.matcher(actionBarString);
        if (!matcher.find()) return null;
        return new UltimateReadyActivateSegment(matcher.group(), matcher.start(), matcher.end());
    }
}
