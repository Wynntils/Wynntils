/*
 * Copyright © Wynntils 2021-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import com.wynntils.core.events.EventThread;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

/** Fires on receiving {@link net.minecraft.network.protocol.game.ClientboundResourcePackPacket} */
@EventThread(EventThread.Type.IO)
@Cancelable
public class ResourcePackEvent extends Event {
    private final String url;
    private final String hash;
    private final boolean required;

    public ResourcePackEvent(String url, String hash, boolean required) {
        this.url = url;
        this.hash = hash;
        this.required = required;
    }

    public String getUrl() {
        return url;
    }

    public String getHash() {
        return hash;
    }

    public boolean isRequired() {
        return required;
    }
}
