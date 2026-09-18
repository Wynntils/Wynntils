/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.overlays.placement;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import com.wynntils.core.components.Managers;
import com.wynntils.core.consumers.overlays.Corner;
import com.wynntils.core.consumers.overlays.CustomNameProperty;
import com.wynntils.core.consumers.overlays.Edge;
import com.wynntils.core.consumers.overlays.Overlay;
import com.wynntils.core.consumers.overlays.OverlayPosition;
import com.wynntils.core.consumers.overlays.OverlaySize;
import com.wynntils.core.consumers.overlays.SectionCoordinates;
import com.wynntils.core.consumers.screens.WynntilsScreen;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.text.StyledText;
import com.wynntils.screens.base.widgets.WynntilsCheckbox;
import com.wynntils.screens.overlays.ordering.OverlayOrderingScreen;
import com.wynntils.screens.overlays.placement.OverlaySnapAxis.SnapTarget;
import com.wynntils.screens.overlays.selection.OverlaySelectionScreen;
import com.wynntils.utils.MathUtils;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.ComponentUtils;
import com.wynntils.utils.mc.KeyboardUtils;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import com.wynntils.utils.type.Pair;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec2;
import org.lwjgl.glfw.GLFW;

public final class OverlayManagementScreen extends WynntilsScreen {
    // This is used to calculate alignment lines
    // If the value is set to 4, alignment lines will render at 1/2, 1/3, 2/3, 1/4, 3/4
    // of the screen both vertically and horizontally.
    private static final int ALIGNMENT_LINES_MAX_SECTIONS_PER_AXIS = 4;

    private static final int BUTTON_WIDTH = 60;
    private static final int BUTTON_SHORT_WIDTH = 20;
    private static final int BUTTON_HEIGHT = 20;
    private static final int MAX_CLICK_DISTANCE = 5;
    private static final int ANIMATION_LENGTH = 30;

    private static final List<Component> HELP_TOOLTIP_LINES = List.of(
            Component.translatable("screens.wynntils.overlayManagement.helpTooltip1"),
            Component.translatable("screens.wynntils.overlayManagement.helpTooltip2"),
            Component.translatable("screens.wynntils.overlayManagement.helpTooltip3"),
            Component.translatable("screens.wynntils.overlayManagement.helpTooltip4"),
            Component.translatable("screens.wynntils.overlayManagement.helpTooltip5"),
            Component.translatable("screens.wynntils.overlayManagement.screenSnapTooltip"),
            Component.translatable("screens.wynntils.overlayManagement.historyTooltip"),
            Component.translatable("screens.wynntils.overlayManagement.openSettingsTooltip"),
            Component.translatable("screens.wynntils.overlayManagement.positionPanel.hint"),
            Component.translatable("screens.wynntils.overlayManagement.helpTooltip6")
                    .withStyle(ChatFormatting.RED));
    private static final List<Component> LOCKED_TOOLTIP_LINES = ComponentUtils.wrapTooltips(
            List.of(
                    Component.translatable("screens.wynntils.overlayManagement.placementLockedTooltip"),
                    Component.translatable("screens.wynntils.overlayManagement.openSettingsTooltip")),
            200);

    private final Set<SnapTarget> verticalAlignmentLinePositions = new HashSet<>();
    private final Set<SnapTarget> horizontalAlignmentLinePositions = new HashSet<>();
    private final Set<SnapTarget> verticalCenterLinePositions = new HashSet<>();
    private final Set<SnapTarget> horizontalCenterLinePositions = new HashSet<>();
    private final Map<Float, CustomColor> verticalScreenGuides = new HashMap<>();
    private final Map<Float, CustomColor> horizontalScreenGuides = new HashMap<>();
    private final OverlaySnapAxis horizontalSnap = new OverlaySnapAxis();
    private final OverlaySnapAxis verticalSnap = new OverlaySnapAxis();
    private final OverlayEditHistory editHistory = new OverlayEditHistory();
    private final OverlayHelpTooltip helpTooltip = new OverlayHelpTooltip();
    private Button undoButton;
    private Button redoButton;

    private SelectionMode selectionMode = SelectionMode.NONE;
    private Overlay selectedOverlay;
    private final boolean fixedSelection;
    private Corner selectedCorner = null;
    private Edge selectedEdge = null;

    private boolean buttonsAtBottom = true;
    private boolean renderAllOverlays;
    private boolean showPreview = true;

    private boolean userInteracted = false;
    private int animationLengthRemaining;

    private OverlayPositionPanel positionPanel;
    private Overlay helpTooltipTarget;
    private boolean pendingPanelClick;
    private double clickX;
    private double clickY;

    private final Screen previousScreen;

    private OverlayManagementScreen(Screen previousScreen, Overlay overlay) {
        super(Component.translatable("screens.wynntils.overlayManagement.name"));
        this.previousScreen = previousScreen;
        selectedOverlay = overlay;
        fixedSelection = true;
        renderAllOverlays = false;
        animationLengthRemaining = ANIMATION_LENGTH;
    }

    private OverlayManagementScreen(Screen previousScreen) {
        super(Component.translatable("screens.wynntils.overlayManagement.name"));
        this.previousScreen = previousScreen;
        selectedOverlay = null;
        fixedSelection = false;
        renderAllOverlays = true;
        animationLengthRemaining = 0;
    }

    public static Screen create(Screen previousScreen) {
        return new OverlayManagementScreen(previousScreen);
    }

    public static Screen create(Screen previousScreen, Overlay overlay) {
        return new OverlayManagementScreen(previousScreen, overlay);
    }

