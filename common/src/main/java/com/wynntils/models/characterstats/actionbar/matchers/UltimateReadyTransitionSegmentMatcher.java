/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.characterstats.actionbar.matchers;

import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.actionbar.ActionBarSegment;
import com.wynntils.handlers.actionbar.ActionBarSegmentMatcher;
import com.wynntils.models.characterstats.actionbar.segments.UltimateReadyTransitionSegment;
import java.util.regex.Pattern;

public class UltimateReadyTransitionSegmentMatcher implements ActionBarSegmentMatcher {
    // The start of an ultimate ready segment, a spacer
    private static final String ULTIMATE_SEGMENT_START = "\uDAFF\uDFE0";

    // The end of an ultimate ready segment, a spacer
    private static final String ULTIMATE_SEGMENT_END = "\uDAFF\uDFDF";

    private static final String READY_TRANSITION_CHARACTERS = "\uE200-\uE21D";

    private static final Pattern ULTIMATE_READY_TRANSITION_SEGMENT_PATTERN =
            Pattern.compile(ULTIMATE_SEGMENT_START + "[" + READY_TRANSITION_CHARACTERS + "]" + ULTIMATE_SEGMENT_END);

    @Override
    public ActionBarSegment parse(StyledText actionBar) {
        String actionBarString = actionBar.getStringWithoutFormatting();
        var matcher = ULTIMATE_READY_TRANSITION_SEGMENT_PATTERN.matcher(actionBarString);
        if (!matcher.find()) return null;
        return new UltimateReadyTransitionSegment(matcher.group(), matcher.start(), matcher.end());
    }
}
