/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.ingredient;

import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Models;
import com.wynntils.core.text.fonts.CommonFonts;
import com.wynntils.core.text.fonts.wynnfonts.BannerBoxFont;
import com.wynntils.core.text.fonts.wynnfonts.BannerSymbolFont;
import com.wynntils.models.elements.type.Skill;
import com.wynntils.models.ingredients.type.IngredientInfo;
import com.wynntils.models.items.items.game.IngredientItem;
import com.wynntils.models.profession.type.ProfessionType;
import com.wynntils.models.stats.type.SkillStatType;
import com.wynntils.models.stats.type.StatListOrdering;
import com.wynntils.models.stats.type.StatType;
import com.wynntils.models.wynnitem.type.ItemObtainInfo;
import com.wynntils.models.wynnitem.type.ItemObtainType;
import com.wynntils.screens.guides.GuideItemStack;
import com.wynntils.utils.StringUtils;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.type.Pair;
import com.wynntils.utils.type.RangedValue;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;

public final class GuideIngredientItemStack extends GuideItemStack {
    private static final CustomColor CURRENT_PAGE = new CustomColor(255, 234, 128);
    private static final CustomColor INACTIVE_PAGE = new CustomColor(69, 84, 73);
    private static final CustomColor INGREDIENT_NAME_COLOR = CustomColor.fromInt(0x20aa20);
    private static final CustomColor INGREDIENT_INFO_COLOR = CustomColor.fromInt(0x7dc97d);
    private static final CustomColor POSITIVE_COLOR = CustomColor.fromInt(0xacfac6);
    private static final CustomColor POSITIVE_SECONDARY_COLOR = CustomColor.fromInt(0x6d9e7e);
    private static final CustomColor NEGATIVE_COLOR = CustomColor.fromInt(0xfaacac);
    private static final CustomColor NEGATIVE_SECONDARY_COLOR = CustomColor.fromInt(0xa87474);
    private static final String DEFAULT_SPACE = "\uDAFF\uDFB9\uDB00\uDC4F";

    private static final int EFFECTIVENESS_GRID_COLUMNS = 3;
    private static final int EFFECTIVENESS_CELL_WIDTH = 38;
    private static final int EFFECTIVENESS_CENTER_ROW = 2;

    private List<Component> generatedTooltip;
    private final IngredientInfo ingredientInfo;

    private boolean statsPage = true;

    public GuideIngredientItemStack(IngredientInfo ingredientInfo) {
        super(ingredientInfo.material().itemStack(), new IngredientItem(ingredientInfo), ingredientInfo.name());

        this.ingredientInfo = ingredientInfo;
    }

    @Override
    public List<Component> getTooltipLines(Item.TooltipContext context, Player player, TooltipFlag isAdvanced) {
        if (generatedTooltip == null) {
            List<Component> tooltip = new ArrayList<>(generateLore());

            generatedTooltip = tooltip;
        }

        return generatedTooltip;
    }

    public void changePage() {
        statsPage = !statsPage;
        generatedTooltip = null;
    }

