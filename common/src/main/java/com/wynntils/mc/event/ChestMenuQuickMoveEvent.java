/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import com.wynntils.core.events.BaseEvent;

public final class ChestMenuQuickMoveEvent extends BaseEvent {
    private final int containerId;

    public ChestMenuQuickMoveEvent(int containerId) {
        this.containerId = containerId;
    }

    public int getContainerId() {
        return containerId;
    }
}
