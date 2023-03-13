/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.players;

import com.wynntils.core.config.Category;
import com.wynntils.core.config.Config;
import com.wynntils.core.config.ConfigCategory;
import com.wynntils.core.config.ConfigInfo;
import com.wynntils.core.features.UserFeature;
import com.wynntils.core.features.properties.StartDisabled;
import com.wynntils.mc.event.PlayerRenderLayerEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@StartDisabled
@ConfigCategory(Category.PLAYERS)
public class PlayerArmorHidingFeature extends UserFeature {
    @ConfigInfo
    public Config<Boolean> hideHelmets = new Config<>(true);

    @ConfigInfo
    public Config<Boolean> hideChestplates = new Config<>(true);

    @ConfigInfo
    public Config<Boolean> hideLeggings = new Config<>(true);

    @ConfigInfo
    public Config<Boolean> hideBoots = new Config<>(true);

    @ConfigInfo
    public Config<Boolean> showCosmetics = new Config<>(true);

    @SubscribeEvent
    public void onPlayerArmorRender(PlayerRenderLayerEvent.Armor event) {
        switch (event.getSlot()) {
            case HEAD -> {
                if (!hideHelmets) return;
                if (!showCosmetics) { // helmet is hidden regardless, no extra logic needed
                    event.setCanceled(true);
                    return;
                }

                // only cancel if helmet item isn't cosmetic - all helmet skins use a pickaxe texture
                ItemStack headItem = event.getPlayer().getItemBySlot(event.getSlot());
                if (headItem.getItem() != Items.DIAMOND_PICKAXE) event.setCanceled(true);
                return;
            }
            case CHEST -> {
                if (hideChestplates) event.setCanceled(true);
                return;
            }
            case LEGS -> {
                if (hideLeggings) event.setCanceled(true);
                return;
            }
            case FEET -> {
                if (hideBoots) event.setCanceled(true);
                return;
            }
        }
    }
}
