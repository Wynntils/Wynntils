/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user;

import com.wynntils.core.config.Config;
import com.wynntils.core.features.UserFeature;
import com.wynntils.core.features.properties.FeatureInfo;
import com.wynntils.core.features.properties.FeatureInfo.Stability;
import com.wynntils.mc.event.PacketEvent.PacketSentEvent;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.wc.utils.ActionBarManager;
import com.wynntils.wc.utils.WynnItemMatchers;
import com.wynntils.wc.utils.WynnUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@FeatureInfo(stability = Stability.STABLE)
public class HealthPotionBlockerFeature extends UserFeature {

    @Config
    public static int threshold = 95;

    @SubscribeEvent
    public void onPotionUse(PacketSentEvent<ServerboundUseItemPacket> e) {
        Component response = getBlockResponse(e);
        if (response != null) {
            e.setCanceled(true);
            McUtils.sendMessageToClient(response);
        }
    }

    @SubscribeEvent
    public void onPotionUseOn(PacketSentEvent<ServerboundUseItemOnPacket> e) {
        if (getBlockResponse(e) != null) e.setCanceled(true);
    }

    private Component getBlockResponse(PacketSentEvent<?> e) {
        if (!WynnUtils.onWorld()) return null;

        ItemStack stack = McUtils.inventory().getSelected();
        if (!WynnItemMatchers.isHealingPotion(stack)) return null;

        if (ActionBarManager.getCurrentHealth() * 100 < ActionBarManager.getMaxHealth() * threshold) return null;

        if (threshold < 100)
            return new TranslatableComponent("feature.wynntils.healthPotionBlocker.thresholdReached", threshold)
                    .withStyle(ChatFormatting.RED);
        return new TranslatableComponent("feature.wynntils.healthPotionBlocker.healthFull")
                .withStyle(ChatFormatting.RED);
    }
}
