/*
 * Copyright © Wynntils 2021-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.ui;

import com.wynntils.core.components.Models;
import com.wynntils.core.config.Category;
import com.wynntils.core.config.ConfigCategory;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.mc.event.ItemTooltipRenderEvent;
import com.wynntils.models.items.items.gui.SoulPointItem;
import com.wynntils.utils.mc.LoreUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@ConfigCategory(Category.UI)
public class SoulPointTimerFeature extends Feature {
    @SubscribeEvent
    public void onTooltipPre(ItemTooltipRenderEvent.Pre event) {
        Optional<SoulPointItem> soulPointItemOpt = Models.Item.asWynnItem(event.getItemStack(), SoulPointItem.class);
        if (soulPointItemOpt.isEmpty()) return;

        List<Component> tooltips =
                LoreUtils.appendTooltip(event.getItemStack(), event.getTooltips(), getTooltipAddon());
        event.setTooltips(tooltips);
    }

    private List<Component> getTooltipAddon() {
        List<Component> addon = new ArrayList<>();

        addon.add(Component.literal(" "));

        int rawSecondsUntilSoulPoint = Models.CharacterStats.getTicksToNextSoulPoint() / 20;
        int minutesUntilSoulPoint = rawSecondsUntilSoulPoint / 60;
        int secondsUntilSoulPoint = rawSecondsUntilSoulPoint % 60;

        addon.add(Component.translatable(
                        "feature.wynntils.soulPointTimer.lore",
                        ChatFormatting.WHITE + String.format("%d:%02d", minutesUntilSoulPoint, secondsUntilSoulPoint))
                .withStyle(ChatFormatting.AQUA));

        return addon;
    }
}
