/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.tooltip.components;

import com.wynntils.core.text.CommonStyles;
import com.wynntils.core.text.fonts.CommonFonts;
import com.wynntils.core.text.fonts.wynnfonts.BannerBoxFont;
import com.wynntils.handlers.tooltip.type.TooltipAlignment;
import com.wynntils.handlers.tooltip.type.TooltipElement;
import com.wynntils.handlers.tooltip.type.TooltipGrid;
import com.wynntils.handlers.tooltip.type.TooltipLine;
import com.wynntils.handlers.tooltip.type.TooltipWrappedText;
import com.wynntils.models.elements.type.Element;
import com.wynntils.models.elements.type.Powder;
import com.wynntils.models.elements.type.PowderSpecialTierInfo;
import com.wynntils.models.gear.type.GearTier;
import com.wynntils.models.items.properties.PowderedItemProperty;
import com.wynntils.utils.MathUtils;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.type.Pair;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

public final class PowderLineBuilder {
    private static final int POWDER_GRID_COLUMNS = 2;
    private static final int POWDER_CELL_WIDTH = 70;
    private static final int POWDER_TABLE_WIDTH = 140;

    public static List<TooltipElement> buildPowders(PowderedItemProperty item, GearTier tier) {
        return buildPowders(item.getPowderSlots(), item.getPowders(), tier, item.getPowderSpecial());
    }

    public static List<TooltipElement> buildPowders(
            int powderSlots,
            List<Pair<Powder, Integer>> appliedPowders,
            GearTier tier,
            Optional<PowderSpecialTierInfo> powderSpecial) {
        List<TooltipElement> lines = new ArrayList<>();

        Component powderHeader =
                BannerBoxFont.buildMessage("powder sockets", tier.getSecondaryColor(), CommonColors.BLACK, "");
        Component powderCount = Component.literal(appliedPowders.size() + "/" + powderSlots)
                .withStyle(CommonStyles.GRAY_LANGUAGE_WYNNCRAFT);

        lines.add(TooltipLine.leftRight(powderHeader, powderCount));
        lines.add(TooltipLine.empty());

        if (powderSlots == 0) {
            Component noSockets = Component.literal("This item has no available Powder Sockets")
                    .withStyle(CommonStyles.GRAY_LANGUAGE_WYNNCRAFT);
            lines.add(TooltipWrappedText.center(noSockets, 140));
        } else {
            if (powderSpecial.isPresent()) {
                PowderSpecialTierInfo powderSpecialInfo = powderSpecial.get();
                Powder powder = powderSpecialInfo.element();

                Component powderSpecialName = Component.empty()
                        .append(Component.literal(getPaddedElement(powder.getElement()))
                                .withStyle(Style.EMPTY
                                        .withFont(CommonFonts.TOOLTIP_ATTRIBUTE_SPRITE_FONT)
                                        .withoutShadow()))
                        .append(Component.literal(" " + powderSpecialInfo.name())
                                .withStyle(Style.EMPTY
                                        .withFont(CommonFonts.WYNNTILS_LANGUAGE_WYNNCRAFT_FONT)
                                        .withColor(powder.getLightColor())));

                Component specialTier = Component.literal("Tier " + MathUtils.toRoman(powderSpecialInfo.tier()))
                        .withStyle(CommonStyles.GRAY_LANGUAGE_WYNNCRAFT);

                lines.add(TooltipLine.leftRight(powderSpecialName, specialTier));

                lines.add(TooltipWrappedText.left(powderSpecialInfo.getDescription()));

                lines.add(TooltipLine.empty());
            }

            TooltipGrid.Builder grid = TooltipGrid.builder(POWDER_GRID_COLUMNS)
                    .cellWidth(POWDER_CELL_WIDTH)
                    .tableWidth(POWDER_TABLE_WIDTH)
                    .spacing(Component.literal(" ")
                            .withStyle(Style.EMPTY.withFont(CommonFonts.WYNNTILS_LANGUAGE_WYNNCRAFT_FONT)))
                    .alignment(TooltipAlignment.LEFT);

            for (int slot = 0; slot < powderSlots; slot++) {
                Powder powder =
                        slot < appliedPowders.size() ? appliedPowders.get(slot).a() : null;
                String socketSprite = powder == null
                        ? "\uE000"
                        : switch (powder) {
                            case EARTH -> "\uE001";
                            case THUNDER -> "\uE002";
                            case WATER -> "\uE003";
                            case FIRE -> "\uE004";
                            case AIR -> "\uE005";
                        };

                String label = powder == null ? "Empty" : powder.getName();
                if (powder != null) {
                    int powderTier = appliedPowders.get(slot).b();
                    label += " " + MathUtils.toRoman(powderTier);
                }

                Component cell = Component.empty()
                        .append(Component.literal(socketSprite)
                                .withStyle(Style.EMPTY
                                        .withFont(CommonFonts.TOOLTIP_SOCKET_FONT)
                                        .withColor(ChatFormatting.WHITE)))
                        .append(Component.literal("\uDAFF\uDFFF")
                                .withStyle(Style.EMPTY.withFont(CommonFonts.SPACE_FONT)))
                        .append(Component.literal(" " + label)
                                .withStyle(Style.EMPTY
                                        .withFont(CommonFonts.WYNNTILS_LANGUAGE_WYNNCRAFT_FONT)
                                        .withColor(powder == null ? ChatFormatting.DARK_GRAY : powder.getLightColor())))
                        .append(Component.literal("\uDB00\uDC08")
                                .withStyle(Style.EMPTY.withFont(CommonFonts.SPACE_FONT)));
                grid.add(cell, TooltipAlignment.LEFT);
            }

            lines.add(grid.build());
        }

        return lines;
    }

    private static String getPaddedElement(Element element) {
        return switch (element) {
            case EARTH -> "\uDAFF\uDFFF\uE000\uDAFF\uDFFF";
            case THUNDER -> "\uE001\uDAFF\uDFFF";
            case WATER -> "\uE002";
            case FIRE -> "\uE003";
            case AIR -> "\uE004\uDAFF\uDFFF";
        };
    }
}
