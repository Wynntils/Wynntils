/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.maps;

import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.components.Managers;
import com.wynntils.core.persisted.config.HiddenConfig;
import com.wynntils.core.text.StyledText;
import com.wynntils.features.map.MainMapFeature;
import com.wynntils.screens.base.WynntilsGridLayoutScreen;
import com.wynntils.screens.base.widgets.InfoButton;
import com.wynntils.screens.base.widgets.TextInputBoxWidget;
import com.wynntils.screens.maps.widgets.WaypointManagerWidget;
import com.wynntils.screens.maps.widgets.WaypointSortButton;
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
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;

public final class WaypointManagementScreen extends WynntilsGridLayoutScreen {
    // Constants
    private static final int GRID_ROWS_PER_PAGE = 43;
    private static final int HEADER_HEIGHT = 12;

    // Collections
    private final List<AbstractWidget> waypointManagerWidgets = new ArrayList<>();
    private final List<CustomPoi> deletedWaypoints = new ArrayList<>();
    private final List<Integer> deletedIndexes = new ArrayList<>();

    // Previous screen
    private final MainMapScreen oldMapScreen;

    // Widgets
    private Button deleteSelectedButton;
    private Button deselectAllButton;
    private Button downButton;
    private Button exportButton;
    private Button filterButton;
    private Button removeMarkersButton;
    private Button selectAllButton;
    private Button setMarkersButton;
    private Button undoDeleteButton;
    private Button upButton;
    private WaypointSortButton activeSortButton;
    private WaypointSortButton iconSortButton;
    private WaypointSortButton nameSortButton;
    private WaypointSortButton xSortButton;
    private WaypointSortButton ySortButton;
    private WaypointSortButton zSortButton;
    private TextInputBoxWidget searchInput;

    // UI size, position etc
    private boolean draggingScroll = false;
    private float backgroundHeight;
    private float backgroundWidth;
    private float backgroundX;
    private float backgroundY;
    private float scrollButtonHeight;
    private float scrollButtonRenderX;
    private float scrollButtonRenderY;
    private int maxWaypointsToDisplay;
    private int scrollAreaHeight;
    private int scrollOffset = 0;

    // Waypoint display
    private boolean selectionMode = false;
    private List<CustomPoi> selectedWaypoints = new ArrayList<>();
    private List<CustomPoi> waypoints;
    private Map<Texture, Boolean> filteredIcons = new EnumMap<>(Texture.class);
    private WaypointSortOrder sortOrder;

    private WaypointManagementScreen(MainMapScreen oldMapScreen) {
        super(Component.literal("Waypoint Management Screen"));

        this.oldMapScreen = oldMapScreen;
    }

    public static Screen create() {
        return new WaypointManagementScreen(null);
    }

    public static Screen create(MainMapScreen oldMapScreen) {
        return new WaypointManagementScreen(oldMapScreen);
    }

    @Override
    public void onClose() {
        McUtils.mc().setScreen(oldMapScreen);
    }

