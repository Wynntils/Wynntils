/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.maps.widgets;

import com.mojang.blaze3d.platform.cursor.CursorTypes;
import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.screens.maps.PoiCreationScreen;
import com.wynntils.screens.maps.WaypointManagementScreen;
import com.wynntils.services.map.pois.CustomPoi;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import org.lwjgl.glfw.GLFW;

public class WaypointManagerWidget extends AbstractWidget {
    private final Button editButton;
    private final Button deleteButton;
    private final Button upButton;
    private final Button downButton;
    private final Button selectButton;
    private final WaypointManagementScreen managementScreen;

    private final boolean selected;
    private final boolean selectionMode;

    private final float iconRenderX;
    private float iconRenderY;
    private final float iconWidth;
    private final float iconHeight;

    private final CustomColor color;
    private final CustomPoi poi;

    public WaypointManagerWidget(
            int x,
            int y,
            int width,
            int height,
            CustomPoi waypoint,
            WaypointManagementScreen managementScreen,
            boolean selectionMode,
            boolean selected) {
        super(x, y, width, height, Component.literal(waypoint.getName()));
        this.poi = waypoint;
        this.managementScreen = managementScreen;
        this.selectionMode = selectionMode;
        this.selected = selected;

        color = waypoint.getVisibility() == CustomPoi.Visibility.HIDDEN ? CommonColors.GRAY : CommonColors.WHITE;

        editButton = new Button.Builder(
                        Component.translatable("screens.wynntils.waypointManagementGui.edit"),
                        (button) -> McUtils.mc().setScreen(PoiCreationScreen.create(managementScreen, waypoint)))
                .pos(x + width - 20 - (40 * 2), y)
                .size(40, 20)
                .build();

        deleteButton = new Button.Builder(
                        Component.translatable("screens.wynntils.waypointManagementGui.delete"), (button) -> {
                            managementScreen.deleteWaypoint(waypoint, true);
                        })
                .pos(x + width - 20 - 40, y)
                .size(40, 20)
                .build();

        upButton = new Button.Builder(
                        Component.literal("ʌ"), (button) -> managementScreen.updateWaypointPosition(waypoint, -1))
                .pos(x + width - 20, y)
                .size(10, 20)
                .build();

        downButton = new Button.Builder(
                        Component.literal("v"), (button) -> managementScreen.updateWaypointPosition(waypoint, 1))
                .pos(x + width - 10, y)
                .size(10, 20)
                .build();

        Component selectButtonText = selected
                ? Component.translatable("screens.wynntils.waypointManagementGui.deselect")
                : Component.translatable("screens.wynntils.waypointManagementGui.select");

        selectButton = new Button.Builder(selectButtonText, (button) -> managementScreen.selectWaypoint(waypoint))
                .pos(x + width - (40 * 2 + 20), y)
                .size(40 * 2 + 20, 20)
                .build();

        List<CustomPoi> pois = managementScreen.getWaypoints();

        // Don't allow waypoints to be moved if at the top/bottom of the list
        if (pois.indexOf(waypoint) == 0) {
            upButton.active = false;
        }

        if (pois.indexOf(waypoint) == (pois.size() - 1)) {
            downButton.active = false;
        }

        // Scale the icon to fill 80% of the button
        float scaleFactor = 0.8f
                * Math.min(width, height)
                / Math.max(poi.getIcon().width(), poi.getIcon().height());
        iconWidth = poi.getIcon().width() * scaleFactor;
        iconHeight = poi.getIcon().height() * scaleFactor;

        // Calculate x/y position of the icon to keep it centered
        iconRenderX = x + 2;
        iconRenderY = (y + height / 2f) - iconHeight / 2f;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderIcon(guiGraphics);

        FontRenderer.getInstance()
                .renderScrollingText(
                        guiGraphics,
                        StyledText.fromString(poi.getName()),
                        getX() + 25,
                        getY() + 10,
                        95,
                        color,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL);

        FontRenderer.getInstance()
                .renderText(
                        guiGraphics,
                        StyledText.fromString(String.valueOf(poi.getLocation().getX())),
                        getX() + 140,
                        getY() + 10,
                        CommonColors.WHITE,
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL);

        Optional<Integer> poiY = poi.getLocation().getY();

        FontRenderer.getInstance()
                .renderText(
                        guiGraphics,
                        poiY.map(integer -> StyledText.fromString(String.valueOf(integer)))
                                .orElse(StyledText.EMPTY),
                        getX() + 170,
                        getY() + 10,
                        color,
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL);

        FontRenderer.getInstance()
                .renderText(
                        guiGraphics,
                        StyledText.fromString(String.valueOf(poi.getLocation().getZ())),
                        getX() + 200,
                        getY() + 10,
                        color,
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL);

        // In selection mode we don't want the edit/delete/move buttons
        if (selectionMode) {
            selectButton.render(guiGraphics, mouseX, mouseY, partialTick);

            // Border to show selected waypoints, orange when selected, white if not
            RenderUtils.drawRectBorders(
                    guiGraphics,
                    selected ? CommonColors.ORANGE : CommonColors.WHITE,
                    getX(),
                    getY(),
                    getX() + width,
                    getY() + height - 1,
                    1f);
        } else {
            editButton.render(guiGraphics, mouseX, mouseY, partialTick);
            deleteButton.render(guiGraphics, mouseX, mouseY, partialTick);
            upButton.render(guiGraphics, mouseX, mouseY, partialTick);
            downButton.render(guiGraphics, mouseX, mouseY, partialTick);
        }

        if (this.isHovered) {
            guiGraphics.requestCursor(CursorTypes.POINTING_HAND);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        if (!isMouseOver(event.x(), event.y())) return false;

        if (event.button() == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
            McUtils.playSoundUI(SoundEvents.EXPERIENCE_ORB_PICKUP);

            Models.Marker.USER_WAYPOINTS_PROVIDER.addLocation(
                    poi.getLocation().asLocation(), poi.getIcon(), poi.getColor(), poi.getColor(), poi.getName());
            return true;
        } else if (event.button() == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            Models.Marker.USER_WAYPOINTS_PROVIDER.removeLocation(
                    poi.getLocation().asLocation());
            return true;
        }

        boolean clickedButton;

        // Determine if a button was clicked or should we select the widget
        if (selectionMode) {
            clickedButton = selectButton.mouseClicked(event, isDoubleClick);
        } else {
            clickedButton = editButton.mouseClicked(event, isDoubleClick)
                    || deleteButton.mouseClicked(event, isDoubleClick)
                    || upButton.mouseClicked(event, isDoubleClick)
                    || downButton.mouseClicked(event, isDoubleClick);
        }

        if (clickedButton) {
            return clickedButton;
        } else {
            managementScreen.selectWaypoint(poi);
            return true;
        }
    }

    @Override
    public void setY(int y) {
        super.setY(y);

        editButton.setY(y);
        deleteButton.setY(y);
        upButton.setY(y);
        downButton.setY(y);
        selectButton.setY(y);

        iconRenderY = (y + height / 2f) - iconHeight / 2f;
    }

    private void renderIcon(GuiGraphics guiGraphics) {
        RenderUtils.drawScalingTexturedRect(
                guiGraphics,
                poi.getIcon().identifier(),
                poi.getIconColor(),
                iconRenderX,
                iconRenderY,
                iconWidth,
                iconHeight,
                poi.getIcon().width(),
                poi.getIcon().height());
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
}
