/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features;

import com.wynntils.core.features.FeatureBase;
import com.wynntils.core.features.properties.FeatureInfo;
import com.wynntils.core.features.properties.GameplayImpact;
import com.wynntils.core.features.properties.PerformanceImpact;
import com.wynntils.core.features.properties.Stability;
import com.wynntils.mc.event.InventoryKeyPressEvent;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.wc.utils.WynnUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@FeatureInfo(stability = Stability.STABLE, gameplay = GameplayImpact.SMALL, performance = PerformanceImpact.SMALL)
public class MythicBlockerFeature extends FeatureBase {

    public MythicBlockerFeature() {
        setupEventListener();
    }

    @SubscribeEvent
    public void onChestCloseAttempt(InventoryKeyPressEvent e) {
        if (!WynnUtils.onWorld() || !McUtils.mc().options.keyInventory.matches(e.getKeyCode(), e.getScanCode())) return;
        if (!(McUtils.mc().screen instanceof AbstractContainerScreen<?>)) return;
        String title = McUtils.mc().screen.getTitle().getString();
        if (!title.startsWith("Loot Chest")
                && !title.startsWith("Daily Rewards")
                && !title.contains("Objective Rewards")) return;

        AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>) McUtils.mc().screen;
        for (int i = 0; i < 27; i++) {
            ItemStack stack = screen.getMenu().getItems().get(i);
            if (stack.getDisplayName().getString().contains(ChatFormatting.DARK_PURPLE.toString())) {
                McUtils.sendMessageToClient(new TranslatableComponent("feature.wynntils.mythicBlocker.closingBlocked")
                        .withStyle(ChatFormatting.RED));
                e.setCanceled(true);
                return;
            }
        }
    }
}
