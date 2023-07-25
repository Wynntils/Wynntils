/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.wynntils;

import com.wynntils.core.config.Category;
import com.wynntils.core.config.ConfigCategory;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.mc.event.AddEntityLookupEvent;
import com.wynntils.mc.event.BossHealthUpdateEvent;
import com.wynntils.mc.event.PlayerTeamEvent;
import com.wynntils.mc.event.SetEntityPassengersEvent;
import com.wynntils.mc.event.SetPlayerTeamEvent;
import com.wynntils.mc.mixin.accessors.ClientboundBossEventPacketAccessor;
import com.wynntils.utils.mc.McUtils;
import java.util.UUID;
import net.minecraft.network.protocol.game.ClientboundBossEventPacket;
import net.minecraft.network.protocol.game.ClientboundBossEventPacket.Operation;
import net.minecraft.network.protocol.game.ClientboundBossEventPacket.OperationType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@ConfigCategory(Category.WYNNTILS)
public class FixPacketBugsFeature extends Feature {
    private static final int METHOD_ADD = 0;

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onBossEventPackageReceived(BossHealthUpdateEvent event) {
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
        if (McUtils.mc().level == null) return;

        // Work around bug in Wynncraft that causes a lot of NPEs in Vanilla
        if (event.getMethod() != METHOD_ADD
                && McUtils.mc().level.getScoreboard().getPlayerTeam(event.getTeamName()) == null) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onRemovePlayerFromTeam(PlayerTeamEvent.Removed event) {
        if (McUtils.mc().level == null) return;

        // Work around bug in Wynncraft that causes NPEs in Vanilla
        PlayerTeam playerTeamFromUserName = McUtils.mc().level.getScoreboard().getPlayersTeam(event.getUsername());
        if (playerTeamFromUserName != event.getPlayerTeam()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onSetEntityPassengersPacket(SetEntityPassengersEvent event) {
        if (McUtils.mc().level == null) return;

        // Work around bug in Wynncraft that causes a lot of warnings in Vanilla
        Entity entity = McUtils.mc().level.getEntity(event.getVehicle());
        if (entity == null) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onAddEntityLookup(AddEntityLookupEvent event) {
        // Work around bug in Wynncraft that causes a lot of warnings in Vanilla
        if (event.getEntityMap().containsKey(event.getUUID())) {
            event.setCanceled(true);
        }
    }
}
