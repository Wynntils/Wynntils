/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.tooltip.builders;

import com.wynntils.core.components.Handlers;
import com.wynntils.core.text.CommonStyles;
import com.wynntils.core.text.fonts.CommonFonts;
import com.wynntils.core.text.fonts.wynnfonts.BannerBoxFont;
import com.wynntils.core.text.fonts.wynnfonts.TooltipIdentificationMeterFont;
import com.wynntils.handlers.tooltip.TooltipBuilder;
import com.wynntils.handlers.tooltip.components.HeaderStatsLineBuilder;
import com.wynntils.handlers.tooltip.components.RequirementsLineBuilder;
import com.wynntils.handlers.tooltip.type.ItemEmblemSprite;
import com.wynntils.handlers.tooltip.type.ItemSourceType;
import com.wynntils.handlers.tooltip.type.TooltipElement;
import com.wynntils.handlers.tooltip.type.TooltipLine;
import com.wynntils.models.items.items.game.MountItem;
import com.wynntils.models.mount.type.MountStat;
import com.wynntils.models.wynnitem.type.ItemEmblem;
import com.wynntils.utils.StringUtils;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.type.CappedValue;
import com.wynntils.utils.type.OptionalBoolean;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

public class MountTooltipBuilder extends TooltipBuilder {
    private static final ItemEmblem ITEM_EMBLEM = new ItemEmblem(ItemEmblem.ItemEmblemShape.BADGE, 4);

    private final MountItem item;

    private MountTooltipBuilder(MountItem item, ItemSourceType sourceType, String source) {
        super(sourceType, source);

        this.item = item;
    }

    public static MountTooltipBuilder buildNewItem(MountItem mountItem, ItemSourceType sourceType, String source) {
        return new MountTooltipBuilder(mountItem, sourceType, source);
    }

    private List<TooltipElement> buildNewHeader(String source) {
        List<TooltipElement> lines = new ArrayList<>();

        if (!source.isEmpty()) {
            lines.add(buildSourceLine());
        }

        lines.add(TooltipLine.empty());

        lines.addAll(buildHeader(
                ITEM_EMBLEM,
                ItemEmblemSprite.MOUNT,
                Component.literal(StringUtils.toPossessive(item.getName()) + " "
                                + item.getMountType().getMountItemName())
                        .withStyle(CommonStyles.WHITE_LANGUAGE_WYNNCRAFT),
                BannerBoxFont.buildMessage("mount", CommonColors.WHITE, CommonColors.BLACK, ""),
                BannerBoxFont.buildMessage(
                        item.getMountType().name(), item.getGearTier().getSecondaryColor(), CommonColors.BLACK, "")));

        lines.add(TooltipLine.empty());

        lines.add(HeaderStatsLineBuilder.buildhighlightStatLine(
                StringUtils.integerToShortString(item.getMountInfo().potential()), "Potential", item.getGearTier()));

        MutableComponent colorLine = Component.empty()
                .append(Component.literal("\uE00E")
                        .withoutShadow()
                        .withStyle(Style.EMPTY.withFont(CommonFonts.TOOLTIP_ATTRIBUTE_SPRITE_FONT)))
                .append(Component.literal("\uDB00\uDC01").withStyle(CommonStyles.SPACE))
                .append(Component.literal(" "
                                + item.getMountInfo().primaryColorInfo().displayName() + "-"
                                + item.getMountInfo().secondaryColorInfo().displayName())
                        .withStyle(Style.EMPTY
                                .withFont(CommonFonts.WYNNTILS_LANGUAGE_WYNNCRAFT_FONT)
                                .withColor(ChatFormatting.GRAY)));
        lines.add(TooltipLine.left(colorLine));

        MutableComponent energyLine = Component.empty()
                .append(TooltipIdentificationMeterFont.buildCounterSingleLayerMeter(
                        item.getMountInfo().currentEnergy(),
                        item.getGearTier().getSecondaryColor(),
                        CustomColor.fromChatFormatting(ChatFormatting.DARK_GRAY),
                        "\uDB00\uDC05"))
                .append(Component.literal("Energy " + item.getMountInfo().currentEnergy())
                        .withStyle(Style.EMPTY
                                .withFont(CommonFonts.WYNNTILS_LANGUAGE_WYNNCRAFT_FONT)
                                .withColor(ChatFormatting.GRAY)));
        lines.add(TooltipLine.left(energyLine));

        return lines;
    }

