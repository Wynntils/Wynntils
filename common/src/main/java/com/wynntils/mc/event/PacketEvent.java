/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import net.minecraft.network.protocol.Packet;
import net.minecraftforge.eventbus.api.Event;

/**
 * Fires on packet sending or recieving
 *
 * Please do not misuse this class by looking for specific packet classes; instead create a unique
 * Event class in mc.event and add a mixin for the handler in ClientPacketListenerMixin.
 */
public class PacketEvent extends Event {
    private final Packet packet;

    public PacketEvent(Packet packet) {
        this.packet = packet;
    }

    public Packet getPacket() {
        return packet;
    }

    public static class PacketSentEvent extends PacketEvent {
        public PacketSentEvent(Packet packet) {
            super(packet);
        }
    }

    public static class PacketReceivedEvent extends PacketEvent {
        public PacketReceivedEvent(Packet packet) {
            super(packet);
        }
    }
}
