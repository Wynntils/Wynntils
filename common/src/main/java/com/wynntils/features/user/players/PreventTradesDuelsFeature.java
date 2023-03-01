/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user.players;

import com.wynntils.core.components.Models;
import com.wynntils.core.features.UserFeature;
import com.wynntils.core.features.properties.FeatureCategory;
import com.wynntils.core.features.properties.FeatureInfo;
import com.wynntils.mc.event.PlayerAttackEvent;
import com.wynntils.mc.event.PlayerInteractEvent;
import com.wynntils.utils.wynn.WynnItemMatchers;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@FeatureInfo(category = FeatureCategory.PLAYERS)
public class PreventTradesDuelsFeature extends UserFeature {

    @SubscribeEvent
    public void onPlayerRightClick(PlayerInteractEvent.Interact event) {
        if (!shouldBlockClick(event.getPlayer(), event.getItemStack(), event.getTarget())) return;

        // stops interact packet from going out
        event.setCanceled(true);
    }

    @SubscribeEvent
    public void onPlayerLeftClick(PlayerAttackEvent event) {
        if (!shouldBlockClick(event.getPlayer(), event.getPlayer().getMainHandItem(), event.getTarget())) return;

        // stops attack packet from going out
        event.setCanceled(true);
    }

    private boolean shouldBlockClick(Player player, ItemStack itemStack, Entity target) {
        return player.isShiftKeyDown()
                && WynnItemMatchers.isWeapon(itemStack)
                && target instanceof Player p
                && Models.Player.isLocalPlayer(p);
    }
}
