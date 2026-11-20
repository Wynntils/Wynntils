/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.tooltip.builders;

import com.wynntils.core.components.Handlers;
import com.wynntils.core.components.Models;
import com.wynntils.core.text.CommonStyles;
import com.wynntils.core.text.fonts.CommonFonts;
import com.wynntils.core.text.fonts.wynnfonts.BannerBoxFont;
import com.wynntils.core.text.fonts.wynnfonts.BannerSymbolFont;
import com.wynntils.handlers.tooltip.TooltipBuilder;
import com.wynntils.handlers.tooltip.components.HeaderStatsLineBuilder;
import com.wynntils.handlers.tooltip.components.IdentificationLineBuilder;
import com.wynntils.handlers.tooltip.components.PaginatorLineBuilder;
import com.wynntils.handlers.tooltip.type.ItemEmblemSprite;
import com.wynntils.handlers.tooltip.type.ItemSourceType;
import com.wynntils.handlers.tooltip.type.TooltipAlignment;
import com.wynntils.handlers.tooltip.type.TooltipElement;
import com.wynntils.handlers.tooltip.type.TooltipGrid;
import com.wynntils.handlers.tooltip.type.TooltipLine;
import com.wynntils.handlers.tooltip.type.TooltipWrappedText;
import com.wynntils.models.elements.type.Skill;
import com.wynntils.models.ingredients.type.IngredientInfo;
import com.wynntils.models.profession.type.ProfessionType;
import com.wynntils.models.wynnitem.type.ItemEmblem;
import com.wynntils.models.wynnitem.type.ItemObtainInfo;
import com.wynntils.utils.StringUtils;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.type.Pair;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

public class IngredientTooltipBuilder extends TooltipBuilder {
    private static final ItemEmblem ITEM_EMBLEM = new ItemEmblem(ItemEmblem.ItemEmblemShape.SQUARE, 4);

    private static final int EFFECTIVENESS_DESCRIPTION_LENGTH = 160;

    private static final int EFFECTIVENESS_GRID_COLUMNS = 3;
    private static final int EFFECTIVENESS_CELL_WIDTH = 36;
    private static final int EFFECTIVENESS_TABLE_WIDTH = 108;
    private static final int EFFECTIVENESS_CENTER_ROW = 2;

    private static final int PROFESSION_GRID_COLUMNS = 2;

    private final IngredientInfo ingredientInfo;
    private final int currentPage;

    private IngredientTooltipBuilder(
            IngredientInfo ingredientInfo, int currentPage, ItemSourceType sourceType, String source) {
        super(sourceType, source);

        this.ingredientInfo = ingredientInfo;
        this.currentPage = currentPage;
    }

    public static IngredientTooltipBuilder buildNewItem(
            IngredientInfo ingredientInfo, ItemSourceType sourceType, String source, int currentPage) {
        return new IngredientTooltipBuilder(ingredientInfo, currentPage, sourceType, source);
    }

    private List<TooltipElement> buildHeaderLines() {
        List<TooltipElement> lines = new ArrayList<>();
        lines.add(TooltipLine.empty());

        lines.addAll(buildHeader(
                ITEM_EMBLEM,
                ItemEmblemSprite.INGREDIENT,
                Component.literal(ingredientInfo.name())
                        .withStyle(Style.EMPTY
                                .withFont(CommonFonts.WYNNTILS_LANGUAGE_WYNNCRAFT_FONT)
                                .withColor(CommonColors.INGREDIENT_PRIMARY.asInt())),
                BannerSymbolFont.buildMessage(
                        3,
                        ingredientInfo.tier(),
                        CustomColor.fromChatFormatting(ChatFormatting.DARK_GRAY),
                        CustomColor.fromChatFormatting(Models.Profession.getColorForTier(ingredientInfo.tier())),
                        CommonColors.BLACK,
                        ""),
                BannerBoxFont.buildMessage("ingredient", CommonColors.INGREDIENT_SECONDARY, CommonColors.BLACK, "")));

        return lines;
    }

    private List<TooltipElement> buildStatsPage() {
        List<TooltipElement> lines = new ArrayList<>();

        lines.add(HeaderStatsLineBuilder.buildhighlightStatLine(
                String.valueOf(ingredientInfo.level()), "Crafting Level", CommonColors.INGREDIENT_SECONDARY));

        lines.addAll(buildProfessionLines());

        lines.add(divider(CommonColors.INGREDIENT_SECONDARY));

        lines.addAll(buildStatLines());

        boolean anyModifiers =
                ingredientInfo.positionModifiers().values().stream().anyMatch(value -> value != 0);
        if (anyModifiers) {
            lines.addAll(buildEffectivenessLines());
        }

        return lines;
    }

