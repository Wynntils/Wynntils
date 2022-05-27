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
import com.wynntils.mc.event.InventoryKeyPressEvent;
import com.wynntils.mc.utils.ItemUtils;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.wc.utils.ContainerUtils;
import com.wynntils.wc.utils.WynnUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@FeatureInfo(stability = Stability.STABLE, gameplay = GameplayImpact.SMALL, performance = PerformanceImpact.SMALL)
public class MythicBlockerFeature extends Feature {

    public MythicBlockerFeature() {
        setupEventListener();
    }

    @SubscribeEvent
    public void onChestCloseAttempt(InventoryKeyPressEvent e) {
        if (!WynnUtils.onWorld()) return;
        if (!McUtils.mc().options.keyInventory.matches(e.getKeyCode(), e.getScanCode())) return;
        if (!ContainerUtils.isLootOrRewardChest(McUtils.mc().screen)) return;

        NonNullList<ItemStack> items = ContainerUtils.getItems(McUtils.mc().screen);
        for (int i = 0; i < 27; i++) {
            ItemStack stack = items.get(i);
            if (ItemUtils.isMythic(stack)) {
                McUtils.sendMessageToClient(new TranslatableComponent("feature.wynntils.mythicBlocker.closingBlocked")
                        .withStyle(ChatFormatting.RED));
                e.setCanceled(true);
                return;
            }
        }
    }
}
