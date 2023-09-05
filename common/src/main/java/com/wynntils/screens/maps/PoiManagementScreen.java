/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.maps;

import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Services;
import com.wynntils.core.consumers.screens.WynntilsScreen;
import com.wynntils.core.persisted.config.HiddenConfig;
import com.wynntils.core.text.StyledText;
import com.wynntils.features.map.MainMapFeature;
import com.wynntils.screens.base.TextboxScreen;
import com.wynntils.screens.base.widgets.BasicTexturedButton;
import com.wynntils.screens.base.widgets.TextInputBoxWidget;
import com.wynntils.screens.maps.widgets.PoiManagerWidget;
import com.wynntils.services.map.pois.CustomPoi;
import com.wynntils.utils.MathUtils;
import com.wynntils.utils.StringUtils;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;

public final class PoiManagementScreen extends WynntilsScreen implements TextboxScreen {
    private static final float GRID_DIVISIONS = 64.0f;
    private static final int GRID_ROWS_PER_PAGE = 44;
    private static final int HEADER_HEIGHT = 12;
    private static final int STARTING_WIDGET_ROW = 13;

    private final List<AbstractWidget> poiManagerWidgets = new ArrayList<>();
    private final List<CustomPoi> deletedPois = new ArrayList<>();
    private final List<Integer> deletedIndexes = new ArrayList<>();
    private final List<Texture> iconFilters = new ArrayList<>();
    private final MainMapScreen oldMapScreen;

    private Button filterAllButton;
    private Button undoDeleteButton;
    private boolean draggingScroll = false;
    private double lastMouseY = 0;
    private double mouseDrag = 0;
    private float backgroundHeight;
    private float backgroundWidth;
    private float backgroundX;
    private float backgroundY;
    private float currentTextureScale = 1f;
    private float dividedHeight;
    private float dividedWidth;
    private float scrollButtonHeight;
    private float scrollButtonRenderY;
    private int bottomDisplayedIndex;
    private int maxPoisToDisplay;
    private int scrollOffset = 0;
    private int topDisplayedIndex = 0;
    private List<CustomPoi> waypoints;
    private List<Texture> usedIcons = new ArrayList<>();
    private TextInputBoxWidget focusedTextInput;
    private TextInputBoxWidget searchInput;
    private boolean renderGrid = false;

    private void toggleGrid() {
        renderGrid = !renderGrid;
    }

    private PoiManagementScreen(MainMapScreen oldMapScreen) {
        super(Component.literal("Poi Management Screen"));

        this.oldMapScreen = oldMapScreen;
    }

    public static Screen create() {
        return new PoiManagementScreen(null);
    }

    public static Screen create(MainMapScreen oldMapScreen) {
        return new PoiManagementScreen(oldMapScreen);
    }

    @Override
    public void onClose() {
        McUtils.mc().setScreen(oldMapScreen);
    }

