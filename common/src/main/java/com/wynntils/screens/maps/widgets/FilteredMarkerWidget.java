/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.maps.widgets;

import com.google.common.collect.Lists;
import com.wynntils.core.components.Services;
import com.wynntils.screens.base.widgets.WynntilsButton;
import com.wynntils.services.mapdata.type.MapIcon;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.RenderUtils;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public class FilteredMarkerWidget extends AbstractWidget {
    private final int originalY;

    private final FilterButton mapFilterButton;
    private final FilterButton minimapFilterButton;

    public FilteredMarkerWidget(int x, int y, int width, int height, MapIcon mapIcon, String category) {
        super(x, y, width, height, Component.empty());

        this.originalY = y;

        // Scale the icon to fill 80% of the button
        float mapScaleFactor = 0.8f * Math.min(width, height / 2) / Math.max(mapIcon.getWidth(), mapIcon.getHeight());
        int mapIconWidth = (int) (mapIcon.getWidth() * mapScaleFactor);
        int mapIconHeight = (int) (mapIcon.getHeight() * mapScaleFactor);

        // Calculate x/y position of the icon to keep it centered
        int mapIconRenderX = (int) ((x + width / 2f) - mapIconWidth / 2f);
        int mapIconRenderY = (int) ((y + (height / 2f) / 2f) - mapIconHeight / 2f);

        // Scale the icon to fill 50% of the button
        float minimapScaleFactor =
                0.5f * Math.min(width, height / 2) / Math.max(mapIcon.getWidth(), mapIcon.getHeight());
        int minimapIconWidth = (int) (mapIcon.getWidth() * minimapScaleFactor);
        int minimapIconHeight = (int) (mapIcon.getHeight() * minimapScaleFactor);

        // Calculate x/y position of the icon to keep it centered
        int minimapIconRenderX = (int) ((x + width / 2f) - minimapIconWidth / 2f);
        int minimapIconRenderY = (int) ((height / 2f) + (y + (height / 2f) / 2f) - minimapIconHeight / 2f);

        mapFilterButton =
                new FilterButton(mapIconRenderX, mapIconRenderY, mapIconWidth, mapIconHeight, mapIcon, category, false);
        minimapFilterButton = new FilterButton(
                minimapIconRenderX, minimapIconRenderY, minimapIconWidth, minimapIconHeight, mapIcon, category, true);
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        mapFilterButton.render(guiGraphics, mouseX, mouseY, partialTick);
        minimapFilterButton.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void setY(int y) {
        super.setY(y);

        mapFilterButton.setY((int) ((y + (height / 2f) / 2f) - mapFilterButton.getHeight() / 2f));
        minimapFilterButton.setY(
                (int) ((height / 2f) + (y + (height / 2f) / 2f) - minimapFilterButton.getHeight() / 2f));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isMouseOver(mouseX, mouseY)) return false;

        if (mapFilterButton.isMouseOver(mouseX, mouseY)) {
            return mapFilterButton.mouseClicked(mouseX, mouseY, button);
        }

        if (minimapFilterButton.isMouseOver(mouseX, mouseY)) {
            return minimapFilterButton.mouseClicked(mouseX, mouseY, button);
        }

        return false;
    }

    public int getOriginalY() {
        return originalY;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}

    private static final class FilterButton extends WynntilsButton {
        private final MapIcon mapIcon;
        private final String category;
        private final boolean minimap;
        private final List<Component> tooltip;

        private CustomColor color;

        private FilterButton(int x, int y, int width, int height, MapIcon mapIcon, String category, boolean minimap) {
            super(x, y, width, height, Component.empty());

            this.mapIcon = mapIcon;
            this.category = category;
            this.minimap = minimap;

            tooltip = List.of(
                    minimap
                            ? Component.translatable("screens.wynntils.map.configureMarkers.minimapTooltip")
                            : Component.translatable("screens.wynntils.map.configureMarkers.mapTooltip"));

            updateColor();
        }

        @Override
        public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            RenderUtils.drawScalingTexturedRectWithColor(
                    guiGraphics.pose(),
                    mapIcon.getResourceLocation(),
                    color,
                    getX(),
                    getY(),
                    1,
                    getWidth(),
                    getHeight(),
                    mapIcon.getWidth(),
                    mapIcon.getHeight());

            if (this.isHovered) {
                McUtils.mc()
                        .screen
                        .setTooltipForNextRenderPass(Lists.transform(tooltip, Component::getVisualOrderText));
            }
        }

        @Override
        public void onPress() {
            if (minimap) {
                Services.MapData.filterMinimapCategory(category);
            } else {
                Services.MapData.filterMapCategory(category);
            }

            updateColor();
        }

        private void updateColor() {
            if (minimap) {
                color = Services.MapData.isCategoryFilteredOnMinimap(category) ? CommonColors.WHITE : CommonColors.GRAY;
            } else {
                color = Services.MapData.isCategoryFilteredOnMap(category) ? CommonColors.WHITE : CommonColors.GRAY;
            }
        }
    }
}
