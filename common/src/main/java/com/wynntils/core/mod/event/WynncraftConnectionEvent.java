/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.mod.event;

import net.neoforged.bus.api.Event;

public abstract class WynncraftConnectionEvent extends Event {
    private final String host;

    protected WynncraftConnectionEvent(String host) {
        this.host = host;
    }

    public String getHost() {
        return host;
    }

    public static final class Connected extends WynncraftConnectionEvent {
        public Connected(String host) {
            super(host);
        }
    }

    public static final class Disconnected extends WynncraftConnectionEvent {
        public Disconnected(String host) {
            super(host);
        }
    }

    public static final class Connecting extends WynncraftConnectionEvent {
        public Connecting(String host) {
            super(host);
        }
    }

    public static final class ConnectingAborted extends WynncraftConnectionEvent {
        public ConnectingAborted(String host) {
            super(host);
        }
    }
}
