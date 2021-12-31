/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features;

import com.wynntils.WynntilsMod;
import com.wynntils.framework.feature.Feature;
import com.wynntils.framework.feature.GameplayImpact;
import com.wynntils.framework.feature.PerformanceImpact;
import com.wynntils.framework.feature.Stability;
import com.wynntils.mc.utils.DynamicTag;
import com.wynntils.wc.utils.ItemMatchers;
import com.wynntils.wc.utils.InventoryData;
import com.wynntils.mc.event.InventoryRenderEvent;
import com.wynntils.mc.utils.ItemUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class SoulPointTimerFeature extends Feature {
    @SubscribeEvent
    public static void onInventoryRender(InventoryRenderEvent e) {
        if (!WynntilsMod.onWorld()) return;

        Slot hoveredSlot = e.getHoveredSlot();
        if (hoveredSlot == null || !hoveredSlot.hasItem()) return;

        ItemStack stack = hoveredSlot.getItem();

        if (ItemUtils.hasMarker(stack, "soulpoints")) return;
        if (!ItemMatchers.isSoulPoint(stack)) return;

        ListTag lore = ItemUtils.getLoreTag(stack);

        if (lore == null) {
            lore = new ListTag();
        } else {
            lore.add(StringTag.valueOf("")); // Equivalent to adding "'
        }

        lore.add(
                new DynamicTag(
                        () -> {
                            int rawSecondsUntilSoulPoint =
                                    InventoryData.getTicksTillNextSoulPoint() / 20;
                            int minutesUntilSoulPoint = rawSecondsUntilSoulPoint / 60;
                            int secondsUntilSoulPoint = rawSecondsUntilSoulPoint % 60;

                            return ItemUtils.toLoreForm(
                                    ChatFormatting.AQUA
                                            + "Time until next soul point: "
                                            + ChatFormatting.WHITE
                                            + minutesUntilSoulPoint
                                            + ":"
                                            + String.format("%02d", secondsUntilSoulPoint));
                        }));

        ItemUtils.addMarker(stack, "soulpoint");
        ItemUtils.replaceLore(stack, lore);
    }

    @Override
    public PerformanceImpact getPerformanceImpact() {
        return PerformanceImpact.Medium;
    }

    @Override
    public GameplayImpact getGameplayImpact() {
        return GameplayImpact.Medium;
    }

    @Override
    public Stability getStability() {
        return Stability.Stable;
    }
}
