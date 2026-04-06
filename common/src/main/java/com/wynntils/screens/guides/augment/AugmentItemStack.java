/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.augment;

import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.models.gear.type.GearTier;
import com.wynntils.screens.guides.GuideItemStack;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public abstract class AugmentItemStack extends GuideItemStack {
    private final GearTier gearTier;

    private List<Component> generatedTooltip;

    public AugmentItemStack(ItemStack itemStack, ItemAnnotation annotation, String baseName, GearTier gearTier) {
        super(itemStack, annotation, baseName);

        this.gearTier = gearTier;
    }

    @Override
    public List<Component> getTooltipLines(Item.TooltipContext context, Player player, TooltipFlag flag) {
        if (generatedTooltip == null) {
            List<Component> tooltip = new ArrayList<>();
            tooltip.add(getHoverName());
            tooltip.add(Component.empty());
            tooltip.addAll(generateLore());
            tooltip.add(Component.empty());
            tooltip.add(
                    Component.literal(gearTier.getName() + " Corkian Augment").withStyle(gearTier.getChatFormatting()));
            tooltip.add(Component.translatable("screens.wynntils.wynntilsGuides.augments.footer")
                    .withStyle(ChatFormatting.DARK_GRAY));

            appendFavoriteInfo(tooltip);

            generatedTooltip = tooltip;
        }

        return generatedTooltip;
    }

    public abstract List<Component> generateLore();

    public abstract int getTier();

    public GearTier getGearTier() {
        return gearTier;
    }
}
