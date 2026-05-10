/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.characterstats.actionbar.segments;

import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.actionbar.ActionBarSegment;

public class DialogueSegment extends ActionBarSegment {
    private final StyledText dialogue;

    public DialogueSegment(String segmentText, int startIndex, int endIndex, StyledText dialogue) {
        super(segmentText, startIndex, endIndex);
        this.dialogue = dialogue;
    }

    public StyledText getDialogue() {
        return dialogue;
    }
}
