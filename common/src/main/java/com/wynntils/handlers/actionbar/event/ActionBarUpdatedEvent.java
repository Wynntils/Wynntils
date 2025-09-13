/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.actionbar.event;

import com.wynntils.handlers.actionbar.ActionBarSegment;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import net.neoforged.bus.api.Event;

/**
 * Fired when the action bar is updated. This event can be used for update hooks for various segments.
 * Check out {@link ActionBarRenderEvent} for changing what is displayed on the action bar.
 */
public class ActionBarUpdatedEvent extends Event {
    private final List<ActionBarSegment> segments;

    public ActionBarUpdatedEvent(List<ActionBarSegment> segments) {
        this.segments = Collections.unmodifiableList(segments);
    }

    public List<ActionBarSegment> getSegments() {
        return segments;
    }

    public <T extends ActionBarSegment> void runIfPresent(Class<T> clazz, Consumer<T> consumer) {
        runIfPresentOrElse(clazz, consumer, () -> {});
    }

    public <T extends ActionBarSegment> void runIfPresentOrElse(
            Class<T> clazz, Consumer<T> consumer, Runnable ifNotPresent) {
        segments.stream()
                .filter(clazz::isInstance)
                .map(clazz::cast)
                .findFirst()
                .ifPresentOrElse(consumer, ifNotPresent);
    }
}
