/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.map.pois;

import com.wynntils.core.text.StyledText;
import com.wynntils.models.items.items.gui.TerritoryItem;
import com.wynntils.models.territories.TerritoryInfo;
import com.wynntils.models.territories.profile.TerritoryProfile;
import com.wynntils.models.territories.type.GuildResource;
import com.wynntils.models.territories.type.TerritoryUpgrade;
import com.wynntils.screens.territorymanagement.TerritoryManagementHolder;
import com.wynntils.screens.territorymanagement.TerritoryManagementScreen;
import com.wynntils.services.map.type.DisplayPriority;
import com.wynntils.services.map.type.TerritoryInfoType;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.mc.type.PoiLocation;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

public class ManageTerritoryPoi implements Poi {
    private final Supplier<TerritoryItem> territoryItemSupplier;
    private final TerritoryManagementHolder holder;
    private final TerritoryInfo territoryInfo;
    private final ItemStack itemStack;
    private final PoiLocation territoryCenter;
    private final int width;
    private final int height;

    private TerritoryItem territoryItemCache;

    public ManageTerritoryPoi(
            TerritoryManagementHolder holder,
            TerritoryInfo territoryInfo,
            TerritoryProfile territoryProfile,
            ItemStack itemStack,
            Supplier<TerritoryItem> territoryItemSupplier) {
        this.holder = holder;
        this.territoryInfo = territoryInfo;
        this.itemStack = itemStack;
        this.territoryItemSupplier = territoryItemSupplier;
        this.width = territoryProfile.getEndX() - territoryProfile.getStartX();
        this.height = territoryProfile.getEndZ() - territoryProfile.getStartZ();
        this.territoryCenter = new PoiLocation(
                territoryProfile.getStartX() + width / 2, null, territoryProfile.getStartZ() + height / 2);

        getTerritoryItem();
    }

    @Override
    public PoiLocation getLocation() {
        return territoryCenter;
    }

    @Override
    public DisplayPriority getDisplayPriority() {
        return DisplayPriority.HIGHEST;
    }

    @Override
    public boolean hasStaticLocation() {
        return true;
    }

