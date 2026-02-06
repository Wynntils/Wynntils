/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.character.actionbar.segments;

import com.wynntils.handlers.actionbar.ActionBarSegment;

public class CharacterCreationSegment extends ActionBarSegment {
    private final boolean firstCharacter;

    public CharacterCreationSegment(String segmentText, boolean firstCharacter) {
        super(segmentText);

        this.firstCharacter = firstCharacter;
    }

    public boolean isFirstCharacter() {
        return firstCharacter;
    }

    @Override
    public String toString() {
        return "CharacterCreationSegment{" + "segmentText='" + segmentText + '\'' + '}';
    }
}
