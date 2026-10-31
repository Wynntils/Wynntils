/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.augment;

import com.wynntils.core.components.Models;
import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.handlers.tooltip.TooltipBuilder;
import com.wynntils.models.gear.type.GearTier;
import com.wynntils.screens.guides.GuideItemStack;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public abstract class GuideAugmentItemStack extends GuideItemStack {
    private final GearTier gearTier;

    private List<Component> generatedTooltip;

    public GuideAugmentItemStack(ItemStack itemStack, ItemAnnotation annotation, String baseName, GearTier gearTier) {
        super(itemStack, annotation, baseName);

        this.gearTier = gearTier;
    }

    @Override
    public List<Component> getTooltipLines(Item.TooltipContext context, Player player, TooltipFlag flag) {
        if (generatedTooltip == null) {
            generatedTooltip = createTooltipBuilder().getTooltipLines(Models.Character.getClassType());
        }

        return generatedTooltip;
    }

    protected abstract TooltipBuilder createTooltipBuilder();

    public abstract int getTier();

    public GearTier getGearTier() {
        return gearTier;
    }
}
