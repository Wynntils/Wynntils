/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.dialogue.actionbar.segments;

import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.actionbar.ActionBarSegment;

public class DialogueSegment extends ActionBarSegment {
    private final StyledText content;
    private final String dialogueText;
    private final boolean requiresShift;
    private final boolean hasChoices;

    public DialogueSegment(
            String segmentText,
            int startIndex,
            int endIndex,
            StyledText content,
            String dialogueText,
            boolean requiresShift,
            boolean hasChoices) {
        super(segmentText, startIndex, endIndex);
        this.content = content;
        this.dialogueText = dialogueText;
        this.requiresShift = requiresShift;
        this.hasChoices = hasChoices;
    }

    public StyledText getContent() {
        return content;
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
        return "DialogueSegment{" + "content=" + content + ", segmentText='" + segmentText + "'}";
    }

    public static class DialogueFadeSegment extends ActionBarSegment {
        public DialogueFadeSegment(String segmentText, int startIndex, int endIndex) {
            super(segmentText, startIndex, endIndex);
        }

        @Override
        public String toString() {
            return "DialogueFadeSegment{" + "segmentText='" + segmentText + "'}";
        }
    }
}
