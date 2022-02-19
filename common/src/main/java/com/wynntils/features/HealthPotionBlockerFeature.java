/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features;

import com.google.common.collect.ImmutableList;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.features.Feature;
import com.wynntils.core.features.properties.FeatureInfo;
import com.wynntils.core.features.properties.GameplayImpact;
import com.wynntils.core.features.properties.PerformanceImpact;
import com.wynntils.core.features.properties.Stability;
import com.wynntils.mc.event.PacketEvent.PacketSentEvent;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.wc.utils.WynnItemMatchers;
import com.wynntils.wc.utils.WynnUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@FeatureInfo(
        stability = Stability.STABLE,
        gameplay = GameplayImpact.MEDIUM,
        performance = PerformanceImpact.MEDIUM)
public class HealthPotionBlockerFeature extends Feature {
    @Override
    protected void init(ImmutableList.Builder<Condition> conditions) {}

    @Override
    protected boolean onEnable() {
        WynntilsMod.getEventBus().register(this);
        return true;
    }

    @Override
    protected void onDisable() {
        WynntilsMod.getEventBus().unregister(this);
    }

    @SubscribeEvent
    public void onPotionUse(PacketSentEvent<ServerboundUseItemPacket> e) {
        if (!WynnUtils.onWorld()) return;

        ItemStack stack = McUtils.inventory().getSelected();

        // TODO check for crafting potion
        if (!WynnItemMatchers.isHealingPotion(stack)) return;

        if (McUtils.player().getHealth() == McUtils.player().getMaxHealth()) {
            e.setCanceled(true);
            McUtils.sendMessageToClient(
                    new TextComponent(ChatFormatting.DARK_RED + "You are already at full health!"));
        }
    }
}