    private List<TooltipElement> buildObtainPage() {
        List<ItemObtainInfo> obtainInfo = Models.Ingredient.getObtainInfo(ingredientInfo);

        return buildObtainInfo(
                obtainInfo.isEmpty() ? List.of(ItemObtainInfo.UNKNOWN) : obtainInfo, TooltipAlignment.LEFT);
    }

    private List<TooltipElement> buildProfessionLines() {
        List<TooltipElement> lines = new ArrayList<>();

        MutableComponent header = Component.empty()
                .append(Component.literal("\uDB00\uDC02").withStyle(Style.EMPTY.withFont(CommonFonts.SPACE_FONT)))
                .append(Component.literal("Can be used in recipes for")
                        .withStyle(Style.EMPTY
                                .withFont(CommonFonts.WYNNTILS_LANGUAGE_WYNNCRAFT_FONT)
                                .withColor(ChatFormatting.GRAY)));
        lines.add(TooltipLine.left(header));

        TooltipGrid.Builder grid = TooltipGrid.builder(PROFESSION_GRID_COLUMNS)
                .padding(Component.literal("\uDB00\uDC02").withStyle(Style.EMPTY.withFont(CommonFonts.SPACE_FONT)));

        Models.Profession.getIngredientProfessionOrder().stream()
                .filter(professionType -> ingredientInfo.professions().contains(professionType))
                .forEach(profession -> grid.add(
                        Component.empty()
                                .append(getPaddedProfessionIcon(profession)
                                        .withStyle(Style.EMPTY.withFont(CommonFonts.PROFESSION_FONT)))
                                .append(Component.literal(getPaddedProfessionName(profession))
                                        .withStyle(Style.EMPTY
                                                .withFont(CommonFonts.WYNNTILS_LANGUAGE_WYNNCRAFT_FONT)
                                                .withColor(ChatFormatting.GRAY))),
                        TooltipAlignment.LEFT));

        lines.add(grid.build());

        return lines;
    }

    private List<TooltipElement> buildStatLines() {
        List<TooltipElement> lines = new ArrayList<>();

        List<TooltipElement> effectLines = buildEffectsAndRequirements();
        lines.addAll(effectLines);

        if (!effectLines.isEmpty()) {
            lines.add(TooltipLine.empty());
        }

        lines.addAll(IdentificationLineBuilder.buildIdentificationLines(ingredientInfo.variableStats(), true));

        return lines;
    }

    private List<TooltipElement> buildEffectsAndRequirements() {
        List<TooltipElement> lines = new ArrayList<>();

        if (ingredientInfo.durabilityModifier() != 0) {
            lines.add(getEffectsAndRequirementsLine(
                    "Durability", ingredientInfo.durabilityModifier(), "", ingredientInfo.durabilityModifier() > 0));
        }

        for (Pair<Skill, Integer> skillReq : ingredientInfo.skillRequirements()) {
            int minLevel = skillReq.value();
            lines.add(getEffectsAndRequirementsLine(
                    "Min. " + skillReq.key().getDisplayName(), minLevel, "", minLevel < 0));
        }

        if (ingredientInfo.duration() != 0) {
            lines.add(getEffectsAndRequirementsLine(
                    "Duration", ingredientInfo.duration(), "s", ingredientInfo.duration() > 0));
        }

        if (ingredientInfo.charges() != 0) {
            lines.add(getEffectsAndRequirementsLine(
                    "Charges", ingredientInfo.charges(), "", ingredientInfo.charges() > 0));
        }

        return lines;
    }

