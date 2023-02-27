/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user.players;

import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Models;
import com.wynntils.core.config.Config;
import com.wynntils.core.features.UserFeature;
import com.wynntils.core.features.properties.FeatureCategory;
import com.wynntils.core.features.properties.FeatureInfo;
import com.wynntils.mc.event.PlayerAttackEvent;
import com.wynntils.mc.event.PlayerInteractEvent;
import com.wynntils.utils.wynn.WynnItemMatchers;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@FeatureInfo(category = FeatureCategory.PLAYERS)
public class PreventTradesDuelsFeature extends UserFeature {
    private static final int FIGHTING_CUTOFF = 30; // 30 seconds

    @Config
    public boolean onlyWhileFighting = true;

    @SubscribeEvent
    public void onPlayerRightClick(PlayerInteractEvent.Interact event) {
        handlePlayerClick(event, event.getPlayer(), event.getItemStack(), event.getTarget());
    }

    @SubscribeEvent
    public void onPlayerLeftClick(PlayerAttackEvent event) {
        handlePlayerClick(event, event.getPlayer(), event.getPlayer().getMainHandItem(), event.getTarget());
    }

    private void handlePlayerClick(Event event, Player player, ItemStack itemStack, Entity target) {
        int timeSinceLastFight = (int) ((System.currentTimeMillis() - Models.CombatXp.getLastXpGainTimestamp()) / 1000);
        if (onlyWhileFighting && timeSinceLastFight >= FIGHTING_CUTOFF) return;

        if (!shouldBlockClick(player, itemStack, target)) return;

        // stops interact packet from going out
        event.setCanceled(true);

        if (onlyWhileFighting) {
            Managers.Notification.queueMessage(
                    "Trade/Duel blocked for " + (FIGHTING_CUTOFF - timeSinceLastFight) + " s");
        }
    }

    private boolean shouldBlockClick(Player player, ItemStack item, Entity target) {
        return player.isShiftKeyDown()
                && WynnItemMatchers.isWeapon(item)
                && target instanceof Player p
                && Models.Player.isLocalPlayer(p);
    }
}
