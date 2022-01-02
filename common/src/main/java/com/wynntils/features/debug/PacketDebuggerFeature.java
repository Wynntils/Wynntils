/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.debug;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import com.wynntils.core.features.AbstractFeature;
import com.wynntils.mc.event.PacketEvent.PacketReceivedEvent;
import com.wynntils.mc.event.PacketEvent.PacketSentEvent;
import net.minecraft.network.protocol.Packet;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

public class PacketDebuggerFeature extends AbstractFeature {
    public static final boolean DEBUG_PACKETS = true;

    private String describePacket(Packet packet) {
        return ReflectionToStringBuilder.toString(packet, SHORT_PREFIX_STYLE)
                .replaceFirst("net\\.minecraft\\.network\\.protocol\\..*\\.", "");
    }

    @SubscribeEvent
    public void onPacketSent(PacketSentEvent e) {
        if (!DEBUG_PACKETS) return;

        System.out.println("SENT packet: " + describePacket(e.getPacket()));
    }

    @SubscribeEvent
    public void onPacketReceived(PacketReceivedEvent e) {
        if (!DEBUG_PACKETS) return;

        System.out.println("RECV packet: " + describePacket(e.getPacket()));
    }
}
