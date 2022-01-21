/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features;

import com.wynntils.core.features.Feature;
import com.wynntils.core.features.GameplayImpact;
import com.wynntils.core.features.PerformanceImpact;
import com.wynntils.core.features.Stability;
import com.wynntils.mc.event.InventoryRenderEvent;
import com.wynntils.mc.utils.ItemUtils;
import com.wynntils.mc.utils.objects.DynamicTag;
import com.wynntils.wc.utils.InventoryData;
import com.wynntils.wc.utils.ItemMatchers;
import com.wynntils.wc.utils.WynnUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class SoulPointTimerFeature extends Feature {
    @SubscribeEvent
    public static void onInventoryRender(InventoryRenderEvent e) {
        if (!WynnUtils.onWorld()) return;

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
        return PerformanceImpact.MEDIUM;
    }

    @Override
    public GameplayImpact getGameplayImpact() {
        return GameplayImpact.MEDIUM;
    }

    @Override
    public Stability getStability() {
        return Stability.STABLE;
    }
}
