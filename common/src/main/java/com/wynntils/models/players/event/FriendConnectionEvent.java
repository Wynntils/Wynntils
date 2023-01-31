/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.players.event;

import net.minecraftforge.eventbus.api.Event;

public abstract class FriendConnectionEvent extends Event {
    private final String playerName;

    protected FriendConnectionEvent(String playerName) {
        this.playerName = playerName;
    }

    public String getPlayerName() {
        return playerName;
    }

    public static class Join extends FriendConnectionEvent {
        public Join(String playerName) {
            super(playerName);
        }
    }

    public static class Leave extends FriendConnectionEvent {
        public Leave(String playerName) {
            super(playerName);
        }
    }
}
