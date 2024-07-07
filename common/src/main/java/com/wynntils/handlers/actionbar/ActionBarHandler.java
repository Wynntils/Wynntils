/*
 * Copyright © Wynntils 2022-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.actionbar;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Handler;
import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.actionbar.type.ActionBarPosition;
import com.wynntils.mc.event.ChatPacketReceivedEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

public final class ActionBarHandler extends Handler {
    private static final String VANILLA_PADDING = "\\s{4,}";
    private static final StyledText CENTER_PADDING = StyledText.fromString("§0               ");
    private static final String STANDARD_PADDING = "    ";

    private static final Map<ActionBarPosition, List<ActionBarSegment>> ALL_SEGMENTS = Map.of(
            ActionBarPosition.LEFT,
            new ArrayList<>(),
            ActionBarPosition.CENTER,
            new ArrayList<>(),
            ActionBarPosition.RIGHT,
            new ArrayList<>());
    private final Map<ActionBarPosition, ActionBarSegment> lastSegments = new HashMap<>();
    private StyledText previousRawContent = null;
    private StyledText previousProcessedContent;

    public void registerSegment(ActionBarSegment segment) {
        ALL_SEGMENTS.get(segment.getPosition()).add(segment);
    }

    // FIXME: Fix action bar
    // @SubscribeEvent
    public void onActionBarUpdate(ChatPacketReceivedEvent.GameInfo event) {
        // FIXME: Reverse dependency!
        if (!Models.WorldState.onWorld()) return;

        StyledText content = StyledText.fromComponent(event.getMessage());
        if (content.equals(previousRawContent)) {
            // No changes, skip parsing
            if (!content.equals(previousProcessedContent)) {
                event.setMessage(previousProcessedContent.getComponent());
            }
            return;
        }
        previousRawContent = content;

        StyledText[] contentGroups = content.split(VANILLA_PADDING);

        // Create map of position -> matching part of the content
        Map<ActionBarPosition, StyledText> positionMatches = new EnumMap<>(ActionBarPosition.class);
        switch (contentGroups.length) {
            case 3:
                // normal case
                Arrays.stream(ActionBarPosition.values())
                        .forEach(pos -> positionMatches.put(pos, contentGroups[pos.ordinal()]));
                break;
            case 2:
                // missing center
                WynntilsMod.warn("Only 2 segments in action bar: " + content);
                positionMatches.put(ActionBarPosition.LEFT, contentGroups[0]);
                positionMatches.put(ActionBarPosition.RIGHT, contentGroups[1]);
                break;
            case 1:
                // only center
                WynntilsMod.warn("Only 1 segment in action bar: " + content);
                positionMatches.put(ActionBarPosition.CENTER, contentGroups[0]);
                break;
            default:
                WynntilsMod.warn("0 or more than 3 segments in action bar: " + content);
                return;
        }

        Arrays.stream(ActionBarPosition.values()).forEach(pos -> processPosition(pos, positionMatches));

        StyledText newContentBuilder = StyledText.EMPTY;
        // vanilla segments have three spaces between each segment, regardless of content
        if (!lastSegments.get(ActionBarPosition.LEFT).isHidden()) {
            newContentBuilder = newContentBuilder.append(positionMatches.get(ActionBarPosition.LEFT));
        }
        if (!lastSegments.get(ActionBarPosition.CENTER).isHidden()) {
            newContentBuilder = newContentBuilder.append(STANDARD_PADDING);
            newContentBuilder = newContentBuilder.append(positionMatches.get(ActionBarPosition.CENTER));
            newContentBuilder = newContentBuilder.append(STANDARD_PADDING);
        } else {
            // Add padding
            newContentBuilder = newContentBuilder.append(CENTER_PADDING);
        }
        if (!lastSegments.get(ActionBarPosition.RIGHT).isHidden()) {
            newContentBuilder = newContentBuilder.append(positionMatches.get(ActionBarPosition.RIGHT));
        }
        newContentBuilder = newContentBuilder.trim(); // In case either left or right is hidden
        previousProcessedContent = newContentBuilder;
        if (!content.equals(newContentBuilder)) {
            event.setMessage(newContentBuilder.getComponent());
        }
    }

    private void processPosition(ActionBarPosition pos, Map<ActionBarPosition, StyledText> positionMatches) {
        List<ActionBarSegment> potentialSegments = ALL_SEGMENTS.get(pos);
        for (ActionBarSegment segment : potentialSegments) {
            Matcher m = positionMatches.get(pos).getMatcher(segment.getPattern());
            if (m.matches()) {
                ActionBarSegment lastSegment = lastSegments.get(pos);
                if (segment != lastSegment) {
                    // This is a new kind of segment, tell the old one it disappeared
                    if (lastSegment != null) {
                        lastSegment.removed();
                    }
                    lastSegments.put(pos, segment);
                    segment.appeared(m);
                } else {
                    segment.update(m);
                }

                break;
            }
        }
    }
}
