/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features;

import com.wynntils.core.features.Feature;
import com.wynntils.core.features.properties.FeatureInfo;
import com.wynntils.core.features.properties.GameplayImpact;
import com.wynntils.core.features.properties.PerformanceImpact;
import com.wynntils.core.features.properties.Stability;
import com.wynntils.mc.event.PacketEvent;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.wc.utils.ItemMatchers;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@FeatureInfo(
        stability = Stability.STABLE,
        gameplay = GameplayImpact.MEDIUM,
        performance = PerformanceImpact.MEDIUM)
public class HealthPotionBlockerFeature extends Feature {
    @SubscribeEvent
    public void onPotionUse(PacketEvent<ServerboundUseItemPacket> e) {
        ItemStack stack = McUtils.inventory().getCarried();

        if (!ItemMatchers.isHealingPotion(stack)) return;

        // Could it be better to parse it from the health bar or health
        // TODO find out which one is better
        if (McUtils.player().getHealth() == McUtils.player().getMaxHealth()) {
            e.setCanceled(true);
        }
    }
}
