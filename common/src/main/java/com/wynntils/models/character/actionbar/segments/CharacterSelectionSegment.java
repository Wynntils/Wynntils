/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.character.actionbar.segments;

import com.wynntils.handlers.actionbar.ActionBarSegment;

public class CharacterSelectionSegment extends ActionBarSegment {
    public CharacterSelectionSegment(String segmentText) {
        super(segmentText);
    }

    @Override
    public String toString() {
        return "CharacterSelectionSegment{" + "segmentText='" + segmentText + '\'' + '}';
    }
}
