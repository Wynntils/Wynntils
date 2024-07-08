/*
 * Copyright © Wynntils 2022-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.actionbar;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Handler;
import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.actionbar.matchers.HealthBarSegmentMatcher;
import com.wynntils.handlers.actionbar.matchers.HealthTextSegmentMatcher;
import com.wynntils.handlers.actionbar.matchers.HotbarSegmentMatcher;
import com.wynntils.handlers.actionbar.matchers.ManaBarSegmentMatcher;
import com.wynntils.handlers.actionbar.matchers.ManaTextSegmentMatcher;
import com.wynntils.mc.event.ChatPacketReceivedEvent;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.utils.type.IterationDecision;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;

// FIXME: Clean up old classes
public final class ActionBarHandler extends Handler {
    // FIXME: Remove
    public void registerSegment(OldActionBarSegment segment) {}

    private static final ResourceLocation ACTION_BAR_FONT = ResourceLocation.withDefaultNamespace("hud/default/center");
    private static final ResourceLocation COORDINATES_FONT =
            ResourceLocation.withDefaultNamespace("hud/default/top_right");

    // Segment matcher order is the order they get parsed in
    // Currently, the text is parsed the last, as it's the most "unstable" segment
    private static final List<ActionBarSegmentMatcher> SEGMENT_MATCHERS = List.of(
            new HotbarSegmentMatcher(),
            new ManaBarSegmentMatcher(),
            new HealthBarSegmentMatcher(),
            new ManaTextSegmentMatcher(),
            new HealthTextSegmentMatcher());

    private static final FallBackSegmentMatcher FALLBACK_SEGMENT_MATCHER = new FallBackSegmentMatcher();

    private StyledText lastParsedActionBarText = StyledText.EMPTY;
    private StyledText lastRenderedActionBarText = StyledText.EMPTY;

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

        // Skip if the action bar text hasn't changed
        if (lastParsedActionBarText.equals(packetText)) {
            event.setMessage(lastRenderedActionBarText.getComponent());
            return;
        }

        lastParsedActionBarText = packetText;

        List<ActionBarSegment> matchedSegments = parseActionBarSegments(actionBarText);

        for (int i = 0; i < matchedSegments.size(); i++) {
            ActionBarSegment matchedSegment = matchedSegments.get(i);

            if (matchedSegment instanceof FallbackSegment fallbackSegment) {
                WynntilsMod.warn(
                        "Failed to match a portion of the action bar text: " + fallbackSegment.getSegmentText());
                // FIXME: Remove (temporarily only render parsed segments)
                actionBarText = actionBarText.replaceFirst(matchedSegment.getSegmentText(), "");
                continue;
            }

            // actionBarText = actionBarText.replaceFirst(matchedSegment.getSegmentText(), "<" + i + ">");
        }

        WynntilsMod.info("Matched segments: " + matchedSegments);

        StyledText renderedText =
                actionBarText.replaceAll("<\\d+>", "").replaceFirst("", "").append(coordinatesText);
        lastRenderedActionBarText = renderedText;

        event.setMessage(renderedText.getComponent());
    }

    @SubscribeEvent
    public void onWorldStateChange(WorldStateEvent event) {
        lastParsedActionBarText = StyledText.EMPTY;
    }

    public List<ActionBarSegment> parseActionBarSegments(StyledText actionBarText) {
        List<ActionBarSegment> matchedSegments = new ArrayList<>();

        for (ActionBarSegmentMatcher segmentMatcher : SEGMENT_MATCHERS) {
            ActionBarSegment parsedSegment = segmentMatcher.parse(actionBarText.getString());
            if (parsedSegment == null) continue;

            matchedSegments.add(parsedSegment);
            actionBarText = actionBarText.replaceFirst(parsedSegment.getSegmentText(), "");
        }

        String leftOverText = actionBarText.getString();

        // Check if there is any leftover text
        if (!leftOverText.isEmpty()) {
            matchedSegments.add(FALLBACK_SEGMENT_MATCHER.parse(leftOverText));
        }

        return matchedSegments;
    }

    /**
     * A fallback matcher that matches any action bar text that doesn't match any other segment.
     * This is used to prevent the action bar text from being lost if it doesn't match any other segment.
     */
    private static final class FallBackSegmentMatcher implements ActionBarSegmentMatcher {
        @Override
        public ActionBarSegment parse(String actionBar) {
            return new FallbackSegment(actionBar);
        }
    }

    private static final class FallbackSegment extends ActionBarSegment {
        private FallbackSegment(String segmentText) {
            super(segmentText);
        }

        @Override
        public String toString() {
            return "FallbackSegment{" + "segmentText='" + segmentText + '\'' + '}';
        }
    }
}