    @Override
    protected void doInit() {
        dividedWidth = this.width / GRID_DIVISIONS;
        dividedHeight = this.height / GRID_DIVISIONS;
        maxPoisToDisplay = (int) (dividedHeight * GRID_ROWS_PER_PAGE) / 20;
        backgroundX = dividedWidth * 12;
        backgroundWidth = dividedWidth * 43;
        backgroundY = dividedHeight * 7;
        backgroundHeight = dividedHeight * 50;
        currentTextureScale = (float) this.height / Texture.SCROLL_BUTTON.height();

        // If keeping this texture, move to ui_components
        scrollButtonHeight = (dividedWidth / Texture.SCROLL_BUTTON.width()) * Texture.SCROLL_BUTTON.height();

        int importExportButtonWidth = (int) (dividedWidth * 6);

        this.addRenderableWidget(
                new Button.Builder(Component.literal("X").withStyle(ChatFormatting.RED), (button) -> this.onClose())
                        .pos((int) (dividedWidth * 60), (int) (dividedHeight * 4))
                        .size(20, 20)
                        .build());

        this.addRenderableWidget(
                new Button.Builder(Component.literal("Grid").withStyle(ChatFormatting.BLUE), (button) -> toggleGrid())
                        .pos((int) (dividedWidth * 2), (int) (dividedHeight * 4))
                        .size(40, 20)
                        .build());

        this.addRenderableWidget(new Button.Builder(
                        Component.translatable("screens.wynntils.poiManagementGui.import"),
                        (button) -> importFromClipboard())
                .pos((int) (dividedWidth * 22), (int) (dividedHeight * 58))
                .size(importExportButtonWidth, 20)
                .tooltip(Tooltip.create(Component.translatable("screens.wynntils.poiManagementGui.import.tooltip")))
                .build());

        this.addRenderableWidget(new Button.Builder(
                        Component.translatable("screens.wynntils.poiManagementGui.export"),
                        (button) -> exportToClipboard())
                .pos((int) (dividedWidth * 36), (int) (dividedHeight * 58))
                .size(importExportButtonWidth, 20)
                .tooltip(Tooltip.create(Component.translatable("screens.wynntils.poiManagementGui.export.tooltip")))
                .build());

        undoDeleteButton = new Button.Builder(
                        Component.translatable("screens.wynntils.poiManagementGui.undo"), (button) -> undoDelete())
                .pos((int) (dividedWidth * 53), (int) (dividedHeight * 58))
                .size((int) (dividedWidth * 8), 20)
                .build();

        this.addRenderableWidget(undoDeleteButton);

        searchInput = new TextInputBoxWidget(
                (int) (dividedWidth * 14) - 5,
                (int) (dividedHeight * 3),
                (int) (dividedWidth * 18),
                20,
                (s) -> {
                    topDisplayedIndex = 0;
                    populatePois();
                },
                this);

        this.addRenderableWidget(searchInput);

        setFocusedTextInput(searchInput);

        filterAllButton = this.addRenderableWidget(
                new Button.Builder(Component.literal("*").withStyle(ChatFormatting.GREEN), (button) -> iconFilters.clear())
                        .pos((int) (dividedWidth * 32) + 5, (int) (dividedHeight * 3))
                        .size(20, 20)
                        .build());

        this.addRenderableWidget(filterAllButton);

        if (deletedIndexes.isEmpty()) {
            undoDeleteButton.active = false;
        }

        waypoints = Managers.Feature.getFeatureInstance(MainMapFeature.class)
                .customPois
                .get();

        bottomDisplayedIndex = Math.min(maxPoisToDisplay, waypoints.size() - 1);

        populatePois();
    }

    @Override
    public void doRender(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        renderBackground(poseStack);
        renderScrollButton(poseStack);
        super.doRender(poseStack, mouseX, mouseY, partialTick);

        if (renderGrid) {
            RenderUtils.renderDebugGrid(poseStack, GRID_DIVISIONS, dividedWidth, dividedHeight);
        }

        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        StyledText.fromComponent(Component.translatable("screens.wynntils.poiManagementGui.search")),
                        (int) (dividedWidth * 14) - 5,
                        (int) dividedHeight,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.TOP,
                        TextShadow.NORMAL);

