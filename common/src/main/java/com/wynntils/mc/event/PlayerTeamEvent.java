/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import net.minecraft.world.scores.PlayerTeam;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@Cancelable
public abstract class PlayerTeamEvent extends Event {
    private final String username;
    private final PlayerTeam playerTeam;

    protected PlayerTeamEvent(String username, PlayerTeam playerTeam) {
        this.username = username;
        this.playerTeam = playerTeam;
    }

    public String getUsername() {
        return username;
    }

    public PlayerTeam getPlayerTeam() {
        return playerTeam;
    }

    public static final class Added extends PlayerTeamEvent {
        public Added(String username, PlayerTeam playerTeam) {
            super(username, playerTeam);
        }
    }

    public static final class Removed extends PlayerTeamEvent {
        public Removed(String username, PlayerTeam playerTeam) {
            super(username, playerTeam);
        }
    }
}