    private List<Component> generateLore() {
        List<Component> tooltipLines = new ArrayList<>();
        tooltipLines.add(Component.empty());

        MutableComponent emblemLine = Component.empty()
                .append(Component.literal("\uDAFF\uDFF0").withStyle(Style.EMPTY.withFont(CommonFonts.SPACE_FONT)))
                .append(Component.literal("\uE031")
                        .withStyle(Style.EMPTY.withFont(CommonFonts.TOOLTIP_EMBLEM_FRAME_FONT))
                        .withoutShadow())
                .append(Component.literal("\uDAFF\uDFCF").withStyle(Style.EMPTY.withFont(CommonFonts.SPACE_FONT)))
                .append(Component.literal("\uE034")
                        .withStyle(Style.EMPTY
                                .withFont(CommonFonts.TOOLTIP_EMBLEM_SPRITE_FONT)
                                .withColor(CustomColor.fromInt(0x00eb1c).asInt()))
                        .withoutShadow())
                .append(Component.literal("\uDB00\uDC05").withStyle(Style.EMPTY.withFont(CommonFonts.SPACE_FONT)))
                .append(Component.literal(ingredientInfo.name())
                        .withStyle(Style.EMPTY
                                .withFont(CommonFonts.LANGUAGE_WYNNCRAFT_FONT)
                                .withColor(INGREDIENT_NAME_COLOR.asInt())));
        tooltipLines.add(emblemLine);

        MutableComponent tagLine = Component.empty()
                .append(Component.literal("\uDB00\uDC26").withStyle(Style.EMPTY.withFont(CommonFonts.SPACE_FONT)))
                .append(BannerSymbolFont.buildMessage(
                        3,
                        ingredientInfo.tier(),
                        CustomColor.fromChatFormatting(ChatFormatting.DARK_GRAY),
                        CustomColor.fromChatFormatting(getColorForTier(ingredientInfo.tier())),
                        CommonColors.BLACK,
                        "\uDB00\uDC03"))
                .append(BannerBoxFont.buildMessage("ingredient", INGREDIENT_INFO_COLOR, CommonColors.BLACK, ""))
                .append(Component.literal("\uDB00\uDC01").withStyle(Style.EMPTY.withFont(CommonFonts.SPACE_FONT)));
        tooltipLines.add(tagLine);

        tooltipLines.add(Component.empty());

        if (statsPage) {
            MutableComponent levelLine = Component.empty()
                    .append(Component.literal(String.valueOf(ingredientInfo.level()))
                            .withStyle(Style.EMPTY
                                    .withFont(CommonFonts.OFFSET_QUAD_12)
                                    .withColor(INGREDIENT_INFO_COLOR.asInt())))
                    .append(Component.literal(" Crafting Level")
                            .withStyle(Style.EMPTY
                                    .withFont(CommonFonts.LANGUAGE_WYNNCRAFT_FONT)
                                    .withColor(ChatFormatting.WHITE)));
            tooltipLines.add(levelLine);

            tooltipLines.addAll(buildProfessionLines());

            int statDividerIndex = tooltipLines.size();

            List<Pair<Component, Component>> effectPairs = new ArrayList<>(buildEffectsAndRequirements());
            List<Pair<Component, Component>> statPairs = buildStatPairs();

            List<Pair<Component, Component>> alignedPairs = new ArrayList<>(effectPairs);

            if (!effectPairs.isEmpty() && !statPairs.isEmpty()) {
                alignedPairs.add(Pair.of(Component.empty(), Component.empty()));
            }

            alignedPairs.addAll(statPairs);

            int effectivenessDividerIndex = -1;

            boolean anyModifiers =
                    ingredientInfo.positionModifiers().values().stream().anyMatch(value -> value != 0);
            if (anyModifiers) {
                effectivenessDividerIndex = tooltipLines.size();

                List<Component> modifierHeader = new ArrayList<>();
                modifierHeader.add(
                        BannerBoxFont.buildMessage("effectiveness", INGREDIENT_INFO_COLOR, CommonColors.BLACK, ""));
                modifierHeader.add(Component.literal("Grants an effectiveness multiplier")
                        .withStyle(Style.EMPTY.withFont(CommonFonts.LANGUAGE_WYNNCRAFT_FONT))
                        .withStyle(ChatFormatting.GRAY));
                modifierHeader.add(Component.literal("to nearby ingredients when used in")
                        .withStyle(Style.EMPTY.withFont(CommonFonts.LANGUAGE_WYNNCRAFT_FONT))
                        .withStyle(ChatFormatting.GRAY));
                modifierHeader.add(Component.literal("a Crafted Item recipe")
                        .withStyle(Style.EMPTY.withFont(CommonFonts.LANGUAGE_WYNNCRAFT_FONT))
                        .withStyle(ChatFormatting.GRAY));
                tooltipLines.addAll(modifierHeader);
            }

            List<Component> tempLines = new ArrayList<>(tooltipLines);
            for (Pair<Component, Component> pair : alignedPairs) {
                tempLines.add(Component.empty().append(pair.a()).append(pair.b()));
            }

            int widestLine =
                    tempLines.stream().mapToInt(McUtils.mc().font::width).max().orElse(0);

            // Check all of the pairs to see if they need spacing which will make them the widest line
            for (Pair<Component, Component> pair : alignedPairs) {
                Component combined = Component.empty().append(pair.a()).append(pair.b());

                int width = McUtils.mc().font.width(combined);
                String spacing = Managers.Font.calculateOffset(width, widestLine);

                if (spacing.isEmpty()) {
                    width += McUtils.mc()
                            .font
                            .width(Component.literal(DEFAULT_SPACE)
                                    .withStyle(Style.EMPTY.withFont(CommonFonts.SPACE_FONT)));

                    widestLine = Math.max(widestLine, width);
                }
            }

            int index = statDividerIndex;
            for (Pair<Component, Component> pair : alignedPairs) {
                Component combined = Component.empty().append(pair.a()).append(pair.b());

                int currentWidth = McUtils.mc().font.width(combined);

                String spacing = Managers.Font.calculateOffset(currentWidth, widestLine);

                if (spacing.isEmpty()) {
                    spacing = DEFAULT_SPACE;
                }

                tooltipLines.add(
                        index,
                        Component.empty()
                                .append(pair.a())
                                .append(Component.literal(spacing)
                                        .withStyle(Style.EMPTY.withFont(CommonFonts.SPACE_FONT)))
                                .append(pair.b()));
                index++;
            }

            boolean hasStatSection = !alignedPairs.isEmpty();

            if (hasStatSection) {
                effectivenessDividerIndex += alignedPairs.size() + 1;
            } else {
                effectivenessDividerIndex += alignedPairs.size();
            }

            if (anyModifiers) {
                Component gridBackground = Component.literal("\uF000")
                        .withStyle(Style.EMPTY.withFont(CommonFonts.TOOLTIP_ATTRIBUTE_SPRITE_FONT))
                        .withoutShadow();

                int gridWidth = McUtils.mc().font.width(gridBackground);
                int target = gridWidth + ((widestLine - gridWidth) / 2);
                String spacing = Managers.Font.calculateOffset(gridWidth, target);

                Component centeredGridLine = Component.empty()
                        .append(Component.literal(spacing).withStyle(Style.EMPTY.withFont(CommonFonts.SPACE_FONT)))
                        .append(gridBackground);

                tooltipLines.add(centeredGridLine);
                tooltipLines.addAll(buildModifierGrid(spacing));
            }

            Component divider = Component.literal("\uE000")
                    .withStyle(Style.EMPTY
                            .withFont(CommonFonts.TOOLTIP_DIVIDER_FONT)
                            .withColor(INGREDIENT_INFO_COLOR.asInt()));
            int dividerWidth = McUtils.mc().font.width(divider);
            int target = dividerWidth + ((widestLine - dividerWidth) / 2);
            String spacing = Managers.Font.calculateOffset(dividerWidth, target);
            Component centeredDivider = Component.empty()
                    .append(Component.literal(spacing).withStyle(Style.EMPTY.withFont(CommonFonts.SPACE_FONT)))
                    .append(divider);
            if (!alignedPairs.isEmpty()) {
                tooltipLines.add(statDividerIndex, centeredDivider);
            }

            if (anyModifiers) {
                tooltipLines.add(effectivenessDividerIndex, centeredDivider);
            }
        } else {
            Set<ItemObtainType> obtainTypes = new HashSet<>();
            ingredientInfo.obtainInfo().forEach(info -> {
                if (obtainTypes.isEmpty()) {
                    obtainTypes.add(info.sourceType());
                }
            });

            if (ingredientInfo.obtainInfo().isEmpty()) {
                tooltipLines.add(Component.literal("Unknown obtain method")
                        .withStyle(Style.EMPTY
                                .withFont(CommonFonts.LANGUAGE_WYNNCRAFT_FONT)
                                .withColor(ChatFormatting.GRAY)));
            } else {
                ItemObtainType currentObtainType = null;

                for (ItemObtainInfo info : ingredientInfo.obtainInfo()) {
                    ItemObtainType obtainType = info.sourceType();

                    if (currentObtainType != obtainType) {
                        if (currentObtainType != null) {
                            tooltipLines.add(Component.empty());
                        }

                        tooltipLines.add(Component.literal(obtainType.getDisplayName())
                                .withStyle(Style.EMPTY.withFont(CommonFonts.LANGUAGE_WYNNCRAFT_FONT)));
                        currentObtainType = obtainType;
                    }

                    if (info.name().isPresent()) {
                        tooltipLines.add(Component.literal(info.name().get())
                                .withStyle(Style.EMPTY
                                        .withFont(CommonFonts.LANGUAGE_WYNNCRAFT_FONT)
                                        .withColor(ChatFormatting.GRAY)));
                    }
                }
            }
        }

        Component footer = Component.empty()
                .withoutShadow()
                .append(Component.literal("\uDB00\uDC30")
                        .withStyle(Style.EMPTY.withFont(CommonFonts.LANGUAGE_WYNNCRAFT_FONT)))
                .append(Component.literal("\uE001").withStyle(Style.EMPTY.withFont(CommonFonts.WYNNTILS_TOOLTIP)))
                .append(Component.literal("\uDAFF\uDF98\uDB00\uDC4B")
                        .withStyle(Style.EMPTY.withFont(CommonFonts.LANGUAGE_WYNNCRAFT_FONT)))
                .append(Component.literal("\uE000")
                        .withStyle(Style.EMPTY
                                .withColor((statsPage ? CURRENT_PAGE : INACTIVE_PAGE).asInt())
                                .withFont(CommonFonts.TOOLTIP_PAGE_FONT)))
                .append(Component.literal("\uDB00\uDC04")
                        .withStyle(Style.EMPTY.withFont(CommonFonts.LANGUAGE_WYNNCRAFT_FONT)))
                .append(Component.literal("\uE000")
                        .withStyle(Style.EMPTY
                                .withColor((statsPage ? INACTIVE_PAGE : CURRENT_PAGE).asInt())
                                .withFont(CommonFonts.TOOLTIP_PAGE_FONT)));

        int widestLine =
                tooltipLines.stream().mapToInt(McUtils.mc().font::width).max().orElse(0);

        int target = widestLine / 2;
        int currentWidth = McUtils.mc().font.width(footer);
        String spacing = Managers.Font.calculateOffset(currentWidth, target);
        Component paddedFooter = Component.literal(spacing)
                .withStyle(Style.EMPTY.withFont(CommonFonts.SPACE_FONT))
                .append(footer);

        tooltipLines.add(paddedFooter);
        tooltipLines.add(Component.empty());

        return tooltipLines;
    }