    private List<TooltipElement> buildEffectivenessLines() {
        List<TooltipElement> lines = new ArrayList<>();

        lines.add(divider(CommonColors.INGREDIENT_SECONDARY));
        lines.add(TooltipLine.left(BannerBoxFont.buildMessage(
                "effectiveness", CommonColors.INGREDIENT_SECONDARY, CommonColors.BLACK, "")));
        lines.add(TooltipWrappedText.left(
                Component.literal(
                                "Grants an effectiveness multiplier to nearby ingredients when used in a Crafted Item recipe")
                        .withStyle(CommonStyles.GRAY_LANGUAGE_WYNNCRAFT),
                EFFECTIVENESS_DESCRIPTION_LENGTH));

        lines.add(TooltipLine.center(Component.literal("\uF000")
                .withStyle(Style.EMPTY.withFont(CommonFonts.TOOLTIP_ATTRIBUTE_SPRITE_FONT))
                .withoutShadow()));

        TooltipGrid.Builder grid = TooltipGrid.builder(EFFECTIVENESS_GRID_COLUMNS)
                .cellWidth(EFFECTIVENESS_CELL_WIDTH)
                .tableWidth(EFFECTIVENESS_TABLE_WIDTH)
                .alignment(TooltipAlignment.CENTER)
                .spacing(Component.literal("\uDB00\uDC02").withStyle(Style.EMPTY.withFont(CommonFonts.SPACE_FONT)));

        int[][] modifierGrid = Models.Ingredient.createPositionModifierGrid(ingredientInfo);
        for (int row = 0; row < modifierGrid.length; row++) {
            for (int column = 0; column < EFFECTIVENESS_GRID_COLUMNS; column++) {
                // The center of the middle row is where the ingredient itself sits
                Component cell = (row == EFFECTIVENESS_CENTER_ROW && column == 1)
                        ? Component.empty()
                        : buildGridCell(modifierGrid[row][column]);

                grid.add(cell, TooltipAlignment.CENTER);
            }
        }

        lines.add(grid.build());

        return lines;
    }

    private static TooltipLine getEffectsAndRequirementsLine(
            String effectName, int value, String valueSuffix, boolean positive) {
        return TooltipLine.leftRight(
                Component.literal(effectName)
                        .withStyle(Style.EMPTY
                                .withFont(CommonFonts.WYNNTILS_LANGUAGE_WYNNCRAFT_FONT)
                                .withColor(ChatFormatting.WHITE)),
                Component.literal(StringUtils.toSignedCommaString(value) + valueSuffix)
                        .withStyle(Style.EMPTY
                                .withFont(CommonFonts.WYNNTILS_LANGUAGE_WYNNCRAFT_FONT)
                                .withColor((positive ? CommonColors.WYNNCRAFT_GREEN : CommonColors.WYNNCRAFT_RED)
                                        .asInt())));
    }

    private Component buildGridCell(int value) {
        String text = (value == 0 ? "0" : StringUtils.toSignedString(value)) + "%";

        CustomColor color = value == 0
                ? CustomColor.fromChatFormatting(ChatFormatting.GRAY)
                : value > 0 ? CommonColors.WYNNCRAFT_GREEN : CommonColors.WYNNCRAFT_RED;

        return Component.literal(text)
                .withStyle(Style.EMPTY.withFont(CommonFonts.LANGUAGE_FIVE_FONT).withColor(color.asInt()));
    }

    private MutableComponent getPaddedProfessionIcon(ProfessionType profession) {
        String prefix =
                switch (profession) {
                    case ALCHEMISM, ARMOURING, JEWELING -> "\uDB00\uDC01";
                    default -> "";
                };

        String suffix =
                switch (profession) {
                    case COOKING, JEWELING, SCRIBING, WOODWORKING -> "\uDAFF\uDFFE";
                    case ALCHEMISM, ARMOURING, WEAPONSMITHING -> "\uDAFF\uDFFF";
                    default -> "";
                };

        return Component.empty()
                .withStyle(Style.EMPTY.withFont(CommonFonts.SPACE_FONT))
                .append(Component.literal(prefix))
                .append(profession.getProfessionIcon())
                .append(suffix);
    }

    private String getPaddedProfessionName(ProfessionType profession) {
        String suffix =
                switch (profession) {
                    case ALCHEMISM, ARMOURING, COOKING, WEAPONSMITHING -> "\uDB00\uDC04 ";
                    case JEWELING, TAILORING -> "\uDB00\uDC05 ";
                    case SCRIBING -> "\uDB00\uDC06 ";
                    case WOODWORKING -> "\uDB00\uDC0B ";
                    default -> " ";
                };

        return " " + profession.getDisplayName() + suffix;
    }

    @Override
    protected void buildTooltip() {
        List<TooltipElement> lines = new ArrayList<>();

        lines.addAll(buildHeaderLines());
        lines.add(TooltipLine.empty());

        if (sourceType != ItemSourceType.GUIDE || currentPage == 0) {
            lines.addAll(buildStatsPage());
        } else {
            lines.addAll(buildObtainPage());
        }

        if (sourceType == ItemSourceType.GUIDE) {
            Component pageIcon =
                    Component.literal("\uE001").withStyle(Style.EMPTY.withFont(CommonFonts.WYNNTILS_TOOLTIP_ICONS));
            lines.add(PaginatorLineBuilder.buildPaginator(pageIcon, currentPage, 2));
            lines.add(TooltipLine.empty());
        }

        this.tooltipLines = Handlers.Tooltip.alignTooltip(lines);
    }
}
