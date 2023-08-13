/*
 * Copyright © Wynntils 2021-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import com.wynntils.core.events.EventThread;
import net.minecraft.network.protocol.Packet;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.GenericEvent;

/**
 * Fires on packet sending or recieving
 *
 * <p>Please do not misuse this class by looking for specific packet classes; instead create a
 * unique Event class in mc.event and add a mixin for the handler in ClientPacketListenerMixin.
 */
public abstract class PacketEvent<T extends Packet<?>> extends GenericEvent<T> {
    private final T packet;

    protected PacketEvent(T packet) {
        super((Class<T>) packet.getClass());
        this.packet = packet;
    }

    public T getPacket() {
        return packet;
    }

    @Cancelable
    @EventThread(EventThread.Type.ANY)
    public static class PacketSentEvent<T extends Packet<?>> extends PacketEvent<T> {
        public PacketSentEvent(T packet) {
            super(packet);
        }
    }

    @Cancelable
    @EventThread(EventThread.Type.ANY)
    public static class PacketReceivedEvent<T extends Packet<?>> extends PacketEvent<T> {
        public PacketReceivedEvent(T packet) {
            super(packet);
        }
    }
}
