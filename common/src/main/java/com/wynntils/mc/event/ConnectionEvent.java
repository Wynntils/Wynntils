/*
 * Copyright © Wynntils 2021-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import com.wynntils.core.events.EventThread;
import net.neoforged.bus.api.Event;

public abstract class ConnectionEvent extends Event {
    @EventThread(EventThread.Type.RENDER)
    public static class ConnectingEvent extends ConnectionEvent {
        private final String host;
        private final int port;

        public ConnectingEvent(String host, int port) {
            this.host = host;
            this.port = port;
        }

        public String getHost() {
            return host;
        }

        public int getPort() {
            return port;
        }

        @Override
        public String toString() {
            return "ConnectedEvent{" + "host='" + host + '\'' + ", port=" + port + '}';
        }
    }

    @EventThread(EventThread.Type.RENDER)
    public static final class ConnectedEvent extends Event {}

    @EventThread(EventThread.Type.RENDER)
    public static class DisconnectedEvent extends ConnectionEvent {
        private final String reason;

        public DisconnectedEvent(String reason) {
            this.reason = reason;
        }

        public String getReason() {
            return reason;
        }
    }
}
