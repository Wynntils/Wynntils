/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.powder;

import com.wynntils.core.components.Models;
import com.wynntils.handlers.tooltip.builders.PowderTooltipBuilder;
import com.wynntils.handlers.tooltip.type.ItemSourceType;
import com.wynntils.models.elements.type.Powder;
import com.wynntils.models.elements.type.PowderTierInfo;
import com.wynntils.models.items.items.game.PowderItem;
import com.wynntils.screens.guides.GuideItemStack;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public final class GuidePowderItemStack extends GuideItemStack {
    private final int tier;
    private final Powder element;

    private final List<Component> generatedTooltip;

    public GuidePowderItemStack(PowderTierInfo powderTierInfo) {
        super(
                getItemStack(powderTierInfo),
                new PowderItem(powderTierInfo),
                powderTierInfo.element().getName() + " Powder");

        this.element = powderTierInfo.element();
        this.tier = powderTierInfo.tier();
        this.generatedTooltip = PowderTooltipBuilder.buildNewItem(powderTierInfo, ItemSourceType.GUIDE, "")
                .getTooltipLines(Models.Character.getClassType());
    }

    @Override
    public List<Component> getTooltipLines(Item.TooltipContext context, Player player, TooltipFlag flag) {
        List<Component> tooltip = new ArrayList<>(generatedTooltip);

        return tooltip;
    }

    private static ItemStack getItemStack(PowderTierInfo profile) {
        if (profile.tier() <= 3) {
            return new ItemStack(profile.element().getLowTierItem());
        } else {
            return new ItemStack(profile.element().getHighTierItem());
        }
    }

    public int getTier() {
        return tier;
    }

    public Powder getElement() {
        return element;
    }
}