    private List<Component> buildProfessionLines() {
        List<Component> professionLines = new ArrayList<>();

        MutableComponent header = Component.empty()
                .append(Component.literal("\uDB00\uDC02").withStyle(Style.EMPTY.withFont(CommonFonts.SPACE_FONT)))
                .append(Component.literal("Can be used in recipes for")
                        .withStyle(Style.EMPTY
                                .withFont(CommonFonts.LANGUAGE_WYNNCRAFT_FONT)
                                .withColor(ChatFormatting.GRAY)));
        professionLines.add(header);

        List<ProfessionType> professions = Models.Profession.getIngredientProfessionOrder().stream()
                .filter(professionType -> ingredientInfo.professions().contains(professionType))
                .toList();
        List<Pair<ProfessionType, ProfessionType>> professionPairs = IntStream.range(0, (professions.size() + 1) / 2)
                .mapToObj(i -> Pair.of(
                        professions.get(i * 2), (i * 2 + 1 < professions.size()) ? professions.get(i * 2 + 1) : null))
                .toList();

        for (Pair<ProfessionType, ProfessionType> professionPair : professionPairs) {
            MutableComponent professionLine = Component.empty()
                    .append(Component.literal("\uDB00\uDC02").withStyle(Style.EMPTY.withFont(CommonFonts.SPACE_FONT)))
                    .append(getPaddedProfessionIcon(professionPair.a())
                            .withStyle(Style.EMPTY.withFont(CommonFonts.PROFESSION_FONT)))
                    .append(Component.literal(getPaddedProfessionName(professionPair.a()))
                            .withStyle(Style.EMPTY
                                    .withFont(CommonFonts.LANGUAGE_WYNNCRAFT_FONT)
                                    .withColor(ChatFormatting.GRAY)));

            if (professionPair.b() != null) {
                professionLine
                        .append(getPaddedProfessionIcon(professionPair.b())
                                .withStyle(Style.EMPTY.withFont(CommonFonts.PROFESSION_FONT)))
                        .append(Component.literal(getPaddedProfessionName(professionPair.b()))
                                .withStyle(Style.EMPTY
                                        .withFont(CommonFonts.LANGUAGE_WYNNCRAFT_FONT)
                                        .withColor(ChatFormatting.GRAY)));
            }

            professionLines.add(professionLine);
        }

        return professionLines;
    }

