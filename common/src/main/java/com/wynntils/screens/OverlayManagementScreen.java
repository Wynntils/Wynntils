/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.config.ConfigManager;
import com.wynntils.core.features.overlays.Corner;
import com.wynntils.core.features.overlays.Edge;
import com.wynntils.core.features.overlays.Overlay;
import com.wynntils.core.features.overlays.OverlayManager;
import com.wynntils.core.features.overlays.OverlayPosition;
import com.wynntils.core.features.overlays.SectionCoordinates;
import com.wynntils.core.features.overlays.sizes.OverlaySize;
import com.wynntils.mc.render.RenderUtils;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.utils.objects.CommonColors;
import com.wynntils.utils.objects.CustomColor;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.phys.Vec2;

public class OverlayManagementScreen extends Screen {
    private static final int BUTTON_WIDTH = 60;
    private static final int BUTTON_HEIGHT = 20;
    private static final int MAX_CLICK_DISTANCE = 10;

    private static SelectionMode selectionMode = SelectionMode.None;
    private static Overlay selectedOverlay = null;
    private static Corner selectedCorner = null;
    private static Edge selectedEdge = null;

    public OverlayManagementScreen(Overlay overlay) {
        super(new TranslatableComponent("screens.wynntils.overlayManagement.name"));
        selectedOverlay = overlay;
    }

    @Override
    protected void init() {
        setupButtons();
    }

    private void setupButtons() {
        this.addRenderableWidget(new Button(
                this.width / 2 - BUTTON_WIDTH * 2,
                this.height - 150,
                BUTTON_WIDTH,
                BUTTON_HEIGHT,
                new TranslatableComponent("screens.wynntils.overlayManagement.closeSettingsScreen"),
                button -> {
                    McUtils.mc().setScreen(new OverlaySelectionScreen());
                    onClose();
                }));
        this.addRenderableWidget(new Button(
                this.width / 2 - BUTTON_WIDTH / 2,
                this.height - 150,
                BUTTON_WIDTH,
                BUTTON_HEIGHT,
                new TranslatableComponent("screens.wynntils.overlayManagement.testSettings"),
                button -> {}));
        this.addRenderableWidget(new Button(
                this.width / 2 + BUTTON_WIDTH,
                this.height - 150,
                BUTTON_WIDTH,
                BUTTON_HEIGHT,
                new TranslatableComponent("screens.wynntils.overlayManagement.applySettings"),
                button -> {
                    ConfigManager.saveConfig();
                    McUtils.mc().setScreen(null);
                    onClose();
                }));
    }

    @Override
    public void onClose() {
        ConfigManager.loadConfigFile();
        ConfigManager.loadConfigOptions(ConfigManager.getConfigHolders(), true);
        //        for (ConfigHolder configHolder : ConfigManager.getConfigHolders().stream().filter(configHolder ->
        // configHolder.getParent() == selectedOverlay).collect(Collectors.toSet())) {
        //            selectedOverlay.updateConfigOption(configHolder);
        //        }
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        renderSections(poseStack);

        Set<Overlay> overlays = OverlayManager.getOverlays().stream()
                .filter(OverlayManager::isEnabled)
                .collect(Collectors.toSet());

        for (Overlay overlay : overlays) {
            CustomColor color = CommonColors.GREEN;
            RenderUtils.drawRectBorders(
                    poseStack,
                    color,
                    overlay.getRenderX(),
                    overlay.getRenderY(),
                    overlay.getRenderX() + overlay.getWidth(),
                    overlay.getRenderY() + overlay.getHeight(),
                    1,
                    1.8f);
            RenderUtils.drawRect(
                    poseStack,
                    color.withAlpha(30),
                    overlay.getRenderX(),
                    overlay.getRenderY(),
                    0,
                    overlay.getWidth(),
                    overlay.getHeight());
        }

        super.render(poseStack, mouseX, mouseY, partialTick); // This renders widgets
    }

