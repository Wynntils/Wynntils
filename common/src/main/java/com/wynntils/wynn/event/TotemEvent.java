/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.event;

import net.minecraftforge.eventbus.api.Event;

public abstract class TotemEvent extends Event {
    private final int totemNumber;

    protected TotemEvent(int totemNumber) {
        this.totemNumber = totemNumber;
    }

    public int getTotemNumber() {
        return totemNumber;
    }
}
