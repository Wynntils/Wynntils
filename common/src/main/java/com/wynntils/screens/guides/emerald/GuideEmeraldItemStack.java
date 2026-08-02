/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.emerald;

import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.screens.guides.GuideItemStack;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public abstract class GuideEmeraldItemStack extends GuideItemStack {
    private List<Component> generatedTooltip;

    public GuideEmeraldItemStack(ItemStack itemStack, ItemAnnotation annotation, String baseName) {
        super(itemStack, annotation, baseName);
    }

    @Override
    public List<Component> getTooltipLines(Item.TooltipContext context, Player player, TooltipFlag flag) {
        if (generatedTooltip == null) {
            List<Component> tooltip = new ArrayList<>(generateLore());

            generatedTooltip = tooltip;
        }

        return generatedTooltip;
    }

    public abstract List<Component> generateLore();

    public abstract int getTier();
}
