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
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
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
    private static final int GRID_ROWS_PER_PAGE = 44;
    private static final int HEADER_HEIGHT = 12;
    private static final int STARTING_WIDGET_ROW = 13;

    private Map<Texture, Boolean> filteredIcons = new EnumMap<>(Texture.class);
    private final List<AbstractWidget> poiManagerWidgets = new ArrayList<>();
    private final List<BasicTexturedButton> filterButtons = new ArrayList<>();
    private final List<CustomPoi> deletedPois = new ArrayList<>();
    private final List<Integer> deletedIndexes = new ArrayList<>();
    private final MainMapScreen oldMapScreen;

    private boolean draggingScroll = false;
    private boolean selectionMode = false;
    private Button activeSortButton;
    private Button deleteSelectedButton;
    private Button deselectAllButton;
    private Button exportButton;
    private Button filterAllButton;
    private Button nameSortButton;
    private Button selectAllButton;
    private Button undoDeleteButton;
    private Button xSortButton;
    private Button ySortButton;
    private Button zSortButton;
    private float backgroundHeight;
    private float backgroundWidth;
    private float backgroundX;
    private float backgroundY;
    private float dividedHeight;
    private float dividedWidth;
    private float scrollButtonHeight;
    private float scrollButtonRenderY;
    private int bottomDisplayedIndex;
    private int maxPoisToDisplay;
    private int scrollOffset = 0;
    private int topDisplayedIndex = 0;
    private List<CustomPoi> selectedWaypoints = new ArrayList<>();
    private List<CustomPoi> waypoints;
    private PoiSortOrder sortOrder;
    private TextInputBoxWidget focusedTextInput;
    private TextInputBoxWidget searchInput;
    // Remove when finished
    private boolean renderGrid = false;

    // Remove when finished
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

        scrollButtonHeight = (dividedWidth / Texture.SCROLL_BUTTON.width()) * Texture.SCROLL_BUTTON.height();

        int importExportButtonWidth = (int) (dividedWidth * 6);

        this.addRenderableWidget(
                new Button.Builder(Component.literal("X").withStyle(ChatFormatting.RED), (button) -> this.onClose())
                        .pos((int) (dividedWidth * 60), (int) (dividedHeight * 4))
                        .size(20, 20)
                        .build());

        // Remove when finished
        this.addRenderableWidget(
                new Button.Builder(Component.literal("Grid").withStyle(ChatFormatting.BLUE), (button) -> toggleGrid())
                        .pos((int) (dividedWidth * 2), (int) (dividedHeight * 4))
                        .size(40, 20)
                        .build());

        // region import/export
        this.addRenderableWidget(new Button.Builder(
                        Component.translatable("screens.wynntils.poiManagementGui.import"),
                        (button) -> importFromClipboard())
                .pos((int) (dividedWidth * 22), (int) (dividedHeight * 58))
                .size(importExportButtonWidth, 20)
                .tooltip(Tooltip.create(Component.translatable("screens.wynntils.poiManagementGui.import.tooltip")))
                .build());

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
                        Component.translatable("screens.wynntils.poiManagementGui.deselectAll"), (button) -> {
                            toggleSelectAll(false);
                        })
                .pos((int) dividedWidth, (int) (dividedHeight * 58))
                .size((int) (dividedWidth * 8), 20)
                .build();

        deselectAllButton.active = false;

        this.addRenderableWidget(deselectAllButton);

        selectAllButton = new Button.Builder(
                        Component.translatable("screens.wynntils.poiManagementGui.selectAll"), (button) -> {
                            toggleSelectAll(true);
                        })
                .pos((int) dividedWidth, (int) (dividedHeight * 58) - 25)
                .size((int) (dividedWidth * 8), 20)
                .build();

        this.addRenderableWidget(selectAllButton);
        // endregion

        // region search bar
        searchInput = new TextInputBoxWidget(
                (int) (dividedWidth * 14) - 5,
                (int) (dividedHeight * 3),
                (int) (dividedWidth * 18),
                20,
                (s) -> {
                    topDisplayedIndex = 0;
                    scrollOffset = 0;
                    populatePois();
                },
                this);

        this.addRenderableWidget(searchInput);

        setFocusedTextInput(searchInput);
        // endregion

        // region filter all button
        filterAllButton = this.addRenderableWidget(new Button.Builder(
                        Component.literal("*").withStyle(ChatFormatting.GREEN), (button) -> {
                            filteredIcons.replaceAll((key, value) -> false);
                            topDisplayedIndex = 0;
                            scrollOffset = 0;
                            button.active = false;
                            populatePois();
                        })
                .pos((int) (dividedWidth * 32) + 5, (int) (dividedHeight * 3))
                .size(20, 20)
                .tooltip(Tooltip.create(Component.translatable("screens.wynntils.poiManagementGui.filterAll.tooltip")))
                .build());

        filterAllButton.active = false;
        // endregion

        // region sort buttons
        int nameTitleWidth = (McUtils.mc()
                .font
                .width(StyledText.fromComponent(Component.translatable("screens.wynntils.poiManagementGui.name"))
                        .getString()));

        nameSortButton = this.addRenderableWidget(new Button.Builder(
                        Component.literal("").withStyle(ChatFormatting.GREEN), (button) -> {
                            toggleSortType(PoiSortType.NAME);
                        })
                .pos((int) (dividedWidth * 24) + (nameTitleWidth / 2), (int) (dividedHeight * HEADER_HEIGHT) - 12)
                .size(12, 12)
                .build());

        int coordinateTitleWidth = (McUtils.mc().font.width("X"));

        xSortButton = this.addRenderableWidget(new Button.Builder(
                        Component.literal("").withStyle(ChatFormatting.GREEN), (button) -> {
                            toggleSortType(PoiSortType.X);
                        })
                .pos((int) (dividedWidth * 34) + (coordinateTitleWidth / 2), (int) (dividedHeight * HEADER_HEIGHT) - 12)
                .size(12, 12)
                .build());

        ySortButton = this.addRenderableWidget(new Button.Builder(
                        Component.literal("").withStyle(ChatFormatting.GREEN), (button) -> {
                            toggleSortType(PoiSortType.Y);
                        })
                .pos((int) (dividedWidth * 37) + (coordinateTitleWidth / 2), (int) (dividedHeight * HEADER_HEIGHT) - 12)
                .size(12, 12)
                .build());

        zSortButton = this.addRenderableWidget(new Button.Builder(
                        Component.literal("").withStyle(ChatFormatting.GREEN), (button) -> {
                            toggleSortType(PoiSortType.Z);
                        })
                .pos((int) (dividedWidth * 40) + (coordinateTitleWidth / 2), (int) (dividedHeight * HEADER_HEIGHT) - 12)
                .size(12, 12)
                .build());
        // endregion

        if (deletedIndexes.isEmpty()) {
            undoDeleteButton.active = false;
        }

        waypoints = Managers.Feature.getFeatureInstance(MainMapFeature.class)
                .customPois
                .get();

        if (waypoints.isEmpty()) {
            searchInput.visible = false;
            exportButton.active = false;
        }

        bottomDisplayedIndex = Math.min(maxPoisToDisplay, waypoints.size() - 1);

        populatePois();

        createIconFilterButtons();
    }

    @Override
    public void doRender(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        renderBackground(poseStack);
        renderScrollButton(poseStack);
        super.doRender(poseStack, mouseX, mouseY, partialTick);

        if (renderGrid) {
            RenderUtils.renderDebugGrid(poseStack, GRID_DIVISIONS, dividedWidth, dividedHeight);
        }

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

            searchInput.visible = false;

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

        if (filteredIcons.size() > 1) {
            FontRenderer.getInstance()
                    .renderText(
                            poseStack,
                            StyledText.fromComponent(
                                    Component.translatable("screens.wynntils.poiManagementGui.filter")),
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
                        StyledText.fromString("X"),
                        (int) (dividedWidth * 34),
                        (int) (dividedHeight * HEADER_HEIGHT),
                        CommonColors.WHITE,
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.BOTTOM,
                        TextShadow.NORMAL);

        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        StyledText.fromString("Y"),
                        (int) (dividedWidth * 37),
                        (int) (dividedHeight * HEADER_HEIGHT),
                        CommonColors.WHITE,
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.BOTTOM,
                        TextShadow.NORMAL);

        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        StyledText.fromString("Z"),
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

    public void populatePois() {
        for (AbstractWidget widget : poiManagerWidgets) {
            this.removeWidget(widget);
        }

        this.poiManagerWidgets.clear();

        waypoints = Managers.Feature.getFeatureInstance(MainMapFeature.class).customPois.get().stream()
                .filter(poi -> searchMatches(poi.getName()))
                .collect(Collectors.toList());

        searchInput.visible = !waypoints.isEmpty();

        if (!waypoints.isEmpty()) {
            exportButton.active = true;
        }

        if (filteredIcons.containsValue(true)) {
            waypoints = waypoints.stream()
                    .filter(poi -> filteredIcons.getOrDefault(poi.getIcon(), false))
                    .collect(Collectors.toList());
        }

        if (sortOrder != null) {
            switch (sortOrder) {
                case NAME_ASC -> waypoints.sort(
                        Comparator.comparing(CustomPoi::getName, String.CASE_INSENSITIVE_ORDER));
                case NAME_DESC -> waypoints.sort(Comparator.comparing(CustomPoi::getName, String.CASE_INSENSITIVE_ORDER)
                        .reversed());
                case X_ASC -> waypoints.sort(
                        Comparator.comparing(poi -> poi.getLocation().getX()));
                case X_DESC -> waypoints.sort(
                        Comparator.comparing(poi -> poi.getLocation().getX(), Comparator.reverseOrder()));
                case Y_ASC -> waypoints.sort(Comparator.comparing(
                        poi -> poi.getLocation().getY().orElse(null), Comparator.nullsLast(Comparator.naturalOrder())));
                case Y_DESC -> waypoints.sort(Comparator.comparing(
                        poi -> poi.getLocation().getY().orElse(null), Comparator.nullsFirst(Comparator.reverseOrder())));
                case Z_ASC -> waypoints.sort(
                        Comparator.comparing(poi -> poi.getLocation().getZ()));
                case Z_DESC -> waypoints.sort(
                        Comparator.comparing(poi -> poi.getLocation().getZ(), Comparator.reverseOrder()));
            }
        }

        int row = (int) ((int) (dividedHeight * (STARTING_WIDGET_ROW - 1)) + (dividedHeight / 2f));

        for (int i = 0; i < maxPoisToDisplay; i++) {
            if ((topDisplayedIndex + i) > waypoints.size() - 1) {
                break;
            }

            bottomDisplayedIndex = (topDisplayedIndex + i) + scrollOffset;

            CustomPoi poi = waypoints.get(bottomDisplayedIndex);

            PoiManagerWidget poiWidget = new PoiManagerWidget(
                    (int) (dividedWidth * 14),
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

            Component tooltip = add ? Component.translatable(
                    "screens.wynntils.poiManagementGui.exportSelected.tooltip", selectedWaypoints.size()) : Component.translatable("screens.wynntils.poiManagementGui.exportAll.tooltip");

            exportButton.setTooltip(Tooltip.create(tooltip));
        }

        populatePois();
    }

    public void createIconFilterButtons() {
        for (BasicTexturedButton filterButton : filterButtons) {
            this.removeWidget(filterButton);
        }

        List<CustomPoi> allWaypoints = Managers.Feature.getFeatureInstance(MainMapFeature.class)
                .customPois
                .get();

        filteredIcons = Services.Poi.POI_ICONS.stream()
                .filter(texture -> allWaypoints.stream()
                        .map(CustomPoi::getIcon)
                        .anyMatch(customPoiTexture -> texture == customPoiTexture))
                .collect(Collectors.toMap(Function.identity(), texture -> false, (e1, e2) -> e1, LinkedHashMap::new));

        if (filteredIcons.size() < 2) {
            filterAllButton.visible = false;
            return;
        } else {
            filterAllButton.visible = true;
        }

        int xOffset = 22;

        for (Texture icon : filteredIcons.keySet()) {
            BasicTexturedButton filterButton = new BasicTexturedButton(
                    (int) (dividedWidth * 32) + xOffset + 5,
                    (int) (dividedHeight * 3 + 10 - icon.height() / 2f),
                    20,
                    20,
                    icon,
                    (b) -> {
                        topDisplayedIndex = 0;
                        scrollOffset = 0;
                        toggleIcon(icon);
                        populatePois();
                    },
                    filteredIcons.getOrDefault(icon, false)
                            ? List.of(Component.translatable("screens.wynntils.poiManagementGui.filterExclude.tooltip"))
                            : List.of(
                            Component.translatable("screens.wynntils.poiManagementGui.filterInclude.tooltip")));

            filterButtons.add(filterButton);

            this.addRenderableWidget(filterButton);

            xOffset += 22;
        }
    }

    public void setLastDeletedPoi(CustomPoi deletedPoi, int deletedPoiIndex) {
        deletedPois.add(deletedPoi);
        deletedIndexes.add(deletedPoiIndex);

        undoDeleteButton.active = true;

        // Handles deleting when the final poi is visible
        if (scrollOffset == Math.max(0, waypoints.size() - maxPoisToDisplay)) {
            setScrollOffset(1);
        }
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
                    && mouseX <= scrollButtonRenderX + dividedWidth
                    && mouseY >= scrollButtonRenderY
                    && mouseY <= scrollButtonRenderY + scrollButtonHeight) {
                draggingScroll = true;
            }
        }

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

        int newValue = (int) MathUtils.map(
                (float) mouseY,
                (int) (dividedHeight * 10),
                (int) (dividedHeight * 53),
                0,
                Math.max(0, waypoints.size() - maxPoisToDisplay));

        setScrollOffset(-newValue + scrollOffset);

        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    private void renderScrollButton(PoseStack poseStack) {
        scrollButtonRenderY =
                MathUtils.map(scrollOffset, 0, waypoints.size() - maxPoisToDisplay, (int) (dividedHeight * 10), (int)
                        (dividedHeight * 51));

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
        scrollOffset = MathUtils.clamp(scrollOffset - delta, 0, Math.max(0, waypoints.size() - maxPoisToDisplay));

        populatePois();
    }

    private void toggleSortType(PoiSortType sortType) {
        if (activeSortButton != null) {
            activeSortButton.setMessage(Component.literal(""));
        }

        switch (sortType) {
            case NAME -> {
                activeSortButton = nameSortButton;

                if (sortOrder == PoiSortOrder.NAME_ASC) {
                    sortOrder = PoiSortOrder.NAME_DESC;
                } else if (sortOrder == PoiSortOrder.NAME_DESC) {
                    sortOrder = null;
                    break;
                } else {
                    sortOrder = PoiSortOrder.NAME_ASC;
                }

                nameSortButton.setMessage(
                        sortOrder == PoiSortOrder.NAME_ASC ? Component.literal("v") : Component.literal("ʌ"));
            }
            case X -> {
                activeSortButton = xSortButton;

                if (sortOrder == PoiSortOrder.X_ASC) {
                    sortOrder = PoiSortOrder.X_DESC;
                } else if (sortOrder == PoiSortOrder.X_DESC) {
                    sortOrder = null;
                    break;
                } else {
                    sortOrder = PoiSortOrder.X_ASC;
                }

                xSortButton.setMessage(
                        sortOrder == PoiSortOrder.X_ASC ? Component.literal("v") : Component.literal("ʌ"));
            }
            case Y -> {
                activeSortButton = ySortButton;

                if (sortOrder == PoiSortOrder.Y_ASC) {
                    sortOrder = PoiSortOrder.Y_DESC;
                } else if (sortOrder == PoiSortOrder.Y_DESC) {
                    sortOrder = null;
                    break;
                } else {
                    sortOrder = PoiSortOrder.Y_ASC;
                }

                ySortButton.setMessage(
                        sortOrder == PoiSortOrder.Y_ASC ? Component.literal("v") : Component.literal("ʌ"));
            }
            case Z -> {
                activeSortButton = zSortButton;

                if (sortOrder == PoiSortOrder.Z_ASC) {
                    sortOrder = PoiSortOrder.Z_DESC;
                } else if (sortOrder == PoiSortOrder.Z_DESC) {
                    sortOrder = null;
                    break;
                } else {
                    sortOrder = PoiSortOrder.Z_ASC;
                }

                zSortButton.setMessage(
                        sortOrder == PoiSortOrder.Z_ASC ? Component.literal("v") : Component.literal("ʌ"));
            }
        }

        populatePois();
    }

    private void toggleSelectAll(boolean select) {
        selectionMode = select;

        selectAllButton.active = !select;
        deselectAllButton.active = select;
        deleteSelectedButton.active = select;

        Component tooltip;

        if (select) {
            selectedWaypoints = waypoints;

            tooltip = Component.translatable("screens.wynntils.poiManagementGui.exportSelected.tooltip");
        } else {
            selectedWaypoints.clear();

            tooltip = Component.translatable("screens.wynntils.poiManagementGui.exportAll.tooltip");
        }

        exportButton.setTooltip(Tooltip.create(tooltip));

        populatePois();
    }

    private void toggleIcon(Texture icon) {
        filteredIcons.put(icon, !filteredIcons.get(icon));

        if (!filteredIcons.containsValue(false)) {
            filteredIcons.replaceAll((key, value) -> false);
        }

        filterAllButton.active = filteredIcons.containsValue(true);

        selectedWaypoints.clear();
        deselectAllButton.active = false;
        deleteSelectedButton.active = false;
    }

    private boolean searchMatches(String poiName) {
        return StringUtils.partialMatch(poiName, searchInput.getTextBoxInput());
    }

    private void deleteSelectedPois() {
        HiddenConfig<List<CustomPoi>> customPoiConfig =
                Managers.Feature.getFeatureInstance(MainMapFeature.class).customPois;
        List<CustomPoi> allPois = customPoiConfig.get();

        List<CustomPoi> newPois = allPois.stream()
                .filter(customPoi -> !selectedWaypoints.contains(customPoi))
                .toList();

        customPoiConfig.setValue(newPois);

        customPoiConfig.touched();

        selectedWaypoints.clear();
        selectionMode = false;

        deselectAllButton.active = false;
        deleteSelectedButton.active = false;

        exportButton.setTooltip(
                Tooltip.create(Component.translatable("screens.wynntils.poiManagementGui.exportAll.tooltip")));

        if (scrollOffset == Math.max(0, waypoints.size() - maxPoisToDisplay)) {
            setScrollOffset(1);
        }

        populatePois();

        createIconFilterButtons();

        McUtils.sendMessageToClient(
                Component.translatable("screens.wynntils.poiManagementGui.deletedPois", allPois.size() - newPois.size())
                        .withStyle(ChatFormatting.GREEN));
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

        populatePois();

        createIconFilterButtons();

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

        createIconFilterButtons();
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

    private enum PoiSortType {
        NAME,
        X,
        Y,
        Z
    }
}
