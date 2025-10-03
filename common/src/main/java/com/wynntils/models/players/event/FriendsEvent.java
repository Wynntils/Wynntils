/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.players.event;

import com.wynntils.core.events.BaseEvent;

/**
 * These events correspond to data from FriendsModel
 */
public abstract class FriendsEvent extends BaseEvent {
    /**
     * Fired upon obtaining a new friends list.
     * Get the friends list from the friends model manually if required.
     */
    public static final class Listed extends FriendsEvent {}

    /**
     * Fired upon obtaining new online friend list.
     */
    public static final class OnlineListed extends FriendsEvent {}

    /**
     * Fired upon the user adding someone to their friends list
     * @field playerName the name of the player who was added
     */
    public static final class Added extends FriendsEvent {
        private final String playerName;

        public Added(String playerName) {
            this.playerName = playerName;
        }

        public String getPlayerName() {
            return playerName;
        }
    }

    /**
     * Fired upon the user removing someone from their friends list
     * @field playerName the name of the player who was removed
     */
    public static final class Removed extends FriendsEvent {
        private final String playerName;

        public Removed(String playerName) {
            this.playerName = playerName;
        }

        public String getPlayerName() {
            return playerName;
        }
    }

    /**
     * Fired upon a friend disconnecting
     * @field playerName the name of the player who disconnected
     */
    public static final class Left extends FriendsEvent {
        private final String playerName;

        public Left(String playerName) {
            this.playerName = playerName;
        }

        public String getPlayerName() {
            return playerName;
        }
    }

    /**
     * Fired upon a friend connecting
     * @field playerName the name of the player who connected
     */
    public static final class Joined extends FriendsEvent {
        private final String playerName;
        private final String server;

        public Joined(String playerName, String server) {
            this.playerName = playerName;
            this.server = server;
        }

        public String getPlayerName() {
            return playerName;
        }

        public String getServer() {
            return server;
        }
    }
}
