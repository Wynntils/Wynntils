/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import com.wynntils.core.events.BaseEvent;
import com.wynntils.core.events.CancelRequestable;
import com.wynntils.core.events.EventThread;
import java.util.UUID;

public abstract class ServerResourcePackEvent extends BaseEvent {
    /**
     * Fired on receiving {@link net.minecraft.network.protocol.common.ClientboundResourcePackPushPacket}
     */
    @EventThread(EventThread.Type.ANY)
    public static class Load extends ServerResourcePackEvent implements CancelRequestable {
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
