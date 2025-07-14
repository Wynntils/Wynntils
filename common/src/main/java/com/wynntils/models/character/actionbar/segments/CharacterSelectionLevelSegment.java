/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.character.actionbar.segments;

import com.wynntils.handlers.actionbar.ActionBarSegment;

public class CharacterSelectionLevelSegment extends ActionBarSegment {
    private final int level;

    public CharacterSelectionLevelSegment(String segmentText, int level) {
        super(segmentText);
        this.level = level;
    }

    public int getLevel() {
        return level;
    }

    @Override
    public String toString() {
        return "CharacterSelectionLevelSegment{" + "level=" + level + ", segmentText='" + segmentText + '\'' + '}';
    }
}
