/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import com.wynntils.core.events.WynntilsEvent;

public class ChestMenuQuickMoveEvent extends WynntilsEvent {
    private final int containerId;

    public ChestMenuQuickMoveEvent(int containerId) {
        this.containerId = containerId;
    }

    public int getContainerId() {
        return containerId;
    }
}
