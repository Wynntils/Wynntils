/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@Cancelable
public class DropHeldItemEvent extends Event {
    private final boolean fullStack;

    public DropHeldItemEvent(boolean fullStack) {
        this.fullStack = fullStack;
    }

    public boolean isFullStack() {
        return fullStack;
    }
}
