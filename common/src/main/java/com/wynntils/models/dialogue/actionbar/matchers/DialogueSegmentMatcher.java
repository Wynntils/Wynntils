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

public class DialogueSegmentMatcher implements ActionBarSegmentMatcher {
    @Override
    public ActionBarSegment parse(StyledText actionBar) {
        StringBuilder segmentBuilder = new StringBuilder();

        AtomicReference<StyledText> content = new AtomicReference<>(StyledText.EMPTY);
        actionBar.iterate((currentPart, changes) -> {
            FontDescription font = currentPart.getPartStyle().getFont();

            // Dialogue is made out of many special fonts, all of them are located in hud/dialogue
            // The exception is the fade effect which is matched in a separate segment
            if (font instanceof FontDescription.Resource(net.minecraft.resources.Identifier id)) {
                String resourcePath = id.getPath();

                if (resourcePath.startsWith("hud/dialogue") && !resourcePath.endsWith("effect/fade")) {
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
