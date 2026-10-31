/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.tooltip.builders;

import com.wynntils.handlers.tooltip.type.ItemSourceType;
import com.wynntils.handlers.tooltip.type.TooltipElement;
import com.wynntils.handlers.tooltip.type.TooltipLine;
import com.wynntils.handlers.tooltip.type.TooltipWrappedText;
import com.wynntils.models.gear.type.GearTier;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class SimulatorTooltipBuilder extends AugmentTooltipBuilder {
    private SimulatorTooltipBuilder(GearTier gearTier, ItemSourceType sourceType, String source) {
        super(
                Component.literal("Corkian Simulator").withStyle(gearTier.getChatFormatting()),
                gearTier,
                sourceType,
                source);
    }

    public static SimulatorTooltipBuilder buildNewItem(GearTier gearTier, ItemSourceType sourceType, String source) {
        return new SimulatorTooltipBuilder(gearTier, sourceType, source);
    }

    @Override
    protected List<TooltipElement> buildAugmentDetails() {
        List<TooltipElement> lines = new ArrayList<>();

        MutableComponent effectComponent = Component.translatable("screens.wynntils.wynntilsGuides.augments.simulator1")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.translatable("screens.wynntils.wynntilsGuides.augments.simulator2")
                        .withStyle(ChatFormatting.WHITE))
                .append(Component.translatable("screens.wynntils.wynntilsGuides.augments.simulator3")
                        .withStyle(ChatFormatting.GRAY));

        MutableComponent incompatibleComponent = Component.translatable(
                        "screens.wynntils.wynntilsGuides.augments.simulator4")
                .withStyle(ChatFormatting.DARK_GRAY)
                .append(Component.translatable("screens.wynntils.wynntilsGuides.augments.simulator5")
                        .withStyle(ChatFormatting.GRAY));

        lines.add(TooltipWrappedText.left(effectComponent));
        lines.add(TooltipLine.empty());
        lines.add(TooltipLine.left(incompatibleComponent));

        if (sourceType == ItemSourceType.GUIDE) {
            MutableComponent obtainComponent = Component.translatable("screens.wynntils.wynntilsGuides.augments.obtain")
                    .withStyle(ChatFormatting.DARK_GRAY)
                    .append(Component.translatable("screens.wynntils.wynntilsGuides.augments.obtainLootruns")
                            .withStyle(ChatFormatting.AQUA));

            lines.add(TooltipLine.empty());
            lines.add(TooltipLine.left(obtainComponent));
        }

        return lines;
    }
}
