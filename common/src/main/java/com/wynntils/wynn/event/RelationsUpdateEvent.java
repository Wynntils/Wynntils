/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.event;

import java.util.Set;
import net.minecraftforge.eventbus.api.Event;

public abstract class RelationsUpdateEvent extends Event {
    private final Set<String> changedPlayers;
    private final ChangeType changeType;

    public RelationsUpdateEvent(Set<String> changedPlayers, ChangeType changeType) {
        this.changedPlayers = changedPlayers;
        this.changeType = changeType;
    }

    public Set<String> getChangedPlayers() {
        return changedPlayers;
    }

    public ChangeType getChangeType() {
        return changeType;
    }

    public static class FriendList extends RelationsUpdateEvent {
        public FriendList(Set<String> changedPlayers, ChangeType changeType) {
            super(changedPlayers, changeType);
        }
    }

    public static class PartyList extends RelationsUpdateEvent {

        public PartyList(Set<String> changedPlayers, ChangeType changeType) {
            super(changedPlayers, changeType);
        }
    }

    public enum ChangeType {
        ADD,
        REMOVE,
        RELOAD // This is used to indicate that we have a new fully parsed relations list
    }
}
