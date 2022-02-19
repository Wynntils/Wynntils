/*
 * Copyright © Wynntils 2021.
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
import com.wynntils.mc.event.ItemsReceivedEvent;
import com.wynntils.mc.utils.ItemUtils;
import com.wynntils.mc.utils.objects.DynamicTag;
import com.wynntils.wc.utils.WynnInventoryData;
import com.wynntils.wc.utils.WynnItemMatchers;
import com.wynntils.wc.utils.WynnUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@FeatureInfo(
        stability = Stability.STABLE,
        gameplay = GameplayImpact.MEDIUM,
        performance = PerformanceImpact.MEDIUM)
public class SoulPointTimerFeature extends Feature {
    @Override
    protected void onInit(ImmutableList.Builder<Condition> conditions) {}

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
    public void onInventoryRender(ItemsReceivedEvent e) {
        if (!WynnUtils.onServer()) return;

        for (ItemStack stack : e.getItems()) {
            if (!WynnItemMatchers.isSoulPoint(stack)) continue;

            ListTag lore = ItemUtils.getLoreTag(stack);

            if (lore == null) {
                lore = new ListTag();
            } else {
                lore.add(StringTag.valueOf("")); // Equivalent to adding ""
            }

            lore.add(
                    new DynamicTag(
                            () -> {
                                int rawSecondsUntilSoulPoint =
                                        WynnInventoryData.getTicksTillNextSoulPoint() / 20;
                                int minutesUntilSoulPoint = rawSecondsUntilSoulPoint / 60;
                                int secondsUntilSoulPoint = rawSecondsUntilSoulPoint % 60;

                                return ItemUtils.toLoreString(
                                        ChatFormatting.AQUA
                                                + "Time until next soul point: "
                                                + ChatFormatting.WHITE
                                                + minutesUntilSoulPoint
                                                + ":"
                                                + String.format("%02d", secondsUntilSoulPoint));
                            }));

            ItemUtils.replaceLore(stack, lore);
        }
    }
}
