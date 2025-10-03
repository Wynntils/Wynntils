/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.worlds.event;

import com.wynntils.core.events.BaseEvent;

public final class StreamModeEvent extends BaseEvent {
    private final boolean enabled;

    public StreamModeEvent(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
