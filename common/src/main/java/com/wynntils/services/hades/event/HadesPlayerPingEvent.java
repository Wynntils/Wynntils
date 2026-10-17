/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.hades.event;

import com.wynntils.services.hades.type.PlayerPingData;
import net.neoforged.bus.api.Event;

public abstract class HadesPlayerPingEvent extends Event {
    private final PlayerPingData playerPingData;

    protected HadesPlayerPingEvent(PlayerPingData playerPingData) {
        this.playerPingData = playerPingData;
    }

    public PlayerPingData getPlayerPingData() {
        return playerPingData;
    }

    public static class Self extends HadesPlayerPingEvent {
        public Self(PlayerPingData playerPingData) {
            super(playerPingData);
        }
    }

    public static class Other extends HadesPlayerPingEvent {
        public Other(PlayerPingData playerPingData) {
            super(playerPingData);
        }
    }
}
