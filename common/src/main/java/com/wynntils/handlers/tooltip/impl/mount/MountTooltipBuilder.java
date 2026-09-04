/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.tooltip.impl.mount;

import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Models;
import com.wynntils.core.text.CommonStyles;
import com.wynntils.core.text.fonts.CommonFonts;
import com.wynntils.core.text.fonts.wynnfonts.BannerBoxFont;
import com.wynntils.core.text.fonts.wynnfonts.TooltipIdentificationMeterFont;
import com.wynntils.handlers.tooltip.TooltipBuilder;
import com.wynntils.handlers.tooltip.type.TooltipIdentificationDecorator;
import com.wynntils.handlers.tooltip.type.TooltipStyle;
import com.wynntils.models.character.type.ClassType;
import com.wynntils.models.items.items.game.MountItem;
import com.wynntils.models.mount.type.MountStat;
import com.wynntils.utils.StringUtils;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.type.CappedValue;
import com.wynntils.utils.type.Pair;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

public final class MountTooltipBuilder extends TooltipBuilder {
    private static final CustomColor MOUNT_INFO_COLOR = CustomColor.fromInt(0xe0e0e0);
    private static final CustomColor STAT_VALUE_COLOR = CustomColor.fromInt(0xacfac6);
    private static final String DEFAULT_SPACE = "\uDAFF\uDFB9\uDB00\uDC4F";

    private final MountItem mountItem;

    private MountTooltipBuilder(MountItem mountItem, List<Component> header, List<Component> footer, String source) {
        super(header, footer, source, true);
        this.mountItem = mountItem;
    }

    public static MountTooltipBuilder buildNewItem(MountItem mountItem, String source) {
        return new MountTooltipBuilder(mountItem, List.of(), List.of(), source);
    }

