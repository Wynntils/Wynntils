/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import java.util.List;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.neoforged.bus.api.Event;

public class PlayerInfoUpdateEvent extends Event {
    private List<ClientboundPlayerInfoUpdatePacket.Entry> entries;
    private final List<ClientboundPlayerInfoUpdatePacket.Entry> newEntries;

    public PlayerInfoUpdateEvent(
            List<ClientboundPlayerInfoUpdatePacket.Entry> entries,
            List<ClientboundPlayerInfoUpdatePacket.Entry> newEntries) {
        this.entries = entries;
        this.newEntries = newEntries;
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
