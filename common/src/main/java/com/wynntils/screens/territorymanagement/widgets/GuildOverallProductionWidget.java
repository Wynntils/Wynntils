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
import com.wynntils.utils.mc.KeyboardUtils;
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
import net.minecraft.network.chat.MutableComponent;

public class GuildOverallProductionWidget extends AbstractWidget {
    // The screen re-creates this widget on every init, so the toggle state is kept static to
    // survive a resize or a round trip through the territory container
    private static boolean showDeltas = false;
    private static boolean controlWasDown = false;

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

        // This widget receives no input events of its own, so the control key is polled here
        if (!controlWasDown && KeyboardUtils.isControlDown()) {
            showDeltas = !showDeltas;
            controlWasDown = true;
        } else if (!KeyboardUtils.isControlDown()) {
            controlWasDown = false;
        }

        boolean showDelta = showDeltas || KeyboardUtils.isShiftDown();

        // Show overall cost
        lines.add(Component.literal("Overall Cost (per hour):").withStyle(ChatFormatting.GRAY));
        addOverallCostLine(lines, "", "Emeralds", emeraldUsage, emeraldProduction, ChatFormatting.GREEN, showDelta);
        addOverallCostLine(lines, "Ⓑ ", "Ore", oreUsage, oreProduction, ChatFormatting.WHITE, showDelta);
        addOverallCostLine(lines, "Ⓒ ", "Wood", woodUsage, woodProduction, ChatFormatting.GOLD, showDelta);
        addOverallCostLine(lines, "Ⓚ ", "Fish", fishUsage, fishProduction, ChatFormatting.AQUA, showDelta);
        addOverallCostLine(lines, "Ⓙ ", "Crops", cropsUsage, cropsProduction, ChatFormatting.YELLOW, showDelta);

        RenderUtils.renderTooltip(guiGraphics, lines, this.getX(), this.getY());
    }

    private static void addOverallCostLine(
            List<Component> lines,
            String symbol,
            String name,
            long usage,
            int production,
            ChatFormatting color,
            boolean showDelta) {
        MutableComponent line = Component.literal("%s%.1fk %s (%.1f%%)"
                        .formatted(symbol, usage / 1000d, name, (double) usage / production * 100d))
                .withStyle(color);

        if (showDelta) {
            long delta = production - usage;
            line.append(Component.literal(" [%s%.1fk]".formatted(delta >= 0 ? "+" : "", delta / 1000d))
                    .withStyle(delta >= 0 ? ChatFormatting.BLUE : ChatFormatting.RED));
        }

        lines.add(line);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        return false;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
}