    @Override
    protected void doInit() {
        super.doInit();
        // How many waypoint widgets can fit on the background
        maxWaypointsToDisplay = (int) (dividedHeight * GRID_ROWS_PER_PAGE) / 20;
        backgroundX = dividedWidth * 10;
        backgroundWidth = dividedWidth * 44;
        backgroundY = dividedHeight * 7;
        backgroundHeight = dividedHeight * 50;

        // Height of the scroll button relative to the scaled width
        scrollButtonHeight = ((dividedWidth / 2) / Texture.SCROLL_BUTTON.width()) * Texture.SCROLL_BUTTON.height();

        // How far the scrollbar should be able to go
        scrollAreaHeight = (int) (backgroundHeight - scrollButtonHeight) - (int) (dividedHeight * 4);

        // X position of the scroll button
        scrollButtonRenderX = (int) (dividedWidth * 52) + (dividedWidth / 4);

        int importExportButtonWidth = (int) (dividedWidth * 6);

        // region exit button
        this.addRenderableWidget(
                new Button.Builder(Component.literal("X").withStyle(ChatFormatting.RED), (button) -> this.onClose())
                        .pos((int) (dividedWidth * 60), (int) (dividedHeight * 4))
                        .size(20, 20)
                        .build());
        // endregion

        // region info button
        this.addRenderableWidget(new InfoButton(
                (int) (dividedWidth * 3),
                (int) (dividedHeight * 4),
                Component.literal("")
                        .append(Component.translatable("screens.wynntils.waypointManagementGui.help")
                                .withStyle(ChatFormatting.UNDERLINE))
                        .append(Component.literal("\n"))
                        .append(Component.translatable("screens.wynntils.waypointManagementGui.help1")
                                .withStyle(ChatFormatting.GRAY))
                        .append(Component.literal("\n"))
                        .append(Component.translatable("screens.wynntils.waypointManagementGui.help2")
                                .withStyle(ChatFormatting.GRAY))
                        .append(Component.literal("\n"))
                        .append(Component.translatable("screens.wynntils.waypointManagementGui.help3")
                                .withStyle(ChatFormatting.GRAY))));
        // endregion

        // region import/export
        this.addRenderableWidget(new Button.Builder(
                        Component.translatable("screens.wynntils.waypointManagementGui.import"),
                        (button) -> importFromClipboard())
                .pos((int) (dividedWidth * 22), (int) (dividedHeight * 58))
                .size(importExportButtonWidth, 20)
                .tooltip(
                        Tooltip.create(Component.translatable("screens.wynntils.waypointManagementGui.import.tooltip")))
                .build());

        exportButton = new Button.Builder(
                        Component.translatable("screens.wynntils.waypointManagementGui.export"),
                        (button) -> exportToClipboard())
                .pos((int) (dividedWidth * 36), (int) (dividedHeight * 58))
                .size(importExportButtonWidth, 20)
                .tooltip(Tooltip.create(
                        Component.translatable("screens.wynntils.waypointManagementGui.exportAll.tooltip")))
                .build();

        this.addRenderableWidget(exportButton);
        // endregion

        // region delete buttons
        undoDeleteButton = new Button.Builder(
                        Component.translatable("screens.wynntils.waypointManagementGui.undo"), (button) -> undoDelete())
                .pos((int) (dividedWidth * 55), (int) (dividedHeight * 58))
                .size((int) (dividedWidth * 8), 20)
                .build();

        this.addRenderableWidget(undoDeleteButton);

        deleteSelectedButton = new Button.Builder(
                        Component.translatable("screens.wynntils.waypointManagementGui.deleteSelected"),
                        (button) -> deleteSelectedWaypoints())
                .pos((int) (dividedWidth * 55), (int) (dividedHeight * 58) - 25)
                .size((int) (dividedWidth * 8), 20)
                .build();

        deleteSelectedButton.active = false;

        this.addRenderableWidget(deleteSelectedButton);
        // endregion

        // region marker buttons
        setMarkersButton = new Button.Builder(
                        Component.translatable("screens.wynntils.waypointManagementGui.setMarkers"),
                        (button) -> toggleMarkers(true))
                .pos((int) dividedWidth, (int) (dividedHeight * 58) - 75)
                .size((int) (dividedWidth * 8), 20)
                .tooltip(Tooltip.create(
                        Component.translatable("screens.wynntils.waypointManagementGui.setMarkers.tooltip")))
                .build();

        setMarkersButton.active = false;

        this.addRenderableWidget(setMarkersButton);

        removeMarkersButton = new Button.Builder(
                        Component.translatable("screens.wynntils.waypointManagementGui.removeMarkers"),
                        (button) -> toggleMarkers(false))
                .pos((int) dividedWidth, (int) (dividedHeight * 58) - 50)
                .size((int) (dividedWidth * 8), 20)
                .tooltip(Tooltip.create(
                        Component.translatable("screens.wynntils.waypointManagementGui.removeMarkers.tooltip")))
                .build();

        removeMarkersButton.active = false;

        this.addRenderableWidget(removeMarkersButton);
        // endregion

        // region select buttons
        deselectAllButton = new Button.Builder(
                        Component.translatable("screens.wynntils.waypointManagementGui.deselectAll"),
                        (button) -> toggleSelectAll(false))
                .pos((int) dividedWidth, (int) (dividedHeight * 58))
                .size((int) (dividedWidth * 8), 20)
                .build();

        deselectAllButton.active = false;

        this.addRenderableWidget(deselectAllButton);

        selectAllButton = new Button.Builder(
                        Component.translatable("screens.wynntils.waypointManagementGui.selectAll"),
                        (button) -> toggleSelectAll(true))
                .pos((int) dividedWidth, (int) (dividedHeight * 58) - 25)
                .size((int) (dividedWidth * 8), 20)
                .build();

        this.addRenderableWidget(selectAllButton);
        // endregion

        // region up/down buttons
        upButton = new Button.Builder(Component.literal("🠝"), (button) -> updateSelectedWaypointPositions(-1))
                .pos((width / 2) - 22, (int) (dividedHeight * 58))
                .size(20, 20)
                .build();

        upButton.visible = false;

        this.addRenderableWidget(upButton);

        downButton = new Button.Builder(Component.literal("🠟"), (button) -> updateSelectedWaypointPositions(1))
                .pos((width / 2) + 2, (int) (dividedHeight * 58))
                .size(20, 20)
                .build();

        downButton.visible = false;

        this.addRenderableWidget(downButton);
        // endregion

        // region icon filter button
        int filterButtonWidth = (int) (dividedWidth * 10);

        filterButton = new Button.Builder(
                        Component.translatable("screens.wynntils.waypointManagementGui.filter"), (button) -> {
                            scrollOffset = 0;
                            McUtils.mc().setScreen(IconFilterScreen.create(this, filteredIcons));
                        })
                .pos((int) (dividedWidth * 44), (int) (dividedHeight * 3))
                .size(filterButtonWidth, 20)
                .build();

        this.addRenderableWidget(filterButton);
        // endregion

        // region search bar
        searchInput = new TextInputBoxWidget(
                (int) (dividedWidth * 10) + 5,
                (int) (dividedHeight * 3),
                (int) (backgroundWidth - filterButtonWidth - 10),
                20,
                (s) -> {
                    scrollOffset = 0;
                    populateWaypoints();
                },
                this,
                searchInput);

        this.addRenderableWidget(searchInput);

        setFocusedTextInput(searchInput);
        // endregion

        // region sort buttons
        int iconTitleWidth = (McUtils.mc().font.width(I18n.get("screens.wynntils.waypointManagementGui.icon"))) + 1;

        int nameTitleWidth = (McUtils.mc().font.width(I18n.get("screens.wynntils.waypointManagementGui.name"))) + 1;

        int coordinateTitleWidth = (McUtils.mc().font.width("X")) + 1;

        iconSortButton = this.addRenderableWidget(new WaypointSortButton(
                (int) (dividedWidth * 13) - (iconTitleWidth / 2),
                (int) (dividedHeight * HEADER_HEIGHT) - 10,
                iconTitleWidth,
                10,
                Component.translatable("screens.wynntils.waypointManagementGui.icon"),
                this,
                WaypointSortType.ICON));

        nameSortButton = this.addRenderableWidget(new WaypointSortButton(
                (int) (dividedWidth * 22) - (nameTitleWidth / 2),
                (int) (dividedHeight * HEADER_HEIGHT) - 10,
                nameTitleWidth,
                10,
                Component.translatable("screens.wynntils.waypointManagementGui.name"),
                this,
                WaypointSortType.NAME));

        xSortButton = this.addRenderableWidget(new WaypointSortButton(
                (int) (dividedWidth * 32) - (coordinateTitleWidth / 2),
                (int) (dividedHeight * HEADER_HEIGHT) - 10,
                coordinateTitleWidth,
                10,
                Component.literal("X"),
                this,
                WaypointSortType.X));

        ySortButton = this.addRenderableWidget(new WaypointSortButton(
                (int) (dividedWidth * 35) - (coordinateTitleWidth / 2),
                (int) (dividedHeight * HEADER_HEIGHT) - 10,
                coordinateTitleWidth,
                10,
                Component.literal("Y"),
                this,
                WaypointSortType.Y));

        zSortButton = this.addRenderableWidget(new WaypointSortButton(
                (int) (dividedWidth * 38) - (coordinateTitleWidth / 2),
                (int) (dividedHeight * HEADER_HEIGHT) - 10,
                coordinateTitleWidth,
                10,
                Component.literal("Z"),
                this,
                WaypointSortType.Z));
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
            iconSortButton.visible = false;
            nameSortButton.visible = false;
            xSortButton.visible = false;
            ySortButton.visible = false;
            zSortButton.visible = false;
            exportButton.active = false;
        }