    @Override
    protected void doInit() {
        resetHelpTooltip();
        resetSelection();
        closePositionPanel();
        setupButtons();
        calculateAlignmentLinePositions();
    }

    @Override
    public void doRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderScreenGuides(guiGraphics);
        if (selectionMode != SelectionMode.NONE) {
            renderAlignmentLines(guiGraphics);
        }

        Set<Overlay> overlays = Managers.Overlay.getOverlays().stream()
                .filter(Managers.Overlay::isEnabled)
                .collect(Collectors.toSet());

        // We want to render the tooltip for what will actually be interacted with
        boolean hoveringPanel = positionPanel != null && positionPanel.contains(mouseX, mouseY);
        boolean renderedTooltip = hoveringPanel;
        Overlay hoveredHelpTooltip = null;

        // Buttons have the highest priority so check those first
        for (GuiEventListener listener : this.children) {
            if (listener.isMouseOver(mouseX, mouseY)) {
                renderedTooltip = true;
                break;
            }
        }

        Overlay helpTooltipOverlay = getHoveredHelpTooltip(mouseX, mouseY);

        if (selectionMode == SelectionMode.EDGE) {
            guiGraphics.requestCursor(selectedEdge.isVerticalLine() ? CursorTypes.RESIZE_EW : CursorTypes.RESIZE_NS);
        } else if (selectionMode != SelectionMode.NONE) {
            guiGraphics.requestCursor(CursorTypes.RESIZE_ALL);
        }

        for (Overlay overlay : overlays) {
            if (!renderAllOverlays && overlay != selectedOverlay) continue;

            CustomColor color = getOverlayColor(overlay);
            RenderUtils.drawRectBorders(
                    guiGraphics,
                    color,
                    overlay.getRenderX(),
                    overlay.getRenderY(),
                    overlay.getRenderX() + overlay.getWidth(),
                    overlay.getRenderY() + overlay.getHeight(),
                    1.8f);
            int colorAlphaRect = fixedSelection && overlay == selectedOverlay
                    ? (int) Math.max(MathUtils.map(animationLengthRemaining, 0, ANIMATION_LENGTH, 30, 255), 30)
                    : 30;
            RenderUtils.drawRect(
                    guiGraphics,
                    color.withAlpha(colorAlphaRect),
                    overlay.getRenderX(),
                    overlay.getRenderY(),
                    overlay.getWidth(),
                    overlay.getHeight());

            String overlayName = overlay.getTranslatedName();

            // Show the custom name for info boxes/custom bars if given
            if (overlay instanceof CustomNameProperty customNameProperty) {
                if (!customNameProperty.getCustomName().get().isEmpty()) {
                    overlayName = customNameProperty.getCustomName().get();
                }
            }
            if (overlay.isPlacementLocked()) {
                overlayName += Component.translatable("screens.wynntils.overlayManagement.lockedSuffix")
                        .getString();
            }

            // Only display overlay name when not rendering preview of the overlay
            if (!showPreview) {
                float yOffset =
                        switch (overlay.getRenderVerticalAlignment()) {
                            case TOP -> 1.8f;
                            case MIDDLE -> 0f;
                            case BOTTOM -> -1.8f;
                        };

                float xOffset =
                        switch (overlay.getRenderHorizontalAlignment()) {
                            case LEFT -> 1.8f;
                            case CENTER -> 0f;
                            case RIGHT -> -1.8f;
                        };

                float renderX = overlay.getRenderX() + xOffset;
                float renderY = overlay.getRenderY() + yOffset;

                FontRenderer.getInstance()
                        .renderAlignedTextInBox(
                                guiGraphics,
                                StyledText.fromString(overlayName),
                                renderX,
                                renderX + overlay.getWidth(),
                                renderY,
                                renderY + overlay.getHeight(),
                                overlay.getWidth(),
                                color,
                                overlay.getRenderHorizontalAlignment(),
                                overlay.getRenderVerticalAlignment(),
                                TextShadow.OUTLINE);
            }

            boolean hovering = isMouseHoveringOverlay(overlay, mouseX, mouseY);

            if (hovering && !hoveringPanel && selectionMode == SelectionMode.NONE) {
                guiGraphics.requestCursor(CursorTypes.POINTING_HAND);
            }

            // If tooltip has yet been rendered then we need to check
            // if an overlay is hovered and display the tooltip for that.
            if (!renderedTooltip
                    && !fixedSelection
                    && showPreview
                    && overlay != selectedOverlay
                    && hovering
                    && selectionMode == SelectionMode.NONE) {
                guiGraphics.setTooltipForNextFrame(Component.literal(overlayName), mouseX, mouseY);
                renderedTooltip = true;
            } else if (!renderedTooltip
                    && overlay == selectedOverlay
                    && hovering
                    && selectionMode == SelectionMode.NONE) {
                if (overlay.isPlacementLocked()) {
                    guiGraphics.setTooltipForNextFrame(
                            Lists.transform(LOCKED_TOOLTIP_LINES, Component::getVisualOrderText), mouseX, mouseY);
                } else if (overlay == helpTooltipOverlay) {
                    hoveredHelpTooltip = overlay;
                }
                renderedTooltip = true;
            }
        }

        // Render widgets
        for (Renderable renderable : this.renderables) {
            if (positionPanel != null && positionPanel.getWidgets().contains(renderable)) continue;
            renderable.render(guiGraphics, hoveringPanel ? -1 : mouseX, hoveringPanel ? -1 : mouseY, partialTick);
        }
        if (positionPanel != null) {
            positionPanel.render(guiGraphics);
            positionPanel.getWidgets().forEach(widget -> widget.render(guiGraphics, mouseX, mouseY, partialTick));
        }

