/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.tooltip.builders;

import com.wynntils.core.components.Handlers;
import com.wynntils.core.text.fonts.CommonFonts;
import com.wynntils.core.text.fonts.wynnfonts.BannerBoxFont;
import com.wynntils.handlers.tooltip.TooltipBuilder;
import com.wynntils.handlers.tooltip.type.ItemEmblemSprite;
import com.wynntils.handlers.tooltip.type.ItemSourceType;
import com.wynntils.handlers.tooltip.type.TooltipElement;
import com.wynntils.handlers.tooltip.type.TooltipLine;
import com.wynntils.handlers.tooltip.type.TooltipWrappedText;
import com.wynntils.models.rewards.type.RuneType;
import com.wynntils.models.wynnitem.type.ItemEmblem;
import com.wynntils.utils.EnumUtils;
import com.wynntils.utils.colors.CommonColors;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

public class RuneTooltipBuilder extends TooltipBuilder {
    private static final ItemEmblem ITEM_EMBLEM = new ItemEmblem(ItemEmblem.ItemEmblemShape.SQUARE, 4);

    private static final int MINIMUM_WIDTH = 134;
    private final RuneType runeType;

    private RuneTooltipBuilder(RuneType runeType, ItemSourceType sourceType, String source) {
        super(sourceType, source);

        this.runeType = runeType;
    }

    public static RuneTooltipBuilder buildNewItem(RuneType runeType, ItemSourceType sourceType, String source) {
        return new RuneTooltipBuilder(runeType, sourceType, source);
    }

    @Override
    protected void buildTooltip() {
        List<TooltipElement> lines = new ArrayList<>();
        lines.add(TooltipLine.empty());

        lines.addAll(buildHeader(
                ITEM_EMBLEM,
                ItemEmblemSprite.RUNE,
                Component.literal(EnumUtils.toNiceString(runeType) + " Rune")
                        .withStyle(Style.EMPTY
                                .withFont(CommonFonts.WYNNTILS_LANGUAGE_WYNNCRAFT_FONT)
                                .withColor(runeType.getColor().asInt())),
                BannerBoxFont.buildMessage("rune", CommonColors.RESOURCE_PRIMARY, CommonColors.BLACK, ""),
                BannerBoxFont.buildMessage("resource", CommonColors.RESOURCE_SECONDARY, CommonColors.BLACK, "")));

        lines.add(divider(CommonColors.RESOURCE_SECONDARY));

        MutableComponent usageLine = Component.literal("Use this item to enter ")
                .append(Component.literal("Raids").withStyle(ChatFormatting.WHITE))
                .append(Component.literal(" or craft a "))
                .append(Component.literal("Corrupted Dungeon Key").withStyle(ChatFormatting.WHITE))
                .withStyle(Style.EMPTY
                        .withFont(CommonFonts.WYNNTILS_LANGUAGE_WYNNCRAFT_FONT)
                        .withColor(ChatFormatting.GRAY));
        lines.add(TooltipWrappedText.left(usageLine));

        if (sourceType == ItemSourceType.GUIDE) {
            lines.add(TooltipLine.empty());

            MutableComponent obtainLine = Component.literal("Obtained from ")
                    .append(Component.literal("World Events").withStyle(ChatFormatting.AQUA))
                    .append(Component.literal(", "))
                    .append(Component.literal("Raids").withStyle(ChatFormatting.AQUA))
                    .append(Component.literal(", "))
                    .append(Component.literal("Rune Guardians").withStyle(ChatFormatting.AQUA))
                    .append(Component.literal(", "))
                    .append(Component.literal("Shrines").withStyle(ChatFormatting.AQUA))
                    .append(Component.literal(" and "))
                    .append(Component.literal("Altars").withStyle(ChatFormatting.AQUA))
                    .withStyle(Style.EMPTY
                            .withFont(CommonFonts.WYNNTILS_LANGUAGE_WYNNCRAFT_FONT)
                            .withColor(ChatFormatting.GRAY));

            lines.add(TooltipWrappedText.left(obtainLine));
        }

        this.tooltipLines = Handlers.Tooltip.alignTooltip(lines, MINIMUM_WIDTH);
    }
}
