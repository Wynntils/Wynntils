/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.maps.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.components.Services;
import com.wynntils.core.text.StyledText;
import com.wynntils.screens.maps.WaypointCreationScreen;
import com.wynntils.screens.maps.WaypointManagementScreen;
import com.wynntils.services.mapdata.features.builtin.WaypointLocation;
import com.wynntils.services.mapdata.type.MapIcon;
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

    private final MapIcon mapIcon;
    private final float iconRenderX;
    private float iconRenderY;
    private final float iconWidth;
    private final float iconHeight;

    private final CustomColor iconColor;
    private final CustomColor labelColor;
    private final TextShadow labelShadow;
    private final String waypointLabel;
    private final WaypointLocation waypoint;

    public WaypointManagerWidget(
            int x,
            int y,
            int width,
            int height,
            WaypointLocation waypoint,
            WaypointManagementScreen managementScreen,
            boolean selectionMode,
            boolean selected) {
        super(
                x,
                y,
                width,
                height,
                Component.literal(waypoint.getAttributes().get().getLabel().get()));
        this.waypoint = waypoint;
        this.managementScreen = managementScreen;
        this.selectionMode = selectionMode;
        this.selected = selected;

        labelColor = waypoint.getAttributes().get().getLabelColor().orElse(CommonColors.WHITE);
        labelShadow = waypoint.getAttributes().get().getLabelShadow().orElse(TextShadow.NORMAL);
        iconColor = waypoint.getAttributes().get().getIconColor().orElse(CommonColors.WHITE);
        waypointLabel = waypoint.getAttributes().get().getLabel().get();

        editButton = new Button.Builder(
                        Component.translatable("screens.wynntils.waypointManagementGui.edit"),
                        (button) -> McUtils.mc().setScreen(WaypointCreationScreen.create(managementScreen, waypoint)))
                .pos(x + width - 20 - (40 * 2), y)
                .size(40, 20)
                .build();

        deleteButton = new Button.Builder(
                        Component.translatable("screens.wynntils.waypointManagementGui.delete"), (button) -> {
                            managementScreen.deleteWaypoint(waypoint);
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

        List<WaypointLocation> waypoints = managementScreen.getWaypoints();

        // Don't allow waypoints to be moved if at the top/bottom of the list
        if (waypoints.indexOf(waypoint) == 0) {
            upButton.active = false;
        }

        if (waypoints.indexOf(waypoint) == (waypoints.size() - 1)) {
            downButton.active = false;
        }

        Optional<String> iconId = waypoint.getAttributes().get().getIconId();

        if (iconId.isPresent()) {
            Optional<MapIcon> mapIconOpt = Services.MapData.getIcon(iconId.get());

            if (mapIconOpt.isPresent()) {
                mapIcon = mapIconOpt.get();

                // Scale the icon to fill 80% of the button
                float scaleFactor = 0.8f * Math.min(width, height) / Math.max(mapIcon.getWidth(), mapIcon.getHeight());
                iconWidth = mapIcon.getWidth() * scaleFactor;
                iconHeight = mapIcon.getHeight() * scaleFactor;

                // Calculate x/y position of the icon to keep it centered
                iconRenderX = x + 2;
                iconRenderY = (y + height / 2f) - iconHeight / 2f;
            } else {
                iconWidth = -1;
                iconHeight = -1;
                iconRenderX = -1;
                iconRenderY = -1;
                mapIcon = null;
            }
        } else {
            iconWidth = -1;
            iconHeight = -1;
            iconRenderX = -1;
            iconRenderY = -1;
            mapIcon = null;
        }
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        PoseStack poseStack = guiGraphics.pose();

        renderIcon(poseStack);

        FontRenderer.getInstance()
                .renderScrollingText(
                        poseStack,
                        StyledText.fromString(waypointLabel),
                        getX() + 25,
                        getY() + 10,
                        95,
                        labelColor,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.MIDDLE,
                        labelShadow);

        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        StyledText.fromString(
                                String.valueOf(waypoint.getLocation().x())),
                        getX() + 140,
                        getY() + 10,
                        CommonColors.WHITE,
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL);

        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        StyledText.fromString(
                                String.valueOf(waypoint.getLocation().y())),
                        getX() + 170,
                        getY() + 10,
                        CommonColors.WHITE,
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL);

        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        StyledText.fromString(
                                String.valueOf(waypoint.getLocation().z())),
                        getX() + 200,
                        getY() + 10,
                        CommonColors.WHITE,
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL);

        // In selection mode we don't want the edit/delete/move buttons
        if (selectionMode) {
            selectButton.render(guiGraphics, mouseX, mouseY, partialTick);

            // Border to show selected waypoints, orange when selected, white if not
            RenderUtils.drawRectBorders(
                    poseStack,
                    selected ? CommonColors.ORANGE : CommonColors.WHITE,
                    getX(),
                    getY(),
                    getX() + width,
                    getY() + height,
                    0,
                    1f);
        } else {
            editButton.render(guiGraphics, mouseX, mouseY, partialTick);
            deleteButton.render(guiGraphics, mouseX, mouseY, partialTick);
            upButton.render(guiGraphics, mouseX, mouseY, partialTick);
            downButton.render(guiGraphics, mouseX, mouseY, partialTick);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isMouseOver(mouseX, mouseY)) return false;

        if (button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
            McUtils.playSoundUI(SoundEvents.EXPERIENCE_ORB_PICKUP);
            Services.UserMarker.addUserMarkedFeature(waypoint);
            return true;
        } else if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            Services.UserMarker.removeUserMarkedFeature(waypoint);
            return true;
        }

        boolean clickedButton;

        // Determine if a button was clicked or should we select the widget
        if (selectionMode) {
            clickedButton = selectButton.mouseClicked(mouseX, mouseY, button);
        } else {
            clickedButton = editButton.mouseClicked(mouseX, mouseY, button)
                    || deleteButton.mouseClicked(mouseX, mouseY, button)
                    || upButton.mouseClicked(mouseX, mouseY, button)
                    || downButton.mouseClicked(mouseX, mouseY, button);
        }

        if (clickedButton) {
            return clickedButton;
        } else {
            managementScreen.selectWaypoint(waypoint);
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

        if (mapIcon != null) {
            iconRenderY = (y + height / 2f) - iconHeight / 2f;
        }
    }

    private void renderIcon(PoseStack poseStack) {
        if (mapIcon == null) return;

        RenderUtils.drawScalingTexturedRectWithColor(
                poseStack,
                mapIcon.getResourceLocation(),
                iconColor,
                iconRenderX,
                iconRenderY,
                1,
                iconWidth,
                iconHeight,
                mapIcon.getWidth(),
                mapIcon.getHeight());
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
}