    private List<TooltipElement> buildRequirements() {
        List<TooltipElement> lines = new ArrayList<>();

        lines.add(divider(item.getGearTier()));

        lines.add(RequirementsLineBuilder.buildLevelRequirementLine(
                item.getMountType().getLevel()));

        // We currently have no way of knowing if the player has completed the quest requirement so just default to
        // unknown
        lines.add(RequirementsLineBuilder.buildQuestRequirementLine(
                item.getMountType().getQuestRequirement(), false, OptionalBoolean.NULL));

        return lines;
    }

    private List<TooltipElement> buildStats() {
        List<TooltipElement> lines = new ArrayList<>();

        lines.add(divider(item.getGearTier()));

        MutableComponent statsHeader = Component.empty()
                .append(Component.literal("Level/Limit ")
                        .withStyle(Style.EMPTY
                                .withFont(CommonFonts.WYNNTILS_LANGUAGE_WYNNCRAFT_FONT)
                                .withColor(ChatFormatting.GRAY)))
                .append(Component.literal("(Max)")
                        .withStyle(Style.EMPTY
                                .withFont(CommonFonts.WYNNTILS_LANGUAGE_WYNNCRAFT_FONT)
                                .withColor(ChatFormatting.DARK_GRAY)));
        lines.add(TooltipLine.right(statsHeader));

        for (Map.Entry<MountStat, CappedValue> statEntry :
                item.getMountInfo().stats().entrySet()) {
            MutableComponent statName = Component.empty()
                    .append(Component.literal(statEntry.getKey().getName())
                            .withStyle(Style.EMPTY.withFont(CommonFonts.WYNNTILS_LANGUAGE_WYNNCRAFT_FONT)));

            MutableComponent statValue = Component.empty()
                    .append(Component.literal(
                                    String.valueOf(statEntry.getValue().current()))
                            .withStyle(Style.EMPTY
                                    .withFont(CommonFonts.WYNNTILS_LANGUAGE_WYNNCRAFT_FONT)
                                    .withColor(CommonColors.WYNNCRAFT_GREEN.asInt())))
                    .append(Component.literal("/" + statEntry.getValue().max())
                            .withStyle(Style.EMPTY
                                    .withFont(CommonFonts.WYNNTILS_LANGUAGE_WYNNCRAFT_FONT)
                                    .withColor(ChatFormatting.GRAY)))
                    .append(Component.literal(
                                    " (" + item.getMountInfo().maxStats().get(statEntry.getKey()) + ")")
                            .withStyle(Style.EMPTY
                                    .withFont(CommonFonts.WYNNTILS_LANGUAGE_WYNNCRAFT_FONT)
                                    .withColor(ChatFormatting.DARK_GRAY)));

            statValue.append(" ");

            CappedValue primaryMeter = statEntry.getValue();

            int maxStat = item.getMountInfo().maxStats().getOrDefault(statEntry.getKey(), primaryMeter.max());

            CappedValue secondaryMeter = new CappedValue(primaryMeter.max(), maxStat);

            statValue.append(TooltipIdentificationMeterFont.buildCounterDoubleLayerMeter(
                    primaryMeter,
                    secondaryMeter,
                    CustomColor.fromChatFormatting(ChatFormatting.GREEN),
                    CustomColor.fromChatFormatting(ChatFormatting.DARK_GRAY),
                    CustomColor.fromChatFormatting(ChatFormatting.GRAY),
                    ""));

            lines.add(TooltipLine.leftRight(statName, statValue));
        }

        return lines;
    }

    @Override
    protected void buildTooltip() {
        List<TooltipElement> lines = new ArrayList<>();

        List<TooltipElement> header = buildNewHeader(source);
        List<TooltipElement> requirements = buildRequirements();
        List<TooltipElement> stats = buildStats();

        lines.addAll(header);
        lines.addAll(requirements);
        lines.addAll(stats);

        this.tooltipLines = Handlers.Tooltip.alignTooltip(lines);
    }
}
