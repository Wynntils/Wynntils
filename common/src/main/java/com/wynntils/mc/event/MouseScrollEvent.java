/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import com.wynntils.core.events.BaseEvent;
import net.neoforged.bus.api.ICancellableEvent;

public class MouseScrollEvent extends BaseEvent implements ICancellableEvent {
    private final double windowPointer;
    private final double xOffset;
    private final double yOffset;

    public MouseScrollEvent(double windowPointer, double xOffset, double yOffset) {
        this.windowPointer = windowPointer;
        this.xOffset = xOffset;
        this.yOffset = yOffset;
    }

    public boolean isScrollingUp() {
        return yOffset > 0;
    }
}
