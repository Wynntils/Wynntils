/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.players;

import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.properties.StartDisabled;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.mc.event.PlayerRenderLayerEvent;
import com.wynntils.mc.extension.EntityRenderStateExtension;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;

@StartDisabled
@ConfigCategory(Category.PLAYERS)
public class PlayerArmorHidingFeature extends Feature {
    @Persisted
    private final Config<Boolean> hideHelmets = new Config<>(true);

    @Persisted
    private final Config<Boolean> hideChestplates = new Config<>(true);

    @Persisted
    private final Config<Boolean> hideLeggings = new Config<>(true);

    @Persisted
    private final Config<Boolean> hideBoots = new Config<>(true);

    @Persisted
    private final Config<Boolean> showCosmetics = new Config<>(true);

    @SubscribeEvent
    public void onPlayerArmorRender(PlayerRenderLayerEvent.Armor event) {
        switch (event.getSlot()) {
            case HEAD -> {
                if (!hideHelmets.get()) return;
                if (!showCosmetics.get()) { // helmet is hidden regardless, no extra logic needed
                    event.setCanceled(true);
                    return;
                }

                Entity entity = ((EntityRenderStateExtension) event.getPlayerRenderState()).getEntity();
                if (!(entity instanceof AbstractClientPlayer player)) return;

                // Only cancel if the helmet item isn't cosmetic.
                ItemStack headItem = player.getItemBySlot(event.getSlot());
                if (headItem.getItem() != Items.POTION) {
                    event.setCanceled(true);
                }
            }
            case CHEST -> {
                if (hideChestplates.get()) {
                    event.setCanceled(true);
                }
            }
            case LEGS -> {
                if (hideLeggings.get()) {
                    event.setCanceled(true);
                }
            }
            case FEET -> {
                if (hideBoots.get()) {
                    event.setCanceled(true);
                }
            }
        }
    }
}
