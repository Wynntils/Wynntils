/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.playerlist.event;

import java.util.List;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

public class PlayerListColumnUpdatedEvent extends Event implements ICancellableEvent {
    private final List<ClientboundPlayerInfoUpdatePacket.Entry> entries;

    protected PlayerListColumnUpdatedEvent(List<ClientboundPlayerInfoUpdatePacket.Entry> entries) {
        this.entries = entries;
    }

    public List<ClientboundPlayerInfoUpdatePacket.Entry> getEntries() {
        return entries;
    }

    /**
     * Indicates whether the list is exhaustive, and thus contains all friends or members of the party/guild.
     * @return true if we know that all names are included (i.e. strictly less than 19 players).
     */
    public boolean isExhaustive() {
        return this.entries.size() < 19;
    }

    public static class Friends extends PlayerListColumnUpdatedEvent {
        public Friends(List<ClientboundPlayerInfoUpdatePacket.Entry> entries) {
            super(entries);
        }
    }

    public static class Party extends PlayerListColumnUpdatedEvent {
        public Party(List<ClientboundPlayerInfoUpdatePacket.Entry> entries) {
            super(entries);
        }
    }

    public static class Guild extends PlayerListColumnUpdatedEvent {
        public Guild(List<ClientboundPlayerInfoUpdatePacket.Entry> entries) {
            super(entries);
        }
    }
}
