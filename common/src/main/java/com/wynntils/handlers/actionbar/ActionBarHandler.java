/*
 * Copyright © Wynntils 2022-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.actionbar;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Handler;
import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.actionbar.matchers.HotbarSegmentMatcher;
import com.wynntils.mc.event.ChatPacketReceivedEvent;
import com.wynntils.utils.type.IterationDecision;
import com.wynntils.utils.type.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;

// FIXME: Clean up old classes
public final class ActionBarHandler extends Handler {
    // FIXME: Remove
    public void registerSegment(OldActionBarSegment segment) {}

    List<String> LEVEL_CHARS = Arrays.asList(
            "\uE000", "\uE001", "\uE002", "\uE003", "\uE004", "\uE005", "\uE006", "\uE007", "\uE008", "\uE009");

    Map<String, Character> DISPLAY_CHARS = Map.ofEntries(
            Map.entry("\uE010", '0'),
            Map.entry("\uE011", '1'),
            Map.entry("\uE012", '2'),
            Map.entry("\uE013", '3'),
            Map.entry("\uE014", '4'),
            Map.entry("\uE015", '5'),
            Map.entry("\uE016", '6'),
            Map.entry("\uE017", '7'),
            Map.entry("\uE018", '8'),
            Map.entry("\uE019", '9'),
            Map.entry("\uE01A", 'k'),
            Map.entry("\uE01B", 'm'),
            Map.entry("\uE01C", 'b'),
            Map.entry("\uE01D", 't'),
            Map.entry("\uE01E", '.'),
            Map.entry("\uE01F", '/'));

    private static final ResourceLocation ACTION_BAR_FONT = ResourceLocation.withDefaultNamespace("hud/default/center");
    private static final ResourceLocation COORDINATES_FONT =
            ResourceLocation.withDefaultNamespace("hud/default/top_right");

    private static final List<ActionBarSegmentMatcher> SEGMENT_MATCHERS = List.of(new HotbarSegmentMatcher());

    @SubscribeEvent
    public void onActionBarUpdate(ChatPacketReceivedEvent.GameInfo event) {
        // FIXME: Reverse dependency!
        if (!Models.WorldState.onWorld()) return;

        StyledText packetText = StyledText.fromComponent(event.getMessage());

        // Separate the action bar text from the coordinates
        StyledText actionBarText = packetText.iterate((part, changes) -> {
            if (!ACTION_BAR_FONT.equals(part.getPartStyle().getFont())) {
                changes.remove(part);
            }

            return IterationDecision.CONTINUE;
        });

        StyledText coordinatesText = packetText.iterate((part, changes) -> {
            if (!COORDINATES_FONT.equals(part.getPartStyle().getFont())) {
                changes.remove(part);
            }

            return IterationDecision.CONTINUE;
        });

        if (actionBarText.isEmpty()) {
            WynntilsMod.warn("Failed to find action bar text in packet: " + packetText.getString());
            return;
        }

        StringReader actionBarReader = new StringReader(actionBarText.getString());
        List<ActionBarSegment> matchedSegments = new ArrayList<>();
        for (ActionBarSegmentMatcher segmentMatcher : SEGMENT_MATCHERS) {
            // FIXME: Catch read errors
            ActionBarSegment parsedSegment = segmentMatcher.read(actionBarReader);
            if (parsedSegment == null) {
                WynntilsMod.warn("Failed to parse action bar segment: " + actionBarText.getString());
                return;
            }

            matchedSegments.add(parsedSegment);
            // FIXME: Remove
            actionBarText = actionBarText.replaceFirst(parsedSegment.getSegmentText(), "");
        }

        String leftOverText = actionBarReader.readRemaining();
        WynntilsMod.info("Leftover text: " + leftOverText);

        event.setMessage(actionBarText.append(coordinatesText).getComponent());
    }
}
