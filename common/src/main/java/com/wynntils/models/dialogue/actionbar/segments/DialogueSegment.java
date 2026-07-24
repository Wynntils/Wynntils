/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.dialogue.actionbar.segments;

import com.wynntils.handlers.actionbar.ActionBarSegment;

public class DialogueSegment extends ActionBarSegment {
    private final String dialogueText;
    private final boolean requiresShift;
    private final boolean hasChoices;

    public DialogueSegment(
            String segmentText,
            int startIndex,
            int endIndex,
            String dialogueText,
            boolean requiresShift,
            boolean hasChoices) {
        super(segmentText, startIndex, endIndex);
        this.dialogueText = dialogueText;
        this.requiresShift = requiresShift;
        this.hasChoices = hasChoices;
    }

    public String getDialogueText() {
        return dialogueText;
    }

    public boolean requiresShift() {
        return requiresShift;
    }

    public boolean hasChoices() {
        return hasChoices;
    }

    @Override
    public String toString() {
        return "DialogueSegment{" + "segmentText='" + segmentText + "'}";
    }
}