        updateAllUsedIcons();
        populateWaypoints();
    }

    @Override
    public void doRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.doRender(guiGraphics, mouseX, mouseY, partialTick);
        PoseStack poseStack = guiGraphics.pose();
        renderScrollButton(poseStack);

        if (Managers.Feature.getFeatureInstance(MainMapFeature.class)
                .customPois
                .get()
                .isEmpty()) {
            FontRenderer.getInstance()
                    .renderText(
                            poseStack,
                            StyledText.fromComponent(
                                    Component.translatable("screens.wynntils.waypointManagementGui.noWaypoints")),
                            (int) (dividedWidth * 32),
                            (int) (dividedHeight * 32),
                            CommonColors.WHITE,
                            HorizontalAlignment.CENTER,
                            VerticalAlignment.MIDDLE,
                            TextShadow.NORMAL);

            return;
        }

        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        StyledText.fromComponent(
                                Component.translatable("screens.wynntils.waypointManagementGui.search")),
                        (int) (dividedWidth * 10) + 5,
                        (int) dividedHeight,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.TOP,
                        TextShadow.NORMAL);

        if (waypoints.isEmpty()) {
            FontRenderer.getInstance()
                    .renderText(
                            poseStack,
                            StyledText.fromComponent(Component.translatable(
                                    "screens.wynntils.waypointManagementGui.noFilteredWaypoints")),
                            (int) (dividedWidth * 32),
                            (int) (dividedHeight * 32),
                            CommonColors.WHITE,
                            HorizontalAlignment.CENTER,
                            VerticalAlignment.MIDDLE,
                            TextShadow.NORMAL);
        } else {
            RenderUtils.drawRect(
                    poseStack,
                    CommonColors.WHITE,
                    (int) (dividedWidth * 12),
                    (int) (dividedHeight * HEADER_HEIGHT),
                    0,
                    (int) (dividedWidth * 38),
                    1);
        }
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(guiGraphics, mouseX, mouseY, partialTick);

        RenderUtils.drawScalingTexturedRect(
                guiGraphics.pose(),
                Texture.WAYPOINT_MANAGER_BACKGROUND.resource(),
                backgroundX,
                backgroundY,
                0,
                backgroundWidth,
                backgroundHeight,
                Texture.WAYPOINT_MANAGER_BACKGROUND.width(),
                Texture.WAYPOINT_MANAGER_BACKGROUND.height());
    }

    @Override
    public boolean doMouseClicked(double mouseX, double mouseY, int button) {
        if (!draggingScroll && (waypoints.size() > maxWaypointsToDisplay)) {
            if (MathUtils.isInside(
                    (int) mouseX,
                    (int) mouseY,
                    (int) scrollButtonRenderX,
                    (int) (scrollButtonRenderX + (dividedWidth / 2)),
                    (int) scrollButtonRenderY,
                    (int) (scrollButtonRenderY + scrollButtonHeight))) {
                draggingScroll = true;

                return true;
            }
        }

        return super.doMouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (!draggingScroll) return false;

        int renderY = (int) ((this.height - backgroundHeight) / 2 + (int) (dividedHeight * 3));
        int scrollAreaStartY = renderY + 7;

        int newValue = Math.round(MathUtils.map(
                (float) mouseY,
                scrollAreaStartY,
                scrollAreaStartY + scrollAreaHeight,
                0,
                Math.max(0, waypoints.size() - maxWaypointsToDisplay)));

        scroll(newValue - scrollOffset);

        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        draggingScroll = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY) {
        double scrollValue = -Math.signum(deltaY);
        scroll((int) scrollValue);

        return true;
    }

    public void selectWaypoint(CustomPoi selectedWaypoint) {
        boolean addedWaypoint = true;

        // Deselect a waypoint
        if (selectedWaypoints.contains(selectedWaypoint)) {
            selectedWaypoints.remove(selectedWaypoint);

            addedWaypoint = false;
        } else {
            selectedWaypoints.add(selectedWaypoint);
        }

        selectAllButton.active = selectedWaypoints.size() < waypoints.size();

        if (addedWaypoint || selectedWaypoints.isEmpty()) {
            selectionMode = addedWaypoint;

            deselectAllButton.active = addedWaypoint;
            deleteSelectedButton.active = addedWaypoint;
            setMarkersButton.active = addedWaypoint;
            removeMarkersButton.active = addedWaypoint;

            upButton.visible = addedWaypoint;
            downButton.visible = addedWaypoint;

            // Export tooltip should display how many of the waypoints will be exported
            Component tooltip = addedWaypoint
                    ? Component.translatable(
                            "screens.wynntils.waypointManagementGui.exportSelected.tooltip", selectedWaypoints.size())
                    : Component.translatable("screens.wynntils.waypointManagementGui.exportAll.tooltip");

            exportButton.setTooltip(Tooltip.create(tooltip));
        }

        populateWaypoints();
    }

    public void toggleSortType(WaypointSortType sortType, WaypointSortButton selectedButton) {
        if (activeSortButton != null && activeSortButton != selectedButton) {
            activeSortButton.setSelected(false);
        }

        activeSortButton = selectedButton;

        boolean selected = toggleSortOrder(sortType);

        selectedButton.setSelected(selected);

        populateWaypoints();
    }

    public void deleteWaypoint(CustomPoi waypointToDelete, boolean save) {
        HiddenConfig<List<CustomPoi>> savedWaypoints =
                Managers.Feature.getFeatureInstance(MainMapFeature.class).customPois;
        int deletedWaypointIndex = savedWaypoints.get().indexOf(waypointToDelete);

        savedWaypoints.get().remove(waypointToDelete);
        if (save) {
            savedWaypoints.touched();
        }
        // Managers.Feature.getFeatureInstance(MainMapFeature.class).updateWaypoints();

        deletedWaypoints.add(waypointToDelete);
        deletedIndexes.add(deletedWaypointIndex);

        undoDeleteButton.active = true;

        if (savedWaypoints.get().isEmpty()) {
            searchInput.visible = false;
            iconSortButton.visible = false;
            nameSortButton.visible = false;
            xSortButton.visible = false;
            ySortButton.visible = false;
            zSortButton.visible = false;
            selectAllButton.active = false;
            exportButton.active = false;
        }

        // Handles scrolling when deleting the last waypoint
        if (scrollOffset == Math.max(0, waypoints.size() - maxWaypointsToDisplay)) {
            scroll(-1);
        }

        updateAllUsedIcons();

        populateWaypoints();
    }

    public void updateWaypointPosition(CustomPoi waypointToMove, int direction) {
        int waypointToMoveIndex = waypoints.indexOf(waypointToMove);

        // If waypoint is at the top/bottom of list or if it was selected but then filtered out, don't move it
        if (waypointToMoveIndex == -1
                || waypointToMoveIndex + direction < 0
                || waypointToMoveIndex + direction > waypoints.size() - 1) {
            return;
        }

        CustomPoi waypointToSwap = waypoints.get(waypoints.indexOf(waypointToMove) + direction);

        HiddenConfig<List<CustomPoi>> savedWaypoints =
                Managers.Feature.getFeatureInstance(MainMapFeature.class).customPois;

        Collections.swap(
                savedWaypoints.get(),
                savedWaypoints.get().indexOf(waypointToMove),
                savedWaypoints.get().indexOf(waypointToSwap));

        savedWaypoints.touched();
        // Managers.Feature.getFeatureInstance(MainMapFeature.class).updateWaypoints();

        populateWaypoints();
    }

    public List<CustomPoi> getWaypoints() {
        return Collections.unmodifiableList(waypoints);
    }

    public void setFilteredIcons(Map<Texture, Boolean> filteredIcons) {
        this.filteredIcons = filteredIcons;
    }

    private void renderScrollButton(PoseStack poseStack) {
        // Don't render the scroll button if it will not be useable
        if (waypoints.size() <= maxWaypointsToDisplay) return;

        // Calculate where the scroll button should be on the Y axis
        scrollButtonRenderY = (this.height - backgroundHeight) / 2
                + (int) (dividedHeight * 3)
                + MathUtils.map(scrollOffset, 0, waypoints.size() - maxWaypointsToDisplay, 0, scrollAreaHeight);

        RenderUtils.drawScalingTexturedRect(
                poseStack,
                Texture.SCROLL_BUTTON.resource(),
                scrollButtonRenderX,
                scrollButtonRenderY,
                1,
                (dividedWidth / 2),
                scrollButtonHeight,
                Texture.SCROLL_BUTTON.width(),
                Texture.SCROLL_BUTTON.height());
    }

    private void scroll(int delta) {
        // Calculate how many waypoint widgets should be scrolled past
        scrollOffset = MathUtils.clamp(scrollOffset + delta, 0, Math.max(0, waypoints.size() - maxWaypointsToDisplay));

        populateWaypoints();
    }

    private void populateWaypoints() {
        // Remove old widgets
        for (AbstractWidget widget : waypointManagerWidgets) {
            this.removeWidget(widget);
        }

        this.waypointManagerWidgets.clear();
        //
        //        // Get full list of waypoints
        //        pois = Managers.Feature.getFeatureInstance(MainMapFeature.class).customPois.get().stream()
        //                .filter(poi -> searchMatches(poi.getName()))
        //                .collect(Collectors.toList());

        // No waypoints
        if (waypoints.isEmpty()) {
            return;
        }

        //        // Filter waypoints based on filtered icons
        //        pois = pois.stream()
        //                .filter(poi -> filteredIcons.getOrDefault(poi.getIcon(), true))
        //                .collect(Collectors.toList());

        // Hide buttons if no filtered waypoints
        iconSortButton.visible = !waypoints.isEmpty();
        nameSortButton.visible = !waypoints.isEmpty();
        xSortButton.visible = !waypoints.isEmpty();
        ySortButton.visible = !waypoints.isEmpty();
        zSortButton.visible = !waypoints.isEmpty();
        exportButton.active = !waypoints.isEmpty();
        selectAllButton.active = !waypoints.isEmpty();
        deselectAllButton.active = !selectedWaypoints.isEmpty();
        setMarkersButton.active = !selectedWaypoints.isEmpty();
        removeMarkersButton.active = !selectedWaypoints.isEmpty();

        // Only hide search bar & filter button if no waypoints at all
        // Can't check if filtered waypoints is empty as they may be empty due to the filters
        if (Managers.Feature.getFeatureInstance(MainMapFeature.class)
                .customPois
                .get()
                .isEmpty()) {
            searchInput.visible = false;
            filterButton.visible = false;
        }

        // No filtered waypoints
        if (waypoints.isEmpty()) {
            return;
        }

        // Sort waypoints
        if (sortOrder != null) {
            sortWaypoints();
        }

        // Starting Y position for the waypoints
        int row = (int) ((int) (dividedHeight * HEADER_HEIGHT) + (dividedHeight / 2f));

        int currentWaypoint;

        // Render manager widgets
        for (int i = 0; i < maxWaypointsToDisplay; i++) {
            // Get the waypoint to render
            currentWaypoint = i + scrollOffset;

            // If user has less waypoints than maxWaypointsToDisplay, make sure we don't try and get a waypoint out of
            // range
            if (currentWaypoint > waypoints.size() - 1) {
                break;
            }

            CustomPoi waypoint = waypoints.get(currentWaypoint);

            WaypointManagerWidget waypointWidget = new WaypointManagerWidget(
                    (int) (dividedWidth * 12),
                    row,
                    (int) (dividedWidth * 38),
                    20,
                    waypoint,
                    this,
                    dividedWidth,
                    selectionMode,
                    selectedWaypoints.contains(waypoint));

            // Each widget height is 20, add 20 for Y position of next widget
            row += 20;

            waypointManagerWidgets.add(waypointWidget);

            this.addRenderableWidget(waypointWidget);
        }
    }

    private void sortWaypoints() {
        // Sort waypoints, ignore case and for null Y's, treat them as 0
        //        switch (sortOrder) {
        //            case ICON_ASC -> pois.sort(Comparator.comparing(CustomPoi::getIcon));
        //            case ICON_DESC -> pois.sort(Comparator.comparing(CustomPoi::getIcon).reversed());
        //            case NAME_ASC -> pois.sort(Comparator.comparing(CustomPoi::getName,
        // String.CASE_INSENSITIVE_ORDER));
        //            case NAME_DESC -> pois.sort(Comparator.comparing(CustomPoi::getName,
        // String.CASE_INSENSITIVE_ORDER)
        //                    .reversed());
        //            case X_ASC -> pois.sort(
        //                    Comparator.comparing(poi -> poi.getLocation().getX()));
        //            case X_DESC -> pois.sort(
        //                    Comparator.comparing(poi -> poi.getLocation().getX(), Comparator.reverseOrder()));
        //            case Y_ASC -> pois.sort(Comparator.comparing(
        //                    poi -> poi.getLocation().getY().orElse(null),
        // Comparator.nullsFirst(Comparator.naturalOrder())));
        //            case Y_DESC -> pois.sort(Comparator.comparing(
        //                    poi -> poi.getLocation().getY().orElse(null),
        // Comparator.nullsLast(Comparator.reverseOrder())));
        //            case Z_ASC -> pois.sort(
        //                    Comparator.comparing(poi -> poi.getLocation().getZ()));
        //            case Z_DESC -> pois.sort(
        //                    Comparator.comparing(poi -> poi.getLocation().getZ(), Comparator.reverseOrder()));
        //        }
    }

    private void toggleMarkers(boolean addMarkers) {
        // FIXME: Services.UserMarker
        //        if (addMarkers) {
        //            selectedPois.forEach(poi -> Models.Marker.USER_WAYPOINTS_PROVIDER.addLocation(
        //                    poi.getLocation().asLocation(), poi.getIcon(), poi.getColor(), poi.getColor(),
        // poi.getName()));
        //        } else {
        //            selectedPois.forEach(poi -> Models.Marker.USER_WAYPOINTS_PROVIDER.removeLocation(
        //                    poi.getLocation().asLocation()));
        //        }
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
                    "screens.wynntils.waypointManagementGui.exportSelected.tooltip", selectedWaypoints.size());
        } else {
            selectedWaypoints.clear();

            tooltip = Component.translatable("screens.wynntils.waypointManagementGui.exportAll.tooltip");
        }

        exportButton.setTooltip(Tooltip.create(tooltip));

        populateWaypoints();
    }

    private boolean toggleSortOrder(WaypointSortType sortType) {
        WaypointSortOrder newOrder = null;
        boolean selected = true;

        // Update the sort order of waypoints, first click is ascending, second descending and third will reset
        switch (sortType) {
            case ICON -> {
                if (sortOrder == null) {
                    newOrder = WaypointSortOrder.ICON_ASC;
                } else if (sortOrder == WaypointSortOrder.ICON_ASC) {
                    newOrder = WaypointSortOrder.ICON_DESC;
                }
            }
            case NAME -> {
                if (sortOrder == null) {
                    newOrder = WaypointSortOrder.NAME_ASC;
                } else if (sortOrder == WaypointSortOrder.NAME_ASC) {
                    newOrder = WaypointSortOrder.NAME_DESC;
                }
            }
            case X -> {
                if (sortOrder == null) {
                    newOrder = WaypointSortOrder.X_ASC;
                } else if (sortOrder == WaypointSortOrder.X_ASC) {
                    newOrder = WaypointSortOrder.X_DESC;
                }
            }
            case Y -> {
                if (sortOrder == null) {
                    newOrder = WaypointSortOrder.Y_ASC;
                } else if (sortOrder == WaypointSortOrder.Y_ASC) {
                    newOrder = WaypointSortOrder.Y_DESC;
                }
            }
            case Z -> {
                if (sortOrder == null) {
                    newOrder = WaypointSortOrder.Z_ASC;
                } else if (sortOrder == WaypointSortOrder.Z_ASC) {
                    newOrder = WaypointSortOrder.Z_DESC;
                }
            }
            default -> {
                selected = false;
            }
        }

        sortOrder = newOrder;

        // If the selected sort button is now the active sort, first 2 clicks true, 3rd false
        return newOrder != null && selected;
    }

    private void updateAllUsedIcons() {
        // Get all icons from all waypoints, default their "active" boolean to true.
        //        filteredIcons = Managers.Feature.getFeatureInstance(MainMapFeature.class).customPois.get().stream()
        //                .map(CustomPoi::getIcon)
        //                .distinct()
        //                .collect(Collectors.toMap(
        //                        Function.identity(),
        //                        texture -> filteredIcons.getOrDefault(texture, true),
        //                        (existing, replacement) -> existing,
        //                        () -> new EnumMap<>(Texture.class)));

        // Only show filter button if there is more than 1 used icon
        filterButton.visible = filteredIcons.size() > 1;
    }

    private boolean searchMatches(String waypointName) {
        return StringUtils.partialMatch(waypointName, searchInput.getTextBoxInput());
    }

    private void updateSelectedWaypointPositions(int direction) {
        // Get selected waypoints in the same order they are in waypoints
        List<CustomPoi> orderedWaypoints = waypoints.stream()
                .filter(waypoint -> selectedWaypoints.contains(waypoint))
                .collect(Collectors.toList());

        // If we are shifting waypoints down the list needs to be reversed
        if (direction == 1) {
            Collections.reverse(orderedWaypoints);
        }

        for (CustomPoi selectedWaypoint : orderedWaypoints) {
            updateWaypointPosition(selectedWaypoint, direction);
        }
    }

    private void deleteSelectedWaypoints() {
        HiddenConfig<List<CustomPoi>> savedWaypoints =
                Managers.Feature.getFeatureInstance(MainMapFeature.class).customPois;

        for (CustomPoi waypoint : selectedWaypoints) {
            deleteWaypoint(waypoint, false);
        }

        savedWaypoints.touched();

        McUtils.sendMessageToClient(Component.translatable(
                        "screens.wynntils.waypointManagementGui.deletedWaypoints", selectedWaypoints.size())
                .withStyle(ChatFormatting.GREEN));

        if (savedWaypoints.get().isEmpty()) {
            selectAllButton.active = false;
            filterButton.visible = false;
            iconSortButton.visible = false;
            nameSortButton.visible = false;
            xSortButton.visible = false;
            ySortButton.visible = false;
            zSortButton.visible = false;
        }

        // Scroll up 1 waypoint widget
        if (scrollOffset == Math.max(0, waypoints.size() - maxWaypointsToDisplay)) {
            scroll(-1);
        }

        // If any waypoint widgets were selected, deselect them all as they were deleted
        toggleSelectAll(false);
    }

    private void importFromClipboard() {
        String clipboard = McUtils.mc().keyboardHandler.getClipboard();

        CustomPoi[] newWaypoints;
        try {
            newWaypoints = Managers.Json.GSON.fromJson(clipboard, CustomPoi[].class);
        } catch (JsonSyntaxException e) {
            McUtils.sendErrorToClient(I18n.get("screens.wynntils.waypointManagementGui.import.error"));
            return;
        }

        if (newWaypoints == null) {
            McUtils.sendErrorToClient(I18n.get("screens.wynntils.waypointManagementGui.import.error"));
            return;
        }

        HiddenConfig<List<CustomPoi>> waypointsConfig =
                Managers.Feature.getFeatureInstance(MainMapFeature.class).customPois;
        List<CustomPoi> existingWaypoints = new ArrayList<>(waypointsConfig.get());

        // Only add waypoints that don't already exist
        List<CustomPoi> waypointsToAdd = Stream.of(newWaypoints)
                .filter(newWaypoint -> !existingWaypoints.contains(newWaypoint))
                .toList();

        existingWaypoints.addAll(waypointsToAdd);
        waypointsConfig.setValue(existingWaypoints);
        waypointsConfig.touched();
        // Managers.Feature.getFeatureInstance(MainMapFeature.class).updateWaypoints();

        // Enable search and filter after importing new waypoints
        if (!waypointsConfig.get().isEmpty()) {
            searchInput.visible = true;
            filterButton.visible = true;
        }

        updateAllUsedIcons();

        populateWaypoints();

        McUtils.sendMessageToClient(
                Component.translatable("screens.wynntils.waypointManagementGui.import.success", waypointsToAdd.size())
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

        McUtils.sendMessageToClient(Component.translatable(
                        "screens.wynntils.waypointManagementGui.exportedWaypoints", waypointsToExport.size())
                .withStyle(ChatFormatting.GREEN));
    }

    private void undoDelete() {
        HiddenConfig<List<CustomPoi>> allWaypoints =
                Managers.Feature.getFeatureInstance(MainMapFeature.class).customPois;

        // Potentially a waypoint that was deleted had been imported so don't want a duplicate
        if (!allWaypoints.get().contains(deletedWaypoints.getLast())) {
            allWaypoints.get().add(deletedIndexes.getLast(), deletedWaypoints.getLast());

            allWaypoints.touched();
            // Managers.Feature.getFeatureInstance(MainMapFeature.class).updateWaypoints();

            // Scroll up 1 waypoint widget if not already at the top of the list
            scrollOffset = Math.max(scrollOffset - 1, 0);

            updateAllUsedIcons();

            populateWaypoints();
        }

        deletedIndexes.removeLast();
        deletedWaypoints.removeLast();

        undoDeleteButton.active = !deletedIndexes.isEmpty();
    }

    public enum WaypointSortType {
        ICON,
        NAME,
        X,
        Y,
        Z
    }

    private enum WaypointSortOrder {
        ICON_ASC,
        ICON_DESC,
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
