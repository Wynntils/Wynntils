/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import com.wynntils.core.events.EventThread;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

public abstract class ServerResourcePackEvent extends Event {
    /**
     * Fired on receiving {@link net.minecraft.network.protocol.common.ClientboundResourcePackPacket}
     */
    @EventThread(EventThread.Type.ANY)
    @Cancelable
    public static class Load extends ServerResourcePackEvent {
        private final String url;
        private final String hash;
        private final boolean required;

        public Load(String url, String hash, boolean required) {
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

    @EventThread(EventThread.Type.ANY)
    public static class Clear extends ServerResourcePackEvent {}
}
