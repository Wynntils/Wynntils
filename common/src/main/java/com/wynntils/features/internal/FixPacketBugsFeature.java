/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.internal;

import com.wynntils.core.features.InternalFeature;
import com.wynntils.core.features.properties.EventListener;
import com.wynntils.core.features.properties.StartEnabled;
import com.wynntils.mc.event.BossHealthUpdateEvent;
import com.wynntils.mc.event.RemovePlayerFromTeamEvent;
import com.wynntils.mc.event.SetPlayerTeamEvent;
import com.wynntils.mc.mixin.accessors.ClientboundBossEventPacketAccessor;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.wc.utils.WynnUtils;
import java.util.UUID;
import net.minecraft.network.protocol.game.ClientboundBossEventPacket;
import net.minecraft.network.protocol.game.ClientboundBossEventPacket.Operation;
import net.minecraft.network.protocol.game.ClientboundBossEventPacket.OperationType;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@EventListener
@StartEnabled
public class FixPacketBugsFeature extends InternalFeature {
    public static FixPacketBugsFeature INSTANCE;

    private static final int METHOD_ADD = 0;

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onBossEventPackageReceived(BossHealthUpdateEvent event) {
        if (!WynnUtils.onServer()) return;

        ClientboundBossEventPacket packet = event.getPacket();
        Operation operation = ((ClientboundBossEventPacketAccessor) packet).getOperation();
        OperationType type = operation.getType();
        UUID id = ((ClientboundBossEventPacketAccessor) packet).getId();

        if (type != OperationType.ADD
                && type != OperationType.REMOVE
                && !event.getBossEvents().containsKey(id)) {
            // Any other operation than add/remove with invalid id will cause a NPE
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onSetPlayerTeamPacket(SetPlayerTeamEvent event) {
        if (!WynnUtils.onServer()) return;

        // Work around bug in Wynncraft that causes a lot of NPEs in Vanilla
        if (event.getMethod() != METHOD_ADD
                && McUtils.mc().level.getScoreboard().getPlayerTeam(event.getTeamName()) == null) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onRemovePlayerFromTeam(RemovePlayerFromTeamEvent event) {
        if (!WynnUtils.onServer()) return;

        // Work around bug in Wynncraft that causes NPEs in Vanilla
        PlayerTeam playerTeamFromUserName = McUtils.mc().level.getScoreboard().getPlayersTeam(event.getUsername());
        if (playerTeamFromUserName != event.getPlayerTeam()) {
            event.setCanceled(true);
        }
    }
}
