/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import com.wynntils.core.events.BaseEvent;
import com.wynntils.core.events.CancelRequestable;

public class DropHeldItemEvent extends BaseEvent implements CancelRequestable {
    private final boolean fullStack;

    public DropHeldItemEvent(boolean fullStack) {
        this.fullStack = fullStack;
    }

    public boolean isFullStack() {
        return fullStack;
    }
}