        if (usedIcons.size() > 1) {
            FontRenderer.getInstance()
                    .renderText(
                            poseStack,
                            StyledText.fromComponent(Component.translatable("screens.wynntils.poiManagementGui.filter")),
                            (int) (dividedWidth * 32) + 5,
                            (int) dividedHeight,
                            CommonColors.WHITE,
                            HorizontalAlignment.LEFT,
                            VerticalAlignment.TOP,
                            TextShadow.NORMAL);
        }

        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        StyledText.fromComponent(Component.translatable("screens.wynntils.poiManagementGui.icon")),
                        (int) (dividedWidth * 15),
                        (int) (dividedHeight * HEADER_HEIGHT),
                        CommonColors.WHITE,
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.BOTTOM,
                        TextShadow.NORMAL);

        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        StyledText.fromComponent(Component.translatable("screens.wynntils.poiManagementGui.name")),
                        (int) (dividedWidth * 24),
                        (int) (dividedHeight * HEADER_HEIGHT),
                        CommonColors.WHITE,
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.BOTTOM,
                        TextShadow.NORMAL);

        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        StyledText.fromComponent(Component.literal("X")),
                        (int) (dividedWidth * 34),
                        (int) (dividedHeight * HEADER_HEIGHT),
                        CommonColors.WHITE,
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.BOTTOM,
                        TextShadow.NORMAL);

        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        StyledText.fromComponent(Component.literal("Y")),
                        (int) (dividedWidth * 37),
                        (int) (dividedHeight * HEADER_HEIGHT),
                        CommonColors.WHITE,
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.BOTTOM,
                        TextShadow.NORMAL);

        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        StyledText.fromComponent(Component.literal("Z")),
                        (int) (dividedWidth * 40),
                        (int) (dividedHeight * HEADER_HEIGHT),
                        CommonColors.WHITE,
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.BOTTOM,
                        TextShadow.NORMAL);

        RenderUtils.drawRect(
                poseStack,
                CommonColors.WHITE,
                (int) (dividedWidth * 14),
                (int) (dividedHeight * HEADER_HEIGHT),
                0,
                (int) (dividedWidth * 38),
                1);
    }

    @Override
    public void renderBackground(PoseStack poseStack) {
        super.renderBackground(poseStack);

        RenderUtils.drawScalingTexturedRect(
                poseStack,
                Texture.WAYPOINT_MANAGER_BACKGROUND.resource(),
                backgroundX,
                backgroundY,
                0,
                backgroundWidth,
                backgroundHeight,
                Texture.WAYPOINT_MANAGER_BACKGROUND.width(),
                Texture.WAYPOINT_MANAGER_BACKGROUND.height());
    }

    private void renderScrollButton(PoseStack poseStack) {
        scrollButtonRenderY = MathUtils.map(
                scrollOffset,
                0,
                waypoints.size() - maxPoisToDisplay,
                (int) (dividedHeight * 10),
                (int) (dividedHeight * 51));

        RenderUtils.drawScalingTexturedRect(
                poseStack,
                Texture.SCROLL_BUTTON.resource(),
                (int) (dividedWidth * 53),
                scrollButtonRenderY,
                0,
                dividedWidth,
                scrollButtonHeight,
                Texture.SCROLL_BUTTON.width(),
                Texture.SCROLL_BUTTON.height());
    }

    private void setScrollOffset(int delta) {
        scrollOffset =
                MathUtils.clamp(scrollOffset - delta, 0, Math.max(0, waypoints.size() - maxPoisToDisplay));

        populatePois();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        setScrollOffset((int) delta);

        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (!draggingScroll) return false;

        int newValue = (int) MathUtils.map(
                (float) mouseY, (int) (dividedHeight * 10), (int) (dividedHeight * 53), 0, Math.max(0, waypoints.size() - maxPoisToDisplay));

        setScrollOffset(-newValue + scrollOffset);

        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean doMouseClicked(double mouseX, double mouseY, int button) {
        for (GuiEventListener child : children()) {
            if (child.isMouseOver(mouseX, mouseY) && child != searchInput) {
                child.mouseClicked(mouseX, mouseY, button);
                return true;
            }
        }

        if (!draggingScroll) {
            float scrollButtonRenderX = (int) dividedWidth * 53;

            if (mouseX >= scrollButtonRenderX
                    && mouseX <= scrollButtonRenderX + Texture.SCROLL_BUTTON.width() * currentTextureScale
                    && mouseY >= scrollButtonRenderY
                    && mouseY <= scrollButtonRenderY + Texture.SCROLL_BUTTON.height() * currentTextureScale) {
                draggingScroll = true;
                lastMouseY = mouseY;
            }
        }

        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        draggingScroll = false;
        return true;
    }

    public void populatePois() {
        for (AbstractWidget widget : poiManagerWidgets) {
            this.removeWidget(widget);
        }

        this.poiManagerWidgets.clear();

        waypoints = Managers.Feature.getFeatureInstance(MainMapFeature.class).customPois.get().stream()
                .filter(poi -> searchMatches(poi.getName()))
                .collect(Collectors.toList());

        if (!iconFilters.isEmpty()) {
            waypoints = Managers.Feature.getFeatureInstance(MainMapFeature.class).customPois.get().stream()
                    .filter(poi -> iconFilters.contains(poi.getIcon()))
                    .collect(Collectors.toList());
        }

        int row = (int) ((int) (dividedHeight * (STARTING_WIDGET_ROW - 1)) + (dividedHeight / 2f));

        for (int i = 0; i < maxPoisToDisplay; i++) {
            if ((topDisplayedIndex + i) > waypoints.size() - 1) {
                break;
            }

            bottomDisplayedIndex = (topDisplayedIndex + i) + scrollOffset;

            CustomPoi poi = waypoints.get(bottomDisplayedIndex);

            PoiManagerWidget poiWidget =
                    new PoiManagerWidget((int) (dividedWidth * 14), row, (int) (dividedWidth * 38), 20, poi, this, dividedWidth);

            row += 20;

            poiManagerWidgets.add(poiWidget);

            this.addRenderableWidget(poiWidget);
        }

        createIconFilterButtons();
    }

    private void createIconFilterButtons() {
        usedIcons = Services.Poi.POI_ICONS.stream()
                .filter(texture -> waypoints.stream()
                        .map(CustomPoi::getIcon)
                        .anyMatch(customPoiTexture -> texture == customPoiTexture))
                .toList();

        filterAllButton.active = true;

        int xOffset = 22;

        for (Texture icon : usedIcons) {
            this.addRenderableWidget(new BasicTexturedButton(
                    (int) (dividedWidth * 32) + xOffset + 5,
                    (int) (dividedHeight * 3 + 10 - icon.height() / 2f),
                    20,
                    20,
                    icon,
                    (b) -> {
                        topDisplayedIndex = 0;
                        toggleIcon(icon);
                        populatePois();
                    },
                    List.of(Component.literal("Filter your waypoints to include this icon."))
            ));

            xOffset += 22;
        }
    }

    private boolean searchMatches(String poiName) {
        return StringUtils.partialMatch(poiName, searchInput.getTextBoxInput());
    }

    private void toggleIcon(Texture icon) {
        if (iconFilters.contains(icon)) {
            iconFilters.remove(icon);
        } else {
            iconFilters.add(icon);
        }
    }

    private void importFromClipboard() {
        String clipboard = McUtils.mc().keyboardHandler.getClipboard();

        CustomPoi[] customPois;
        try {
            customPois = Managers.Json.GSON.fromJson(clipboard, CustomPoi[].class);
        } catch (JsonSyntaxException e) {
            McUtils.sendErrorToClient(I18n.get("screens.wynntils.poiManagementGui.import.error"));
            return;
        }

        if (customPois == null) {
            McUtils.sendErrorToClient(I18n.get("screens.wynntils.poiManagementGui.import.error"));
            return;
        }

        HiddenConfig<List<CustomPoi>> customPoiConfig =
                Managers.Feature.getFeatureInstance(MainMapFeature.class).customPois;
        List<CustomPoi> existingPois = customPoiConfig.get();

        List<CustomPoi> poisToAdd = Stream.of(customPois)
                .filter(newPoi -> !existingPois.contains(newPoi))
                .toList();

        existingPois.addAll(poisToAdd);
        customPoiConfig.touched();

        populatePois();

        McUtils.sendMessageToClient(
                Component.translatable("screens.wynntils.poiManagementGui.import.success", poisToAdd.size())
                        .withStyle(ChatFormatting.GREEN));
    }

    private void exportToClipboard() {
        McUtils.mc()
                .keyboardHandler
                .setClipboard(Managers.Feature.getFeatureInstance(MainMapFeature.class).customPois.get().stream()
                        .map(Managers.Json.GSON::toJson)
                        .toList()
                        .toString());

        McUtils.sendMessageToClient(Component.translatable(
                        "screens.wynntils.poiManagementGui.exportedWaypoints",
                        Managers.Feature.getFeatureInstance(MainMapFeature.class)
                                .customPois
                                .get()
                                .size())
                .withStyle(ChatFormatting.GREEN));
    }

    public void setLastDeletedPoi(CustomPoi deletedPoi, int deletedPoiIndex) {
        deletedPois.add(deletedPoi);
        deletedIndexes.add(deletedPoiIndex);

        undoDeleteButton.active = true;
    }

    private void undoDelete() {
        Managers.Feature.getFeatureInstance(MainMapFeature.class)
                .customPois
                .get()
                .add(deletedIndexes.get(deletedIndexes.size() - 1), deletedPois.get(deletedPois.size() - 1));

        deletedIndexes.remove(deletedIndexes.size() - 1);
        deletedPois.remove(deletedPois.size() - 1);

        if (deletedIndexes.isEmpty()) {
            undoDeleteButton.active = false;
        }

        populatePois();
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        return (focusedTextInput != null && focusedTextInput.charTyped(codePoint, modifiers))
                || super.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return (focusedTextInput != null && focusedTextInput.keyPressed(keyCode, scanCode, modifiers))
                || super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public TextInputBoxWidget getFocusedTextInput() {
        return focusedTextInput;
    }

    @Override
    public void setFocusedTextInput(TextInputBoxWidget focusedTextInput) {
        this.focusedTextInput = focusedTextInput;
    }
}
