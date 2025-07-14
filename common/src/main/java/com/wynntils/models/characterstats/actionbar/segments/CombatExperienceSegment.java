/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.characterstats.actionbar.segments;

import com.wynntils.utils.type.CappedValue;

public class CombatExperienceSegment extends ExperienceSegment {
    public CombatExperienceSegment(String segmentText, CappedValue progress) {
        super(segmentText, progress);
    }

    @Override
    public String toString() {
        return "CombatExperienceSegment{" + "progress=" + progress + ", segmentText='" + segmentText + '\'' + '}';
    }
}
