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
import com.wynntils.mc.render.FontRenderer;
import com.wynntils.mc.render.HorizontalAlignment;
import com.wynntils.mc.render.RenderUtils;
import com.wynntils.mc.render.TextRenderSetting;
import com.wynntils.mc.render.TextRenderTask;
import com.wynntils.mc.render.VerticalAlignment;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.utils.objects.CommonColors;
import com.wynntils.utils.objects.CustomColor;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.phys.Vec2;
import org.lwjgl.glfw.GLFW;

public class OverlayManagementScreen extends Screen {
    private static final int BUTTON_WIDTH = 60;
    private static final int BUTTON_HEIGHT = 20;
    private static final int MAX_CLICK_DISTANCE = 10;
    private static final List<Component> HELP_TOOLTIP_LINES = List.of(
            new TextComponent("Resize the overlay by dragging the edges or corners."),
            new TextComponent("Move it by dragging the center of the overlay."),
            new TextComponent("Use your arrows to change vertical"),
            new TextComponent("and horizontal alignment."),
            new TextComponent("The overlay name will render respecting"),
            new TextComponent("the current overlay alignments."));

    private static final List<Component> CLOSE_TOOLTIP_LINES =
            List.of(new TextComponent("Click here to stop editing and reset changes."));

    private static final List<Component> TEST_TOOLTIP_LINES = List.of(
            new TextComponent("Click here to toggle test mode."),
            new TextComponent("In test mode, you can see how your overlay setup would look in-game,"),
            new TextComponent("using preview render mode."));

    private static final List<Component> APPLY_TOOLTIP_LINES =
            List.of(new TextComponent("Click here to apply changes to current overlay."));

    private boolean testMode = false;
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

    @Override
    public void onClose() {
        reloadConfigForOverlay();
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        if (testMode) {
            FontRenderer.getInstance()
                    .renderText(
                            poseStack,
                            this.width / 2f,
                            this.height - 160,
                            new TextRenderTask(
                                    "Test mode on.",
                                    new TextRenderSetting(
                                            0,
                                            CommonColors.WHITE,
                                            FontRenderer.TextAlignment.CENTER_ALIGNED,
                                            FontRenderer.TextShadow.NORMAL)));
        } else {
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

                int xOffset =
                        switch (overlay.getRenderHorizontalAlignment()) {
                            case Left -> 3;
                            case Center -> 0;
                            case Right -> -3;
                        };

                int yOffset =
                        switch (overlay.getRenderVerticalAlignment()) {
                            case Top -> 5;
                            case Middle -> 0;
                            case Bottom -> -5;
                        };

                FontRenderer.getInstance()
                        .renderTextWithAlignment(
                                poseStack,
                                overlay.getRenderX() + xOffset,
                                overlay.getRenderY() + yOffset,
                                new TextRenderTask(
                                        overlay.getTranslatedName(),
                                        new TextRenderSetting(
                                                overlay.getWidth(),
                                                color,
                                                FontRenderer.TextAlignment.fromHorizontalAlignment(
                                                        overlay.getRenderHorizontalAlignment()),
                                                FontRenderer.TextShadow.OUTLINE)),
                                overlay.getRenderedWidth() + xOffset,
                                overlay.getRenderedHeight() + yOffset,
                                overlay.getRenderHorizontalAlignment(),
                                overlay.getRenderVerticalAlignment());

                if (isMouseHoveringOverlay(overlay, mouseX, mouseY)) {
                    RenderUtils.drawTooltipAt(
                            poseStack,
                            mouseX
                                    - RenderUtils.getToolTipWidth(
                                            RenderUtils.componentToClientTooltipComponent(HELP_TOOLTIP_LINES),
                                            FontRenderer.getInstance().getFont()),
                            mouseY,
                            100,
                            HELP_TOOLTIP_LINES,
                            FontRenderer.getInstance().getFont(),
                            false);
                }
            }
        }

        super.render(poseStack, mouseX, mouseY, partialTick); // This renders widgets
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (testMode) return super.mouseClicked(mouseX, mouseY, button);

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

