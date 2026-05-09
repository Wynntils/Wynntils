/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.actionbar.matchers;

import com.wynntils.core.text.StyledText;
import com.wynntils.core.text.StyledTextPart;
import com.wynntils.handlers.actionbar.ActionBarSegment;
import com.wynntils.handlers.actionbar.ActionBarSegmentMatcher;
import com.wynntils.handlers.actionbar.segments.DialogueSegment;
import net.minecraft.network.chat.FontDescription;

/**
 * Matches Wynncraft dialogue HUD content from styled action-bar packets.
 *
 * <p>Dialogue uses custom fonts for body text, controls, choices, and decorative glyphs. This matcher turns those styled
 * parts into one {@link DialogueSegment} so features can react to dialogue state without parsing raw components.
 */
public final class DialogueSegmentMatcher implements ActionBarSegmentMatcher {
    private static final String DIALOGUE_FONT_PREFIX = "hud/dialogue/";
    private static final String DIALOGUE_BODY_FONT_PREFIX = "hud/dialogue/text/wynncraft/body_";
    private static final String DIALOGUE_CHOICE_FONT_PREFIX = "hud/dialogue/text/wynncraft/choice_";
    private static final String DIALOGUE_CONTROL_FONT = "hud/dialogue/text/control";

    @Override
    public ActionBarSegment parse(StyledText actionBar) {
        String unformattedText = actionBar.getStringWithoutFormatting();
        StringBuilder dialogueText = new StringBuilder();
        boolean foundDialogue = false;
        boolean requiresShift = false;
        boolean hasChoices = false;
        int segmentStart = -1;
        int segmentEnd = -1;
        int rawIndex = 0;
        String lastBodyPath = null;

        for (StyledTextPart part : actionBar) {
            int partStart = rawIndex;
            rawIndex += part.length();

            if (!(part.getPartStyle().getFont() instanceof FontDescription.Resource resource)) continue;

            String path = resource.id().getPath();
            if (!path.startsWith(DIALOGUE_FONT_PREFIX)) continue;

            foundDialogue = true;
            if (segmentStart == -1) {
                segmentStart = partStart;
            }
            // Keep the whole dialogue HUD matched so decorative dialogue glyphs do not become fallback segments.
            segmentEnd = rawIndex;

            if (path.startsWith(DIALOGUE_BODY_FONT_PREFIX)) {
                String cleanedBodyText =
                        cleanDialogueText(StyledText.fromPart(part).getStringWithoutFormatting());
                if (!cleanedBodyText.isBlank()) {
                    if (lastBodyPath != null
                            && !lastBodyPath.equals(path)
                            && dialogueText.length() > 0
                            && dialogueText.charAt(dialogueText.length() - 1) != ' ') {
                        dialogueText.append(' ');
                    }

                    dialogueText.append(cleanedBodyText);
                    lastBodyPath = path;
                }
            } else if (path.equals(DIALOGUE_CONTROL_FONT)) {
                requiresShift = true;
            } else if (path.startsWith(DIALOGUE_CHOICE_FONT_PREFIX)) {
                hasChoices = true;
            }
        }

        String cleanedText = dialogueText.toString().trim();
        if (!foundDialogue || cleanedText.isBlank()) return null;

        String segmentText = unformattedText.substring(segmentStart, segmentEnd);
        return new DialogueSegment(segmentText, segmentStart, segmentEnd, cleanedText, requiresShift, hasChoices);
    }

    private static String cleanDialogueText(String text) {
        // Wynncraft uses spacer glyphs inside dialogue text; collapse them without trimming real characters.
        StringBuilder output = new StringBuilder(text.length());
        boolean pendingSpace = false;
        int[] codePoints = text.codePoints().toArray();

        for (int codePoint : codePoints) {
            if (isDialogueSeparator(codePoint)) {
                pendingSpace = output.length() > 0;
                continue;
            }

            if (pendingSpace && (output.length() == 0 || output.charAt(output.length() - 1) != ' ')) {
                output.append(' ');
            }

            pendingSpace = false;
            output.appendCodePoint(codePoint);
        }

        if (pendingSpace && (output.length() == 0 || output.charAt(output.length() - 1) != ' ')) {
            output.append(' ');
        }

        return output.toString();
    }

    private static boolean isDialogueSeparator(int codePoint) {
        int type = Character.getType(codePoint);
        Character.UnicodeBlock block = Character.UnicodeBlock.of(codePoint);
        return Character.isWhitespace(codePoint)
                || Character.isISOControl(codePoint)
                || type == Character.PRIVATE_USE
                || type == Character.SURROGATE
                || block == Character.UnicodeBlock.PRIVATE_USE_AREA
                || block == Character.UnicodeBlock.SUPPLEMENTARY_PRIVATE_USE_AREA_A
                || block == Character.UnicodeBlock.SUPPLEMENTARY_PRIVATE_USE_AREA_B;
    }
}
