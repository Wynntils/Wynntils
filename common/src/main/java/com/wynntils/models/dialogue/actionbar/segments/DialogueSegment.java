/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.dialogue.actionbar.segments;

import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.actionbar.ActionBarSegment;

public class DialogueSegment extends ActionBarSegment {
    private final StyledText content;

    public DialogueSegment(String segmentText, int startIndex, int endIndex, StyledText content) {
        super(segmentText, startIndex, endIndex);
        this.content = content;
    }

    public StyledText getContent() {
        return content;
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