    private List<Pair<Component, Component>> buildEffectsAndRequirements() {
        List<Pair<Component, Component>> linePairs = new ArrayList<>();

        if (ingredientInfo.durabilityModifier() != 0) {
            linePairs.add(getEffectsAndRequirementsPair(
                    "Durability", ingredientInfo.durabilityModifier(), ingredientInfo.durabilityModifier() > 0));
        }

        for (Pair<Skill, Integer> skillReq : ingredientInfo.skillRequirements()) {
            int minLevel = skillReq.value();
            linePairs.add(
                    getEffectsAndRequirementsPair("Min. " + skillReq.key().getDisplayName(), minLevel, minLevel < 0));
        }

        if (ingredientInfo.duration() != 0) {
            linePairs.add(getEffectsAndRequirementsPair(
                    "Duration", ingredientInfo.duration(), ingredientInfo.duration() > 0));
        }

        if (ingredientInfo.charges() != 0) {
            linePairs.add(
                    getEffectsAndRequirementsPair("Charges", ingredientInfo.charges(), ingredientInfo.charges() > 0));
        }

        return linePairs;
    }

    private List<Pair<Component, Component>> buildStatPairs() {
        List<Pair<Component, Component>> linePairs = new ArrayList<>();

        List<StatType> sortedStatTypes = Models.Stat.getOrderingList(StatListOrdering.WYNNCRAFT);

        Map<StatType, Integer> orderMap = IntStream.range(0, sortedStatTypes.size())
                .boxed()
                .collect(Collectors.toMap(sortedStatTypes::get, Function.identity()));
        List<Pair<StatType, RangedValue>> orderedStats = ingredientInfo.variableStats().stream()
                .sorted(Comparator.comparingInt(pair -> orderMap.getOrDefault(pair.a(), Integer.MAX_VALUE)))
                .toList();

        for (Pair<StatType, RangedValue> valuedStat : orderedStats) {
            boolean isPositive =
                    valuedStat.value().low() >= 0 && !valuedStat.key().treatAsInverted();
            if (valuedStat.value().isFixed()) {
                linePairs.add(Pair.of(
                        Component.literal(valuedStat.key().getDisplayName())
                                .withStyle(Style.EMPTY
                                        .withFont(CommonFonts.LANGUAGE_WYNNCRAFT_FONT)
                                        .withColor(ChatFormatting.WHITE)),
                        Component.literal(StringUtils.toSignedString(
                                        valuedStat.value().low()))
                                .withStyle(Style.EMPTY
                                        .withFont(CommonFonts.LANGUAGE_WYNNCRAFT_FONT)
                                        .withColor((isPositive ? POSITIVE_COLOR : NEGATIVE_COLOR).asInt()))));
            } else {
                MutableComponent statName = Component.literal(valuedStat.key().getDisplayName())
                        .withStyle(Style.EMPTY
                                .withFont(CommonFonts.LANGUAGE_WYNNCRAFT_FONT)
                                .withColor(ChatFormatting.WHITE));

                if (valuedStat.key() instanceof SkillStatType skillStatType) {
                    statName = Component.empty()
                            .append(Component.literal(getIconPrefix(skillStatType))
                                    .withStyle(Style.EMPTY.withFont(CommonFonts.TOOLTIP_ATTRIBUTE_SPRITE_FONT)))
                            .withoutShadow()
                            .append(statName);
                }

                int leftValue = isPositive
                        ? valuedStat.value().low()
                        : valuedStat.value().high();
                int rightValue = isPositive
                        ? valuedStat.value().high()
                        : valuedStat.value().low();
                MutableComponent statValue = Component.empty()
                        .withStyle(Style.EMPTY.withFont(CommonFonts.LANGUAGE_WYNNCRAFT_FONT))
                        .append(Component.literal(StringUtils.toSignedString(leftValue)
                                        + valuedStat.key().getUnit().getDisplayName())
                                .withColor((isPositive ? POSITIVE_COLOR : NEGATIVE_COLOR).asInt()))
                        .append(Component.literal(" to ")
                                .withColor((isPositive ? POSITIVE_SECONDARY_COLOR : NEGATIVE_SECONDARY_COLOR).asInt()))
                        .append(Component.literal(StringUtils.toSignedString(rightValue)
                                        + valuedStat.key().getUnit().getDisplayName())
                                .withColor((isPositive ? POSITIVE_COLOR : NEGATIVE_COLOR).asInt()));

                linePairs.add(Pair.of(statName, statValue));
            }
        }

        return linePairs;
    }