    @Override
    protected List<Component> buildTooltipLines(
            ClassType currentClass, TooltipStyle style, TooltipIdentificationDecorator identificationDecorator) {
        List<Component> tooltipLines = new ArrayList<>();
        tooltipLines.add(Component.empty());

        MutableComponent emblemLine = Component.empty()
                .append(Component.literal("\uDAFF\uDFF0").withStyle(Style.EMPTY.withFont(CommonFonts.SPACE_FONT)))
                .append(Component.literal("\uE034")
                        .withStyle(Style.EMPTY.withFont(CommonFonts.TOOLTIP_EMBLEM_FRAME_FONT))
                        .withoutShadow())
                .append(Component.literal("\uDAFF\uDFCF").withStyle(Style.EMPTY.withFont(CommonFonts.SPACE_FONT)))
                .append(Component.literal("\uE037")
                        .withStyle(Style.EMPTY
                                .withFont(CommonFonts.TOOLTIP_EMBLEM_SPRITE_FONT)
                                .withColor(CustomColor.fromInt(0x00eb1c).asInt()))
                        .withoutShadow())
                .append(Component.literal("\uDB00\uDC05").withStyle(Style.EMPTY.withFont(CommonFonts.SPACE_FONT)))
                .append(Component.literal(StringUtils.toPossessive(mountItem.getName()) + " "
                                + mountItem.getMountType().getMountItemName())
                        .withStyle(Style.EMPTY.withFont(CommonFonts.LANGUAGE_WYNNCRAFT_FONT)));
        tooltipLines.add(emblemLine);

        MutableComponent tagLine = Component.empty()
                .append(Component.literal("\uDB00\uDC26").withStyle(Style.EMPTY.withFont(CommonFonts.SPACE_FONT)))
                .append(BannerBoxFont.buildMessage("mount", CommonColors.WHITE, CommonColors.BLACK, "\uDB00\uDC03"))
                .append(BannerBoxFont.buildMessage(
                        mountItem.getMountType().name(), MOUNT_INFO_COLOR, CommonColors.BLACK, ""))
                .append(Component.literal("\uDB00\uDC01").withStyle(Style.EMPTY.withFont(CommonFonts.SPACE_FONT)));
        tooltipLines.add(tagLine);

        tooltipLines.add(Component.empty());

        MutableComponent potentialLine = Component.empty()
                .append(Component.literal(StringUtils.integerToShortString(
                        mountItem.getMountInfo().potential())))
                .withStyle(Style.EMPTY.withFont(CommonFonts.OFFSET_QUAD_12).withColor(MOUNT_INFO_COLOR.asInt()))
                .append(Component.literal(" Potential")
                        .withStyle(Style.EMPTY
                                .withFont(CommonFonts.LANGUAGE_WYNNCRAFT_FONT)
                                .withColor(ChatFormatting.WHITE)));
        tooltipLines.add(potentialLine);

        MutableComponent colorLine = Component.empty()
                .append(Component.literal("\uE00E")
                        .withoutShadow()
                        .withStyle(Style.EMPTY.withFont(CommonFonts.TOOLTIP_ATTRIBUTE_SPRITE_FONT)))
                .append(Component.literal("\uDB00\uDC01").withStyle(CommonStyles.SPACE))
                .append(Component.literal(" "
                                + mountItem.getMountInfo().primaryColorInfo().displayName() + "-"
                                + mountItem.getMountInfo().secondaryColorInfo().displayName())
                        .withStyle(Style.EMPTY
                                .withFont(CommonFonts.LANGUAGE_WYNNCRAFT_FONT)
                                .withColor(ChatFormatting.GRAY)));
        tooltipLines.add(colorLine);

        MutableComponent energyLine = Component.empty()
                .append(TooltipIdentificationMeterFont.buildCounterSingleLayerMeter(
                        mountItem.getMountInfo().currentEnergy(),
                        MOUNT_INFO_COLOR,
                        CustomColor.fromChatFormatting(ChatFormatting.DARK_GRAY),
                        "\uDB00\uDC05"))
                .append(Component.literal("Energy " + mountItem.getMountInfo().currentEnergy())
                        .withStyle(Style.EMPTY
                                .withFont(CommonFonts.LANGUAGE_WYNNCRAFT_FONT)
                                .withColor(ChatFormatting.GRAY)));
        tooltipLines.add(energyLine);

        int headerQuestDividerIndex = tooltipLines.size();

        List<Pair<Component, Component>> requirementPairs = new ArrayList<>(buildRequirements());

        List<Pair<Component, Component>> statPairs = new ArrayList<>(buildStats());

        List<Pair<Component, Component>> alignedPairs = new ArrayList<>(requirementPairs);
        alignedPairs.addAll(statPairs);

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

        int index = headerQuestDividerIndex;
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
                            .append(Component.literal(spacing).withStyle(Style.EMPTY.withFont(CommonFonts.SPACE_FONT)))
                            .append(pair.b()));
            index++;
        }

        Component divider =
                Component.literal("\uE000").withStyle(Style.EMPTY.withFont(CommonFonts.TOOLTIP_DIVIDER_FONT));
        int dividerWidth = McUtils.mc().font.width(divider);
        int dividerTarget = dividerWidth + ((widestLine - dividerWidth) / 2);
        String dividerSpacing = Managers.Font.calculateOffset(dividerWidth, dividerTarget);
        Component centeredDivider = Component.empty()
                .append(Component.literal(dividerSpacing).withStyle(Style.EMPTY.withFont(CommonFonts.SPACE_FONT)))
                .append(divider);
        tooltipLines.add(headerQuestDividerIndex, centeredDivider);
        int questStatDividerIndex = headerQuestDividerIndex + requirementPairs.size() + 1;
        tooltipLines.add(questStatDividerIndex, centeredDivider);

        if (mountItem.getMountInfo().estimatedMaxStats()) {
            Component estimatedLine = Component.literal("Max stats are estimated")
                    .withStyle(Style.EMPTY
                            .withFont(CommonFonts.LANGUAGE_WYNNCRAFT_FONT)
                            .withColor(ChatFormatting.GRAY));
            int estimatedWidth = McUtils.mc().font.width(estimatedLine);
            int estimatedTarget = estimatedWidth + ((widestLine - estimatedWidth) / 2);
            String estimatedSpacing = Managers.Font.calculateOffset(estimatedWidth, estimatedTarget);
            Component centeredEstimate = Component.empty()
                    .append(Component.literal(estimatedSpacing)
                            .withStyle(Style.EMPTY.withFont(CommonFonts.SPACE_FONT))
                            .append(estimatedLine));
            int estimatedLineIndex = questStatDividerIndex + 1;
            tooltipLines.add(estimatedLineIndex, centeredEstimate);
            tooltipLines.add(estimatedLineIndex + 1, Component.empty());
        }

        return prependSource(postProcessTooltipLines(tooltipLines));
    }

    private List<Pair<Component, Component>> buildRequirements() {
        List<Pair<Component, Component>> requirementPairs = new ArrayList<>();

        MutableComponent levelLineLeft = Component.empty()
                .append(Component.literal(
                                Models.CombatXp.getCombatLevel().current()
                                                >= mountItem.getMountType().getLevel()
                                        ? "\uE006"
                                        : "\uE007")
                        .withStyle(Style.EMPTY
                                .withFont(CommonFonts.TOOLTIP_REQUIREMENT_SPRITE_FONT)
                                .withoutShadow()))
                .append(Component.literal("\uDAFF\uDFFF").withStyle(Style.EMPTY.withFont(CommonFonts.SPACE_FONT)))
                .append(Component.literal(" Combat Level")
                        .withStyle(Style.EMPTY.withFont(CommonFonts.LANGUAGE_WYNNCRAFT_FONT)));
        MutableComponent levelLineRight = Component.empty()
                .append(Component.literal(
                                String.valueOf(mountItem.getMountType().getLevel()))
                        .withStyle(Style.EMPTY
                                .withFont(CommonFonts.LANGUAGE_WYNNCRAFT_FONT)
                                .withColor(ChatFormatting.GRAY)));
        requirementPairs.add(Pair.of(levelLineLeft, levelLineRight));

        // We currently have no way of knowing if the player has completed the quest requirement so just default to
        // incomplete
        MutableComponent questLineLeft = Component.empty()
                .append(Component.literal("\uE007")
                        .withStyle(Style.EMPTY
                                .withFont(CommonFonts.TOOLTIP_REQUIREMENT_SPRITE_FONT)
                                .withoutShadow()))
                .append(Component.literal("\uDAFF\uDFFF").withStyle(Style.EMPTY.withFont(CommonFonts.SPACE_FONT)))
                .append(Component.literal(" Quest")
                        .withStyle(Style.EMPTY.withFont(CommonFonts.LANGUAGE_WYNNCRAFT_FONT)));
        MutableComponent questLineRight = Component.empty()
                .append(Component.literal(mountItem.getMountType().getQuestRequirement())
                        .withStyle(Style.EMPTY
                                .withFont(CommonFonts.LANGUAGE_WYNNCRAFT_FONT)
                                .withColor(ChatFormatting.GRAY)));
        requirementPairs.add(Pair.of(questLineLeft, questLineRight));

        return requirementPairs;
    }

    private List<Pair<Component, Component>> buildStats() {
        List<Pair<Component, Component>> statPairs = new ArrayList<>();

        MutableComponent statsHeader = Component.empty()
                .append(Component.literal("Level/Limit ")
                        .withStyle(Style.EMPTY
                                .withFont(CommonFonts.LANGUAGE_WYNNCRAFT_FONT)
                                .withColor(ChatFormatting.GRAY)))
                .append(Component.literal("(Max)")
                        .withStyle(Style.EMPTY
                                .withFont(CommonFonts.LANGUAGE_WYNNCRAFT_FONT)
                                .withColor(ChatFormatting.DARK_GRAY)));
        statPairs.add(Pair.of(Component.empty(), statsHeader));

        for (Map.Entry<MountStat, CappedValue> statEntry :
                mountItem.getMountInfo().stats().entrySet()) {
            MutableComponent statLineLeft = Component.empty()
                    .append(Component.literal(statEntry.getKey().getName())
                            .withStyle(Style.EMPTY.withFont(CommonFonts.LANGUAGE_WYNNCRAFT_FONT)));
            MutableComponent statLineRight = Component.empty()
                    .append(Component.literal(
                                    String.valueOf(statEntry.getValue().current()))
                            .withStyle(Style.EMPTY
                                    .withFont(CommonFonts.LANGUAGE_WYNNCRAFT_FONT)
                                    .withColor(STAT_VALUE_COLOR.asInt())))
                    .append(Component.literal("/" + statEntry.getValue().max())
                            .withStyle(Style.EMPTY
                                    .withFont(CommonFonts.LANGUAGE_WYNNCRAFT_FONT)
                                    .withColor(ChatFormatting.GRAY)))
                    .append(Component.literal(
                                    " (" + mountItem.getMountInfo().maxStats().get(statEntry.getKey()) + ")")
                            .withStyle(Style.EMPTY
                                    .withFont(CommonFonts.LANGUAGE_WYNNCRAFT_FONT)
                                    .withColor(ChatFormatting.DARK_GRAY)));

            statLineRight.append(" ");

            CappedValue primaryMeter = statEntry.getValue();

            int maxStat = mountItem.getMountInfo().maxStats().getOrDefault(statEntry.getKey(), primaryMeter.max());

            CappedValue secondaryMeter = new CappedValue(primaryMeter.max(), maxStat);

            statLineRight.append(TooltipIdentificationMeterFont.buildCounterDoubleLayerMeter(
                    primaryMeter,
                    secondaryMeter,
                    CustomColor.fromChatFormatting(ChatFormatting.GREEN),
                    CustomColor.fromChatFormatting(ChatFormatting.DARK_GRAY),
                    CustomColor.fromChatFormatting(ChatFormatting.GRAY),
                    ""));

            statPairs.add(Pair.of(statLineLeft, statLineRight));
        }

        return statPairs;
    }
}
