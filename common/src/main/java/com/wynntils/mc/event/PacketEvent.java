/*
 * Copyright © Wynntils 2021-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import com.wynntils.core.events.BaseEvent;
import com.wynntils.core.events.CancelRequestable;
import com.wynntils.core.events.EventThread;
import net.minecraft.network.protocol.Packet;

/**
 * Fires on packet sending or receiving
 *
 * <p>Please do not misuse this class by looking for specific packet classes; instead create a
 * unique event class in mc.event and add a mixin for the handler in ClientPacketListenerMixin.
 */
public abstract class PacketEvent<T extends Packet<?>> extends BaseEvent {
    private final T packet;

    protected PacketEvent(T packet) {
        this.packet = packet;
    }

    public T getPacket() {
        return packet;
    }

    @EventThread(EventThread.Type.ANY)
    public static final class PacketSentEvent<T extends Packet<?>> extends PacketEvent<T> implements CancelRequestable {
        public PacketSentEvent(T packet) {
            super(packet);
        }
    }

    @EventThread(EventThread.Type.ANY)
    public static final class PacketReceivedEvent<T extends Packet<?>> extends PacketEvent<T>
            implements CancelRequestable {
        public PacketReceivedEvent(T packet) {
            super(packet);
        }
    }
}
