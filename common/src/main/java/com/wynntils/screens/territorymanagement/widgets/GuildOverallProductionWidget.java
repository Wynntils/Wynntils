/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.territorymanagement.widgets;

import com.wynntils.core.components.Managers;
import com.wynntils.features.ui.CustomTerritoryManagementScreenFeature;
import com.wynntils.models.territories.type.GuildResource;
import com.wynntils.screens.territorymanagement.TerritoryManagementHolder;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.type.CappedValue;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
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

        // §f§lGuild Output
        //
        // §7Total resource output
        //
        // §a+1701462 Emeralds per Hour
        // §a424177/532000 in storage
        // §fⒷ +309712 Ore per Hour
        // §fⒷ 123020/153300 in storage
        // §6Ⓒ +368392 Wood per Hour
        // §6Ⓒ 121088/153300 in storage
        // §bⓀ +333606 Fish per Hour
        // §bⓀ 123370/153300 in storage
        // §eⒿ +298642 Crops per Hour
        // §eⒿ 122500/153300 in storage
        //
        // §aOverall Cost (per hour):
        // §a- §71262000 Emeralds§8 (98%)
        // §a- §7Ⓑ 261600 Ore§8 (99%)
        // §a- §7Ⓒ 301000 Wood§8 (99%)
        // §a- §7Ⓚ 153000 Fish§8 (97%)
        // §a- §7Ⓙ 273000 Crops§8 (99%)

        int emeraldProduction = holder.getOverallProductionForResource(GuildResource.EMERALDS);
        int oreProduction = holder.getOverallProductionForResource(GuildResource.ORE);
        int woodProduction = holder.getOverallProductionForResource(GuildResource.WOOD);
        int fishProduction = holder.getOverallProductionForResource(GuildResource.FISH);
        int cropsProduction = holder.getOverallProductionForResource(GuildResource.CROPS);

        CappedValue emeraldStorage = holder.getOverallStorageForResource(GuildResource.EMERALDS);
        CappedValue oreStorage = holder.getOverallStorageForResource(GuildResource.ORE);
        CappedValue woodStorage = holder.getOverallStorageForResource(GuildResource.WOOD);
        CappedValue fishStorage = holder.getOverallStorageForResource(GuildResource.FISH);
        CappedValue cropsStorage = holder.getOverallStorageForResource(GuildResource.CROPS);

        long emeraldUsage = holder.getOverallUsageForResource(GuildResource.EMERALDS);
        long oreUsage = holder.getOverallUsageForResource(GuildResource.ORE);
        long woodUsage = holder.getOverallUsageForResource(GuildResource.WOOD);
        long fishUsage = holder.getOverallUsageForResource(GuildResource.FISH);
        long cropsUsage = holder.getOverallUsageForResource(GuildResource.CROPS);

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

        lines.add(Component.literal("Overall Cost (per hour):").withStyle(ChatFormatting.GRAY));
        lines.add(Component.literal("- %d Emeralds (%.1f%%)"
                        .formatted(emeraldUsage, (double) emeraldUsage / emeraldProduction * 100d))
                .withStyle(ChatFormatting.GREEN));
        lines.add(Component.literal("- Ⓑ %d Ore (%.1f%%)".formatted(oreUsage, (double) oreUsage / oreProduction * 100d))
                .withStyle(ChatFormatting.WHITE));
        lines.add(Component.literal(
                        "- Ⓒ %d Wood (%.1f%%)".formatted(woodUsage, (double) woodUsage / woodProduction * 100d))
                .withStyle(ChatFormatting.GOLD));
        lines.add(Component.literal(
                        "- Ⓚ %d Fish (%.1f%%)".formatted(fishUsage, (double) fishUsage / fishProduction * 100d))
                .withStyle(ChatFormatting.AQUA));
        lines.add(Component.literal(
                        "- Ⓙ %d Crops (%.1f%%)".formatted(cropsUsage, (double) cropsUsage / cropsProduction * 100d))
                .withStyle(ChatFormatting.YELLOW));

        guiGraphics.renderComponentTooltip(FontRenderer.getInstance().getFont(), lines, this.getX(), this.getY());
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return false;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
}
