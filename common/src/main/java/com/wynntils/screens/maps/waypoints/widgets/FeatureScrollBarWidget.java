/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.maps.waypoints.widgets;

import com.wynntils.core.components.Services;
import com.wynntils.core.text.StyledText;
import com.wynntils.screens.base.TooltipProvider;
import com.wynntils.screens.maps.managers.widgets.ScrollBarWidget;
import com.wynntils.screens.maps.waypoints.WaypointCategoryScreen;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class FeatureScrollBarWidget extends ScrollBarWidget implements TooltipProvider {
    private static final int WIDGET_HEIGHT_PADDING = 4;
    private static final int ENTRY_HEIGHT = 28;

    private final WaypointCategoryScreen parent;
    private List<FeatureEntryWidget> registeredWidgets = new ArrayList<>();

    public FeatureScrollBarWidget(int x, int y, int width, int height, WaypointCategoryScreen parent) {
        super(x, y, width, height);
        this.parent = parent;
    }

    public void populateWidgets(String categoryId) {
        registeredWidgets = new ArrayList<>();

        registeredWidgets = Services.Waypoints.getWaypoints().stream()
                .filter(waypoint -> waypoint.getCategoryId().equals(categoryId))
                .map(waypoint -> new FeatureEntryWidget(0, 0, 0, ENTRY_HEIGHT, waypoint, parent))
                .toList();
        scrollOffsetY = 0;

        updateWidgetPositions();
    }

    @Override
    protected int getWidgetHeightPadding() {
        return WIDGET_HEIGHT_PADDING;
    }

    private int getHeightWithPadding(int index, int widgetHeight, int totalWidgets) {
        return widgetHeight + (index < totalWidgets - 1 ? getWidgetHeightPadding() : 0);
    }

    @Override
    public List<FeatureEntryWidget> getWidgets() {
        return Collections.unmodifiableList(registeredWidgets);
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);

        int headerX = getX() + 10;
        int headerY = getY() - Texture.MANAGER_SCROLL_BAR_HEADER.height() + 1;

        RenderUtils.drawNineSliceScalingTexturedRect(
                guiGraphics,
                Texture.MANAGER_SCROLL_BAR_HEADER,
                headerX,
                headerY,
                170,
                Texture.MANAGER_SCROLL_BAR_HEADER.height());

        FontRenderer.getInstance()
                .renderText(
                        guiGraphics,
                        StyledText.fromComponent(Component.translatable("screens.wynntils.waypointCategory.features")),
                        headerX + 10,
                        headerY + Texture.MANAGER_SCROLL_BAR_HEADER.height() / 2f,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL);

        if (registeredWidgets.isEmpty()) {
            FontRenderer.getInstance()
                    .renderText(
                            guiGraphics,
                            StyledText.fromComponent(
                                    Component.translatable("screens.wynntils.waypointCategory.noFeatures")),
                            getX() + this.width / 2f,
                            getY() + this.height / 2f,
                            CommonColors.WHITE,
                            HorizontalAlignment.CENTER,
                            VerticalAlignment.MIDDLE,
                            TextShadow.NORMAL);
        }
    }

    @Override
    protected void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        RenderUtils.drawNineSliceScalingTexturedRect(
                guiGraphics,
                Texture.MANAGER_WIDGET_BACKGROUND,
                getX(),
                getY() - Texture.MANAGER_SCROLL_BAR_HEADER.height() / 2f,
                this.width,
                this.height + Texture.MANAGER_SCROLL_BAR_HEADER.height() / 2f);
    }

    @Override
    protected void renderWidgets(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        getWidgets().forEach(widget -> widget.render(guiGraphics, mouseX, mouseY, partialTick));
    }

    @Override
    public List<Component> getTooltipLines() {
        for (FeatureEntryWidget widget : getWidgets()) {
            if (widget.isHovered() && widget.visible) {
                return widget.getTooltipLines();
            }
        }

        return List.of();
    }

    public void updateWidgetPositions() {
        int viewportTop = getY() + SCROLL_BAR_HEIGHT_PADDING;
        int viewportBottom = viewportTop + getViewportHeight();
        int viewportLeft = getX() + SCROLL_BAR_WIDTH_PADDING;

        int currentY = getY() + SCROLL_BAR_HEIGHT_PADDING - scrollOffsetY;

        List<FeatureEntryWidget> widgets = getWidgets();
        for (int i = 0; i < widgets.size(); i++) {
            FeatureEntryWidget widget = widgets.get(i);
            int widgetHeight = widget.getHeight();
            if (currentY + widgetHeight >= viewportTop && currentY <= viewportBottom) {
                widget.visible = true;
                widget.setX(viewportLeft);
                widget.setY(currentY);
                widget.setWidth(getViewportWidth());
            } else {
                widget.visible = false;
            }
            currentY += getHeightWithPadding(i, widgetHeight, widgets.size());
        }
    }

    @Override
    protected void onScrollOffsetChanged() {
        updateWidgetPositions();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY) {
        if (!isInsideViewport(mouseX, mouseY)) {
            return false;
        }

        return super.mouseScrolled(mouseX, mouseY, deltaX, deltaY);
    }
}
