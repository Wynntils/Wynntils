/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.tooltip.builders;

import com.wynntils.core.components.Handlers;
import com.wynntils.core.text.fonts.wynnfonts.BannerBoxFont;
import com.wynntils.handlers.tooltip.TooltipBuilder;
import com.wynntils.handlers.tooltip.type.ItemSourceType;
import com.wynntils.handlers.tooltip.type.TooltipElement;
import com.wynntils.handlers.tooltip.type.TooltipLine;
import com.wynntils.models.emeralds.type.EmeraldUnits;
import com.wynntils.utils.colors.CustomColor;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public class EmeraldUnitTooltipBuilder extends TooltipBuilder {
    private final EmeraldUnits unit;

    private EmeraldUnitTooltipBuilder(EmeraldUnits unit, ItemSourceType sourceType, String source) {
        super(sourceType, source);

        this.unit = unit;
    }

    public static EmeraldUnitTooltipBuilder buildNewItem(EmeraldUnits unit, ItemSourceType sourceType, String source) {
        return new EmeraldUnitTooltipBuilder(unit, sourceType, source);
    }

    @Override
    protected void buildTooltip() {
        List<TooltipElement> lines = new ArrayList<>();

        Component name =
                Component.empty().withStyle(ChatFormatting.GREEN).append(Component.literal(unit.getDisplayName()));
        lines.add(TooltipLine.left(name));

        lines.add(TooltipLine.left(getBanner()));

        lines.add(TooltipLine.empty());

        Component worthLine = Component.translatable("screens.wynntils.wynntilsGuides.emeralds.emeralds.usage1")
                .withStyle(ChatFormatting.DARK_GRAY)
                .append(Component.literal(unit.getMultiplier() + EmeraldUnits.EMERALD.getSymbol())
                        .withStyle(ChatFormatting.GREEN));
        lines.add(TooltipLine.center(worthLine));

        this.tooltipLines = Handlers.Tooltip.alignTooltip(lines);
    }

    private Component getBanner() {
        return switch (unit) {
            case EMERALD_BLOCK -> buildBanner("eb");
            case LIQUID_EMERALD -> buildBanner("le");
            default -> buildBanner("e");
        };
    }

    private Component buildBanner(String text) {
        return Component.empty()
                .append(BannerBoxFont.buildMessage(
                        text,
                        CustomColor.fromChatFormatting(ChatFormatting.DARK_GREEN),
                        CustomColor.fromChatFormatting(ChatFormatting.BLACK),
                        "\uDB00\uDC02"))
                .append(BannerBoxFont.buildMessage(
                        "currency",
                        CustomColor.fromChatFormatting(ChatFormatting.DARK_GRAY),
                        CustomColor.fromChatFormatting(ChatFormatting.WHITE),
                        ""));
    }
}
