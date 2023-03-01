/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user;

import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Models;
import com.wynntils.core.config.Config;
import com.wynntils.core.features.UserFeature;
import com.wynntils.core.features.properties.FeatureInfo;
import com.wynntils.core.features.properties.FeatureInfo.Stability;
import com.wynntils.mc.event.PlayerInteractEvent;
import com.wynntils.mc.event.UseItemEvent;
import com.wynntils.models.items.items.game.CraftedConsumableItem;
import com.wynntils.models.items.items.game.HealthPotionItem;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.type.CappedValue;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@FeatureInfo(stability = Stability.STABLE)
public class HealthPotionBlockerFeature extends UserFeature {
    @Config
    public int threshold = 95;

    @SubscribeEvent
    public void onPotionUse(UseItemEvent event) {
        if (checkPotionUse()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onPotionUseOn(PlayerInteractEvent event) {
        if (checkPotionUse()) {
            event.setCanceled(true);
        }
    }

    private boolean checkPotionUse() {
        ItemStack itemStack = McUtils.inventory().getSelected();
        Optional<HealthPotionItem> potionOpt = Models.Item.asWynnItem(itemStack, HealthPotionItem.class);
        Optional<CraftedConsumableItem> craftedConsumableOpt =
                Models.Item.asWynnItem(itemStack, CraftedConsumableItem.class);
        if (potionOpt.isEmpty() && craftedConsumableOpt.isEmpty()) return false;

        // Check if crafted potion is a health potion
        if (craftedConsumableOpt.isPresent()) {
            if (!craftedConsumableOpt.get().isHealing()) return false;
        }

        CappedValue health = Models.Character.getHealth();
        int percentage = health.getPercentage();

        if (percentage >= threshold) {
            MutableComponent response = (percentage < 100)
                    ? Component.translatable("feature.wynntils.healthPotionBlocker.thresholdReached", percentage)
                    : Component.translatable("feature.wynntils.healthPotionBlocker.healthFull");
            Managers.Notification.queueMessage(response.withStyle(ChatFormatting.RED));
            return true;
        }

        return false;
    }
}
