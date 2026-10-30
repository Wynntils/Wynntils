/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.tooltip.builders;

import com.wynntils.core.components.Handlers;
import com.wynntils.handlers.tooltip.TooltipBuilder;
import com.wynntils.handlers.tooltip.type.ItemSourceType;
import com.wynntils.handlers.tooltip.type.TooltipElement;
import com.wynntils.handlers.tooltip.type.TooltipLine;
import com.wynntils.models.activities.type.Dungeon;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public class DungeonKeyTooltipBuilder extends TooltipBuilder {
    private final Dungeon dungeon;
    private final boolean corrupted;
    private final boolean broken;
    private final ChatFormatting highlightColor;

    private DungeonKeyTooltipBuilder(
            Dungeon dungeon, boolean corrupted, boolean broken, ItemSourceType sourceType, String source) {
        super(sourceType, source);

        this.dungeon = dungeon;
        this.corrupted = corrupted;
        this.broken = broken;
        this.highlightColor = calculateHighlightColor(corrupted, broken);
    }

    public static DungeonKeyTooltipBuilder buildNewItem(
            Dungeon dungeon, boolean corrupted, boolean broken, ItemSourceType sourceType, String source) {
        return new DungeonKeyTooltipBuilder(dungeon, corrupted, broken, sourceType, source);
    }

    @Override
    protected void buildTooltip() {
        List<TooltipElement> lines = new ArrayList<>();

        Component name = Component.empty()
                .withStyle(highlightColor)
                .append(Component.literal(getName(dungeon, corrupted, broken)));
        lines.add(TooltipLine.left(name));

        if (broken) {
            lines.addAll(getBrokenKeyTooltip());
        } else {
            lines.addAll(getUnbrokenKeyTooltip());
        }

        lines.addAll(getDungeonInfoTooltipSection());
        if (sourceType == ItemSourceType.GUIDE) {
            lines.add(TooltipLine.empty());
            lines.addAll(getObtainTooltipSection());
        }

        this.tooltipLines = Handlers.Tooltip.alignTooltip(lines);
    }

    private List<TooltipElement> getUnbrokenKeyTooltip() {
        List<TooltipElement> lines = new ArrayList<>();

        lines.add(TooltipLine.left(Component.translatable("screens.wynntils.wynntilsGuides.misc.keys.usage1")
                .withStyle(ChatFormatting.GRAY)));
        lines.add(TooltipLine.left(
                Component.translatable("screens.wynntils.wynntilsGuides.misc.keys.usage2", dungeon.getName())
                        .withStyle(ChatFormatting.WHITE)
                        .append(Component.translatable("screens.wynntils.wynntilsGuides.misc.keys.usage3")
                                .withStyle(ChatFormatting.GRAY))));

        return lines;
    }

    private List<TooltipElement> getBrokenKeyTooltip() {
        List<TooltipElement> lines = new ArrayList<>();

        lines.add(TooltipLine.left(Component.translatable("screens.wynntils.wynntilsGuides.misc.keys.broken.usage1")
                .withStyle(ChatFormatting.GRAY)));
        lines.add(TooltipLine.left(Component.translatable("screens.wynntils.wynntilsGuides.misc.keys.broken.usage2")
                .withStyle(ChatFormatting.WHITE)
                .append(Component.translatable("screens.wynntils.wynntilsGuides.misc.keys.broken.usage3")
                        .withStyle(ChatFormatting.GRAY))));
        lines.add(TooltipLine.left(
                Component.literal(getName(dungeon, true, false)).withStyle(highlightColor)));

        return lines;
    }

    private List<TooltipElement> getDungeonInfoTooltipSection() {
        List<TooltipElement> lines = new ArrayList<>();

        lines.add(TooltipLine.empty());
        lines.add(TooltipLine.left(Component.translatable("screens.wynntils.wynntilsGuides.misc.keys.usage4")
                .withStyle(highlightColor)));

        Dungeon.DungeonData dungeonData =
                (corrupted ? dungeon.getCorruptedDungeonData() : dungeon.getDungeonData()).orElseThrow();
        lines.add(TooltipLine.left(Component.literal("- ")
                .withStyle(highlightColor)
                .append(Component.translatable("screens.wynntils.wynntilsGuides.misc.keys.usage5")
                        .withStyle(ChatFormatting.GRAY))
                .append(Component.literal(dungeonData.getCombatLevel() + "").withStyle(ChatFormatting.WHITE))));

        lines.add(TooltipLine.left(Component.literal("- ")
                .withStyle(highlightColor)
                .append(Component.translatable("screens.wynntils.wynntilsGuides.misc.keys.usage6")
                        .withStyle(ChatFormatting.GRAY))
                .append(Component.literal(dungeonData.getXPos() + " " + dungeonData.getZPos())
                        .withStyle(ChatFormatting.WHITE))));

        return lines;
    }

    private List<TooltipElement> getObtainTooltipSection() {
        List<TooltipElement> itemLore = new ArrayList<>();

        itemLore.add(TooltipLine.left(Component.translatable("screens.wynntils.wynntilsGuides.misc.keys.obtain1")));

        if (broken) {
            itemLore.add(
                    TooltipLine.left(Component.translatable("screens.wynntils.wynntilsGuides.misc.keys.broken.obtain2")
                            .withStyle(ChatFormatting.AQUA)));
        } else {
            itemLore.add(TooltipLine.left(Component.translatable("screens.wynntils.wynntilsGuides.misc.keys.obtain2")
                    .withStyle(ChatFormatting.AQUA)));
        }

        return itemLore;
    }

    private static String getName(Dungeon dungeon, boolean corrupted, boolean broken) {
        StringBuilder name = new StringBuilder();

        if (broken) {
            name.append("Broken ");
        }

        if (corrupted) {
            name.append("Corrupted ");
        }

        return name.append(dungeon.getName()).append(" Key").toString();
    }

    private static ChatFormatting calculateHighlightColor(boolean corrupted, boolean broken) {
        ChatFormatting highlightColor = ChatFormatting.GOLD;

        if (corrupted || broken) {
            highlightColor = ChatFormatting.DARK_RED;
        }

        return highlightColor;
    }
}
