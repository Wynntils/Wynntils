/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

public class PongReceivedEvent extends Event implements ICancellableEvent {
    private final long time;

    public PongReceivedEvent(long time) {
        this.time = time;
    }

    public long getTime() {
        return time;
    }
}
