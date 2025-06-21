/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.worlds.actionbar.segments;

import com.wynntils.handlers.actionbar.ActionBarSegment;

public class CharacterCreationSegment extends ActionBarSegment {
    public CharacterCreationSegment(String segmentText) {
        super(segmentText);
    }

    @Override
    public String toString() {
        return "CharacterCreationSegment{" + "segmentText='" + segmentText + '\'' + '}';
    }
}
