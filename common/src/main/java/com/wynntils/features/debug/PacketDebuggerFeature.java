/*
 * Copyright © Wynntils 2021-2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.debug;

import com.wynntils.core.Reference;
import com.wynntils.core.features.DebugFeatureBase;
import com.wynntils.mc.event.PacketEvent.PacketReceivedEvent;
import com.wynntils.mc.event.PacketEvent.PacketSentEvent;
import java.util.Arrays;
import java.util.List;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class PacketDebuggerFeature extends DebugFeatureBase {

    public static final boolean DEBUG_PACKETS = false;

    /* These packets just spam the log; ignore them. */
    private static final List<Class<? extends Packet<?>>> IGNORE_LIST = Arrays.asList(
            // General
            ServerboundKeepAlivePacket.class,
            ClientboundKeepAlivePacket.class,
            ClientboundSetTimePacket.class,
            ClientboundUpdateAdvancementsPacket.class,
            ClientboundUpdateAttributesPacket.class,
            ClientboundLevelParticlesPacket.class,
            ClientboundPlayerInfoPacket.class,
            ClientboundSetEquipmentPacket.class,
            // Chunks
            ClientboundForgetLevelChunkPacket.class,
            ClientboundLightUpdatePacket.class,
            ClientboundSetChunkCacheCenterPacket.class,
            // Entities
            ClientboundAddEntityPacket.class,
            ClientboundAddMobPacket.class,
            ClientboundMoveEntityPacket.Pos.class,
            ClientboundMoveEntityPacket.PosRot.class,
            ClientboundMoveEntityPacket.Rot.class,
            ClientboundRotateHeadPacket.class,
            ClientboundSetEntityDataPacket.class,
            ClientboundSetEntityMotionPacket.class,
            ClientboundTeleportEntityPacket.class,
            // Client movement
            ServerboundMovePlayerPacket.Pos.class,
            ServerboundMovePlayerPacket.PosRot.class,
            ServerboundMovePlayerPacket.Rot.class);

    public PacketDebuggerFeature() {
        setupEventListener();
    }

    private String describePacket(Packet<?> packet) {
        return ReflectionToStringBuilder.toString(packet, ToStringStyle.SHORT_PREFIX_STYLE)
                .replaceFirst("net\\.minecraft\\.network\\.protocol\\..*\\.", "");
    }

    @SubscribeEvent
    public void onPacketSent(PacketSentEvent<?> e) {
        if (!DEBUG_PACKETS) return;

        Packet<?> packet = e.getPacket();
        if (IGNORE_LIST.contains(packet.getClass())) return;

        Reference.LOGGER.info("SENT packet: " + describePacket(packet));
    }

    @SubscribeEvent
    public void onPacketReceived(PacketReceivedEvent<?> e) {
        if (!DEBUG_PACKETS) return;

        Packet<?> packet = e.getPacket();
        if (IGNORE_LIST.contains(packet.getClass())) return;

        Reference.LOGGER.info("RECV packet: " + describePacket(packet));
    }
}
