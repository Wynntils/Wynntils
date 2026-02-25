/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.characterstats.actionbar.matchers;

import com.wynntils.handlers.actionbar.ActionBarSegment;
import com.wynntils.models.characterstats.actionbar.segments.MeterTransitionSegment;
import java.util.List;

public class UltimateMeterTransitionFromNormalSegmentMatcher extends AbstractMeterSegmentMatcher {
    // Meter transition characters, which are used for the meter transition animation in the action bar
    private static final String METER_TRANSITION_CHARACTERS = "\uE190-\uE197";

    private static final List<String> METER_TRANSITION_CHARACTER_LIST = List.of(METER_TRANSITION_CHARACTERS);

    @Override
    protected List<String> getCharacterRange() {
        return METER_TRANSITION_CHARACTER_LIST;
    }

    @Override
    protected boolean isUltimateMeter() {
        return true;
    }

    @Override
    protected ActionBarSegment createSegment(String segmentText, int startIndex, int endIndex, String segmentValue) {
        return new MeterTransitionSegment(segmentText, startIndex, endIndex);
    }
}
