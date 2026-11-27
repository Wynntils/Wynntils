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
import com.wynntils.handlers.tooltip.components.PaginatorLineBuilder;
import com.wynntils.handlers.tooltip.components.RequirementsLineBuilder;
import com.wynntils.handlers.tooltip.type.ItemSourceType;
import com.wynntils.handlers.tooltip.type.TooltipElement;
import com.wynntils.handlers.tooltip.type.TooltipLine;
import com.wynntils.models.gear.type.GearTier;
import com.wynntils.models.profession.type.CraftedItemRecipe;
import com.wynntils.models.wynnitem.type.ItemEmblem;
import com.wynntils.utils.EnumUtils;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.type.Pair;
import com.wynntils.utils.type.RangedValue;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

public class CraftedItemRecipeTooltipBuilder extends TooltipBuilder {
    private final CraftedItemRecipe item;

    protected CraftedItemRecipeTooltipBuilder(
            CraftedItemRecipe craftedItemRecipe, ItemSourceType sourceType, String source) {
        super(sourceType, source);

        this.item = craftedItemRecipe;
    }

    public static CraftedItemRecipeTooltipBuilder buildNewItem(
            CraftedItemRecipe craftedItemRecipe, ItemSourceType sourceType, String source) {
        return new CraftedItemRecipeTooltipBuilder(craftedItemRecipe, sourceType, source);
    }

    private List<TooltipElement> buildHeader(String source) {
        List<TooltipElement> lines = new ArrayList<>();

        if (!source.isEmpty()) {
            lines.add(buildSourceLine());
        }

        lines.add(TooltipLine.empty());

        Component name = Component.literal("Crafted " + EnumUtils.toNiceString(item.getCraftedItemType()))
                .withStyle(Style.EMPTY
                        .withFont(CommonFonts.WYNNTILS_LANGUAGE_WYNNCRAFT_FONT)
                        .withColor(GearTier.CRAFTED.getChatFormatting()));

        lines.addAll(buildHeader(
                ItemEmblem.fromCraftedItemRecipe(item.getCraftedItemType()),
                item.getCraftedItemType().getEmblemSprite(),
                name,
                BannerBoxFont.buildMessage(
                        GearTier.CRAFTED.getApiName(),
                        CustomColor.fromChatFormatting(GearTier.CRAFTED.getChatFormatting()),
                        CommonColors.BLACK,
                        ""),
                BannerBoxFont.buildMessage(
                        item.getCraftedItemType().name(),
                        GearTier.CRAFTED.getSecondaryColor(),
                        CommonColors.BLACK,
                        "")));

        lines.add(TooltipLine.empty());

        if (item.getCraftedItemType().isWeapon()) {
            Pair<Double, Double> dps = item.getAverageDps();
            lines.add(HeaderStatsLineBuilder.buildDpsLine(dps.a(), dps.b(), GearTier.CRAFTED));

            lines.add(DurabilityLineBuilder.buildDurabilityLine(GearTier.CRAFTED, item.getDurabilityOrDuration()));
            lines.add(HeaderStatsLineBuilder.buildAttackSpeedLine(item.getAttackSpeed()));
            lines.add(HeaderStatsLineBuilder.buildDamageRangesGrid(item.getDamages()));
        } else if (item.getCraftedItemType().isGear()) {
            RangedValue health = item.getHealth();
            lines.add(
                    health.isFixed()
                            ? HeaderStatsLineBuilder.buildHealthLine(health.low(), GearTier.CRAFTED)
                            : HeaderStatsLineBuilder.buildHealthLine(health, GearTier.CRAFTED));

            lines.add(DurabilityLineBuilder.buildDurabilityLine(GearTier.CRAFTED, item.getDurabilityOrDuration()));

            lines.addAll(HeaderStatsLineBuilder.buildDefenceLines(item.getDefences()));
        }

        return lines;
    }

    private List<TooltipElement> buildRequirements() {
        List<TooltipElement> lines = new ArrayList<>();

        lines.add(TooltipBuilder.divider(GearTier.CRAFTED));

        if (item.getSkillRequirements().stream().anyMatch(requirement -> requirement.b() > 0)) {
            lines.addAll(
                    RequirementsLineBuilder.buildSkillReqLines(item.getSkillRequirements(), null, GearTier.CRAFTED));
        }

        if (item.getCraftedItemType().isWeapon()) {
            lines.add(RequirementsLineBuilder.buildClassRequirementLine(
                    item.getCraftedItemType().getItemClassType()));
        }

        lines.add(RequirementsLineBuilder.buildLevelRequirementLine(item.getLevel()));

        return lines;
    }

    private List<TooltipElement> buildIdentificationLines() {
        List<TooltipElement> lines = new ArrayList<>();

        lines.add(TooltipBuilder.divider(GearTier.CRAFTED));

        return lines;
    }

    private List<TooltipElement> buildPaginator() {
        List<TooltipElement> lines = new ArrayList<>();

        Component pageIcon =
                Component.literal("\uE001").withStyle(Style.EMPTY.withFont(CommonFonts.WYNNTILS_TOOLTIP_ICONS));
        lines.add(PaginatorLineBuilder.buildPaginator(pageIcon, 0, 3));
        lines.add(TooltipLine.empty());

        return lines;
    }

    @Override
    protected ChatFormatting getSourceColor() {
        return GearTier.CRAFTED.getChatFormatting();
    }

    @Override
    protected void buildTooltip() {
        List<TooltipElement> header = buildHeader(source);
        List<TooltipElement> requirements = buildRequirements();
        List<TooltipElement> identifications = buildIdentificationLines();
        List<TooltipElement> footer = buildPaginator();

        List<TooltipElement> lines = new ArrayList<>();

        lines.addAll(header);
        lines.addAll(requirements);
        lines.addAll(identifications);
        lines.addAll(footer);

        this.tooltipLines = Handlers.Tooltip.alignTooltip(lines);
    }
}