    private List<Component> buildModifierGrid(String initialSpace) {
        List<Component> modifierLines = new ArrayList<>();

        int[][] modifierGrid = Models.Ingredient.createPositionModifierGrid(ingredientInfo);

        for (int row = 0; row < modifierGrid.length; row++) {
            MutableComponent line = Component.empty()
                    .append(Component.literal(initialSpace).withStyle(Style.EMPTY.withFont(CommonFonts.SPACE_FONT)));

            for (int column = 0; column < EFFECTIVENESS_GRID_COLUMNS; column++) {
                Component cell = (row == EFFECTIVENESS_CENTER_ROW && column == 1)
                        ? Component.empty()
                        : buildGridCell(modifierGrid[row][column]);

                line = line.append(centerInCell(cell));
            }

            modifierLines.add(line);
        }

        return modifierLines;
    }

    private Component buildGridCell(int value) {
        String text = (value == 0 ? "0" : StringUtils.toSignedString(value)) + "%";

        CustomColor color = value == 0
                ? CustomColor.fromChatFormatting(ChatFormatting.GRAY)
                : value > 0 ? POSITIVE_COLOR : NEGATIVE_COLOR;

        return Component.literal(text)
                .withStyle(Style.EMPTY.withFont(CommonFonts.LANGUAGE_FIVE_FONT).withColor(color.asInt()));
    }