        if (isMouseHoveringOverlay(selectedOverlay, mouseX, mouseY)) {
            selectionMode = SelectionMode.Area;

            return false;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (testMode) return false;

        resetSelection();
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (testMode) return false;

        if (keyCode == GLFW.GLFW_KEY_UP || keyCode == GLFW.GLFW_KEY_DOWN) {
            int index = selectedOverlay.getRenderVerticalAlignment().ordinal();

            if (keyCode == GLFW.GLFW_KEY_DOWN) {
                index += 1;
            } else {
                index -= 1;
            }

            VerticalAlignment[] values = VerticalAlignment.values();
            index = (values.length + index) % values.length;
            selectedOverlay.setVerticalAlignmentOverride(values[index]);
            saveConfigForOverlay();
        } else if (keyCode == GLFW.GLFW_KEY_RIGHT || keyCode == GLFW.GLFW_KEY_LEFT) {
            int index = selectedOverlay.getRenderHorizontalAlignment().ordinal();

            if (keyCode == GLFW.GLFW_KEY_RIGHT) {
                index += 1;
            } else {
                index -= 1;
            }

            HorizontalAlignment[] values = HorizontalAlignment.values();
            index = (values.length + index) % values.length;
            selectedOverlay.setHorizontalAlignmentOverride(values[index]);
            saveConfigForOverlay();
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (testMode) return false;

        // Order:
        //  - Corners
        //  - Edges
        //  - OverlayArea

        handleOverlayCornerDrag(button, dragX, dragY);

        handleOverlayEdgeDrag(button, dragX, dragY);

        handleOverlayBodyDrag(button, dragX, dragY);

        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    private boolean isMouseHoveringOverlay(Overlay overlay, double mouseX, double mouseY) {
        return (overlay.getRenderX() <= mouseX && overlay.getRenderX() + overlay.getWidth() >= mouseX)
                && (overlay.getRenderY() <= mouseY && overlay.getRenderY() + overlay.getHeight() >= mouseY);
    }

    private void saveConfigForOverlay() {
        ConfigManager.saveConfig();
        reloadConfigForOverlay();
    }

    private void reloadConfigForOverlay() {
        ConfigManager.loadConfigFile();
        ConfigManager.loadConfigOptions(ConfigManager.getConfigHolders(selectedOverlay), true);
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
                },
                (button, poseStack, renderX, renderY) -> RenderUtils.drawTooltipAt(
                        poseStack,
                        renderX,
                        renderY,
                        100,
                        CLOSE_TOOLTIP_LINES,
                        FontRenderer.getInstance().getFont(),
                        false)));
        this.addRenderableWidget(new Button(
                this.width / 2 - BUTTON_WIDTH / 2,
                this.height - 150,
                BUTTON_WIDTH,
                BUTTON_HEIGHT,
                new TranslatableComponent("screens.wynntils.overlayManagement.testSettings"),
                button -> {
                    testMode = !testMode;
                },
                (button, poseStack, renderX, renderY) -> RenderUtils.drawTooltipAt(
                        poseStack,
                        renderX,
                        renderY,
                        100,
                        TEST_TOOLTIP_LINES,
                        FontRenderer.getInstance().getFont(),
                        false)));
        this.addRenderableWidget(new Button(
                this.width / 2 + BUTTON_WIDTH,
                this.height - 150,
                BUTTON_WIDTH,
                BUTTON_HEIGHT,
                new TranslatableComponent("screens.wynntils.overlayManagement.applySettings"),
                button -> {
                    ConfigManager.saveConfig();
                    McUtils.mc().setScreen(new OverlaySelectionScreen());
                    onClose();
                },
                (button, poseStack, renderX, renderY) -> RenderUtils.drawTooltipAt(
                        poseStack,
                        renderX,
                        renderY,
                        100,
                        APPLY_TOOLTIP_LINES,
                        FontRenderer.getInstance().getFont(),
                        false)));
    }

    private void resetSelection() {
        selectionMode = SelectionMode.None;
        selectedCorner = null;
        selectedEdge = null;
    }

    public boolean isTestMode() {
        return testMode;
    }

    private enum SelectionMode {
        None,
        Corner,
        Edge,
        Area
    }
}
