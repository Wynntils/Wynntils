/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import java.util.Map;
import java.util.UUID;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.network.protocol.game.ClientboundBossEventPacket;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@Cancelable
public class BossHealthUpdateEvent extends Event {
    private final ClientboundBossEventPacket packet;
    private final Map<UUID, LerpingBossEvent> bossEvents;

    public BossHealthUpdateEvent(ClientboundBossEventPacket packet, Map<UUID, LerpingBossEvent> bossEvents) {
        this.packet = packet;
        this.bossEvents = bossEvents;
    }

    public ClientboundBossEventPacket getPacket() {
        return packet;
    }

    public Map<UUID, LerpingBossEvent> getBossEvents() {
        return bossEvents;
    }
}
