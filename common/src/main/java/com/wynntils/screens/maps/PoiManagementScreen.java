/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.maps;

import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.components.Managers;
import com.wynntils.core.consumers.screens.WynntilsScreen;
import com.wynntils.core.persisted.config.HiddenConfig;
import com.wynntils.core.text.StyledText;
import com.wynntils.features.map.MainMapFeature;
import com.wynntils.screens.base.TextboxScreen;
import com.wynntils.screens.base.widgets.TextInputBoxWidget;
import com.wynntils.screens.maps.widgets.PoiFilterWidget;
import com.wynntils.screens.maps.widgets.PoiManagerWidget;
import com.wynntils.screens.maps.widgets.PoiSortButton;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
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
    private static final int GRID_ROWS_PER_PAGE = 43;
    private static final int HEADER_HEIGHT = 12;
    private static final int ICONS_PER_ROW = 9;
    private static final int MAX_ICONS_TO_DISPLAY = 45;

    private Map<Texture, Boolean> filteredIcons = new EnumMap<>(Texture.class);
    private final List<AbstractWidget> iconFilterWidgets = new ArrayList<>();
    private final List<AbstractWidget> poiManagerWidgets = new ArrayList<>();
    private final List<CustomPoi> deletedPois = new ArrayList<>();
    private final List<Integer> deletedIndexes = new ArrayList<>();
    private final MainMapScreen oldMapScreen;

    private boolean draggingScroll = false;
    private boolean filterMode = false;
    private boolean selectionMode = false;
    private Button deleteSelectedButton;
    private Button deselectAllButton;
    private Button importButton;
    private Button downButton;
    private Button exportButton;
    private Button filterButton;
    private Button filterAllButton;
    private Button selectAllButton;
    private Button undoDeleteButton;
    private Button upButton;
    private float backgroundHeight;
    private float backgroundWidth;
    private float backgroundX;
    private float backgroundY;
    private float dividedHeight;
    private float dividedWidth;
    private float scrollButtonHeight;
    private float scrollButtonRenderY;
    private int bottomDisplayedIndex;
    private int iconButtonSize;
    private int maxPoisToDisplay;
    private int scrollOffset = 0;
    private List<CustomPoi> selectedWaypoints = new ArrayList<>();
    private List<CustomPoi> waypoints;
    private List<Texture> usedIcons;
    private PoiSortButton activeSortButton;
    private PoiSortButton nameSortButton;
    private PoiSortButton xSortButton;
    private PoiSortButton ySortButton;
    private PoiSortButton zSortButton;
    private PoiSortOrder sortOrder;
    private TextInputBoxWidget focusedTextInput;
    private TextInputBoxWidget searchInput;

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
        iconButtonSize = (int) (dividedWidth * 4);
        maxPoisToDisplay = (int) (dividedHeight * GRID_ROWS_PER_PAGE) / 20;
        backgroundX = dividedWidth * 10;
        backgroundWidth = dividedWidth * 44;
        backgroundY = dividedHeight * 7;
        backgroundHeight = dividedHeight * 50;

        scrollButtonHeight = (dividedWidth / Texture.SCROLL_BUTTON.width()) * Texture.SCROLL_BUTTON.height();

        int importExportButtonWidth = (int) (dividedWidth * 6);

        this.addRenderableWidget(
                new Button.Builder(Component.literal("X").withStyle(ChatFormatting.RED), (button) -> this.onClose())
                        .pos((int) (dividedWidth * 60), (int) (dividedHeight * 4))
                        .size(20, 20)
                        .build());

        // region import/export
        importButton = new Button.Builder(
                        Component.translatable("screens.wynntils.poiManagementGui.import"),
                        (button) -> importFromClipboard())
                .pos((int) (dividedWidth * 22), (int) (dividedHeight * 58))
                .size(importExportButtonWidth, 20)
                .tooltip(Tooltip.create(Component.translatable("screens.wynntils.poiManagementGui.import.tooltip")))
                .build();

        this.addRenderableWidget(importButton);

        exportButton = new Button.Builder(
                        Component.translatable("screens.wynntils.poiManagementGui.export"),
                        (button) -> exportToClipboard())
                .pos((int) (dividedWidth * 36), (int) (dividedHeight * 58))
                .size(importExportButtonWidth, 20)
                .tooltip(Tooltip.create(Component.translatable("screens.wynntils.poiManagementGui.exportAll.tooltip")))
                .build();

        this.addRenderableWidget(exportButton);
        // endregion

        // region delete buttons
        undoDeleteButton = new Button.Builder(
                        Component.translatable("screens.wynntils.poiManagementGui.undo"), (button) -> undoDelete())
                .pos((int) (dividedWidth * 55), (int) (dividedHeight * 58))
                .size((int) (dividedWidth * 8), 20)
                .build();

        this.addRenderableWidget(undoDeleteButton);

        deleteSelectedButton = new Button.Builder(
                        Component.translatable("screens.wynntils.poiManagementGui.deleteSelected"),
                        (button) -> deleteSelectedPois())
                .pos((int) (dividedWidth * 55), (int) (dividedHeight * 58) - 25)
                .size((int) (dividedWidth * 8), 20)
                .build();

        deleteSelectedButton.active = false;

        this.addRenderableWidget(deleteSelectedButton);
        // endregion

        // region select buttons
        deselectAllButton = new Button.Builder(
                        Component.translatable("screens.wynntils.poiManagementGui.deselectAll"),
                        (button) -> toggleSelectAll(false))
                .pos((int) dividedWidth, (int) (dividedHeight * 58))
                .size((int) (dividedWidth * 8), 20)
                .build();

        deselectAllButton.active = false;

        this.addRenderableWidget(deselectAllButton);

        selectAllButton = new Button.Builder(
                        Component.translatable("screens.wynntils.poiManagementGui.selectAll"),
                        (button) -> toggleSelectAll(true))
                .pos((int) dividedWidth, (int) (dividedHeight * 58) - 25)
                .size((int) (dividedWidth * 8), 20)
                .build();

        this.addRenderableWidget(selectAllButton);
        // endregion

        // region up/down buttons
        upButton = new Button.Builder(Component.literal("ʌ"), (button) -> updateSelectedPoiPositions(-1))
                .pos((width / 2) - 22, (int) (dividedHeight * 58))
                .size(20, 20)
                .build();

        upButton.visible = false;

        this.addRenderableWidget(upButton);

        downButton = new Button.Builder(Component.literal("v"), (button) -> updateSelectedPoiPositions(1))
                .pos((width / 2) + 2, (int) (dividedHeight * 58))
                .size(20, 20)
                .build();

        downButton.visible = false;

        this.addRenderableWidget(downButton);
        // endregion

        // region icon filter button
        int filterButtonWidth = (int) (dividedWidth * 10);

        filterButton = new Button.Builder(
                        Component.translatable("screens.wynntils.poiManagementGui.filter"),
                        (button) -> toggleIconFilter(!filterMode))
                .pos((int) (dividedWidth * 44), (int) (dividedHeight * 3))
                .size(filterButtonWidth, 20)
                .build();

        this.addRenderableWidget(filterButton);

        filterAllButton = new Button.Builder(
                        Component.translatable("screens.wynntils.poiManagementGui.filterAll"), (button) -> {
                            filteredIcons.replaceAll((key, value) -> false);
                            button.active = false;
                            populateIcons();
                        })
                .pos((int) backgroundX, (int) (dividedHeight * 3))
                .size(filterButtonWidth, 20)
                .tooltip(Tooltip.create(Component.translatable("screens.wynntils.poiManagementGui.filterAll.tooltip")))
                .build();

        filterAllButton.active = false;
        filterAllButton.visible = false;

        this.addRenderableWidget(filterAllButton);
        // endregion

        // region search bar
        searchInput = new TextInputBoxWidget(
                (int) (dividedWidth * 10) + 5,
                (int) (dividedHeight * 3),
                (int) (backgroundWidth - filterButtonWidth - 10),
                20,
                (s) -> {
                    scrollOffset = 0;
                    populatePois();
                },
                this,
                searchInput);

        this.addRenderableWidget(searchInput);

        setFocusedTextInput(searchInput);
        // endregion

        // region sort buttons
        int nameTitleWidth = (McUtils.mc()
                        .font
                        .width(StyledText.fromComponent(
                                        Component.translatable("screens.wynntils.poiManagementGui.name"))
                                .getString()))
                + 1;

        int coordinateTitleWidth = (McUtils.mc().font.width("X")) + 1;

        nameSortButton = this.addRenderableWidget(new PoiSortButton(
                (int) (dividedWidth * 22) - (nameTitleWidth / 2),
                (int) (dividedHeight * HEADER_HEIGHT) - 10,
                nameTitleWidth,
                10,
                Component.translatable("screens.wynntils.poiManagementGui.name"),
                this,
                PoiSortType.NAME));

        xSortButton = this.addRenderableWidget(new PoiSortButton(
                (int) (dividedWidth * 32) - (coordinateTitleWidth / 2),
                (int) (dividedHeight * HEADER_HEIGHT) - 10,
                coordinateTitleWidth,
                10,
                Component.literal("X"),
                this,
                PoiSortType.X));

        ySortButton = this.addRenderableWidget(new PoiSortButton(
                (int) (dividedWidth * 35) - (coordinateTitleWidth / 2),
                (int) (dividedHeight * HEADER_HEIGHT) - 10,
                coordinateTitleWidth,
                10,
                Component.literal("Y"),
                this,
                PoiSortType.Y));

        zSortButton = this.addRenderableWidget(new PoiSortButton(
                (int) (dividedWidth * 38) - (coordinateTitleWidth / 2),
                (int) (dividedHeight * HEADER_HEIGHT) - 10,
                coordinateTitleWidth,
                10,
                Component.literal("Z"),
                this,
                PoiSortType.Z));
        // endregion

        if (deletedIndexes.isEmpty()) {
            undoDeleteButton.active = false;
        }

        waypoints = Managers.Feature.getFeatureInstance(MainMapFeature.class)
                .customPois
                .get();

        if (waypoints.isEmpty()) {
            searchInput.visible = false;
            filterButton.visible = false;
            nameSortButton.visible = false;
            xSortButton.visible = false;
            ySortButton.visible = false;
            zSortButton.visible = false;
            exportButton.active = false;
        }

        updateAllUsedIcons();

        usedIcons = new ArrayList<>(filteredIcons.keySet());

        bottomDisplayedIndex = Math.min(maxPoisToDisplay, waypoints.size() - 1);

        populatePois();
    }

    @Override
    public void doRender(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        renderBackground(poseStack);
        renderScrollButton(poseStack);
        super.doRender(poseStack, mouseX, mouseY, partialTick);

        //        RenderUtils.renderDebugGrid(poseStack, GRID_DIVISIONS, dividedWidth, dividedHeight);

        if (Managers.Feature.getFeatureInstance(MainMapFeature.class)
                .customPois
                .get()
                .isEmpty()) {
            FontRenderer.getInstance()
                    .renderText(
                            poseStack,
                            StyledText.fromComponent(
                                    Component.translatable("screens.wynntils.poiManagementGui.noPois")),
                            (int) (dividedWidth * 32),
                            (int) (dividedHeight * 32),
                            CommonColors.WHITE,
                            HorizontalAlignment.CENTER,
                            VerticalAlignment.MIDDLE,
                            TextShadow.NORMAL);

            return;
        } else if (waypoints.isEmpty()) {
            FontRenderer.getInstance()
                    .renderText(
                            poseStack,
                            StyledText.fromComponent(
                                    Component.translatable("screens.wynntils.poiManagementGui.noFilteredPois")),
                            (int) (dividedWidth * 32),
                            (int) (dividedHeight * 32),
                            CommonColors.WHITE,
                            HorizontalAlignment.CENTER,
                            VerticalAlignment.MIDDLE,
                            TextShadow.NORMAL);
        } else if (filterMode) {
            return;
        }

        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        StyledText.fromComponent(Component.translatable("screens.wynntils.poiManagementGui.search")),
                        (int) (dividedWidth * 10) + 5,
                        (int) dividedHeight,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.TOP,
                        TextShadow.NORMAL);

        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        StyledText.fromComponent(Component.translatable("screens.wynntils.poiManagementGui.icon")),
                        (int) (dividedWidth * 13),
                        (int) (dividedHeight * HEADER_HEIGHT),
                        CommonColors.WHITE,
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.BOTTOM,
                        TextShadow.NORMAL);

        RenderUtils.drawRect(
                poseStack,
                CommonColors.WHITE,
                (int) (dividedWidth * 12),
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

    public void selectPoi(CustomPoi selectedPoi) {
        boolean add = true;
        boolean updateState = false;

        if (selectedWaypoints.contains(selectedPoi)) {
            selectedWaypoints.remove(selectedPoi);

            add = false;

            if (selectedWaypoints.isEmpty()) {
                updateState = true;
            }
        } else {
            selectedWaypoints.add(selectedPoi);

            updateState = true;
        }

        selectAllButton.active = selectedWaypoints.size() < waypoints.size();

        if (updateState) {
            selectionMode = add;

            deselectAllButton.active = add;
            deleteSelectedButton.active = add;

            upButton.visible = add;
            downButton.visible = add;

            Component tooltip = add
                    ? Component.translatable(
                            "screens.wynntils.poiManagementGui.exportSelected.tooltip", selectedWaypoints.size())
                    : Component.translatable("screens.wynntils.poiManagementGui.exportAll.tooltip");

            exportButton.setTooltip(Tooltip.create(tooltip));
        }

        populatePois();
    }

    public void toggleSortType(PoiSortType sortType, PoiSortButton selectedButton) {
        if (activeSortButton != null && activeSortButton != selectedButton) {
            activeSortButton.setSelected(false);
        }

        activeSortButton = selectedButton;

        boolean selected = toggleSortOrder(sortType);

        selectedButton.setSelected(selected);

        populatePois();
    }

    public void deletePoi(CustomPoi poiToDelete) {
        HiddenConfig<List<CustomPoi>> customPois = Managers.Feature.getFeatureInstance(MainMapFeature.class).customPois;
        int deletedPoiIndex = customPois.get().indexOf(poiToDelete);

        customPois.get().remove(poiToDelete);
        customPois.touched();

        deletedPois.add(poiToDelete);
        deletedIndexes.add(deletedPoiIndex);

        undoDeleteButton.active = true;

        // Handles deleting when the final poi is visible
        if (scrollOffset == Math.max(0, waypoints.size() - maxPoisToDisplay)) {
            setScrollOffset(1);
        }

        populatePois();
        updateAllUsedIcons();
    }

    public void updatePoiPosition(CustomPoi poiToMove, int direction) {
        int poiToMoveIndex = waypoints.indexOf(poiToMove);

        if (poiToMoveIndex == -1
                || poiToMoveIndex + direction < 0
                || poiToMoveIndex + direction > waypoints.size() - 1) {
            return;
        }

        CustomPoi poiToSwap = waypoints.get(waypoints.indexOf(poiToMove) + direction);

        HiddenConfig<List<CustomPoi>> customPois = Managers.Feature.getFeatureInstance(MainMapFeature.class).customPois;

        Collections.swap(
                customPois.get(),
                customPois.get().indexOf(poiToMove),
                customPois.get().indexOf(poiToSwap));

        customPois.touched();

        populatePois();
    }

    public void toggleIcon(Texture icon) {
        filteredIcons.put(icon, !filteredIcons.get(icon));

        if (!filteredIcons.containsValue(false)) {
            filteredIcons.replaceAll((key, value) -> false);
        }

        filterAllButton.active = filteredIcons.containsValue(true);

        populateIcons();
    }

    @Override
    public boolean doMouseClicked(double mouseX, double mouseY, int button) {
        // FIXME: Search bar cursor dissapears after interacting with it but still remains focused
        // should be unfocused
        for (GuiEventListener child : children()) {
            if (child.isMouseOver(mouseX, mouseY)) {
                return child.mouseClicked(mouseX, mouseY, button);
            }
        }

        if (!draggingScroll) {
            float scrollButtonRenderX = (int) (dividedWidth * 52);

            if (mouseX >= scrollButtonRenderX
                    && mouseX <= scrollButtonRenderX + dividedWidth
                    && mouseY >= scrollButtonRenderY
                    && mouseY <= scrollButtonRenderY + scrollButtonHeight) {
                draggingScroll = true;
            }
        }

        // Returning super.doMouseClicked(mouseX, mouseY, button) causes ConcurrentModificationException
        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        draggingScroll = false;
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        setScrollOffset((int) delta);

        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (!draggingScroll) return false;

        int newValue;

        if (!filterMode) {
            newValue = (int) MathUtils.map(
                    (float) mouseY,
                    (int) (dividedHeight * 10),
                    (int) (dividedHeight * 52),
                    0,
                    Math.max(0, waypoints.size() - maxPoisToDisplay));
        } else {
            newValue = (int) MathUtils.map(
                    (float) mouseY,
                    (int) (dividedHeight * 10),
                    (int) (dividedHeight * 52),
                    0,
                    Math.max(0, usedIcons.size() - MAX_ICONS_TO_DISPLAY));
        }

        setScrollOffset(-newValue + scrollOffset);

        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    private void renderScrollButton(PoseStack poseStack) {
        if (!filterMode) {
            scrollButtonRenderY = MathUtils.map(
                    scrollOffset, 0, waypoints.size() - maxPoisToDisplay, (int) (dividedHeight * 10), (int)
                            (dividedHeight * 51));
        } else {
            scrollButtonRenderY = MathUtils.map(
                    scrollOffset, 0, usedIcons.size() - MAX_ICONS_TO_DISPLAY, (int) (dividedHeight * 10), (int)
                            (dividedHeight * 51));
        }

        RenderUtils.drawScalingTexturedRect(
                poseStack,
                Texture.SCROLL_BUTTON.resource(),
                (int) (dividedWidth * 52),
                scrollButtonRenderY,
                0,
                dividedWidth,
                scrollButtonHeight,
                Texture.SCROLL_BUTTON.width(),
                Texture.SCROLL_BUTTON.height());
    }

    private void setScrollOffset(int delta) {
        if (!filterMode) {
            scrollOffset = MathUtils.clamp(scrollOffset - delta, 0, Math.max(0, waypoints.size() - maxPoisToDisplay));

            populatePois();
        } else {
            scrollOffset =
                    MathUtils.clamp(scrollOffset - delta, 0, Math.max(0, usedIcons.size() - MAX_ICONS_TO_DISPLAY));

            populateIcons();
        }
    }

    private void populatePois() {
        for (AbstractWidget widget : poiManagerWidgets) {
            this.removeWidget(widget);
        }

        this.poiManagerWidgets.clear();

        waypoints = Managers.Feature.getFeatureInstance(MainMapFeature.class).customPois.get().stream()
                .filter(poi -> searchMatches(poi.getName()))
                .collect(Collectors.toList());

        if (filteredIcons.containsValue(true)) {
            waypoints = waypoints.stream()
                    .filter(poi -> filteredIcons.getOrDefault(poi.getIcon(), false))
                    .collect(Collectors.toList());
        }

        nameSortButton.visible = !waypoints.isEmpty();
        xSortButton.visible = !waypoints.isEmpty();
        ySortButton.visible = !waypoints.isEmpty();
        zSortButton.visible = !waypoints.isEmpty();
        exportButton.active = !waypoints.isEmpty();
        selectAllButton.active = !waypoints.isEmpty();
        deselectAllButton.active = !selectedWaypoints.isEmpty();

        if (Managers.Feature.getFeatureInstance(MainMapFeature.class)
                .customPois
                .get()
                .isEmpty()) {
            searchInput.visible = false;
            filterButton.visible = false;
        }

        if (sortOrder != null) {
            waypoints = sortPois();
        }

        int row = (int) ((int) (dividedHeight * HEADER_HEIGHT) + (dividedHeight / 2f));

        for (int i = 0; i < maxPoisToDisplay; i++) {
            bottomDisplayedIndex = i + scrollOffset;

            if (bottomDisplayedIndex > waypoints.size() - 1) {
                break;
            }

            CustomPoi poi = waypoints.get(bottomDisplayedIndex);

            PoiManagerWidget poiWidget = new PoiManagerWidget(
                    (int) (dividedWidth * 12),
                    row,
                    (int) (dividedWidth * 38),
                    20,
                    poi,
                    this,
                    dividedWidth,
                    selectionMode,
                    selectedWaypoints.contains(poi));

            row += 20;

            poiManagerWidgets.add(poiWidget);

            this.addRenderableWidget(poiWidget);
        }
    }

    private List<CustomPoi> sortPois() {
        List<CustomPoi> sortedPois = waypoints;

        switch (sortOrder) {
            case NAME_ASC -> sortedPois.sort(Comparator.comparing(CustomPoi::getName, String.CASE_INSENSITIVE_ORDER));
            case NAME_DESC -> sortedPois.sort(Comparator.comparing(CustomPoi::getName, String.CASE_INSENSITIVE_ORDER)
                    .reversed());
            case X_ASC -> sortedPois.sort(
                    Comparator.comparing(poi -> poi.getLocation().getX()));
            case X_DESC -> sortedPois.sort(
                    Comparator.comparing(poi -> poi.getLocation().getX(), Comparator.reverseOrder()));
            case Y_ASC -> sortedPois.sort(Comparator.comparing(
                    poi -> poi.getLocation().getY().orElse(null), Comparator.nullsFirst(Comparator.naturalOrder())));
            case Y_DESC -> sortedPois.sort(Comparator.comparing(
                    poi -> poi.getLocation().getY().orElse(null), Comparator.nullsLast(Comparator.reverseOrder())));
            case Z_ASC -> sortedPois.sort(
                    Comparator.comparing(poi -> poi.getLocation().getZ()));
            case Z_DESC -> sortedPois.sort(
                    Comparator.comparing(poi -> poi.getLocation().getZ(), Comparator.reverseOrder()));
        }

        return sortedPois;
    }

    private void toggleSelectAll(boolean select) {
        selectionMode = select;

        upButton.visible = select;
        downButton.visible = select;
        deleteSelectedButton.active = select;

        Component tooltip;

        if (select) {
            selectedWaypoints = waypoints;

            tooltip = Component.translatable(
                    "screens.wynntils.poiManagementGui.exportSelected.tooltip", selectedWaypoints.size());
        } else {
            selectedWaypoints.clear();

            tooltip = Component.translatable("screens.wynntils.poiManagementGui.exportAll.tooltip");
        }

        exportButton.setTooltip(Tooltip.create(tooltip));

        populatePois();
    }

    private boolean toggleSortOrder(PoiSortType sortType) {
        PoiSortOrder newOrder = null;
        boolean selected = true;

        switch (sortType) {
            case NAME -> {
                if (sortOrder == null) {
                    newOrder = PoiSortOrder.NAME_ASC;
                } else if (sortOrder == PoiSortOrder.NAME_ASC) {
                    newOrder = PoiSortOrder.NAME_DESC;
                }
            }
            case X -> {
                if (sortOrder == null) {
                    newOrder = PoiSortOrder.X_ASC;
                } else if (sortOrder == PoiSortOrder.X_ASC) {
                    newOrder = PoiSortOrder.X_DESC;
                }
            }
            case Y -> {
                if (sortOrder == null) {
                    newOrder = PoiSortOrder.Y_ASC;
                } else if (sortOrder == PoiSortOrder.Y_ASC) {
                    newOrder = PoiSortOrder.Y_DESC;
                }
            }
            case Z -> {
                if (sortOrder == null) {
                    newOrder = PoiSortOrder.Z_ASC;
                } else if (sortOrder == PoiSortOrder.Z_ASC) {
                    newOrder = PoiSortOrder.Z_DESC;
                }
            }
            default -> {
                selected = false;
            }
        }

        sortOrder = newOrder;

        return newOrder != null && selected;
    }

    private void updateAllUsedIcons() {
        filteredIcons = Managers.Feature.getFeatureInstance(MainMapFeature.class).customPois.get().stream()
                .map(CustomPoi::getIcon)
                .distinct()
                .collect(Collectors.toMap(
                        Function.identity(),
                        texture -> false,
                        (existing, replacement) -> existing,
                        () -> new EnumMap<>(Texture.class)));

        usedIcons = new ArrayList<>(filteredIcons.keySet());

        filterButton.visible = filteredIcons.size() > 1;
    }

    private void toggleIconFilter(boolean enabled) {
        filterMode = enabled;

        filterAllButton.visible = enabled;
        searchInput.visible = !enabled;
        nameSortButton.visible = !enabled;
        xSortButton.visible = !enabled;
        ySortButton.visible = !enabled;
        zSortButton.visible = !enabled;
        selectAllButton.visible = !enabled;
        deselectAllButton.visible = !enabled;
        importButton.visible = !enabled;
        exportButton.visible = !enabled;
        deleteSelectedButton.visible = !enabled;
        undoDeleteButton.visible = !enabled;
        upButton.visible = selectionMode && !enabled;
        downButton.visible = selectionMode && !enabled;

        scrollOffset = 0;

        List<AbstractWidget> widgetsToRemove = enabled ? poiManagerWidgets : iconFilterWidgets;

        for (AbstractWidget widget : widgetsToRemove) {
            this.removeWidget(widget);
        }

        Component filterMessage;

        if (enabled) {
            bottomDisplayedIndex = Math.min(MAX_ICONS_TO_DISPLAY, usedIcons.size() - 1);

            filterMessage = Component.translatable("screens.wynntils.poiManagementGui.done");

            populateIcons();
        } else {
            bottomDisplayedIndex = Math.min(maxPoisToDisplay, waypoints.size() - 1);

            filterMessage = Component.translatable("screens.wynntils.poiManagementGui.filter");

            populatePois();
        }

        filterButton.setMessage(filterMessage);
    }

    private void populateIcons() {
        for (AbstractWidget widget : iconFilterWidgets) {
            this.removeWidget(widget);
        }

        this.iconFilterWidgets.clear();

        int row = (int) ((int) (dividedHeight * (HEADER_HEIGHT + 2)) + (dividedHeight / 2f));
        int xPos = (int) (dividedWidth * 13);

        for (int i = 0; i < MAX_ICONS_TO_DISPLAY; i++) {
            bottomDisplayedIndex = i + (scrollOffset * ICONS_PER_ROW);

            if (bottomDisplayedIndex > usedIcons.size() - 1) {
                break;
            }

            Texture icon = usedIcons.get(bottomDisplayedIndex);

            PoiFilterWidget filterWidget = new PoiFilterWidget(
                    xPos, row, iconButtonSize, iconButtonSize, icon, this, filteredIcons.getOrDefault(icon, false));

            iconFilterWidgets.add(filterWidget);

            this.addRenderableWidget(filterWidget);

            if (xPos + (iconButtonSize * 2) > (int) (dividedWidth * 50)) {
                row += iconButtonSize;
                xPos = (int) (dividedWidth * 13);
            } else {
                xPos += iconButtonSize;
            }
        }
    }

    private boolean searchMatches(String poiName) {
        return StringUtils.partialMatch(poiName, searchInput.getTextBoxInput());
    }

    private void updateSelectedPoiPositions(int direction) {
        List<CustomPoi> orderedWaypoints = waypoints.stream()
                .filter(waypoint -> selectedWaypoints.contains(waypoint))
                .collect(Collectors.toList());
        ;

        if (direction == 1) {
            Collections.reverse(orderedWaypoints);
        }

        for (CustomPoi selectedPoi : orderedWaypoints) {
            updatePoiPosition(selectedPoi, direction);
        }
    }

    private void deleteSelectedPois() {
        HiddenConfig<List<CustomPoi>> customPois = Managers.Feature.getFeatureInstance(MainMapFeature.class).customPois;

        for (CustomPoi poi : selectedWaypoints) {
            deletePoi(poi);
        }

        McUtils.sendMessageToClient(
                Component.translatable("screens.wynntils.poiManagementGui.deletedPois", selectedWaypoints.size())
                        .withStyle(ChatFormatting.GREEN));

        if (customPois.get().isEmpty()) {
            selectAllButton.active = false;
            filterButton.visible = false;
            nameSortButton.visible = false;
            xSortButton.visible = false;
            ySortButton.visible = false;
            zSortButton.visible = false;
        }

        if (scrollOffset == Math.max(0, waypoints.size() - maxPoisToDisplay)) {
            setScrollOffset(1);
        }

        toggleSelectAll(false);
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
        List<CustomPoi> existingPois = new ArrayList<>(customPoiConfig.get());

        List<CustomPoi> poisToAdd = Stream.of(customPois)
                .filter(newPoi -> !existingPois.contains(newPoi))
                .toList();

        existingPois.addAll(poisToAdd);
        customPoiConfig.setValue(existingPois);
        customPoiConfig.touched();

        if (!customPoiConfig.get().isEmpty()) {
            filterButton.visible = true;
        }

        populatePois();

        updateAllUsedIcons();

        McUtils.sendMessageToClient(
                Component.translatable("screens.wynntils.poiManagementGui.import.success", poisToAdd.size())
                        .withStyle(ChatFormatting.GREEN));
    }

    private void exportToClipboard() {
        List<CustomPoi> waypointsToExport = selectedWaypoints.isEmpty()
                ? Managers.Feature.getFeatureInstance(MainMapFeature.class)
                        .customPois
                        .get()
                : selectedWaypoints;

        McUtils.mc()
                .keyboardHandler
                .setClipboard(waypointsToExport.stream()
                        .map(Managers.Json.GSON::toJson)
                        .toList()
                        .toString());

        McUtils.sendMessageToClient(
                Component.translatable("screens.wynntils.poiManagementGui.exportedWaypoints", waypointsToExport.size())
                        .withStyle(ChatFormatting.GREEN));
    }

    private void undoDelete() {
        HiddenConfig<List<CustomPoi>> allPois = Managers.Feature.getFeatureInstance(MainMapFeature.class).customPois;

        if (!allPois.get().contains(deletedPois.get(deletedPois.size() - 1))) {
            allPois.get().add(deletedIndexes.get(deletedIndexes.size() - 1), deletedPois.get(deletedPois.size() - 1));

            allPois.touched();

            scrollOffset = Math.max(scrollOffset - 1, 0);

            populatePois();
        }

        deletedIndexes.remove(deletedIndexes.size() - 1);
        deletedPois.remove(deletedPois.size() - 1);

        undoDeleteButton.active = !deletedIndexes.isEmpty();
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

    public List<CustomPoi> getWaypoints() {
        return Collections.unmodifiableList(waypoints);
    }

    public enum PoiSortType {
        NAME,
        X,
        Y,
        Z
    }

    private enum PoiSortOrder {
        NAME_ASC,
        NAME_DESC,
        X_ASC,
        X_DESC,
        Y_ASC,
        Y_DESC,
        Z_ASC,
        Z_DESC
    }
}
