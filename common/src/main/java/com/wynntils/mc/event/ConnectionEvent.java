/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import net.minecraftforge.eventbus.api.Event;

public abstract class ConnectionEvent extends Event {
    public static class ConnectedEvent extends ConnectionEvent {
        private final String host;
        private final int port;

        public ConnectedEvent(String host, int port) {
            this.host = host;
            this.port = port;
        }

        public String getHost() {
            return host;
        }

        public int getPort() {
            return port;
        }
    }

    public static class DisconnectedEvent extends ConnectionEvent {}
}
