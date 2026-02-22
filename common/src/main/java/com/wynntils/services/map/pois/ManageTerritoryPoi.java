package com.wynntils.services.map.pois;

import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.models.items.items.gui.TerritoryItem;
import com.wynntils.models.territories.TerritoryInfo;
import com.wynntils.models.territories.profile.TerritoryProfile;
import com.wynntils.models.territories.type.GuildResource;
import com.wynntils.screens.maps.GuildMapScreen;
import com.wynntils.screens.territorymanagement.TerritoryManagementHolder;
import com.wynntils.services.map.type.DisplayPriority;
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
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ManageTerritoryPoi implements Poi {
    private final Supplier<TerritoryItem> territoryItemSupplier;
    private final TerritoryManagementHolder holder;
    private final TerritoryInfo territoryInfo;
    private final TerritoryProfile territoryProfile;
    private final ItemStack itemStack;
    private final PoiLocation territoryCenter;
    private final int width;
    private final int height;

    private TerritoryItem territoryItemCache;

    public ManageTerritoryPoi(TerritoryManagementHolder holder, TerritoryInfo territoryInfo, TerritoryProfile territoryProfile, ItemStack itemStack, Supplier<TerritoryItem> territoryItemSupplier) {
        this.holder = holder;
        this.territoryInfo = territoryInfo;
        this.territoryProfile = territoryProfile;
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
    public void renderAt(GuiGraphics guiGraphics, float renderX, float renderY, boolean hovered, float scale, float zoomRenderScale, float zoomLevel, boolean showLabels) {
        final float renderWidth = width * zoomRenderScale;
        final float renderHeight = height * zoomRenderScale;
        final float actualRenderX = renderX - renderWidth / 2f;
        final float actualRenderZ = renderY - renderHeight / 2f;

        TerritoryItem territoryItem = getTerritoryItem();

        List<CustomColor> colors = new ArrayList<>();
        for (Map.Entry<GuildResource, Integer> generator : territoryItem.getProduction().entrySet()) {
            switch (generator.getKey()) { // We do not care about emeralds since they are produced everywhere
                case ORE -> colors.add(CustomColor.fromHSV(0, 0.3f, 1f, 1));
                case FISH -> colors.add(CustomColor.fromHSV(0.5f, 0.6f, 0.9f, 1));
                case WOOD -> colors.add(CustomColor.fromHSV(1 / 3f, 0.6f, 0.9f, 1));
                case CROPS -> colors.add(CustomColor.fromHSV(1 / 6f, 0.6f, 0.9f, 1));
            }
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

        if (territoryItem.isHeadquarters()) {
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

    public TerritoryProfile getTerritoryProfile() {
        return territoryProfile;
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
