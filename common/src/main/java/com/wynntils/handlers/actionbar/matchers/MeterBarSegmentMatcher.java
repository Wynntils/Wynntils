/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.actionbar.matchers;

import com.wynntils.handlers.actionbar.ActionBarSegment;
import com.wynntils.handlers.actionbar.segments.MeterBarSegment;
import java.util.List;

public class MeterBarSegmentMatcher extends AbstractMeterSegmentMatcher {
    // All possible meter character ranges, without the animation characters, extracted from the resource pack/font
    private static final List<String> METER_CHARACTERS = List.of("\uE090-\uE091", "\uE096-\uE0D8");

    @Override
    protected List<String> getCharacterRange() {
        return METER_CHARACTERS;
    }

    @Override
    protected ActionBarSegment createSegment(String segmentText) {
        return new MeterBarSegment(segmentText);
    }
}
