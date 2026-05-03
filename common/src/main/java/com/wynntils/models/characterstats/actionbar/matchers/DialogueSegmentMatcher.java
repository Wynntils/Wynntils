package com.wynntils.models.characterstats.actionbar.matchers;

import com.wynntils.core.text.StyledText;
import com.wynntils.core.text.StyledTextPart;
import com.wynntils.handlers.actionbar.ActionBarSegment;
import com.wynntils.handlers.actionbar.ActionBarSegmentMatcher;
import com.wynntils.models.characterstats.actionbar.segments.DialogueSegment;
import net.minecraft.network.chat.FontDescription;

import java.util.Iterator;

public class DialogueSegmentMatcher implements ActionBarSegmentMatcher {

    @Override
    public ActionBarSegment parse(StyledText actionBar) {
        Iterator<StyledTextPart> parts = actionBar.iterator();
        String actionBarString = actionBar.getStringWithoutFormatting();
        StringBuilder segmentBuilder = new StringBuilder();
        String beginning = null;

        while (parts.hasNext()) {
            StyledTextPart part = parts.next();
            FontDescription font = part.getPartStyle().getFont();

            if (font instanceof FontDescription.Resource resource) {
                String path = resource.id().getPath();

                if (path.startsWith("hud/dialogue/")) {
                    if (beginning == null) {
                        beginning = part.getComponent().getString();
                    }
                    segmentBuilder.append(part.getComponent().getString());
                }
            }
        }

        if (beginning != null) {
            // only works if there are no other siblings, than hud/dialogue/... in the right order
            String segmentText = segmentBuilder.toString();
            int startIndex = actionBarString.indexOf(segmentText);
            int endIndex = startIndex + segmentBuilder.length();

            if (startIndex < 0) return null;
            return new DialogueSegment(segmentText, startIndex, endIndex);
        }
        return null;
    }
}
