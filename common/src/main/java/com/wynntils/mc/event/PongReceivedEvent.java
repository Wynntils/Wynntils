/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import com.wynntils.core.events.EventThread;
import net.neoforged.bus.api.Event;

@EventThread(EventThread.Type.IO)
public class PongReceivedEvent extends Event {
    private final long time;

    public PongReceivedEvent(long time) {
        this.time = time;
    }

    public long getTime() {
        return time;
    }
}
