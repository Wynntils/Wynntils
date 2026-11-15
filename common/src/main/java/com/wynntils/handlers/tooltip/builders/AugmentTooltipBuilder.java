/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.tooltip.builders;

import com.wynntils.core.components.Handlers;
import com.wynntils.handlers.tooltip.TooltipBuilder;
import com.wynntils.handlers.tooltip.type.ItemSourceType;
import com.wynntils.handlers.tooltip.type.TooltipElement;
import com.wynntils.handlers.tooltip.type.TooltipLine;
import com.wynntils.handlers.tooltip.type.TooltipWrappedText;
import com.wynntils.models.gear.type.GearTier;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public abstract class AugmentTooltipBuilder extends TooltipBuilder {
    private final Component name;
    private final GearTier gearTier;

    protected AugmentTooltipBuilder(Component name, GearTier gearTier, ItemSourceType sourceType, String source) {
        super(sourceType, source);

        this.name = name;
        this.gearTier = gearTier;
    }

    @Override
    protected void buildTooltip() {
        List<TooltipElement> lines = new ArrayList<>();

        lines.add(TooltipLine.left(name));
        lines.add(TooltipLine.empty());
        lines.addAll(buildAugmentDetails());
        lines.add(TooltipLine.empty());
        lines.add(TooltipLine.left(
                Component.literal(gearTier.getName() + " Corkian Augment").withStyle(gearTier.getChatFormatting())));
        lines.add(TooltipWrappedText.left(Component.translatable("screens.wynntils.wynntilsGuides.augments.footer")
                .withStyle(ChatFormatting.DARK_GRAY)));

        this.tooltipLines = Handlers.Tooltip.alignTooltip(lines, 200);
    }

    protected abstract List<TooltipElement> buildAugmentDetails();
}
