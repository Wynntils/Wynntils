/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wc.custom.item;

import com.wynntils.features.user.SoulPointTimerFeature;
import com.wynntils.wc.utils.WynnInventoryData;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public class SoulPointItemStack extends WynnItemStack {
    private final List<Component> tooltip;

    public SoulPointItemStack(ItemStack stack) {
        super(stack);

        tooltip = getOriginalTooltip();
    }

    @Override
    public List<Component> getTooltipLines(Player player, TooltipFlag isAdvanced) {
        if (SoulPointTimerFeature.INSTANCE.isEnabled()) {
            List<Component> copy = new ArrayList<>(tooltip);

            copy.add(new TextComponent(" "));

            int rawSecondsUntilSoulPoint = WynnInventoryData.getTicksTillNextSoulPoint() / 20;
            int minutesUntilSoulPoint = rawSecondsUntilSoulPoint / 60;
            int secondsUntilSoulPoint = rawSecondsUntilSoulPoint % 60;

            copy.add(new TranslatableComponent(
                    "feature.wynntils.soulPointTimer.lore",
                    minutesUntilSoulPoint,
                    String.format("%02d", secondsUntilSoulPoint)));

            return copy;
        }

        return tooltip;
    }
}