        if (hoveredHelpTooltip == null) {
            resetHelpTooltip();
        } else {
            setHelpTooltipTarget(hoveredHelpTooltip);
            helpTooltip.render(
                    guiGraphics,
                    HELP_TOOLTIP_LINES,
                    FontRenderer.getInstance().getFont(),
                    width,
                    height,
                    mouseX,
                    mouseY);
        }
    }

    @Override
    protected void renderBlurredBackground(GuiGraphics guiGraphics) {}

    @Override
    protected void renderMenuBackground(GuiGraphics guiGraphics) {}

    @Override
    public void tick() {
        if (userInteracted) return;

        if (animationLengthRemaining <= 0) {
            animationLengthRemaining = ANIMATION_LENGTH;
        }

        animationLengthRemaining--;
    }

    @Override
    public void onClose() {
        resetHelpTooltip();
        closePositionPanel();
        resetSelection();
        editHistory.clear();
        reloadConfigForOverlay();
    }

    @Override
    public boolean doMouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        if (positionPanel != null) {
            if (positionPanel.contains(event.x(), event.y())) {
                setFocusedTextInput(null);
                setFocused(null);
                setDragging(false);
                for (var widget : positionPanel.getWidgets()) {
                    if (!widget.visible || !widget.active) continue;
                    if (widget.mouseClicked(event, isDoubleClick)) {
                        setFocused(widget);
                        setDragging(event.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT);
                        break;
                    }
                }
                return true;
            }
            closePositionPanel();
        }

        // Let the buttons of the Screen have priority
        if (super.doMouseClicked(event, isDoubleClick)) return true;

        userInteracted = true;
        animationLengthRemaining = 0;

        // Order:
        //  - Corners
        //  - Edges
        //  - OverlayArea

        // reset
        resetSelection();

        // Ignore clicking on other overlays if editing a specific overlay
        // or other overlays aren't currently rendered
        if (!fixedSelection && renderAllOverlays) {
            Set<Overlay> overlays = Managers.Overlay.getOverlays().stream()
                    .filter(Managers.Overlay::isEnabled)
                    .collect(Collectors.toSet());

            for (Overlay overlay : overlays) {
                if (isMouseHoveringOverlay(overlay, event.x(), event.y())) {
                    selectedOverlay = overlay;
                    break;
                }
            }
        }

        if (selectedOverlay == null) return false;

        if (event.button() == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            if (!isMouseHoveringOverlay(selectedOverlay, event.x(), event.y())) return false;

            Managers.Config.saveConfig();
            onClose();
            McUtils.setScreen(OverlaySelectionScreen.create(selectedOverlay));
            return true;
        }

        Overlay selected = selectedOverlay;

        setupButtons();

        if (selected.isPlacementLocked()) {
            pendingPanelClick = event.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT
                    && isMouseHoveringOverlay(selected, event.x(), event.y());
            clickX = event.x();
            clickY = event.y();
            return false;
        }

        boolean shiftMiddleClick = event.button() == GLFW.GLFW_MOUSE_BUTTON_MIDDLE && KeyboardUtils.isShiftDown();
        if (shiftMiddleClick && !isMouseHoveringOverlay(selectedOverlay, event.x(), event.y())) {
            return false;
        }

        editHistory.begin(selectedOverlay);
        if (shiftMiddleClick) {
            selectedOverlay.getConfigOptionFromString("position").ifPresent(Config::reset);
            selectedOverlay.getConfigOptionFromString("size").ifPresent(Config::reset);
            selectedOverlay
                    .getConfigOptionFromString("horizontalAlignmentOverride")
                    .ifPresent(Config::reset);
            selectedOverlay
                    .getConfigOptionFromString("verticalAlignmentOverride")
                    .ifPresent(Config::reset);

            resetSelection();
            return true;
        }

        Vec2 mousePos = new Vec2((float) event.x(), (float) event.y());

        pendingPanelClick = event.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT
                && isMouseHoveringOverlay(selectedOverlay, event.x(), event.y());
        clickX = event.x();
        clickY = event.y();
        calculateAlignmentLinePositions();

        for (Corner corner : Corner.values()) {
            float distance = selected.getCornerPoints(corner).distanceToSqr(mousePos);
            if (distance < MAX_CLICK_DISTANCE) {
                selectedCorner = corner;
                selectionMode = SelectionMode.CORNER;

                return false;
            }
        }

        for (Edge value : Edge.values()) {
            float minX, maxX, minY, maxY;

            switch (value) {
                case TOP -> {
                    minX = selected.getRenderX();
                    maxX = selected.getRenderX() + selected.getWidth();
                    minY = selected.getRenderY() - MAX_CLICK_DISTANCE / 2f;
                    maxY = selected.getRenderY() + MAX_CLICK_DISTANCE / 2f;
                }
                case LEFT -> {
                    minX = selected.getRenderX() - MAX_CLICK_DISTANCE / 2f;
                    maxX = selected.getRenderX() + MAX_CLICK_DISTANCE / 2f;
                    minY = selected.getRenderY();
                    maxY = selected.getRenderY() + selected.getHeight();
                }
                case RIGHT -> {
                    minX = selected.getRenderX() + selected.getWidth() - MAX_CLICK_DISTANCE / 2f;
                    maxX = selected.getRenderX() + selected.getWidth() + MAX_CLICK_DISTANCE / 2f;
                    minY = selected.getRenderY();
                    maxY = selected.getRenderY() + selected.getHeight();
                }
                case BOTTOM -> {
                    minX = selected.getRenderX();
                    maxX = selected.getRenderX() + selected.getWidth();
                    minY = selected.getRenderY() + selected.getHeight() - MAX_CLICK_DISTANCE / 2f;
                    maxY = selected.getRenderY() + selected.getHeight() + MAX_CLICK_DISTANCE / 2f;
                }
                default -> {
                    // should not happen
                    continue;
                }
            }

            if (event.x() >= minX && event.x() <= maxX && event.y() >= minY && event.y() <= maxY) {
                selectedEdge = value;
                selectionMode = SelectionMode.EDGE;

                return false;
            }
        }

        if (isMouseHoveringOverlay(selectedOverlay, event.x(), event.y())) {
            selectionMode = SelectionMode.AREA;

            return false;
        }

        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY) {
        Overlay hoveredOverlay = getHoveredHelpTooltip(mouseX, mouseY);
        if (hoveredOverlay == null) {
            resetHelpTooltip();
            return super.mouseScrolled(mouseX, mouseY, deltaX, deltaY);
        }

        setHelpTooltipTarget(hoveredOverlay);
        return helpTooltip.scroll(HELP_TOOLTIP_LINES, FontRenderer.getInstance().getFont(), width, height, deltaY);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (positionPanel != null) {
            if (getFocusedTextInput() != null && getFocusedTextInput().visible && getFocusedTextInput().active) {
                getFocusedTextInput().mouseDragged(event, dragX, dragY);
            }
            return true;
        }

        // Let the buttons of the Screen have priority
        if (super.mouseDragged(event, dragX, dragY)) return true;

        if (pendingPanelClick) {
            double totalX = event.x() - clickX;
            double totalY = event.y() - clickY;
            if (totalX * totalX + totalY * totalY <= 9) return true;

            pendingPanelClick = false;
            dragX = totalX;
            dragY = totalY;
        }

        if (selectedOverlay == null) return false;

        if (selectedOverlay.isPlacementLocked()) return true;

        switch (selectionMode) {
            case CORNER -> handleOverlayCornerDrag(dragX, dragY);
            case EDGE -> handleOverlayEdgeDrag(dragX, dragY);
            case AREA -> handleOverlayBodyDrag(dragX, dragY);
            default -> {}
        }

        return false;
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (positionPanel != null) {
            if (getFocusedTextInput() != null && getFocusedTextInput().visible && getFocusedTextInput().active) {
                getFocusedTextInput().mouseReleased(event);
            }
            setDragging(false);
            return true;
        }

        double releaseX = event.x() - clickX;
        double releaseY = event.y() - clickY;
        boolean openPanel = pendingPanelClick
                && event.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT
                && releaseX * releaseX + releaseY * releaseY <= 9;
        pendingPanelClick = false;

        // Let the buttons of the Screen have priority
        boolean handled = super.mouseReleased(event);

        resetSelection();
        if (openPanel && selectedOverlay != null) {
            positionPanel = new OverlayPositionPanel(
                    selectedOverlay, (int) event.x(), (int) event.y(), width, height, this, this::togglePlacementLock);
            positionPanel.getWidgets().forEach(this::addRenderableWidget);
            editHistory.begin(selectedOverlay);
            return true;
        }
        return handled;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        userInteracted = true;
        animationLengthRemaining = 0;

        if (KeyboardUtils.isControlDown() && event.key() == GLFW.GLFW_KEY_Z) {
            restoreHistory(KeyboardUtils.isShiftDown());
            return true;
        }
        if (KeyboardUtils.isControlDown() && event.key() == GLFW.GLFW_KEY_Y) {
            restoreHistory(true);
            return true;
        }

        if (positionPanel != null) {
            if (event.key() == GLFW.GLFW_KEY_ESCAPE || event.key() == GLFW.GLFW_KEY_ENTER) {
                closePositionPanel();
                return true;
            }
            if (getFocusedTextInput() != null && (!getFocusedTextInput().visible || !getFocusedTextInput().active)) {
                setFocusedTextInput(null);
                setFocused(null);
            }
            if (positionPanel.isPlacementLocked() && event.key() == GLFW.GLFW_KEY_TAB) {
                return true;
            }
            if (getFocusedTextInput() != null || event.key() == GLFW.GLFW_KEY_TAB) {
                super.keyPressed(event);
                return true;
            }
            closePositionPanel();
        }

        if (event.key() == GLFW.GLFW_KEY_ENTER) {
            Managers.Config.saveConfig();
            onClose();
            McUtils.setScreen(previousScreen);
            return true;
        } else if (event.key() == GLFW.GLFW_KEY_ESCAPE) {
            onClose();
            McUtils.setScreen(previousScreen);
            return true;
        }

        if (selectedOverlay == null) return false;

        if (selectedOverlay.isPlacementLocked()) {
            if (event.key() == GLFW.GLFW_KEY_UP
                    || event.key() == GLFW.GLFW_KEY_DOWN
                    || event.key() == GLFW.GLFW_KEY_LEFT
                    || event.key() == GLFW.GLFW_KEY_RIGHT) {
                return true;
            }
            return false;
        }

        boolean arrowKey = event.key() == GLFW.GLFW_KEY_UP
                || event.key() == GLFW.GLFW_KEY_DOWN
                || event.key() == GLFW.GLFW_KEY_LEFT
                || event.key() == GLFW.GLFW_KEY_RIGHT;
        if (arrowKey) {
            resetSelection();
            editHistory.begin(selectedOverlay);
        }

        // Shift + Arrow keys change overlay alignment
        if (arrowKey && KeyboardUtils.isShiftDown()) {
            if (event.key() == GLFW.GLFW_KEY_UP || event.key() == GLFW.GLFW_KEY_DOWN) {
                int index = selectedOverlay.getRenderVerticalAlignment().ordinal();

                if (event.key() == GLFW.GLFW_KEY_DOWN) {
                    index += 1;
                } else {
                    index -= 1;
                }

                VerticalAlignment[] values = VerticalAlignment.values();
                index = (values.length + index) % values.length;

                int finalIndex = index;
                selectedOverlay
                        .getConfigOptionFromString("verticalAlignmentOverride")
                        .ifPresent(config -> ((Config<VerticalAlignment>) config).setValue(values[finalIndex]));

            } else if (event.key() == GLFW.GLFW_KEY_RIGHT || event.key() == GLFW.GLFW_KEY_LEFT) {
                int index = selectedOverlay.getRenderHorizontalAlignment().ordinal();

                if (event.key() == GLFW.GLFW_KEY_RIGHT) {
                    index += 1;
                } else {
                    index -= 1;
                }

                HorizontalAlignment[] values = HorizontalAlignment.values();
                index = (values.length + index) % values.length;

                int finalIndex = index;
                selectedOverlay
                        .getConfigOptionFromString("horizontalAlignmentOverride")
                        .ifPresent(config -> ((Config<HorizontalAlignment>) config).setValue(values[finalIndex]));
            }
        } else if (arrowKey) {
            // Arrow keys change overlay position
            int offsetX = 0;
            int offsetY = 0;

            if (event.key() == GLFW.GLFW_KEY_UP) offsetY = -1;
            else if (event.key() == GLFW.GLFW_KEY_DOWN) offsetY = 1;
            else if (event.key() == GLFW.GLFW_KEY_RIGHT) offsetX = 1;
            else if (event.key() == GLFW.GLFW_KEY_LEFT) offsetX = -1;

            final int finalOffsetX = offsetX;
            final int finalOffsetY = offsetY;

            selectedOverlay
                    .getConfigOptionFromString("position")
                    .ifPresent(config -> ((Config<OverlayPosition>) config)
                            .setValue(OverlayPosition.getBestPositionFor(
                                    selectedOverlay,
                                    selectedOverlay.getRenderX(),
                                    selectedOverlay.getRenderY(),
                                    finalOffsetX,
                                    finalOffsetY)));
        }

        if (arrowKey) {
            editHistory.finish();
            updateHistoryButtons();
            calculateAlignmentLinePositions();
            return true;
        }

        if (event.key() == GLFW.GLFW_KEY_LEFT_SHIFT || event.key() == GLFW.GLFW_KEY_RIGHT_SHIFT) {
            horizontalSnap.reset();
            verticalSnap.reset();
        }

        if (event.key() == GLFW.GLFW_KEY_LEFT_CONTROL || event.key() == GLFW.GLFW_KEY_RIGHT_CONTROL) {
            horizontalSnap.restrictToScreenGuides();
            verticalSnap.restrictToScreenGuides();
        }

        return false;
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        if (positionPanel != null && positionPanel.isPlacementLocked()) {
            setFocusedTextInput(null);
            setFocused(null);
            return true;
        }
        return super.charTyped(event);
    }

    public Overlay getSelectedOverlay() {
        return selectedOverlay;
    }

    public boolean shouldRenderAllOverlays() {
        return renderAllOverlays;
    }

    public boolean showPreview() {
        return showPreview;
    }

    private CustomColor getOverlayColor(Overlay overlay) {
        if (overlay == selectedOverlay) return CommonColors.GREEN;

        return fixedSelection ? new CustomColor(200, 200, 200, 255) : CommonColors.LIGHT_BLUE;
    }

    private boolean isMouseHoveringOverlay(Overlay overlay, double mouseX, double mouseY) {
        if (overlay == null) return false;

        return (overlay.getRenderX() <= mouseX && overlay.getRenderX() + overlay.getWidth() >= mouseX)
                && (overlay.getRenderY() <= mouseY && overlay.getRenderY() + overlay.getHeight() >= mouseY);
    }

    private void reloadConfigForOverlay() {
        Managers.Config.reloadConfiguration(true);
    }

    private void handleOverlayEdgeDrag(double dragX, double dragY) {
        if (selectedEdge == null || selectedOverlay == null || selectedOverlay.isPlacementLocked()) {
            return;
        }

        Pair<Double, Double> newDrag = calculateDragAfterSnapping(dragX, dragY);
        dragX = newDrag.a();
        dragY = newDrag.b();

        Overlay overlay = selectedOverlay;
        Edge edge = selectedEdge;

        OverlaySize overlaySize = overlay.getSize();

        final float renderX = overlay.getRenderX();
        final float renderY = overlay.getRenderY();

        switch (edge) {
            case TOP -> {
                overlay.setHeight((float) (overlaySize.getHeight() - dragY));
                overlay.setPosition(
                        OverlayPosition.getBestPositionFor(overlay, renderX, renderY, (float) 0, (float) dragY));
            }
            case LEFT -> {
                overlay.setWidth((float) (overlaySize.getWidth() - dragX));
                overlay.setPosition(
                        OverlayPosition.getBestPositionFor(overlay, renderX, renderY, (float) dragX, (float) 0));
            }
            case RIGHT -> {
                overlay.setWidth((float) (overlaySize.getWidth() + dragX));
                overlay.setPosition(
                        OverlayPosition.getBestPositionFor(overlay, renderX, renderY, (float) 0, (float) 0));
            }
            case BOTTOM -> {
                overlay.setHeight((float) (overlaySize.getHeight() + dragY));
                overlay.setPosition(
                        OverlayPosition.getBestPositionFor(overlay, renderX, renderY, (float) 0, (float) 0));
            }
        }
    }

    private void handleOverlayBodyDrag(double dragX, double dragY) {
        if (selectedOverlay == null || selectedOverlay.isPlacementLocked()) {
            return;
        }

        Pair<Double, Double> newDrag = calculateDragAfterSnapping(dragX, dragY);
        dragX = newDrag.a();
        dragY = newDrag.b();

        Overlay overlay = selectedOverlay;

        overlay.setPosition(OverlayPosition.getBestPositionFor(
                overlay, overlay.getRenderX(), overlay.getRenderY(), (float) dragX, (float) dragY));
    }

    private void handleOverlayCornerDrag(double dragX, double dragY) {
        if (selectedCorner == null || selectedOverlay == null || selectedOverlay.isPlacementLocked()) {
            return;
        }

        Pair<Double, Double> newDrag = calculateDragAfterSnapping(dragX, dragY);
        dragX = newDrag.a();
        dragY = newDrag.b();

        Overlay overlay = selectedOverlay;
        Corner corner = selectedCorner;

        OverlaySize overlaySize = overlay.getSize();

        final float renderX = overlay.getRenderX();
        final float renderY = overlay.getRenderY();

        switch (corner) {
            case TOP_LEFT -> {
                overlay.setWidth((float) (overlaySize.getWidth() - dragX));
                overlay.setHeight((float) (overlaySize.getHeight() - dragY));
                overlay.setPosition(
                        OverlayPosition.getBestPositionFor(overlay, renderX, renderY, (float) dragX, (float) dragY));
            }
            case TOP_RIGHT -> {
                overlay.setWidth((float) (overlaySize.getWidth() + dragX));
                overlay.setHeight((float) (overlaySize.getHeight() - dragY));
                overlay.setPosition(
                        OverlayPosition.getBestPositionFor(overlay, renderX, renderY, (float) 0, (float) dragY));
            }
            case BOTTOM_LEFT -> {
                overlay.setWidth((float) (overlaySize.getWidth() - dragX));
                overlay.setHeight((float) (overlaySize.getHeight() + dragY));
                overlay.setPosition(
                        OverlayPosition.getBestPositionFor(overlay, renderX, renderY, (float) dragX, (float) 0));
            }
            case BOTTOM_RIGHT -> {
                overlay.setWidth((float) (overlaySize.getWidth() + dragX));
                overlay.setHeight((float) (overlaySize.getHeight() + dragY));
                overlay.setPosition(
                        OverlayPosition.getBestPositionFor(overlay, renderX, renderY, (float) 0, (float) 0));
            }
        }
    }

    // Pair<dragX, dragY>
    private Pair<Double, Double> calculateDragAfterSnapping(double dragX, double dragY) {
        if (KeyboardUtils.isShiftDown()) {
            horizontalSnap.reset();
            verticalSnap.reset();
            return new Pair<>(dragX, dragY);
        }

        List<Edge> edges =
                switch (selectionMode) {
                    case NONE -> List.of();
                    case CORNER -> selectedCorner.getEdges();
                    case EDGE -> List.of(selectedEdge);
                    case AREA -> Arrays.stream(Edge.values()).toList();
                };

        double minX = Double.NEGATIVE_INFINITY;
        double maxX = Double.POSITIVE_INFINITY;
        double minY = Double.NEGATIVE_INFINITY;
        double maxY = Double.POSITIVE_INFINITY;
        if (selectionMode != SelectionMode.AREA) {
            if (edges.contains(Edge.LEFT)) {
                maxX = selectedOverlay.getWidth() - OverlaySize.MINIMUM_WIDTH;
            }
            if (edges.contains(Edge.RIGHT)) {
                minX = OverlaySize.MINIMUM_WIDTH - selectedOverlay.getWidth();
            }
            if (edges.contains(Edge.TOP)) {
                maxY = selectedOverlay.getHeight() - OverlaySize.MINIMUM_HEIGHT;
            }
            if (edges.contains(Edge.BOTTOM)) {
                minY = OverlaySize.MINIMUM_HEIGHT - selectedOverlay.getHeight();
            }
        }

        double[] horizontalEdges = edges.stream()
                .filter(Edge::isVerticalLine)
                .mapToDouble(edge -> edge.getEdgePos(selectedOverlay).a().x)
                .toArray();
        double[] verticalEdges = edges.stream()
                .filter(edge -> !edge.isVerticalLine())
                .mapToDouble(edge -> edge.getEdgePos(selectedOverlay).a().y)
                .toArray();
        boolean screenOnly = KeyboardUtils.isControlDown();
        return new Pair<>(
                horizontalSnap.snap(
                        dragX,
                        horizontalEdges,
                        verticalAlignmentLinePositions,
                        selectionMode == SelectionMode.AREA
                                ? (double) (selectedOverlay.getRenderX() + selectedOverlay.getWidth() / 2f)
                                : null,
                        verticalCenterLinePositions,
                        screenOnly,
                        minX,
                        maxX),
                verticalSnap.snap(
                        dragY,
                        verticalEdges,
                        horizontalAlignmentLinePositions,
                        selectionMode == SelectionMode.AREA
                                ? (double) (selectedOverlay.getRenderY() + selectedOverlay.getHeight() / 2f)
                                : null,
                        horizontalCenterLinePositions,
                        screenOnly,
                        minY,
                        maxY));
    }

    private void renderScreenGuides(GuiGraphics guiGraphics) {
        verticalScreenGuides.forEach((x, color) -> RenderUtils.drawLine(guiGraphics, color, x, 0, x, this.height, 1));
        horizontalScreenGuides.forEach((y, color) -> RenderUtils.drawLine(guiGraphics, color, 0, y, this.width, y, 1));
    }

    private void renderAlignmentLines(GuiGraphics guiGraphics) {
        SnapTarget horizontalTarget = horizontalSnap.getTarget();
        if (horizontalTarget != null) {
            float x = horizontalTarget.position();
            RenderUtils.drawLine(
                    guiGraphics,
                    horizontalTarget.screen() ? CommonColors.GREEN : CommonColors.ORANGE,
                    x,
                    0,
                    x,
                    this.height,
                    1);
        }
        SnapTarget verticalTarget = verticalSnap.getTarget();
        if (verticalTarget != null) {
            float y = verticalTarget.position();
            RenderUtils.drawLine(
                    guiGraphics,
                    verticalTarget.screen() ? CommonColors.GREEN : CommonColors.ORANGE,
                    0,
                    y,
                    this.width,
                    y,
                    1);
        }
    }

    private void calculateAlignmentLinePositions() {
        verticalAlignmentLinePositions.clear();
        horizontalAlignmentLinePositions.clear();
        verticalCenterLinePositions.clear();
        horizontalCenterLinePositions.clear();

        verticalScreenGuides.clear();
        horizontalScreenGuides.clear();
        CustomColor minorGuideColor = CommonColors.WHITE.withAlpha(0.25f);
        for (int i = 2; i <= ALIGNMENT_LINES_MAX_SECTIONS_PER_AXIS; i++) {
            if (i == 3) continue;
            for (int j = 1; j < i; j++) {
                verticalScreenGuides.put((float) this.width * j / i, minorGuideColor);
                horizontalScreenGuides.put((float) this.height * j / i, minorGuideColor);
            }
        }

        // Section boundaries take precedence where a minor guide shares their position.
        for (SectionCoordinates section : Managers.Overlay.getSections()) {
            verticalScreenGuides.put((float) section.x1(), CommonColors.WHITE);
            verticalScreenGuides.put((float) section.x2(), CommonColors.WHITE);
            horizontalScreenGuides.put((float) section.y1(), CommonColors.WHITE);
            horizontalScreenGuides.put((float) section.y2(), CommonColors.WHITE);
        }

        // Rendering and snapping consume the exact same screen coordinates.
        verticalScreenGuides.keySet().forEach(x -> verticalAlignmentLinePositions.add(new SnapTarget(x, true)));
        horizontalScreenGuides.keySet().forEach(y -> horizontalAlignmentLinePositions.add(new SnapTarget(y, true)));

        // Moving overlays can align their centers to any screen guide as well as their edges.
        verticalCenterLinePositions.addAll(verticalAlignmentLinePositions);
        horizontalCenterLinePositions.addAll(horizontalAlignmentLinePositions);

        for (Overlay overlay : Managers.Overlay.getOverlays().stream()
                .filter(Managers.Overlay::isEnabled)
                .toList()) {
            if (overlay == selectedOverlay) continue;

            verticalCenterLinePositions.add(new SnapTarget(overlay.getRenderX() + overlay.getWidth() / 2f, false));
            horizontalCenterLinePositions.add(new SnapTarget(overlay.getRenderY() + overlay.getHeight() / 2f, false));

            for (Edge edge : Edge.values()) {
                Pair<Vec2, Vec2> edgePos = edge.getEdgePos(overlay);

                if (edge.isVerticalLine()) {
                    verticalAlignmentLinePositions.add(new SnapTarget(edgePos.a().x, false));
                } else {
                    horizontalAlignmentLinePositions.add(new SnapTarget(edgePos.a().y, false));
                }
            }
        }
    }

    private void setupButtons() {
        // Remove previous buttons
        this.children.stream().toList().forEach(this::removeWidget);

        // Determine if buttons should be at the top or bottom of the screen
        int yPos = buttonsAtBottom ? this.height - 25 : 5;
        int historyY = buttonsAtBottom ? yPos - 24 : yPos + 24;
        undoButton = this.addRenderableWidget(Button.builder(
                        Component.translatable("screens.wynntils.overlayManagement.undo"),
                        button -> restoreHistory(false))
                .bounds(this.width / 2 - BUTTON_WIDTH - 2, historyY, BUTTON_WIDTH, BUTTON_HEIGHT)
                .tooltip(Tooltip.create(Component.translatable("screens.wynntils.overlayManagement.undoTooltip")))
                .build());
        redoButton = this.addRenderableWidget(Button.builder(
                        Component.translatable("screens.wynntils.overlayManagement.redo"),
                        button -> restoreHistory(true))
                .bounds(this.width / 2 + 2, historyY, BUTTON_WIDTH, BUTTON_HEIGHT)
                .tooltip(Tooltip.create(Component.translatable("screens.wynntils.overlayManagement.redoTooltip")))
                .build());
        updateHistoryButtons();

        this.addRenderableWidget(new WynntilsCheckbox(
                this.width / 2 - BUTTON_WIDTH - 23 - 100,
                yPos,
                BUTTON_HEIGHT,
                Component.translatable("screens.wynntils.overlayManagement.showPreview"),
                showPreview,
                80,
                (c, b) -> showPreview = !showPreview,
                ComponentUtils.wrapTooltips(
                        List.of(Component.translatable("screens.wynntils.overlayManagement.showPreviewTooltip")),
                        150)));

        this.addRenderableWidget(new Button.Builder(
                        Component.translatable("screens.wynntils.overlayManagement.close"), button -> {
                            onClose();
                            McUtils.setScreen(previousScreen);
                        })
                .pos(this.width / 2 - BUTTON_WIDTH - 23, yPos)
                .size(BUTTON_WIDTH, BUTTON_HEIGHT)
                .tooltip(Tooltip.create(Component.translatable("screens.wynntils.overlayManagement.closeTooltip")))
                .build());

        this.addRenderableWidget(new Button.Builder(
                        buttonsAtBottom ? Component.literal("🠝") : Component.literal("🠟"), button -> {
                            buttonsAtBottom = !buttonsAtBottom;
                            setupButtons();
                        })
                .pos(this.width / 2 - 21, yPos)
                .size(BUTTON_SHORT_WIDTH, BUTTON_HEIGHT)
                .tooltip(Tooltip.create(
                        buttonsAtBottom
                                ? Component.translatable("screens.wynntils.overlayManagement.moveButtonsUpTooltip")
                                : Component.translatable("screens.wynntils.overlayManagement.moveButtonsDownTooltip")))
                .build());

        this.addRenderableWidget(new Button.Builder(Component.literal("☰"), button -> {
                    Managers.Config.saveConfig();
                    onClose();
                    McUtils.setScreen(OverlayOrderingScreen.create(this));
                })
                .pos(this.width / 2 + 1, yPos)
                .size(BUTTON_SHORT_WIDTH, BUTTON_HEIGHT)
                .tooltip(Tooltip.create(Component.translatable("screens.wynntils.overlayManagement.orderTooltip")))
                .build());

        this.addRenderableWidget(new Button.Builder(
                        Component.translatable("screens.wynntils.overlayManagement.apply"), button -> {
                            Managers.Config.saveConfig();
                            onClose();
                            McUtils.setScreen(previousScreen);
                        })
                .pos(this.width / 2 + 23, yPos)
                .size(BUTTON_WIDTH, BUTTON_HEIGHT)
                .tooltip(Tooltip.create(Component.translatable("screens.wynntils.overlayManagement.applyTooltip")))
                .build());

        if (selectedOverlay != null) {
            this.addRenderableWidget(new WynntilsCheckbox(
                    this.width / 2 + 23 + BUTTON_WIDTH + 10,
                    yPos,
                    BUTTON_HEIGHT,
                    Component.translatable("screens.wynntils.overlayManagement.showOthers"),
                    renderAllOverlays,
                    120,
                    (c, b) -> renderAllOverlays = b,
                    ComponentUtils.wrapTooltips(
                            List.of(Component.translatable("screens.wynntils.overlayManagement.showOthersTooltip")),
                            150)));
        }
    }

    private void resetSelection() {
        if (positionPanel == null) {
            editHistory.finish();
        }
        updateHistoryButtons();
        horizontalSnap.reset();
        verticalSnap.reset();
        pendingPanelClick = false;
        selectionMode = SelectionMode.NONE;
        selectedCorner = null;
        selectedEdge = null;
    }

    private void togglePlacementLock(boolean locked) {
        if (selectedOverlay == null || selectedOverlay.isPlacementLocked() == locked) return;

        // Keep the layout edits made before clicking the lock button as their own history step.
        editHistory.finish();
        editHistory.begin(selectedOverlay);
        selectedOverlay.setPlacementLocked(locked);
        editHistory.finish();

        if (locked) {
            setFocusedTextInput(null);
            setFocused(null);
        }
        if (positionPanel != null) {
            positionPanel.setPlacementLocked(locked);
            editHistory.begin(selectedOverlay);
        }
        updateHistoryButtons();
        calculateAlignmentLinePositions();
    }

    private void restoreHistory(boolean redo) {
        closePositionPanel();
        resetSelection();
        Overlay restored = redo ? editHistory.redo() : editHistory.undo();
        if (restored != null) {
            selectedOverlay = restored;
            userInteracted = true;
            animationLengthRemaining = 0;
            setupButtons();
            calculateAlignmentLinePositions();
        }
        updateHistoryButtons();
    }

    private void updateHistoryButtons() {
        if (undoButton != null) {
            undoButton.active = editHistory.canUndo();
        }
        if (redoButton != null) {
            redoButton.active = editHistory.canRedo();
        }
    }

    private Overlay getHoveredHelpTooltip(double mouseX, double mouseY) {
        if (positionPanel != null && positionPanel.contains(mouseX, mouseY)) return null;
        for (GuiEventListener listener : this.children) {
            if (listener.isMouseOver(mouseX, mouseY)) return null;
        }
        if (selectedOverlay == null || selectionMode != SelectionMode.NONE) {
            return null;
        }

        Set<Overlay> overlays = Managers.Overlay.getOverlays().stream()
                .filter(Managers.Overlay::isEnabled)
                .collect(Collectors.toSet());
        for (Overlay overlay : overlays) {
            if (!renderAllOverlays && overlay != selectedOverlay) continue;
            if (!isMouseHoveringOverlay(overlay, mouseX, mouseY)) continue;

            if (overlay != selectedOverlay) {
                if (!fixedSelection && showPreview) return null;
                continue;
            }

            if (!overlay.isPlacementLocked()) return overlay;
            return null;
        }

        return null;
    }

    private void setHelpTooltipTarget(Overlay overlay) {
        if (helpTooltipTarget == overlay) return;

        helpTooltipTarget = overlay;
        helpTooltip.reset();
    }

    private void resetHelpTooltip() {
        helpTooltipTarget = null;
        helpTooltip.reset();
    }

    private void closePositionPanel() {
        if (positionPanel == null) return;

        positionPanel.getWidgets().forEach(this::removeWidget);
        positionPanel = null;
        setFocusedTextInput(null);
        setFocused(null);
        editHistory.finish();
        updateHistoryButtons();
        calculateAlignmentLinePositions();
    }

    private enum SelectionMode {
        NONE,
        CORNER,
        EDGE,
        AREA
    }
}
