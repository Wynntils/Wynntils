/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.characterstats.actionbar.matchers;

import com.wynntils.handlers.actionbar.ActionBarSegment;
import com.wynntils.models.characterstats.actionbar.segments.CombatExperienceSegment;
import com.wynntils.utils.type.CappedValue;

public class CombatExperienceSegmentMatcher extends ExperienceSegmentMatcher {
    private static final String EXPERIENCE_CHAR_START = "\uE110";
    private static final String EXPERIENCE_CHAR_END = "\uE144";

    @Override
    protected String getExperienceCharStart() {
        return EXPERIENCE_CHAR_START;
    }

    @Override
    protected String getExperienceCharEnd() {
        return EXPERIENCE_CHAR_END;
    }

    @Override
    protected ActionBarSegment createExperienceSegment(String levelSegmentText, CappedValue progress) {
        return new CombatExperienceSegment(levelSegmentText, progress);
    }
}
