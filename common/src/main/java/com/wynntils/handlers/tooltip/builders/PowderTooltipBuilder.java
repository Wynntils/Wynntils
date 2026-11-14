/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.tooltip.builders;

import com.wynntils.core.components.Handlers;
import com.wynntils.core.components.Models;
import com.wynntils.core.text.fonts.CommonFonts;
import com.wynntils.handlers.tooltip.TooltipBuilder;
import com.wynntils.handlers.tooltip.type.ItemSourceType;
import com.wynntils.handlers.tooltip.type.TooltipElement;
import com.wynntils.handlers.tooltip.type.TooltipLine;
import com.wynntils.handlers.tooltip.type.TooltipWrappedText;
import com.wynntils.models.elements.type.Powder;
import com.wynntils.models.elements.type.PowderTierInfo;
import com.wynntils.utils.MathUtils;
import com.wynntils.utils.StringUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

public class PowderTooltipBuilder extends TooltipBuilder {
    private final PowderTierInfo powderTierInfo;
    private final Powder element;
    private final int tier;

    private PowderTooltipBuilder(PowderTierInfo powderTierInfo, ItemSourceType sourceType, String source) {
        super(sourceType, source);

        this.powderTierInfo = powderTierInfo;
        this.element = powderTierInfo.element();
        this.tier = powderTierInfo.tier();
    }

    public static PowderTooltipBuilder buildNewItem(
            PowderTierInfo powderTierInfo, ItemSourceType sourceType, String source) {
        return new PowderTooltipBuilder(powderTierInfo, sourceType, source);
    }

    @Override
    protected void buildTooltip() {
        List<TooltipElement> lines = new ArrayList<>();

        Component name = Component.empty()
                .withStyle(Style.EMPTY.withColor(element.getLightColor()))
                .append(Component.literal(String.valueOf(element.getSymbol()))
                        .withStyle(Style.EMPTY.withFont(CommonFonts.COMMON_FONT)))
                .append(Component.literal(" " + element.getName() + " Powder " + MathUtils.toRoman(tier)));
        lines.add(TooltipWrappedText.left(name, 200));

        String tierStringBuilder = element.getDarkColor()
                + "■".repeat(Math.max(0, tier))
                + ChatFormatting.DARK_GRAY
                + "■".repeat(Math.max(0, 7 - tier));

        String elementName = element.getName();
        Powder opposingElement = Models.Element.getOpposingElement(element);

        lines.add(TooltipLine.left(Component.literal("Tier " + tier + " [")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal(tierStringBuilder))
                .append(Component.literal("]").withStyle(ChatFormatting.GRAY))));

        lines.add(TooltipLine.empty());

        lines.add(TooltipLine.left(Component.literal("Effect on Weapons:").withStyle(element.getDarkColor())));
        lines.add(TooltipLine.left(Component.empty()
                .append(Component.literal(element.getDarkColor() + "- " + ChatFormatting.GRAY + "+"
                        + powderTierInfo.min() + "-" + powderTierInfo.max() + " " + element.getLightColor()))
                .append(Component.literal(String.valueOf(element.getSymbol()))
                        .withStyle(Style.EMPTY.withFont(CommonFonts.COMMON_FONT).withColor(element.getLightColor())))
                .append(Component.literal(
                        element.getLightColor() + " " + elementName + " " + ChatFormatting.GRAY + "Damage"))));
        lines.add(TooltipLine.left(Component.empty()
                .append(Component.literal(element.getDarkColor() + "- " + ChatFormatting.GRAY + "+"
                        + powderTierInfo.convertedFromNeutral() + "% " + ChatFormatting.GOLD + "✣ Neutral"
                        + ChatFormatting.GRAY + " to " + element.getLightColor()))
                .append(Component.literal(String.valueOf(element.getSymbol()))
                        .withStyle(Style.EMPTY.withFont(CommonFonts.COMMON_FONT).withColor(element.getLightColor())))
                .append(Component.literal(element.getLightColor() + " " + elementName))));

        lines.add(TooltipLine.empty());

        lines.add(TooltipLine.left(Component.literal("Effect on Armour:").withStyle(element.getDarkColor())));
        lines.add(TooltipLine.left(Component.literal(
                element.getDarkColor() + "- " + ChatFormatting.GRAY + "+" + powderTierInfo.health() + " Health")));
        lines.add(TooltipLine.left(Component.empty()
                .append(Component.literal(element.getDarkColor() + "- " + ChatFormatting.GRAY + "+"
                        + powderTierInfo.addedDefence() + " " + element.getLightColor()))
                .append(Component.literal(String.valueOf(element.getSymbol()))
                        .withStyle(Style.EMPTY.withFont(CommonFonts.COMMON_FONT).withColor(element.getLightColor())))
                .append(Component.literal(
                        element.getLightColor() + " " + elementName + " " + ChatFormatting.GRAY + "Defence"))));
        lines.add(TooltipLine.left(Component.empty()
                .append(Component.literal(element.getDarkColor() + "- " + ChatFormatting.GRAY + "-"
                        + powderTierInfo.removedDefence() + " " + opposingElement.getLightColor()))
                .append(Component.literal(String.valueOf(opposingElement.getSymbol()))
                        .withStyle(Style.EMPTY
                                .withFont(CommonFonts.COMMON_FONT)
                                .withColor(opposingElement.getLightColor())))
                .append(Component.literal(opposingElement.getLightColor() + " "
                        + StringUtils.capitalizeFirst(opposingElement.name().toLowerCase(Locale.ROOT)) + " "
                        + ChatFormatting.GRAY + "Defence"))));
        lines.add(TooltipLine.left(Component.literal(
                element.getDarkColor() + "Ingredient Effectiveness: " + ChatFormatting.GRAY + "50%")));

        lines.add(TooltipLine.empty());

        lines.add(TooltipWrappedText.left(
                Component.literal(
                                "Hold this and right-click on a "
                                        + (tier > 1 ? "Lv. " + powderTierInfo.levelRequirement() + "+" : "piece of")
                                        + " equipment to socket it or use it as an ingredient when crafting. Powders are refunded when removed.")
                        .withStyle(ChatFormatting.DARK_GRAY),
                164));

        if (sourceType == ItemSourceType.GUIDE && tier > 3) {
            lines.add(TooltipLine.empty());
            lines.add(TooltipWrappedText.left(
                    Component.literal("Adding 2 powders of Tier 4 or higher will unlock a special attack/effect")
                            .withStyle(ChatFormatting.DARK_GRAY),
                    164));
        }

        this.tooltipLines = Handlers.Tooltip.alignTooltip(lines);
    }
}
