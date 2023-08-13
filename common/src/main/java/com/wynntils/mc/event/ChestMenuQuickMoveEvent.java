/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import net.minecraftforge.eventbus.api.Event;

public class ChestMenuQuickMoveEvent extends Event {
    private final int containerId;

    public ChestMenuQuickMoveEvent(int containerId) {
        this.containerId = containerId;
    }

    public int getContainerId() {
        return containerId;
    }
}
