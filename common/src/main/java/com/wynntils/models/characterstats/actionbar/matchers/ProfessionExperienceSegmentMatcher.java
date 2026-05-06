/*
 * Copyright © Wynntils 2025-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.characterstats.actionbar.matchers;

import com.wynntils.handlers.actionbar.ActionBarSegment;
import com.wynntils.models.characterstats.actionbar.segments.ProfessionExperienceSegment;
import com.wynntils.utils.type.CappedValue;

public class ProfessionExperienceSegmentMatcher extends ExperienceSegmentMatcher {
    private static final String EXPERIENCE_CHAR_START = "\uE150";
    private static final String EXPERIENCE_CHAR_END = "\uE184";

    @Override
    protected String getExperienceCharStart() {
        return EXPERIENCE_CHAR_START;
    }

    @Override
    protected String getExperienceCharEnd() {
        return EXPERIENCE_CHAR_END;
    }

    @Override
    protected ActionBarSegment createExperienceSegment(
            String levelSegmentText, int startIndex, int endIndex, CappedValue progress) {
        return new ProfessionExperienceSegment(levelSegmentText, startIndex, endIndex, progress);
    }
}
