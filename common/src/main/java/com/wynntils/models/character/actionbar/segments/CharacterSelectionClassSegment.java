/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.character.actionbar.segments;

import com.wynntils.handlers.actionbar.ActionBarSegment;
import com.wynntils.models.character.type.ClassType;

public class CharacterSelectionClassSegment extends ActionBarSegment {
    private final ClassType classType;
    private final boolean isReskinned;

    public CharacterSelectionClassSegment(String segmentText, ClassType classType, boolean isReskinned) {
        super(segmentText);

        this.classType = classType;
        this.isReskinned = isReskinned;
    }

    public ClassType getClassType() {
        return classType;
    }

    public boolean isReskinned() {
        return isReskinned;
    }

    @Override
    public String toString() {
        return "CharacterSelectionClassSegment{" + "classType="
                + classType + ", isReskinned="
                + isReskinned + ", segmentText='"
                + segmentText + '\'' + '}';
    }
}
