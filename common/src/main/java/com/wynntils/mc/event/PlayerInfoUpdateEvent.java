/*
 * Copyright © Wynntils 2025-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import java.util.EnumSet;
import java.util.List;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.neoforged.bus.api.Event;

public class PlayerInfoUpdateEvent extends Event {
    private EnumSet<ClientboundPlayerInfoUpdatePacket.Action> actions;
    private List<ClientboundPlayerInfoUpdatePacket.Entry> entries;
    private final List<ClientboundPlayerInfoUpdatePacket.Entry> newEntries;

    public PlayerInfoUpdateEvent(
            EnumSet<ClientboundPlayerInfoUpdatePacket.Action> actions,
            List<ClientboundPlayerInfoUpdatePacket.Entry> entries,
            List<ClientboundPlayerInfoUpdatePacket.Entry> newEntries) {
        this.actions = actions;
        this.entries = entries;
        this.newEntries = newEntries;
    }

    public EnumSet<ClientboundPlayerInfoUpdatePacket.Action> getActions() {
        return this.actions;
    }

    public List<ClientboundPlayerInfoUpdatePacket.Entry> getEntries() {
        return entries;
    }

    public List<ClientboundPlayerInfoUpdatePacket.Entry> getNewEntries() {
        return newEntries;
    }

    public void setEntries(List<ClientboundPlayerInfoUpdatePacket.Entry> entries) {
        this.entries = entries;
    }
}
