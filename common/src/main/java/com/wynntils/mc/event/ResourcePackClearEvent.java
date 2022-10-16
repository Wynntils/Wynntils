/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@Cancelable
public class ResourcePackClearEvent extends Event {
    private final String hash;

    public ResourcePackClearEvent(String hash) {
        this.hash = hash;
    }

    public String getHash() {
        return hash;
    }
}