    @Override
    public void renderAt(
            GuiGraphics guiGraphics,
            float renderX,
            float renderY,
            boolean hovered,
            float scale,
            float zoomRenderScale,
            float zoomLevel,
            boolean showLabels) {
        final float renderWidth = width * zoomRenderScale;
        final float renderHeight = height * zoomRenderScale;
        final float actualRenderX = renderX - renderWidth / 2f;
        final float actualRenderZ = renderY - renderHeight / 2f;

        TerritoryItem territoryItem = getTerritoryItem();

        List<CustomColor> colors = new ArrayList<>();
        if (McUtils.screen() instanceof TerritoryManagementScreen territoryManagementScreen) {
            TerritoryInfoType infoType = territoryManagementScreen.getInfoType();
            Map<TerritoryUpgrade, Integer> upgrades = territoryItem.getUpgrades();
            switch (infoType) {
                case RESOURCE:
                    for (Map.Entry<GuildResource, Integer> generator :
                            territoryItem.getProduction().entrySet()) {
                        switch (generator
                                .getKey()) { // We do not care about emeralds since they are produced everywhere
                            case ORE -> colors.add(CustomColor.fromHSV(0, 0.3f, 1f, 1));
                            case FISH -> colors.add(CustomColor.fromHSV(0.5f, 0.6f, 0.9f, 1));
                            case WOOD -> colors.add(CustomColor.fromHSV(1 / 3f, 0.6f, 0.9f, 1));
                            case CROPS -> colors.add(CustomColor.fromHSV(1 / 6f, 0.6f, 0.9f, 1));
                        }
                    }
                    break;
                case DEFENSE:
                    switch (territoryItem.getDefenseDifficulty()) {
                        case VERY_LOW -> colors.add(CustomColor.fromChatFormatting(ChatFormatting.DARK_GREEN));
                        case LOW -> colors.add(CustomColor.fromChatFormatting(ChatFormatting.GREEN));
                        case MEDIUM -> colors.add(CustomColor.fromChatFormatting(ChatFormatting.YELLOW));
                        case HIGH -> colors.add(CustomColor.fromChatFormatting(ChatFormatting.RED));
                        case VERY_HIGH -> colors.add(CustomColor.fromChatFormatting(ChatFormatting.DARK_RED));
                        default -> colors.add(CommonColors.WHITE);
                    }
                    break;
                case PRODUCTION:
                    int emeraldUpgrades = upgrades.getOrDefault(TerritoryUpgrade.EMERALD_RATE, 0)
                            + upgrades.getOrDefault(TerritoryUpgrade.EFFICIENT_EMERALDS, 0);
                    int resourceUpgrades = upgrades.getOrDefault(TerritoryUpgrade.RESOURCE_RATE, 0)
                            + upgrades.getOrDefault(TerritoryUpgrade.EFFICIENT_RESOURCES, 0);
                    if (emeraldUpgrades > 0) {
                        if (resourceUpgrades > 0) {
                            colors.add(CustomColor.fromHSV(0.5f, 0.8f, 0.9f, 1));
                        } else {
                            colors.add(CustomColor.fromHSV(1 / 3f, 0.8f, 0.9f, 1));
                        }
                        break;
                    }
                    // 4 3 or above -> 100% saturation
                    // 3 3 or below -> 40% saturation
                    if (resourceUpgrades > 6) {
                        colors.add(CustomColor.fromHSV(1 / 6f, 1.0f, 1.0f, 1));
                    } else if (resourceUpgrades > 0) {
                        colors.add(CustomColor.fromHSV(1 / 6f, 0.45f, 0.9f, 1));
                    } else {
                        colors.add(CustomColor.fromHSV(0, 0, 0.6f, 1));
                    }
                    break;
                case SEEKING:
                    int tomeSeek = upgrades.getOrDefault(TerritoryUpgrade.TOME_SEEKING, 0);
                    int emeraldSeek = upgrades.getOrDefault(TerritoryUpgrade.EMERALD_SEEKING, 0);
                    if (tomeSeek > 0 && emeraldSeek > 0) {
                        colors.add(CustomColor.fromHSV(1 / 2f, 0.8f, 0.9f, 1));
                    } else if (tomeSeek > 0) {
                        colors.add(CustomColor.fromHSV(2 / 3f, 0.8f, 0.9f, 1));
                    } else if (emeraldSeek > 0) {
                        colors.add(CustomColor.fromHSV(1 / 3f, 0.8f, 0.9f, 1));
                    } else {
                        colors.add(CustomColor.fromHSV(0, 0, 0.6f, 1));
                    }
                    break;
                case TREASURY:
                    float treasuryBonus = territoryItem.getTreasuryBonus();
                    colors.add(CustomColor.fromHSV(5 / 6f, (treasuryBonus / 100) * (1f / 0.3f), 0.9f, 1));
                    break;
                default:
                    colors.add(CommonColors.WHITE);
            }
        } else {
            colors.add(CommonColors.WHITE);
        }

        RenderUtils.drawMulticoloredRect(
                guiGraphics,
                colors.stream().map(x -> x.withAlpha(80)).toList(),
                actualRenderX,
                actualRenderZ,
                renderWidth,
                renderHeight);
        RenderUtils.drawMulticoloredRectBorders(
                guiGraphics, colors, actualRenderX, actualRenderZ, renderWidth, renderHeight, 1.5f, 0.5f);

        if (territoryItem.isSelected()) {
            RenderUtils.drawTexturedRect(
                    guiGraphics,
                    Texture.CHECKMARK_GREEN,
                    actualRenderX + renderWidth / 2f - Texture.CHECKMARK_GREEN.width() / 2f,
                    actualRenderZ + renderHeight / 2f - Texture.CHECKMARK_GREEN.height() / 2f);
        } else if (territoryItem.isHeadquarters()) {
            RenderUtils.drawTexturedRect(
                    guiGraphics,
                    Texture.GUILD_HEADQUARTERS,
                    actualRenderX + renderWidth / 2f - Texture.GUILD_HEADQUARTERS.width() / 2f,
                    actualRenderZ + renderHeight / 2f - Texture.GUILD_HEADQUARTERS.height() / 2f);
        } else {
            String shortName = Arrays.stream(territoryItem.getName().split(" "))
                    .map(s -> s.substring(0, 1))
                    .collect(Collectors.joining());
            FontRenderer.getInstance()
                    .renderAlignedTextInBox(
                            guiGraphics,
                            StyledText.fromString(shortName),
                            actualRenderX,
                            actualRenderX + renderWidth,
                            actualRenderZ,
                            actualRenderZ + renderHeight,
                            0,
                            colors.getFirst(),
                            HorizontalAlignment.CENTER,
                            VerticalAlignment.MIDDLE,
                            TextShadow.OUTLINE);
        }

        if (hovered) {
            FontRenderer.getInstance()
                    .renderAlignedTextInBox(
                            guiGraphics,
                            StyledText.fromString(territoryItem.getName()),
                            actualRenderX,
                            actualRenderX + renderWidth,
                            actualRenderZ,
                            actualRenderZ + renderHeight,
                            0,
                            CommonColors.WHITE,
                            HorizontalAlignment.CENTER,
                            VerticalAlignment.TOP,
                            TextShadow.OUTLINE);
        }
    }

    @Override
    public int getWidth(float mapZoom, float scale) {
        return (int) (width * mapZoom);
    }

    @Override
    public int getHeight(float mapZoom, float scale) {
        return (int) (height * mapZoom);
    }

    @Override
    public String getName() {
        return territoryItemCache.getName();
    }

    public TerritoryInfo getTerritoryInfo() {
        return territoryInfo;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public void onClick() {
        holder.territoryItemClicked(getTerritoryItem());
    }

    public TerritoryItem getTerritoryItem() {
        return tryGetUpdatedTerritoryItem();
    }

    private TerritoryItem tryGetUpdatedTerritoryItem() {
        TerritoryItem territoryItem = territoryItemSupplier.get();
        if (territoryItem != null) {
            territoryItemCache = territoryItem;
        }
        return territoryItemCache;
    }
}
