/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.tooltip.components;

import com.wynntils.core.text.CommonStyles;
import com.wynntils.core.text.fonts.CommonFonts;
import com.wynntils.handlers.tooltip.TooltipBuilder;
import com.wynntils.handlers.tooltip.type.TooltipAlignment;
import com.wynntils.handlers.tooltip.type.TooltipElement;
import com.wynntils.handlers.tooltip.type.TooltipGrid;
import com.wynntils.handlers.tooltip.type.TooltipLine;
import com.wynntils.models.elements.type.Element;
import com.wynntils.models.gear.type.GearAttackSpeed;
import com.wynntils.models.gear.type.GearTier;
import com.wynntils.models.stats.type.DamageType;
import com.wynntils.utils.StringUtils;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.type.Pair;
import com.wynntils.utils.type.RangedValue;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

public final class HeaderStatsLineBuilder {
    private static final int DEFENCE_GRID_COLUMNS = 3;
    private static final int DEFENCE_CELL_WIDTH = 40;
    private static final int DAMAGE_GRID_COLUMNS = 3;

    public static TooltipElement buildDpsLine(int dps, GearTier tier) {
        return buildhighlightStatLine(String.format("%,d", dps), "DPS", tier);
    }

    public static TooltipElement buildHealthLine(int health, GearTier tier) {
        return buildhighlightStatLine(StringUtils.toSignedCommaString(health), "Health", tier);
    }

    public static TooltipElement buildhighlightStatLine(String value, String label, GearTier tier) {
        return buildhighlightStatLine(value, label, TooltipBuilder.getDividerColor(tier));
    }

    public static TooltipElement buildhighlightStatLine(String value, String label, CustomColor highlightColor) {
        return TooltipLine.left(Component.empty()
                .append(Component.literal(value)
                        .withStyle(
                                Style.EMPTY.withFont(CommonFonts.OFFSET_QUAD_12).withColor(highlightColor.asInt())))
                .append(Component.literal(" " + label).withStyle(CommonStyles.WHITE_LANGUAGE_WYNNCRAFT)));
    }

    public static TooltipElement buildAttackSpeedLine(GearAttackSpeed attackSpeed) {
        Component attackSpeedLine = Component.empty()
                .withStyle(Style.EMPTY.withFont(CommonFonts.WYNNTILS_LANGUAGE_WYNNCRAFT_FONT))
                .append(Component.literal("\uE007")
                        .withStyle(Style.EMPTY
                                .withFont(CommonFonts.TOOLTIP_ATTRIBUTE_SPRITE_FONT)
                                .withoutShadow()))
                .append(Component.literal(" " + attackSpeed.getName()).withStyle(ChatFormatting.GRAY))
                .append(Component.literal(" (" + attackSpeed.getHitsPerSecond() + " hits/s)")
                        .withStyle(ChatFormatting.DARK_GRAY));

        return TooltipLine.left(attackSpeedLine);
    }

    public static List<TooltipElement> buildDefenceLines(List<Pair<Element, Integer>> defences) {
        if (defences.isEmpty()) return List.of();

        return List.of(
                TooltipLine.left(
                        Component.literal("Elemental Defences").withStyle(CommonStyles.GRAY_LANGUAGE_WYNNCRAFT)),
                buildDefenceGrid(defences));
    }

    public static TooltipGrid buildDamageGrid(List<Pair<DamageType, RangedValue>> damages) {
        TooltipGrid.Builder grid = TooltipGrid.builder(DAMAGE_GRID_COLUMNS)
                .spacing(Component.literal(" ")
                        .withStyle(Style.EMPTY.withFont(CommonFonts.WYNNTILS_LANGUAGE_WYNNCRAFT_FONT)));

        for (Pair<DamageType, RangedValue> damage : damages) {
            Component cell = Component.empty()
                    .append(Component.literal(damage.a().getTooltipSprite())
                            .withStyle(Style.EMPTY
                                    .withFont(CommonFonts.TOOLTIP_ATTRIBUTE_SPRITE_FONT)
                                    .withoutShadow()))
                    .append(Component.literal(
                                    " " + damage.b().low() + "-" + damage.b().high())
                            .withStyle(Style.EMPTY
                                    .withFont(CommonFonts.WYNNTILS_LANGUAGE_WYNNCRAFT_FONT)
                                    .withColor(ChatFormatting.GRAY)));

            grid.add(cell, TooltipAlignment.LEFT);
        }

        return grid.build();
    }

    public static TooltipGrid buildDefenceGrid(List<Pair<Element, Integer>> defences) {
        TooltipGrid.Builder grid = TooltipGrid.builder(DEFENCE_GRID_COLUMNS)
                .cellWidth(DEFENCE_CELL_WIDTH)
                .spacing(Component.literal(" ")
                        .withStyle(Style.EMPTY.withFont(CommonFonts.WYNNTILS_LANGUAGE_WYNNCRAFT_FONT)));

        for (Pair<Element, Integer> defence : defences) {
            Component cell = Component.empty()
                    .append(Component.empty()
                            .withStyle(
                                    Style.EMPTY.withColor(ChatFormatting.WHITE).withoutShadow())
                            .append(Component.literal(defence.a().getTooltipSprite())
                                    .withStyle(Style.EMPTY.withFont(CommonFonts.TOOLTIP_ATTRIBUTE_SPRITE_FONT)))
                            .append(Component.literal(" ")
                                    .withStyle(Style.EMPTY.withFont(CommonFonts.WYNNTILS_LANGUAGE_WYNNCRAFT_FONT))))
                    .append(Component.literal(StringUtils.toSignedCommaString(defence.b()))
                            .withStyle(Style.EMPTY
                                    .withFont(CommonFonts.WYNNTILS_LANGUAGE_WYNNCRAFT_FONT)
                                    .withColor(ChatFormatting.GRAY)));

            grid.add(cell, TooltipAlignment.LEFT);
        }

        return grid.build();
    }
}
