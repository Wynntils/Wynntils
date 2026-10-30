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
import com.wynntils.models.rewards.type.WardType;
import com.wynntils.models.wynnitem.type.ItemEmblem;
import com.wynntils.utils.EnumUtils;
import com.wynntils.utils.colors.CommonColors;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

public class WardTooltipBuilder extends TooltipBuilder {
    private static final ItemEmblem ITEM_EMBLEM = new ItemEmblem(ItemEmblem.ItemEmblemShape.SQUARE, 4);

    private static final int MINIMUM_WIDTH = 135;
    private final WardType wardType;

    private WardTooltipBuilder(WardType wardType, ItemSourceType sourceType, String source) {
        super(sourceType, source);

        this.wardType = wardType;
    }

    public static WardTooltipBuilder buildNewItem(WardType wardType, ItemSourceType sourceType, String source) {
        return new WardTooltipBuilder(wardType, sourceType, source);
    }

    @Override
    protected void buildTooltip() {
        List<TooltipElement> lines = new ArrayList<>();
        lines.add(TooltipLine.empty());

        lines.addAll(buildHeader(
                ITEM_EMBLEM,
                ItemEmblemSprite.WARD,
                Component.literal(EnumUtils.toNiceString(wardType) + " Ward")
                        .withStyle(Style.EMPTY
                                .withFont(CommonFonts.WYNNTILS_LANGUAGE_WYNNCRAFT_FONT)
                                .withColor(wardType.getColor().asInt())),
                BannerBoxFont.buildMessage("ward", CommonColors.RESOURCE_PRIMARY, CommonColors.BLACK, ""),
                BannerBoxFont.buildMessage("resource", CommonColors.RESOURCE_SECONDARY, CommonColors.BLACK, "")));

        lines.add(divider(CommonColors.RESOURCE_SECONDARY));

        MutableComponent usageLine = Component.literal(
                        "A powerful artifact used to ascend items to new heights through the ")
                .withStyle(Style.EMPTY
                        .withFont(CommonFonts.WYNNTILS_LANGUAGE_WYNNCRAFT_FONT)
                        .withColor(ChatFormatting.GRAY))
                .append(Component.literal("\uE00A")
                        .withStyle(
                                Style.EMPTY.withFont(CommonFonts.MERCHANT_FONT).withoutShadow()))
                .append(Component.literal(" Item Upgrader")
                        .withStyle(Style.EMPTY
                                .withFont(CommonFonts.WYNNTILS_LANGUAGE_WYNNCRAFT_FONT)
                                .withColor(ChatFormatting.WHITE)))
                .append(Component.literal(".")
                        .withStyle(Style.EMPTY
                                .withFont(CommonFonts.WYNNTILS_LANGUAGE_WYNNCRAFT_FONT)
                                .withColor(ChatFormatting.GRAY)));
        lines.add(TooltipWrappedText.left(usageLine));

        if (sourceType == ItemSourceType.GUIDE) {
            lines.add(TooltipLine.empty());

            MutableComponent obtainLine = Component.literal("Obtained from ")
                    .withStyle(Style.EMPTY
                            .withFont(CommonFonts.WYNNTILS_LANGUAGE_WYNNCRAFT_FONT)
                            .withColor(ChatFormatting.GRAY))
                    .append(Component.literal(EnumUtils.toNiceString(wardType.getItemObtainType()) + "s")
                            .withStyle(ChatFormatting.AQUA));
            lines.add(TooltipLine.left(obtainLine));
        }

        this.tooltipLines = Handlers.Tooltip.alignTooltip(lines, MINIMUM_WIDTH);
    }
}
