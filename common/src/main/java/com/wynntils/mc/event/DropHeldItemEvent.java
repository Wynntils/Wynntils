/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import com.wynntils.core.events.WynntilsEvent;
import net.minecraftforge.eventbus.api.Cancelable;

@Cancelable
public class DropHeldItemEvent extends WynntilsEvent {
    private final boolean fullStack;

    public DropHeldItemEvent(boolean fullStack) {
        this.fullStack = fullStack;
    }

    public boolean isFullStack() {
        return fullStack;
    }
}
