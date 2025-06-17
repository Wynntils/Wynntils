/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.characterstats.actionbar.segments;

import com.wynntils.utils.type.CappedValue;

public class ProfessionExperienceSegment extends ExperienceSegment {
    public ProfessionExperienceSegment(String segmentText, CappedValue progress) {
        super(segmentText, progress);
    }

    @Override
    public String toString() {
        return "ProfessionExperienceSegment{" + "progress=" + progress + ", segmentText='" + segmentText + '\'' + '}';
    }
}
