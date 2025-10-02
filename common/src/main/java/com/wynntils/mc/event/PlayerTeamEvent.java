/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import com.wynntils.core.events.BaseEvent;
import com.wynntils.core.events.OperationCancelable;
import net.minecraft.world.scores.PlayerTeam;

public abstract class PlayerTeamEvent extends BaseEvent implements OperationCancelable {
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
