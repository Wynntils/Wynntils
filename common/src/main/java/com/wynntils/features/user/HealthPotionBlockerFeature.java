/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user;

import com.wynntils.core.config.Config;
import com.wynntils.core.features.UserFeature;
import com.wynntils.core.features.properties.FeatureInfo;
import com.wynntils.core.features.properties.FeatureInfo.Stability;
import com.wynntils.core.notifications.NotificationManager;
import com.wynntils.mc.event.PlayerInteractEvent;
import com.wynntils.mc.event.UseItemEvent;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.wynn.item.parsers.WynnItemMatchers;
import com.wynntils.wynn.model.ActionBarModel;
import com.wynntils.wynn.utils.WynnUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@FeatureInfo(stability = Stability.STABLE)
public class HealthPotionBlockerFeature extends UserFeature {
    @Config
    public static int threshold = 95;

    @SubscribeEvent
    public void onPotionUse(UseItemEvent event) {
        Component response = getBlockResponse();
        if (response != null) {
            event.setCanceled(true);
            NotificationManager.queueMessage(response);
        }
    }

    @SubscribeEvent
    public void onPotionUseOn(PlayerInteractEvent.RightClickBlock event) {
        Component response = getBlockResponse();
        if (response != null) {
            event.setCanceled(true);
            NotificationManager.queueMessage(response);
        }
    }

    private Component getBlockResponse() {
        if (!WynnUtils.onWorld()) return null;

        ItemStack stack = McUtils.inventory().getSelected();
        if (!WynnItemMatchers.isHealingPotion(stack)) return null;

        if (ActionBarModel.getCurrentHealth() * 100 < ActionBarModel.getMaxHealth() * threshold) return null;

        if (threshold < 100)
            return new TranslatableComponent("feature.wynntils.healthPotionBlocker.thresholdReached", threshold)
                    .withStyle(ChatFormatting.RED);
        return new TranslatableComponent("feature.wynntils.healthPotionBlocker.healthFull")
                .withStyle(ChatFormatting.RED);
    }
}
