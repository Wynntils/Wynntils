/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.actionbar.segments;

import com.wynntils.handlers.actionbar.ActionBarSegment;

/**
 * Action-bar segment containing the visible NPC dialogue text and its current interaction state.
 *
 * <p>The raw segment text still covers the full dialogue HUD range, while {@code dialogueText} is cleaned for feature
 * logic.
 */
public final class DialogueSegment extends ActionBarSegment {
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
        return "DialogueSegment{"
                + "dialogueText='"
                + dialogueText
                + '\''
                + ", requiresShift="
                + requiresShift
                + ", hasChoices="
                + hasChoices
                + '}';
    }
}
