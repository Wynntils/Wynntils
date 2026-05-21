/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.dialogue.actionbar.matchers;

import com.wynntils.core.text.StyledText;
import com.wynntils.core.text.StyledTextPart;
import com.wynntils.core.text.type.StyleType;
import com.wynntils.handlers.actionbar.ActionBarSegment;
import com.wynntils.handlers.actionbar.ActionBarSegmentMatcher;
import com.wynntils.models.dialogue.actionbar.segments.DialogueSegment;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.resources.Identifier;

import java.util.LinkedList;
import java.util.List;

public class DialogueSegmentMatcher implements ActionBarSegmentMatcher {
    private static final String DIALOGUE_FONT_PREFIX = "hud/dialogue/";
    private static final String BODY_FONT_MARKER = "/body_";
    private static final String CHOICE_FONT_MARKER = "/choice";
    private static final String CONTROL_FONT = "hud/dialogue/text/control";
    private static final String FADE_FONT_PREFIX = "hud/dialogue/effect/fade";
    private static final char POSITION_MARKER = '\uDAFF';

    @Override
    public ActionBarSegment parse(StyledText actionBar) {
        List<StyledTextPart> dialogueParts = new LinkedList<>();
        StringBuilder segmentBuilder = new StringBuilder();
        StringBuilder dialogueTextBuilder = new StringBuilder();
        boolean requiresShift = false;
        boolean hasChoices = false;
        int segmentStart = -1;
        int segmentEnd = -1;
        int rawIndex = 0;

        for (StyledTextPart part : actionBar) {
            int partStart = rawIndex;
            rawIndex += part.length();
            FontDescription font = part.getPartStyle().getFont();

            if (!(font instanceof FontDescription.Resource(Identifier id))) continue;

            String path = id.getPath();
            if (!path.startsWith(DIALOGUE_FONT_PREFIX) || path.startsWith(FADE_FONT_PREFIX)) continue;

            if (segmentStart == -1) {
                segmentStart = partStart;
            }

            segmentEnd = rawIndex;
            dialogueParts.add(part);
            segmentBuilder.append(part.getString(null, StyleType.NONE));

            if (path.equals(CONTROL_FONT)) {
                requiresShift = true;
            }
            if (path.contains(CHOICE_FONT_MARKER)) {
                hasChoices = true;
            }
            if (path.contains(BODY_FONT_MARKER)) {
                dialogueTextBuilder.append(part.getString(null, StyleType.NONE));
            }
        }

        if (dialogueParts.isEmpty()) return null;

        String actionBarString = actionBar.getString(StyleType.NONE);
        int startIndex = Math.max(0, segmentStart);
        int endIndex = Math.min(segmentEnd, actionBarString.length());
        String segmentText = segmentBuilder.toString();
        StyledText content = StyledText.fromParts(dialogueParts);
        String dialogueText = cleanDialogueText(dialogueTextBuilder.toString());

        return new DialogueSegment(segmentText, startIndex, endIndex, content, dialogueText, requiresShift, hasChoices);
    }

    private static String cleanDialogueText(String text) {
        StringBuilder output = new StringBuilder();
        boolean skipNext = false;
        char lastChar = 0;

        for (char c : text.trim().toCharArray()) {
            if (skipNext) {
                skipNext = false;
                continue;
            }
            if (c == POSITION_MARKER) {
                if (lastChar != ' ') {
                    output.append(' ');
                    lastChar = ' ';
                }
                skipNext = true;
                continue;
            }
            if (c == ' ' && lastChar == ' ') {
                continue;
            }
            lastChar = c;
            output.append(c);
        }

        return output.toString().trim();
    }
}
