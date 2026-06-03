/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.dialogue.actionbar.matchers;

import com.wynntils.core.text.StyledText;
import com.wynntils.core.text.type.StyleType;
import com.wynntils.handlers.actionbar.ActionBarSegment;
import com.wynntils.handlers.actionbar.ActionBarSegmentMatcher;
import com.wynntils.models.dialogue.actionbar.segments.DialogueSegment;
import com.wynntils.utils.type.IterationDecision;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.resources.Identifier;

public class DialogueSegmentMatcher implements ActionBarSegmentMatcher {
    private static final String DIALOGUE_START_PATH = "hud/dialogue";
    private static final String FADE_END_PATH = "effect/fade";

    @Override
    public ActionBarSegment parse(StyledText actionBar) {
        StringBuilder segmentBuilder = new StringBuilder();

        AtomicReference<StyledText> content = new AtomicReference<>(StyledText.EMPTY);
        actionBar.iterate((currentPart, changes) -> {
            FontDescription font = currentPart.getPartStyle().getFont();

            // Dialogue is made out of many special fonts, all of them are located in hud/dialogue
            // The exception is the fade effect which is matched in a separate segment
            if (font instanceof FontDescription.Resource(Identifier id)) {
                String resourcePath = id.getPath();

                if (resourcePath.startsWith(DIALOGUE_START_PATH) && !resourcePath.endsWith(FADE_END_PATH)) {
                    segmentBuilder.append(currentPart.getString(null, StyleType.NONE));
                    content.getAndUpdate(styledText -> styledText.appendPart(currentPart));
                }
            }
            return IterationDecision.CONTINUE;
        });

        String segmentString = segmentBuilder.toString();
        if (segmentString.isBlank()) return null;
        String actionBarString = actionBar.getString(StyleType.NONE);
        int startIndex = actionBarString.indexOf(segmentString);

        return new DialogueSegment(segmentString, startIndex, startIndex + segmentString.length(), content.get());
    }
}
