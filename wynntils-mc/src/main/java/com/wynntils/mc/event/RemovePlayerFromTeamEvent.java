/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import net.minecraft.world.scores.PlayerTeam;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@Cancelable
public class RemovePlayerFromTeamEvent extends Event {
    private final String username;
    private final PlayerTeam playerTeam;

    public RemovePlayerFromTeamEvent(String username, PlayerTeam playerTeam) {
        this.username = username;
        this.playerTeam = playerTeam;
    }

    public String getUsername() {
        return username;
    }

    public PlayerTeam getPlayerTeam() {
        return playerTeam;
    }
}
