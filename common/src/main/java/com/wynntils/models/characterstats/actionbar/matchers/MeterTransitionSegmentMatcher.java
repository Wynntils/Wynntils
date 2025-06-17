/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.characterstats.actionbar.matchers;

import com.wynntils.handlers.actionbar.ActionBarSegment;
import com.wynntils.models.characterstats.actionbar.segments.MeterTransitionSegment;
import java.util.List;

public class MeterTransitionSegmentMatcher extends AbstractMeterSegmentMatcher {
    // Meter transition characters, which are used for the meter transition animation in the action bar
    private static final String METER_TRANSITION_CHARACTERS = "\uE0E0-\uE0E4";

    // Meter transition type characters, which display whether sprint, breath or profession meter is transitioning
    // Note: These characters can identify the type of meter transition, but we do not use them,
    //       as they are rather slow to update. Sprint and breath meters are identified in MeterBarSegmentMatcher,
    //       and the profession meters are identified by the experience bar.
    private static final String METER_TRANSITION_TYPE_CHARACTERS = "\uE0F0-\uE0F5";

    // The separator between different transition characters in the action bar,
    // as there can be multiple transition characters in a single segment along with a transition type character
    private static final String SEPARATOR = "\uDAFF\uDFE7";

    private static final List<String> METER_TRANSITION_CHARACTER_LIST =
            List.of(METER_TRANSITION_CHARACTERS, METER_TRANSITION_TYPE_CHARACTERS, SEPARATOR);

    @Override
    protected List<String> getCharacterRange() {
        return METER_TRANSITION_CHARACTER_LIST;
    }

    @Override
    protected boolean isMultipleSegments() {
        return true;
    }

    @Override
    protected ActionBarSegment createSegment(String segmentText, String segmentValue) {
        return new MeterTransitionSegment(segmentText);
    }
}
