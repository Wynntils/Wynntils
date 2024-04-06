/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@Cancelable
public class MouseScrollEvent extends Event {
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
