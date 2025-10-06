/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.players.event;

import com.wynntils.core.events.BaseEvent;

/**
 * These events correspond to data from PartyModel
 */
public abstract class PartyEvent extends BaseEvent {
    /**
     * Fired upon obtaining a new party list.
     * Get the party list from the party model manually if required.
     */
    public static final class Listed extends PartyEvent {}

    /**
     * Fired when someone invites you to their party
     * @field playerName the name of the player who invited your
     */
    public static final class Invited extends PartyEvent {
        private final String playerName;

        public Invited(String playerName) {
            this.playerName = playerName;
        }

        public String getPlayerName() {
            return playerName;
        }
    }

    /**
     * Fired upon someone else joining the user's party
     * @field playerName the name of the player who joined
     */
    public static final class OtherJoined extends PartyEvent {
        private final String playerName;

        public OtherJoined(String playerName) {
            this.playerName = playerName;
        }

        public String getPlayerName() {
            return playerName;
        }
    }

    /**
     * Fired upon someone else leaving the user's party
     * @field playerName the name of the player who left
     */
    public static final class OtherLeft extends PartyEvent {
        private final String playerName;

        public OtherLeft(String playerName) {
            this.playerName = playerName;
        }

        public String getPlayerName() {
            return playerName;
        }
    }

    /**
     * Fired upon a party member disconnecting
     * @field playerName the name of the player who disconnected
     */
    public static final class OtherDisconnected extends PartyEvent {
        private final String playerName;

        public OtherDisconnected(String playerName) {
            this.playerName = playerName;
        }

        public String getPlayerName() {
            return playerName;
        }
    }

    /**
     * Fired upon a party member reconnecting
     */
    public static final class OtherReconnected extends PartyEvent {
        private final String playerName;

        public OtherReconnected(String playerName) {
            this.playerName = playerName;
        }

        public String getPlayerName() {
            return playerName;
        }
    }

    public static final class PriorityChanged extends PartyEvent {
        private final String playerName;
        private final int priority;

        public PriorityChanged(String playerName, int priority) {
            this.playerName = playerName;
            this.priority = priority;
        }

        public String getPlayerName() {
            return playerName;
        }

        public int getPriority() {
            return priority;
        }
    }

    /**
     * Fired upon any member being promoted to party leader
     */
    public static final class Promoted extends PartyEvent {
        private final String oldLeader;
        private final String newLeader;

        public Promoted(String oldLeader, String newLeader) {
            this.oldLeader = oldLeader;
            this.newLeader = newLeader;
        }

        public String getOldLeader() {
            return oldLeader;
        }

        public String getNewLeader() {
            return newLeader;
        }
    }
}
