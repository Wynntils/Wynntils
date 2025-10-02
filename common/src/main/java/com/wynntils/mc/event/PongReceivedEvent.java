/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import com.wynntils.core.events.BaseEvent;
import com.wynntils.core.events.EventThread;

@EventThread(EventThread.Type.IO)
public class PongReceivedEvent extends BaseEvent {
    private final long time;

    public PongReceivedEvent(long time) {
        this.time = time;
    }

    public long getTime() {
        return time;
    }
}
