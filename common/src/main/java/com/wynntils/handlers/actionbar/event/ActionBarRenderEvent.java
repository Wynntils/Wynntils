/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.actionbar.event;

import com.wynntils.handlers.actionbar.ActionBarSegment;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import net.neoforged.bus.api.Event;

/**
 * This event is fired on every instance of action bar recalculation, effectively, acting as a render event.
 * It allows for the modification of the action bar segments that are displayed to the player.
 * If you want to listen to segment changes, you should use {@link ActionBarUpdatedEvent}.
 */
public class ActionBarRenderEvent extends Event {
    private final Map<ActionBarSegment, Boolean> segments;

    public ActionBarRenderEvent(List<ActionBarSegment> segments) {
        this.segments = segments.stream().collect(Collectors.toMap(segment -> segment, segment -> true));
    }

    public Set<ActionBarSegment> getSegments() {
        return segments.keySet();
    }

    public Set<ActionBarSegment> getEnabledSegments() {
        return segments.entrySet().stream()
                .filter(Map.Entry::getValue)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    public Set<ActionBarSegment> getDisabledSegments() {
        return segments.entrySet().stream()
                .filter(entry -> !entry.getValue())
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    public void setSegmentEnabled(ActionBarSegment segment, boolean enabled) {
        segments.put(segment, enabled);
    }
}
