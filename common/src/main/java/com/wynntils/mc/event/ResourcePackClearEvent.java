/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import com.wynntils.core.events.WynntilsEvent;
import net.minecraftforge.eventbus.api.Cancelable;

@Cancelable
public class ResourcePackClearEvent extends WynntilsEvent {
    private final String hash;

    public ResourcePackClearEvent(String hash) {
        this.hash = hash;
    }

    public String getHash() {
        return hash;
    }
}