    private Component centerInCell(Component component) {
        int textWidth = McUtils.mc().font.width(component);

        int left = Math.max(0, (EFFECTIVENESS_CELL_WIDTH - textWidth) / 2);
        int right = Math.max(0, EFFECTIVENESS_CELL_WIDTH - textWidth - left);

        return Component.empty()
                .append(Component.literal(Managers.Font.calculateOffset(0, left))
                        .withStyle(Style.EMPTY.withFont(CommonFonts.SPACE_FONT)))
                .append(component)
                .append(Component.literal(Managers.Font.calculateOffset(0, right))
                        .withStyle(Style.EMPTY.withFont(CommonFonts.SPACE_FONT)));
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

    private ChatFormatting getColorForTier(Integer tier) {
        return switch (tier) {
            case 3 -> ChatFormatting.AQUA;
            case 2 -> ChatFormatting.LIGHT_PURPLE;
            case 1 -> ChatFormatting.YELLOW;
            default -> ChatFormatting.DARK_GRAY;
        };
    }

    private static Pair<Component, Component> getEffectsAndRequirementsPair(
            String effectName, int value, boolean isGood) {
        return Pair.of(
                Component.literal(effectName)
                        .withStyle(Style.EMPTY
                                .withFont(CommonFonts.LANGUAGE_WYNNCRAFT_FONT)
                                .withColor(ChatFormatting.WHITE)),
                Component.literal(StringUtils.toSignedString(value))
                        .withStyle(Style.EMPTY
                                .withFont(CommonFonts.LANGUAGE_WYNNCRAFT_FONT)
                                .withColor((isGood ? POSITIVE_COLOR : NEGATIVE_COLOR).asInt())));
    }

    private static String getIconPrefix(SkillStatType skillStatType) {
        return switch (skillStatType.getSkill()) {
            case STRENGTH -> "\uDAFF\uDFFF\uE010\uDB00\uDC02 ";
            case DEXTERITY -> "\uE011\uDB00\uDC02 ";
            case INTELLIGENCE -> "\uDAFF\uDFFF\uE012\uDB00\uDC02 ";
            case DEFENCE -> "\uDAFF\uDFFF\uE013\uDB00\uDC01\uDB00\uDC02 ";
            case AGILITY -> "\uE014\uDB00\uDC02 ";
        };
    }

    public IngredientInfo getIngredientInfo() {
        return ingredientInfo;
    }
}
