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
import com.wynntils.models.emeralds.type.EmeraldUnits;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public class EmeraldPouchTooltipBuilder extends TooltipBuilder {
    private final int tier;

    private EmeraldPouchTooltipBuilder(int tier, ItemSourceType sourceType, String source) {
        super(sourceType, source);

        this.tier = tier;
    }

    public static EmeraldPouchTooltipBuilder buildNewItem(int tier, ItemSourceType sourceType, String source) {
        return new EmeraldPouchTooltipBuilder(tier, sourceType, source);
    }

    @Override
    protected void buildTooltip() {
        int upTo = (tier - 1) / 3;
        int rows = 0;
        String totalString = "";

        switch (tier % 3) {
            case 0 -> {
                rows = 6;
                totalString = "54";
            }
            case 1 -> {
                rows = 1;
                totalString = "9";
            }
            case 2 -> {
                rows = 3;
                totalString = "27";
            }
        }

        if (tier >= 10) {
            rows = 6;
        }

        if (tier >= 7) {
            totalString = tier - 6 + "stx";
        } else if (tier >= 4) {
            totalString += EmeraldUnits.LIQUID_EMERALD.getSymbol();
        } else {
            totalString += EmeraldUnits.EMERALD_BLOCK.getSymbol();
        }

        List<TooltipElement> lines = new ArrayList<>();

        Component name = Component.literal("Emerald Pouch ")
                .withStyle(ChatFormatting.GREEN)
                .append(Component.literal("[Tier " + tier + "]").withStyle(ChatFormatting.DARK_GREEN));
        lines.add(TooltipLine.left(name));

        lines.add(TooltipLine.empty());

        lines.add(TooltipWrappedText.left(Component.literal("Emerald Pouches allow the wearer to easily ")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal("store ").withStyle(ChatFormatting.AQUA))
                .append(Component.literal("and ").withStyle(ChatFormatting.GRAY))
                .append(Component.literal("convert ").withStyle(ChatFormatting.AQUA))
                .append(Component.literal("emeralds without spending extra inventory slots.")
                        .withStyle(ChatFormatting.GRAY))));

        lines.add(TooltipLine.empty());

        lines.add(TooltipLine.left(Component.empty()
                .append(Component.literal("- " + rows + " Row" + (rows == 1 ? " " : "s "))
                        .withStyle(ChatFormatting.GRAY))
                .append(Component.literal("(" + totalString + " Total)"))
                .withStyle(ChatFormatting.DARK_GRAY)));

        switch (upTo) {
            case 0 ->
                lines.add(TooltipLine.left(
                        Component.literal("- No Auto-Conversions").withStyle(ChatFormatting.GRAY)));
            case 1 ->
                lines.add(TooltipLine.left(Component.literal("- Converts up to")
                        .withStyle(ChatFormatting.GRAY)
                        .append(Component.literal(" Emerald Blocks").withStyle(ChatFormatting.WHITE))));
            default ->
                lines.add(TooltipLine.left(Component.literal("- Converts up to")
                        .withStyle(ChatFormatting.GRAY)
                        .append(Component.literal(" Liquid Emeralds").withStyle(ChatFormatting.WHITE))));
        }

        this.tooltipLines = Handlers.Tooltip.alignTooltip(lines, 200);
    }
}
