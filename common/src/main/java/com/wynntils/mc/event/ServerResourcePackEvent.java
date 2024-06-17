/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import com.wynntils.core.events.EventThread;
import java.util.UUID;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

public abstract class ServerResourcePackEvent extends Event {
    /**
     * Fired on receiving {@link net.minecraft.network.protocol.common.ClientboundResourcePackPushPacket}
     */
    @EventThread(EventThread.Type.ANY)
    @Cancelable
    public static class Load extends ServerResourcePackEvent {
        private final UUID id;
        private final String url;
        private final String hash;
        private final boolean required;

        public Load(UUID id, String url, String hash, boolean required) {
            this.id = id;
            this.url = url;
            this.hash = hash;
            this.required = required;
        }

        public UUID getId() {
            return id;
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
