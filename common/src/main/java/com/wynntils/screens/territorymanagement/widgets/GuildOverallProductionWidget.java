/*
 * Copyright © Wynntils 2024-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.territorymanagement.widgets;

import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Models;
import com.wynntils.features.ui.CustomTerritoryManagementScreenFeature;
import com.wynntils.models.territories.type.GuildResource;
import com.wynntils.screens.territorymanagement.TerritoryManagementHolder;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.type.CappedValue;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class GuildOverallProductionWidget extends AbstractWidget {
    private final TerritoryManagementHolder holder;

    public GuildOverallProductionWidget(int x, int y, int width, int height, TerritoryManagementHolder holder) {
        super(x, y, width, height, Component.literal("Guild Overall Production"));
        this.holder = holder;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (!Managers.Feature.getFeatureInstance(CustomTerritoryManagementScreenFeature.class)
                .screenTerritoryProductionTooltip
                .get()) {
            return;
        }

        int emeraldProduction = holder.getOverallProductionForResource(GuildResource.EMERALDS)
                + Models.Guild.getReceivedTributesForResource(GuildResource.EMERALDS);
        int oreProduction = holder.getOverallProductionForResource(GuildResource.ORE)
                + Models.Guild.getReceivedTributesForResource(GuildResource.ORE);
        int woodProduction = holder.getOverallProductionForResource(GuildResource.WOOD)
                + Models.Guild.getReceivedTributesForResource(GuildResource.WOOD);
        int fishProduction = holder.getOverallProductionForResource(GuildResource.FISH)
                + Models.Guild.getReceivedTributesForResource(GuildResource.FISH);
        int cropsProduction = holder.getOverallProductionForResource(GuildResource.CROPS)
                + Models.Guild.getReceivedTributesForResource(GuildResource.CROPS);

        CappedValue emeraldStorage = holder.getOverallStorageForResource(GuildResource.EMERALDS);
        CappedValue oreStorage = holder.getOverallStorageForResource(GuildResource.ORE);
        CappedValue woodStorage = holder.getOverallStorageForResource(GuildResource.WOOD);
        CappedValue fishStorage = holder.getOverallStorageForResource(GuildResource.FISH);
        CappedValue cropsStorage = holder.getOverallStorageForResource(GuildResource.CROPS);

        long emeraldUsage = holder.getOverallUsageForResource(GuildResource.EMERALDS)
                + Models.Guild.getSentTributesForResource(GuildResource.EMERALDS);
        long oreUsage = holder.getOverallUsageForResource(GuildResource.ORE)
                + Models.Guild.getSentTributesForResource(GuildResource.ORE);
        long woodUsage = holder.getOverallUsageForResource(GuildResource.WOOD)
                + Models.Guild.getSentTributesForResource(GuildResource.WOOD);
        long fishUsage = holder.getOverallUsageForResource(GuildResource.FISH)
                + Models.Guild.getSentTributesForResource(GuildResource.FISH);
        long cropsUsage = holder.getOverallUsageForResource(GuildResource.CROPS)
                + Models.Guild.getSentTributesForResource(GuildResource.CROPS);

        List<Component> lines = new ArrayList<>();
        lines.add(Component.literal("Guild Output").withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD));
        lines.add(Component.literal(""));
        lines.add(Component.literal("Total resource output").withStyle(ChatFormatting.GRAY));
        lines.add(Component.literal(""));

        lines.add(Component.literal("+%d Emeralds per Hour".formatted(emeraldProduction))
                .withStyle(ChatFormatting.GREEN));
        lines.add(Component.literal("%s in storage".formatted(emeraldStorage)).withStyle(ChatFormatting.GREEN));
        lines.add(
                Component.literal("Ⓑ +%d Ore per Hour".formatted(oreProduction)).withStyle(ChatFormatting.WHITE));
        lines.add(Component.literal("Ⓑ %s in storage".formatted(oreStorage)).withStyle(ChatFormatting.WHITE));
        lines.add(Component.literal("Ⓒ +%d Wood per Hour".formatted(woodProduction))
                .withStyle(ChatFormatting.GOLD));
        lines.add(Component.literal("Ⓒ %s in storage".formatted(woodStorage)).withStyle(ChatFormatting.GOLD));
        lines.add(Component.literal("Ⓚ +%d Fish per Hour".formatted(fishProduction))
                .withStyle(ChatFormatting.AQUA));
        lines.add(Component.literal("Ⓚ %s in storage".formatted(fishStorage)).withStyle(ChatFormatting.AQUA));
        lines.add(Component.literal("Ⓙ +%d Crops per Hour".formatted(cropsProduction))
                .withStyle(ChatFormatting.YELLOW));
        lines.add(Component.literal("Ⓙ %s in storage".formatted(cropsStorage)).withStyle(ChatFormatting.YELLOW));

        lines.add(Component.literal(""));

        long emeraldDelta = emeraldProduction - emeraldUsage;
        long oreDelta = oreProduction - oreUsage;
        long woodDelta = woodProduction - woodUsage;
        long fishDelta = fishProduction - fishUsage;
        long cropDelta = cropsProduction - cropsUsage;
        // Show overall cost
        lines.add(Component.literal("Overall Cost (per hour):").withStyle(ChatFormatting.GRAY));
        lines.add(Component.literal("%.1fk Emeralds (%.1f%%)"
                        .formatted(emeraldUsage / 1000d, (double) emeraldUsage / emeraldProduction * 100d))
                .withStyle(ChatFormatting.GREEN)
                .append(Component.literal(" [%s%.1fk]".formatted(emeraldDelta >= 0 ? "+" : "", emeraldDelta / 1000d))
                        .withStyle(emeraldDelta >= 0 ? ChatFormatting.BLUE : ChatFormatting.RED)));
        lines.add(Component.literal(
                        "Ⓑ %.1fk Ore (%.1f%%)".formatted(oreUsage / 1000d, (double) oreUsage / oreProduction * 100d))
                .withStyle(ChatFormatting.WHITE)
                .append(Component.literal(" [%s%.1fk]".formatted(oreDelta >= 0 ? "+" : "", oreDelta / 1000d))
                        .withStyle(oreDelta >= 0 ? ChatFormatting.BLUE : ChatFormatting.RED)));
        lines.add(Component.literal("Ⓒ %.1fk Wood (%.1f%%)"
                        .formatted(woodUsage / 1000d, (double) woodUsage / woodProduction * 100d))
                .withStyle(ChatFormatting.GOLD)
                .append(Component.literal(" [%s%.1fk]".formatted(woodDelta >= 0 ? "+" : "", woodDelta / 1000d))
                        .withStyle(woodDelta >= 0 ? ChatFormatting.BLUE : ChatFormatting.RED)));
        lines.add(Component.literal("Ⓚ %.1fk Fish (%.1f%%)"
                        .formatted(fishUsage / 1000d, (double) fishUsage / fishProduction * 100d))
                .withStyle(ChatFormatting.AQUA)
                .append(Component.literal(" [%s%.1fk]".formatted(fishDelta >= 0 ? "+" : "", fishDelta / 1000d))
                        .withStyle(fishDelta >= 0 ? ChatFormatting.BLUE : ChatFormatting.RED)));
        lines.add(Component.literal("Ⓙ %.1fk Crops (%.1f%%)"
                        .formatted(cropsUsage / 1000d, (double) cropsUsage / cropsProduction * 100d))
                .withStyle(ChatFormatting.YELLOW)
                .append(Component.literal(" [%s%.1fk]".formatted(cropDelta >= 0 ? "+" : "", cropDelta / 1000d))
                        .withStyle(cropDelta >= 0 ? ChatFormatting.BLUE : ChatFormatting.RED)));

        RenderUtils.renderTooltip(guiGraphics, lines, this.getX(), this.getY());
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        return false;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
}
