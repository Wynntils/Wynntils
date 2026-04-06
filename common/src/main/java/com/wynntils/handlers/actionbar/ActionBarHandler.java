/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.actionbar;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Handler;
import com.wynntils.core.text.StyledText;
import com.wynntils.core.text.type.StyleType;
import com.wynntils.handlers.actionbar.event.ActionBarRenderEvent;
import com.wynntils.handlers.actionbar.event.ActionBarUpdatedEvent;
import com.wynntils.mc.event.SystemMessageEvent;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.utils.type.IterationDecision;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.SubscribeEvent;

public final class ActionBarHandler extends Handler {
    private static final FontDescription COORDINATES_FONT =
            new FontDescription.Resource(Identifier.withDefaultNamespace("hud/gameplay/default/top_right"));

    private final List<ActionBarSegmentMatcher> segmentMatchers = new ArrayList<>();

    private StyledText lastParsedActionBarText = StyledText.EMPTY;
    private List<ActionBarSegment> lastMatchedSegments = new ArrayList<>();

    public void registerSegment(ActionBarSegmentMatcher segmentMatcher) {
        segmentMatchers.add(segmentMatcher);
    }

    @SubscribeEvent
    public void onActionBarUpdate(SystemMessageEvent.GameInfoReceivedEvent event) {
        StyledText packetText = StyledText.fromComponent(event.getMessage());

        // Separate the action bar text from the coordinates
        StyledText actionBarText = packetText.iterate((part, changes) -> {
            if (part.getPartStyle().getFont().equals(COORDINATES_FONT)) {
                changes.remove(part);
            }

            return IterationDecision.CONTINUE;
        });

        if (actionBarText.isEmpty()) {
            WynntilsMod.warn("Failed to find action bar text in packet: " + packetText.getString());
            return;
        }

        List<ActionBarSegment> matchedSegments = matchSegments(actionBarText, packetText);
        ActionBarRenderEvent actionBarRenderEvent = new ActionBarRenderEvent(matchedSegments);
        WynntilsMod.postEvent(actionBarRenderEvent);

        // Remove disabled segments from the action bar text using index-based substring removal
        StyledText renderedText = removeDisabledSegments(actionBarText, actionBarRenderEvent.getDisabledSegments());

        // Append coordinates if needed
        if (actionBarRenderEvent.shouldRenderCoordinates()) {
            StyledText coordinatesText = packetText.iterate((part, changes) -> {
                if (!COORDINATES_FONT.equals(part.getPartStyle().getFont())) {
                    changes.remove(part);
                }

                return IterationDecision.CONTINUE;
            });

            renderedText = coordinatesText.append(renderedText);
        }

        if (packetText.equals(renderedText)) return;

        event.setMessage(renderedText.getComponent());
    }

    private List<ActionBarSegment> matchSegments(StyledText actionBarText, StyledText packetText) {
        List<ActionBarSegment> matchedSegments;

        // Skip parsing if the action bar text is the same as the last parsed one
        if (lastParsedActionBarText.equals(packetText)) {
            matchedSegments = lastMatchedSegments;
        } else {
            matchedSegments = parseActionBarSegments(actionBarText);

            lastParsedActionBarText = packetText;
            lastMatchedSegments = matchedSegments;

            if (WynntilsMod.isDevelopmentBuild() || WynntilsMod.isDevelopmentEnvironment()) {
                debugChecks(matchedSegments, actionBarText);
            }

            WynntilsMod.postEvent(new ActionBarUpdatedEvent(matchedSegments));
        }

        return matchedSegments;
    }

    @SubscribeEvent
    public void onWorldStateChange(WorldStateEvent event) {
        lastParsedActionBarText = StyledText.EMPTY;
        lastMatchedSegments = new ArrayList<>();
    }

