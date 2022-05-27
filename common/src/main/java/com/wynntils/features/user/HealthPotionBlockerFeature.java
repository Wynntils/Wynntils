/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user;

import com.wynntils.core.features.UserFeature;
import com.wynntils.core.features.properties.EventListener;
import com.wynntils.core.features.properties.FeatureInfo;
import com.wynntils.core.features.properties.FeatureInfo.GameplayImpact;
import com.wynntils.core.features.properties.FeatureInfo.PerformanceImpact;
import com.wynntils.core.features.properties.FeatureInfo.Stability;
import com.wynntils.mc.event.PacketEvent.PacketSentEvent;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.wc.utils.WynnItemMatchers;
import com.wynntils.wc.utils.WynnUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@EventListener
@FeatureInfo(stability = Stability.STABLE, gameplay = GameplayImpact.MEDIUM, performance = PerformanceImpact.MEDIUM)
public class HealthPotionBlockerFeature extends UserFeature {
    public static HealthPotionBlockerFeature INSTANCE;

    @SubscribeEvent
    public void onPotionUse(PacketSentEvent<ServerboundUseItemPacket> e) {
        if (!WynnUtils.onWorld()) return;

        ItemStack stack = McUtils.inventory().getSelected();
        if (!WynnItemMatchers.isHealingPotion(stack)) return;

        if (McUtils.player().getHealth() == McUtils.player().getMaxHealth()) {
            e.setCanceled(true);
            McUtils.sendMessageToClient(new TranslatableComponent("feature.wynntils.potionBlocker.healthFull")
                    .withStyle(ChatFormatting.RED));
        }
    }
}
