/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.characterstats.actionbar.matchers;

import com.wynntils.core.text.StyledText;
import com.wynntils.core.text.StyledTextPart;
import com.wynntils.handlers.actionbar.ActionBarSegment;
import com.wynntils.handlers.actionbar.ActionBarSegmentMatcher;
import com.wynntils.models.characterstats.actionbar.segments.DialogueSegment;
import com.wynntils.utils.wynn.DialogueUtils;
import java.util.LinkedList;
import java.util.List;
import net.minecraft.network.chat.FontDescription;

public class DialogueSegmentMatcher implements ActionBarSegmentMatcher {
    private static final String DIALOGUE_FONT_PREFIX = "hud/dialogue/";
    private static final String DIALOGUE_CONTROL_FONT = "hud/dialogue/text/control";

    @Override
    public ActionBarSegment parse(StyledText actionBar) {
        List<StyledTextPart> dialogueParts = new LinkedList<>();
        String actionBarString = actionBar.getStringWithoutFormatting();
        boolean requiresShift = false;
        int segmentStart = -1;
        int segmentEnd = -1;
        int rawIndex = 0;

        for (StyledTextPart part : actionBar) {
            int partStart = rawIndex;
            rawIndex += part.length();
            FontDescription font = part.getPartStyle().getFont();

            if (font instanceof FontDescription.Resource resource) {
                String path = resource.id().getPath();

                if (path.startsWith(DIALOGUE_FONT_PREFIX)) {
                    if (segmentStart == -1) {
                        segmentStart = partStart;
                    }

                    segmentEnd = rawIndex;
                    dialogueParts.add(part);

                    if (path.equals(DIALOGUE_CONTROL_FONT)) {
                        requiresShift = true;
                    }
                }
            }
        }

        if (dialogueParts.isEmpty()) return null;

        StyledText dialogue = StyledText.fromParts(dialogueParts);
        DialogueUtils.Content content = DialogueUtils.getDialogueContent(dialogue.getComponent(), false);
        String dialogueText = content.getCleanText() == null ? "" : content.getCleanText();
        int startIndex = Math.max(0, segmentStart);
        int endIndex = Math.min(segmentEnd, actionBarString.length());
        String segmentText = actionBarString.substring(startIndex, endIndex);

        return new DialogueSegment(
                segmentText, startIndex, endIndex, dialogue, dialogueText, requiresShift, content.hasChoices());
    }
}