    private List<ActionBarSegment> parseActionBarSegments(StyledText actionBarText) {
        List<ActionBarSegment> matchedSegments = new ArrayList<>();

        // Pass the full StyledText to each matcher, which will use getStringWithoutFormatting()
        // to match patterns that may span multiple styled text parts
        for (ActionBarSegmentMatcher segmentMatcher : segmentMatchers) {
            ActionBarSegment parsedSegment = segmentMatcher.parse(actionBarText);
            if (parsedSegment == null) continue;

            matchedSegments.add(parsedSegment);
        }

        // Sort matched segments by their start index
        matchedSegments.sort(Comparator.comparingInt(ActionBarSegment::getStartIndex));

        // Find unmatched regions and create fallback segments for them
        String unformattedText = actionBarText.getStringWithoutFormatting();
        int currentIndex = 0;

        List<ActionBarSegment> fallbackSegments = new ArrayList<>();
        for (ActionBarSegment segment : matchedSegments) {
            if (segment.getStartIndex() > currentIndex) {
                String leftoverText = unformattedText.substring(currentIndex, segment.getStartIndex());
                if (!leftoverText.isEmpty()) {
                    fallbackSegments.add(new FallbackSegment(leftoverText, currentIndex, segment.getStartIndex()));
                }
            }
            currentIndex = Math.max(currentIndex, segment.getEndIndex());
        }

        // Check for any trailing unmatched text
        if (currentIndex < unformattedText.length()) {
            String leftoverText = unformattedText.substring(currentIndex);
            if (!leftoverText.isEmpty()) {
                fallbackSegments.add(new FallbackSegment(leftoverText, currentIndex, unformattedText.length()));
            }
        }

        matchedSegments.addAll(fallbackSegments);

        return matchedSegments;
    }

    /**
     * Removes disabled segments from the action bar text using index-based substring operations.
     * This avoids the multi-part matching issue that replaceFirst has.
     */
    private static StyledText removeDisabledSegments(StyledText actionBarText, Set<ActionBarSegment> disabledSegments) {
        if (disabledSegments.isEmpty()) return actionBarText;

        // Sort disabled segments by start index in reverse order so we can remove from back to front
        // without invalidating earlier indices
        List<ActionBarSegment> sortedDisabled = disabledSegments.stream()
                .sorted(Comparator.comparingInt(ActionBarSegment::getStartIndex).reversed())
                .toList();

        for (ActionBarSegment segment : sortedDisabled) {
            int textLength = actionBarText.length(StyleType.NONE);
            int startIndex = segment.getStartIndex();
            int endIndex = segment.getEndIndex();

            // Clamp indices to valid range
            startIndex = Math.min(startIndex, textLength);
            endIndex = Math.min(endIndex, textLength);

            if (startIndex >= endIndex) continue;

            // Build new text by concatenating the parts before and after the disabled segment
            StyledText before =
                    startIndex > 0 ? actionBarText.substring(0, startIndex, StyleType.NONE) : StyledText.EMPTY;
            StyledText after = endIndex < textLength
                    ? actionBarText.substring(endIndex, textLength, StyleType.NONE)
                    : StyledText.EMPTY;

            actionBarText = StyledText.concat(before, after);
        }

        return actionBarText;
    }

    private static void debugChecks(List<ActionBarSegment> matchedSegments, StyledText actionBarText) {
        List<ActionBarSegment> fallbackSegments = matchedSegments.stream()
                .filter(segment -> segment instanceof FallbackSegment)
                .toList();

        fallbackSegments.forEach(segment ->
                WynntilsMod.warn("Failed to match a portion of the action bar text, using a fallback: " + segment));

        if (!fallbackSegments.isEmpty()) {
            WynntilsMod.warn("Action bar text: " + actionBarText.getString(StyleType.COMPLETE));
        }
    }

    private static final class FallbackSegment extends ActionBarSegment {
        private FallbackSegment(String segmentText, int startIndex, int endIndex) {
            super(segmentText, startIndex, endIndex);
        }

        @Override
        public String toString() {
            return "FallbackSegment{" + "segmentText='" + segmentText + '\'' + '}';
        }
    }
}
