/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import net.minecraft.server.packs.repository.Pack;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@Cancelable
public class ResourcePackClearEvent extends Event {
    private final Pack serverPack;

    public ResourcePackClearEvent(Pack serverPack) {
        this.serverPack = serverPack;
    }

    public Pack getServerPack() {
        return serverPack;
    }
}
