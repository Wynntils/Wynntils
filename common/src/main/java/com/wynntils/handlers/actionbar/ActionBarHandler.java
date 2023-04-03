/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.actionbar;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Handler;
import com.wynntils.core.components.Models;
import com.wynntils.handlers.actionbar.type.ActionBarPosition;
import com.wynntils.mc.event.ChatPacketReceivedEvent;
import com.wynntils.utils.mc.type.CodedString;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class ActionBarHandler extends Handler {
    // example: "§c❤ 218/218§0    §7502§f S§7 -1580    §b✺ 1/119"
    private static final Pattern ACTIONBAR_PATTERN = Pattern.compile("(?<LEFT>§[^§]+)(?<CENTER>.*)(?<RIGHT>§[^§]+)");
    private static final CodedString CENTER_PADDING = CodedString.of("§0               ");

    private final Map<ActionBarPosition, List<ActionBarSegment>> allSegments = Map.of(
            ActionBarPosition.LEFT,
            new ArrayList<>(),
            ActionBarPosition.CENTER,
            new ArrayList<>(),
            ActionBarPosition.RIGHT,
            new ArrayList<>());
    private final Map<ActionBarPosition, ActionBarSegment> lastSegments = new HashMap<>();
    private CodedString previousRawContent = null;
    private CodedString previousProcessedContent;

    public void registerSegment(ActionBarSegment segment) {
        allSegments.get(segment.getPosition()).add(segment);
    }

    @SubscribeEvent
    public void onActionBarUpdate(ChatPacketReceivedEvent.GameInfo event) {
        // FIXME: Reverse dependency!
        if (!Models.WorldState.onWorld()) return;

        CodedString content = CodedString.fromComponent(event.getMessage());
        if (content.equals(previousRawContent)) {
            // No changes, skip parsing
            if (!content.equals(previousProcessedContent)) {
                event.setMessage(previousProcessedContent.asComponent());
            }
            return;
        }
        previousRawContent = content;

        Matcher matcher = content.match(ACTIONBAR_PATTERN);
        if (!matcher.matches()) {
            WynntilsMod.warn("ActionBarHandler pattern failed to match: " + content);
            return;
        }

        // Create map of position -> matching part of the content
        Map<ActionBarPosition, CodedString> positionMatches = new HashMap<>();
        Arrays.stream(ActionBarPosition.values())
                .forEach(pos -> positionMatches.put(pos, CodedString.of(matcher.group(pos.name()))));

        Arrays.stream(ActionBarPosition.values()).forEach(pos -> processPosition(pos, positionMatches));

        StringBuilder newContentBuilder = new StringBuilder();
        if (!lastSegments.get(ActionBarPosition.LEFT).isHidden()) {
            newContentBuilder.append(positionMatches.get(ActionBarPosition.LEFT));
        }
        if (!lastSegments.get(ActionBarPosition.CENTER).isHidden()) {
            newContentBuilder.append(positionMatches.get(ActionBarPosition.CENTER));
        } else {
            // Add padding
            newContentBuilder.append(CENTER_PADDING);
        }
        if (!lastSegments.get(ActionBarPosition.RIGHT).isHidden()) {
            newContentBuilder.append(positionMatches.get(ActionBarPosition.RIGHT));
        }
        CodedString newContent = CodedString.of(newContentBuilder.toString());
        previousProcessedContent = newContent;

        if (!content.equals(newContent)) {
            event.setMessage(newContent.asComponent());
        }
    }

    private void processPosition(ActionBarPosition pos, Map<ActionBarPosition, CodedString> positionMatches) {
        List<ActionBarSegment> potentialSegments = allSegments.get(pos);
        for (ActionBarSegment segment : potentialSegments) {
            Matcher m = positionMatches.get(pos).match(segment.getPattern());
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
            }
        }
    }
}
