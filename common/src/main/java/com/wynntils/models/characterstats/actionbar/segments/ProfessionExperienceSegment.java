/*
 * Copyright © Wynntils 2025-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.characterstats.actionbar.segments;

import com.wynntils.utils.type.CappedValue;

public class ProfessionExperienceSegment extends ExperienceSegment {
    public ProfessionExperienceSegment(String segmentText, int startIndex, int endIndex, CappedValue progress) {
        super(segmentText, startIndex, endIndex, progress);
    }

    @Override
    public String toString() {
        return "ProfessionExperienceSegment{" + "progress=" + progress + ", segmentText='" + segmentText + '\'' + '}';
    }
}
