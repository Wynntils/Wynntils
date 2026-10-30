/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.tooltip.builders;

import com.wynntils.core.components.Handlers;
import com.wynntils.core.text.fonts.CommonFonts;
import com.wynntils.core.text.fonts.wynnfonts.BannerBoxFont;
import com.wynntils.handlers.tooltip.TooltipBuilder;
import com.wynntils.handlers.tooltip.components.DurabilityLineBuilder;
import com.wynntils.handlers.tooltip.components.HeaderStatsLineBuilder;
import com.wynntils.handlers.tooltip.components.RequirementsLineBuilder;
import com.wynntils.handlers.tooltip.type.ItemSourceType;
import com.wynntils.handlers.tooltip.type.TooltipElement;
import com.wynntils.handlers.tooltip.type.TooltipLine;
import com.wynntils.models.profession.type.GatheringToolInfo;
import com.wynntils.models.wynnitem.type.ItemEmblem;
import com.wynntils.utils.EnumUtils;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.type.CappedValue;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

public class GatheringToolTooltipBuilder extends TooltipBuilder {
    private static final ItemEmblem ITEM_EMBLEM = new ItemEmblem(ItemEmblem.ItemEmblemShape.SQUARE, 4);

    private final GatheringToolInfo toolInfo;

    private GatheringToolTooltipBuilder(GatheringToolInfo toolInfo, ItemSourceType sourceType, String source) {
        super(sourceType, source);

        this.toolInfo = toolInfo;
    }

    public static GatheringToolTooltipBuilder buildNewItem(
            GatheringToolInfo toolInfo, ItemSourceType sourceType, String source) {
        return new GatheringToolTooltipBuilder(toolInfo, sourceType, source);
    }

    @Override
    protected void buildTooltip() {
        List<TooltipElement> lines = new ArrayList<>();
        lines.add(TooltipLine.empty());

        lines.addAll(buildHeader(
                ITEM_EMBLEM,
                toolInfo.gatheringToolType().getEmblemSprite(),
                Component.literal(toolInfo.name())
                        .withStyle(Style.EMPTY
                                .withFont(CommonFonts.WYNNTILS_LANGUAGE_WYNNCRAFT_FONT)
                                .withColor(CustomColor.fromChatFormatting(
                                                toolInfo.gearTier().getChatFormatting())
                                        .asInt())),
                BannerBoxFont.buildMessage(
                        toolInfo.gearTier().getName(),
                        CustomColor.fromChatFormatting(toolInfo.gearTier().getChatFormatting()),
                        CommonColors.BLACK,
                        ""),
                BannerBoxFont.buildMessage(
                        EnumUtils.toNiceString(toolInfo.gatheringToolType()),
                        toolInfo.gearTier().getSecondaryColor(),
                        CommonColors.BLACK,
                        "")));

        lines.add(TooltipLine.empty());

        lines.add(HeaderStatsLineBuilder.buildhighlightStatLine(
                String.valueOf(toolInfo.gatheringSpeed()), "Gathering Speed", toolInfo.gearTier()));

        lines.add(DurabilityLineBuilder.buildDurabilityLine(
                toolInfo.gearTier(), new CappedValue(toolInfo.durability(), toolInfo.durability())));

        lines.add(divider(toolInfo.gearTier()));

        lines.add(RequirementsLineBuilder.buildProfessionLevelRequirementLine(
                toolInfo.professionType(), toolInfo.level(), false));

        this.tooltipLines = Handlers.Tooltip.alignTooltip(lines);
    }
}
