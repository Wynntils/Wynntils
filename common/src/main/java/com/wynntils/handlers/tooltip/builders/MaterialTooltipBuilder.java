/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.tooltip.builders;

import com.wynntils.core.components.Handlers;
import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.core.text.fonts.CommonFonts;
import com.wynntils.core.text.fonts.wynnfonts.BannerBoxFont;
import com.wynntils.core.text.fonts.wynnfonts.BannerSymbolFont;
import com.wynntils.handlers.tooltip.TooltipBuilder;
import com.wynntils.handlers.tooltip.components.RequirementsLineBuilder;
import com.wynntils.handlers.tooltip.type.ItemEmblemSprite;
import com.wynntils.handlers.tooltip.type.ItemSourceType;
import com.wynntils.handlers.tooltip.type.TooltipElement;
import com.wynntils.handlers.tooltip.type.TooltipLine;
import com.wynntils.handlers.tooltip.type.TooltipWrappedText;
import com.wynntils.models.profession.type.MaterialInfo;
import com.wynntils.models.wynnitem.type.ItemEmblem;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

public class MaterialTooltipBuilder extends TooltipBuilder {
    private static final ItemEmblem ITEM_EMBLEM = new ItemEmblem(ItemEmblem.ItemEmblemShape.SQUARE, 4);

    private static final int MINIMUM_WIDTH = 156;

    private final MaterialInfo materialInfo;
    private final int tier;

    private MaterialTooltipBuilder(MaterialInfo materialInfo, int tier, ItemSourceType sourceType, String source) {
        super(sourceType, source);

        this.materialInfo = materialInfo;
        this.tier = tier;
    }

    public static MaterialTooltipBuilder buildNewItem(
            MaterialInfo materialInfo, int tier, ItemSourceType sourceType, String source) {
        return new MaterialTooltipBuilder(materialInfo, tier, sourceType, source);
    }

    @Override
    protected void buildTooltip() {
        List<TooltipElement> lines = new ArrayList<>();
        lines.add(TooltipLine.empty());

        lines.addAll(buildHeader(
                ITEM_EMBLEM,
                ItemEmblemSprite.MATERIAL,
                Component.literal(materialInfo.name())
                        .withStyle(Style.EMPTY
                                .withFont(CommonFonts.WYNNTILS_LANGUAGE_WYNNCRAFT_FONT)
                                .withColor(CommonColors.MATERIAL_PRIMARY.asInt())),
                BannerSymbolFont.buildMessage(
                        3,
                        tier,
                        CustomColor.fromChatFormatting(ChatFormatting.DARK_GRAY),
                        CustomColor.fromChatFormatting(Models.Profession.getColorForTier(tier)),
                        CommonColors.BLACK,
                        ""),
                BannerBoxFont.buildMessage("material", CommonColors.MATERIAL_SECONDARY, CommonColors.BLACK, "")));

        lines.add(divider(CommonColors.MATERIAL_SECONDARY));

        lines.add(RequirementsLineBuilder.buildProfessionLevelRequirementLine(
                materialInfo.professionType(), materialInfo.level(), true));

        lines.add(divider(CommonColors.MATERIAL_SECONDARY));

        StyledText[] splitLore = materialInfo.lore().split("\n\n");
        StyledText mountUsage = splitLore[0];
        StyledText otherUsage = splitLore[1];

        lines.add(TooltipWrappedText.center(mountUsage.getComponent()));
        lines.add(TooltipLine.empty());
        lines.add(TooltipWrappedText.center(otherUsage.getComponent()));

        this.tooltipLines = Handlers.Tooltip.alignTooltip(lines, MINIMUM_WIDTH);
    }
}