    private void renderSections(PoseStack poseStack) {
        for (SectionCoordinates section : OverlayManager.getSections()) {
            RenderUtils.drawRectBorders(
                    poseStack,
                    CustomColor.fromInt(section.hashCode()).withAlpha(255),
                    section.x1(),
                    section.y1(),
                    section.x2(),
                    section.y2(),
                    0,
                    2);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Order:
        //  - Corners
        //  - Edges
        //  - OverlayArea

        // reset
        resetSelection();

        Overlay overlay = selectedOverlay;

        Vec2 mousePos = new Vec2((float) mouseX, (float) mouseY);

        for (Map.Entry<Corner, Vec2> corner : overlay.getCornersMap().entrySet()) {
            float distance = corner.getValue().distanceToSqr(mousePos);
            if (distance < MAX_CLICK_DISTANCE) {
                selectedCorner = corner.getKey();
                selectionMode = SelectionMode.Corner;

                return false;
            }
        }

        for (Edge value : Edge.values()) {
            float minX, maxX, minY, maxY;

            switch (value) {
                case Top -> {
                    minX = overlay.getRenderX();
                    maxX = overlay.getRenderX() + overlay.getWidth();
                    minY = overlay.getRenderY() - MAX_CLICK_DISTANCE / 2f;
                    maxY = overlay.getRenderY() + MAX_CLICK_DISTANCE / 2f;
                }
                case Left -> {
                    minX = overlay.getRenderX() - MAX_CLICK_DISTANCE / 2f;
                    maxX = overlay.getRenderX() + MAX_CLICK_DISTANCE / 2f;
                    minY = overlay.getRenderY();
                    maxY = overlay.getRenderY() + overlay.getHeight();
                }
                case Right -> {
                    minX = overlay.getRenderX() + overlay.getWidth() - MAX_CLICK_DISTANCE / 2f;
                    maxX = overlay.getRenderX() + overlay.getWidth() + MAX_CLICK_DISTANCE / 2f;
                    minY = overlay.getRenderY();
                    maxY = overlay.getRenderY() + overlay.getHeight();
                }
                case Bottom -> {
                    minX = overlay.getRenderX();
                    maxX = overlay.getRenderX() + overlay.getWidth();
                    minY = overlay.getRenderY() + overlay.getHeight() - MAX_CLICK_DISTANCE / 2f;
                    maxY = overlay.getRenderY() + overlay.getHeight() + MAX_CLICK_DISTANCE / 2f;
                }
                default -> {
                    // should not happen
                    continue;
                }
            }

            if (mouseX >= minX && mouseX <= maxX && mouseY >= minY && mouseY <= maxY) {
                selectedEdge = value;
                selectionMode = SelectionMode.Edge;

                return false;
            }
        }

        if ((overlay.getRenderX() <= mouseX && overlay.getRenderX() + overlay.getWidth() >= mouseX)
                && (overlay.getRenderY() <= mouseY && overlay.getRenderY() + overlay.getHeight() >= mouseY)) {
            selectionMode = SelectionMode.Area;

            return false;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        resetSelection();
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        // Order:
        //  - Corners
        //  - Edges
        //  - OverlayArea

        handleOverlayCornerDrag(button, dragX, dragY);

        handleOverlayEdgeDrag(button, dragX, dragY);

        handleOverlayBodyDrag(button, dragX, dragY);

        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    private void handleOverlayEdgeDrag(int button, double dragX, double dragY) {
        if (selectionMode != SelectionMode.Edge || selectedEdge == null || selectedOverlay == null) {
            return;
        }

        Overlay overlay = selectedOverlay;
        Edge edge = selectedEdge;

        OverlaySize overlaySize = overlay.getSize();

        final float renderX = overlay.getRenderX();
        final float renderY = overlay.getRenderY();

        switch (edge) {
            case Top -> {
                overlaySize.setHeight((float) (overlaySize.getHeight() - dragY));
                overlay.setPosition(
                        OverlayPosition.getBestPositionFor(overlay, renderX, renderY, (float) 0, (float) dragY));
            }
            case Left -> {
                overlaySize.setWidth((float) (overlaySize.getWidth() - dragX));
                overlay.setPosition(
                        OverlayPosition.getBestPositionFor(overlay, renderX, renderY, (float) dragX, (float) 0));
            }
            case Right -> {
                overlaySize.setWidth((float) (overlaySize.getWidth() + dragX));
                overlay.setPosition(
                        OverlayPosition.getBestPositionFor(overlay, renderX, renderY, (float) 0, (float) 0));
            }
            case Bottom -> {
                overlaySize.setHeight((float) (overlaySize.getHeight() + dragY));
                overlay.setPosition(
                        OverlayPosition.getBestPositionFor(overlay, renderX, renderY, (float) 0, (float) 0));
            }
        }
    }

    private void handleOverlayBodyDrag(int button, double dragX, double dragY) {
        if (selectionMode != SelectionMode.Area || selectedOverlay == null) {
            return;
        }

        Overlay overlay = selectedOverlay;

        overlay.setPosition(OverlayPosition.getBestPositionFor(
                overlay, overlay.getRenderX(), overlay.getRenderY(), (float) dragX, (float) dragY));
    }

    private void handleOverlayCornerDrag(int button, double dragX, double dragY) {
        if (selectionMode != SelectionMode.Corner || selectedCorner == null || selectedOverlay == null) {
            return;
        }

        Overlay overlay = selectedOverlay;
        Corner corner = selectedCorner;

        OverlaySize overlaySize = overlay.getSize();

        final float renderX = overlay.getRenderX();
        final float renderY = overlay.getRenderY();

        switch (corner) {
            case TopLeft -> {
                overlaySize.setWidth((float) (overlaySize.getWidth() - dragX));
                overlaySize.setHeight((float) (overlaySize.getHeight() - dragY));
                overlay.setPosition(
                        OverlayPosition.getBestPositionFor(overlay, renderX, renderY, (float) dragX, (float) dragY));
            }
            case TopRight -> {
                overlaySize.setWidth((float) (overlaySize.getWidth() + dragX));
                overlaySize.setHeight((float) (overlaySize.getHeight() - dragY));
                overlay.setPosition(
                        OverlayPosition.getBestPositionFor(overlay, renderX, renderY, (float) 0, (float) dragY));
            }
            case BottomLeft -> {
                overlaySize.setWidth((float) (overlaySize.getWidth() - dragX));
                overlaySize.setHeight((float) (overlaySize.getHeight() + dragY));
                overlay.setPosition(
                        OverlayPosition.getBestPositionFor(overlay, renderX, renderY, (float) dragX, (float) 0));
            }
            case BottomRight -> {
                overlaySize.setWidth((float) (overlaySize.getWidth() + dragX));
                overlaySize.setHeight((float) (overlaySize.getHeight() + dragY));
                overlay.setPosition(
                        OverlayPosition.getBestPositionFor(overlay, renderX, renderY, (float) 0, (float) 0));
            }
        }
    }

    private void resetSelection() {
        selectionMode = SelectionMode.None;
        selectedCorner = null;
        selectedEdge = null;
    }

    private enum SelectionMode {
        None,
        Corner,
        Edge,
        Area
    }
}
