/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.maps;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Services;
import com.wynntils.core.consumers.screens.WynntilsScreen;
import com.wynntils.core.text.StyledText;
import com.wynntils.screens.base.widgets.InfoButton;
import com.wynntils.screens.base.widgets.TextInputBoxWidget;
import com.wynntils.screens.maps.widgets.IconButton;
import com.wynntils.screens.maps.widgets.WaypointManagerWidget;
import com.wynntils.screens.maps.widgets.WaypointSortButton;
import com.wynntils.services.mapdata.features.builtin.WaypointLocation;
import com.wynntils.services.mapdata.type.MapIcon;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
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
import net.minecraft.network.chat.MutableComponent;

public final class WaypointManagementScreen extends WynntilsScreen {
    // Constants
    private static final float SCROLL_FACTOR = 10f;
    private static final int MAX_WIDGETS_PER_PAGE = 9;
    private static final int ICONS_PER_PAGE = 6;
    private static final int MAX_ICONS_NO_PAGE = 8;
    private static final int SCROLL_RENDER_X = 341;
    private static final int SCROLL_AREA_HEIGHT = 168;

    // Collections
    private final List<WaypointLocation> deletedWaypoints = new ArrayList<>();
    private final List<Integer> deletedIndexes = new ArrayList<>();
    private List<AbstractWidget> waypointManagerWidgets = new ArrayList<>();
    private List<AbstractWidget> iconButtons = new ArrayList<>();
    private List<WaypointLocation> selectedWaypoints = new ArrayList<>();
    private List<WaypointLocation> waypoints = new ArrayList<>();

    // Previous screen
    private final MainMapScreen oldMapScreen;

    // Widgets
    private Button deleteSelectedButton;
    private Button deselectAllButton;
    private Button downButton;
    private Button exportButton;
    private Button importButton;
    private Button removeMarkersButton;
    private Button selectAllButton;
    private Button setMarkersButton;
    private Button undoDeleteButton;
    private Button upButton;
    private Button nextIconButton;
    private Button previousIconButton;
    private WaypointSortButton activeSortButton;
    private WaypointSortButton iconSortButton;
    private WaypointSortButton nameSortButton;
    private WaypointSortButton xSortButton;
    private WaypointSortButton ySortButton;
    private WaypointSortButton zSortButton;
    private TextInputBoxWidget searchInput;

    // UI size, position etc
    private boolean draggingScroll = false;
    private float scrollY;
    private int iconsScrollOffset = 0;
    private int waypointsScrollOffset = 0;

    // Waypoint display
    private Map<String, Boolean> filteredIcons = new HashMap<>();
    private boolean selectionMode = false;
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
    protected void doInit() {
        // region exit button
        this.addRenderableWidget(
                new Button.Builder(Component.literal("X").withStyle(ChatFormatting.RED), (button) -> this.onClose())
                        .pos((int) (getTranslationX() + Texture.WAYPOINT_MANAGER_BACKGROUND.width() + 10), (int)
                                (getTranslationY() - 25))
                        .size(20, 20)
                        .build());
        // endregion

        // region info button
        this.addRenderableWidget(new InfoButton(
                (int) (getTranslationX() - 30),
                (int) (getTranslationY() - 25),
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
        importButton = new Button.Builder(
                        Component.translatable("screens.wynntils.waypointManagementGui.import"),
                        (button) -> importFromClipboard())
                .pos((width / 2) - 102, (int) (getTranslationY() + Texture.WAYPOINT_MANAGER_BACKGROUND.height() + 10))
                .size(60, 20)
                .tooltip(
                        Tooltip.create(Component.translatable("screens.wynntils.waypointManagementGui.import.tooltip")))
                .build();

        this.addRenderableWidget(importButton);

        exportButton = new Button.Builder(
                        Component.translatable("screens.wynntils.waypointManagementGui.export"),
                        (button) -> exportToClipboard())
                .pos((width / 2) + 42, (int) (getTranslationY() + Texture.WAYPOINT_MANAGER_BACKGROUND.height() + 10))
                .size(60, 20)
                .tooltip(Tooltip.create(
                        Component.translatable("screens.wynntils.waypointManagementGui.exportAll.tooltip")))
                .build();

        this.addRenderableWidget(exportButton);
        // endregion

        // region delete buttons
        undoDeleteButton = new Button.Builder(
                        Component.translatable("screens.wynntils.waypointManagementGui.undo"), (button) -> undoDelete())
                .pos((int) (getTranslationX() + Texture.WAYPOINT_MANAGER_BACKGROUND.width() + 10), (int)
                        (getTranslationY() + Texture.WAYPOINT_MANAGER_BACKGROUND.height() + 10))
                .size(90, 20)
                .build();

        this.addRenderableWidget(undoDeleteButton);

        deleteSelectedButton = new Button.Builder(
                        Component.translatable("screens.wynntils.waypointManagementGui.deleteSelected"),
                        (button) -> deleteSelectedWaypoints())
                .pos(
                        (int) (getTranslationX() + Texture.WAYPOINT_MANAGER_BACKGROUND.width() + 10),
                        (int) (getTranslationY() + Texture.WAYPOINT_MANAGER_BACKGROUND.height() + 10) - 30)
                .size(90, 20)
                .build();

        deleteSelectedButton.active = false;

        this.addRenderableWidget(deleteSelectedButton);
        // endregion

        // region add waypoint button
        this.addRenderableWidget(new Button.Builder(
                        Component.translatable("screens.wynntils.waypointManagementGui.add"),
                        (button) -> McUtils.mc().setScreen(WaypointCreationScreen.create(this)))
                .pos(
                        (int) (getTranslationX() + Texture.WAYPOINT_MANAGER_BACKGROUND.width() + 10),
                        (int) (getTranslationY() + Texture.WAYPOINT_MANAGER_BACKGROUND.height() + 10) - 60)
                .size(90, 20)
                .build());
        // endregion

        // region marker buttons
        setMarkersButton = new Button.Builder(
                        Component.translatable("screens.wynntils.waypointManagementGui.setMarkers"),
                        (button) -> toggleMarkers(true))
                .pos(
                        (int) (getTranslationX() - 100),
                        (int) (getTranslationY() + Texture.WAYPOINT_MANAGER_BACKGROUND.height() + 10) - 90)
                .size(90, 20)
                .tooltip(Tooltip.create(
                        Component.translatable("screens.wynntils.waypointManagementGui.setMarkers.tooltip")))
                .build();

        setMarkersButton.active = false;

        this.addRenderableWidget(setMarkersButton);

        removeMarkersButton = new Button.Builder(
                        Component.translatable("screens.wynntils.waypointManagementGui.removeMarkers"),
                        (button) -> toggleMarkers(false))
                .pos(
                        (int) (getTranslationX() - 100),
                        (int) (getTranslationY() + Texture.WAYPOINT_MANAGER_BACKGROUND.height() + 10) - 60)
                .size(90, 20)
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
                .pos((int) (getTranslationX() - 100), (int)
                        (getTranslationY() + Texture.WAYPOINT_MANAGER_BACKGROUND.height() + 10))
                .size(90, 20)
                .build();

        deselectAllButton.active = false;

        this.addRenderableWidget(deselectAllButton);

        selectAllButton = new Button.Builder(
                        Component.translatable("screens.wynntils.waypointManagementGui.selectAll"),
                        (button) -> toggleSelectAll(true))
                .pos(
                        (int) (getTranslationX() - 100),
                        (int) (getTranslationY() + Texture.WAYPOINT_MANAGER_BACKGROUND.height() + 10) - 30)
                .size(90, 20)
                .build();

        selectAllButton.active = !waypoints.isEmpty();

        this.addRenderableWidget(selectAllButton);
        // endregion

        // region up/down buttons
        upButton = new Button.Builder(Component.literal("🠝"), (button) -> updateSelectedWaypointPositions(-1))
                .pos((width / 2) - 22, (int) (getTranslationY() + Texture.WAYPOINT_MANAGER_BACKGROUND.height() + 10))
                .size(20, 20)
                .build();

        upButton.visible = false;

        this.addRenderableWidget(upButton);

        downButton = new Button.Builder(Component.literal("🠟"), (button) -> updateSelectedWaypointPositions(1))
                .pos((width / 2) + 2, (int) (getTranslationY() + Texture.WAYPOINT_MANAGER_BACKGROUND.height() + 10))
                .size(20, 20)
                .build();

        downButton.visible = false;

        this.addRenderableWidget(downButton);
        // endregion

        // region search bar
        searchInput = new TextInputBoxWidget(
                (int) (getTranslationX() + 5),
                (int) (getTranslationY() - 25),
                Texture.WAYPOINT_MANAGER_BACKGROUND.width() / 2,
                20,
                (s) -> {
                    waypointsScrollOffset = 0;
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
                (int) (getTranslationX() + 10),
                (int) (getTranslationY() + 4),
                iconTitleWidth,
                10,
                Component.translatable("screens.wynntils.waypointManagementGui.icon"),
                this,
                WaypointSortType.ICON));

        nameSortButton = this.addRenderableWidget(new WaypointSortButton(
                (int) (getTranslationX() + 78),
                (int) (getTranslationY() + 4),
                nameTitleWidth,
                10,
                Component.translatable("screens.wynntils.waypointManagementGui.name"),
                this,
                WaypointSortType.NAME));

        xSortButton = this.addRenderableWidget(new WaypointSortButton(
                (int) (getTranslationX() + 148),
                (int) (getTranslationY() + 4),
                coordinateTitleWidth,
                10,
                Component.literal("X"),
                this,
                WaypointSortType.X));

        ySortButton = this.addRenderableWidget(new WaypointSortButton(
                (int) (getTranslationX() + 178),
                (int) (getTranslationY() + 4),
                coordinateTitleWidth,
                10,
                Component.literal("Y"),
                this,
                WaypointSortType.Y));

        zSortButton = this.addRenderableWidget(new WaypointSortButton(
                (int) (getTranslationX() + 208),
                (int) (getTranslationY() + 4),
                coordinateTitleWidth,
                10,
                Component.literal("Z"),
                this,
                WaypointSortType.Z));
        // endregion

        if (deletedIndexes.isEmpty()) {
            undoDeleteButton.active = false;
        }

        waypoints = Services.Waypoints.getWaypoints();

        if (waypoints.isEmpty()) {
            searchInput.visible = false;
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
    public void onClose() {
        McUtils.mc().setScreen(oldMapScreen);
    }

    @Override
    public void doRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.doRender(guiGraphics, mouseX, mouseY, partialTick);
        PoseStack poseStack = guiGraphics.pose();
        renderScroll(poseStack);

        if (Services.Waypoints.getWaypoints().isEmpty()) {
            FontRenderer.getInstance()
                    .renderText(
                            poseStack,
                            StyledText.fromComponent(
                                    Component.translatable("screens.wynntils.waypointManagementGui.noWaypoints")),
                            getTranslationX() + Texture.WAYPOINT_MANAGER_BACKGROUND.width() / 2f,
                            getTranslationY() + Texture.WAYPOINT_MANAGER_BACKGROUND.height() / 2f,
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
                        getTranslationX() + 5,
                        getTranslationY() - 27.5f,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.BOTTOM,
                        TextShadow.NORMAL);

        if (filteredIcons.size() > 1) {
            FontRenderer.getInstance()
                    .renderText(
                            poseStack,
                            StyledText.fromComponent(
                                    Component.translatable("screens.wynntils.waypointManagementGui.filter")),
                            getTranslationX() + 15 + Texture.WAYPOINT_MANAGER_BACKGROUND.width() / 2f,
                            getTranslationY() - 27.5f,
                            CommonColors.WHITE,
                            HorizontalAlignment.LEFT,
                            VerticalAlignment.BOTTOM,
                            TextShadow.NORMAL);
        }

        if (waypoints.isEmpty()) {
            FontRenderer.getInstance()
                    .renderText(
                            poseStack,
                            StyledText.fromComponent(Component.translatable(
                                    "screens.wynntils.waypointManagementGui.noFilteredWaypoints")),
                            getTranslationX() + Texture.WAYPOINT_MANAGER_BACKGROUND.width() / 2f,
                            getTranslationY() + Texture.WAYPOINT_MANAGER_BACKGROUND.height() / 2f,
                            CommonColors.WHITE,
                            HorizontalAlignment.CENTER,
                            VerticalAlignment.MIDDLE,
                            TextShadow.NORMAL);
        } else {
            // FIXME
            // RenderUtils.enableScissor((int) (getTranslationX() + 12), (int) (getTranslationY() + 15), 320, 181);
            waypointManagerWidgets.forEach(widget -> widget.render(guiGraphics, mouseX, mouseY, partialTick));
            // RenderUtils.disableScissor();
        }

        iconButtons.forEach(widget -> widget.render(guiGraphics, mouseX, mouseY, partialTick));
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(guiGraphics, mouseX, mouseY, partialTick);

        RenderUtils.drawTexturedRect(
                guiGraphics.pose(), Texture.WAYPOINT_MANAGER_BACKGROUND, getTranslationX(), getTranslationY());
    }

    @Override
    public boolean doMouseClicked(double mouseX, double mouseY, int button) {
        if (!draggingScroll) {
            if (MathUtils.isInside(
                    (int) mouseX,
                    (int) mouseY,
                    (int) (getTranslationX() + SCROLL_RENDER_X),
                    (int) (getTranslationX() + SCROLL_RENDER_X + Texture.SCROLL_BUTTON.width()),
                    (int) scrollY,
                    (int) (scrollY + Texture.SCROLL_BUTTON.height()))) {
                draggingScroll = true;

                return true;
            }
        }

        for (AbstractWidget widget : Stream.concat(waypointManagerWidgets.stream(), iconButtons.stream())
                .toList()) {
            if (widget.isMouseOver(mouseX, mouseY)) {
                return widget.mouseClicked(mouseX, mouseY, button);
            }
        }

        return super.doMouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (!draggingScroll) return false;

        int scrollAreaStartY = (int) (getTranslationY() + 15 + 16);
        int scrollAreaHeight = SCROLL_AREA_HEIGHT - Texture.SCROLL_BUTTON.height();

        int newOffset = Math.round(MathUtils.map(
                (float) mouseY, scrollAreaStartY, scrollAreaStartY + scrollAreaHeight, 0, getMaxScrollOffset()));

        newOffset = Math.max(0, Math.min(newOffset, getMaxScrollOffset()));

        scroll(newOffset);

        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        draggingScroll = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY) {
        int scrollAmount = (int) (-deltaY * SCROLL_FACTOR);
        int newOffset = Math.max(0, Math.min(waypointsScrollOffset + scrollAmount, getMaxScrollOffset()));
        scroll(newOffset);

        return true;
    }

    public void selectWaypoint(WaypointLocation selectedWaypoint) {
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

    public void deleteWaypoint(WaypointLocation waypointToDelete) {
        int deletedWaypointIndex = Services.Waypoints.getWaypoints().indexOf(waypointToDelete);

        Services.Waypoints.removeWaypoint(waypointToDelete);

        deletedWaypoints.add(waypointToDelete);
        deletedIndexes.add(deletedWaypointIndex);

        undoDeleteButton.active = true;

        if (Services.Waypoints.getWaypoints().isEmpty()) {
            searchInput.visible = false;
            iconSortButton.visible = false;
            nameSortButton.visible = false;
            xSortButton.visible = false;
            ySortButton.visible = false;
            zSortButton.visible = false;
            selectAllButton.active = false;
            exportButton.active = false;
        }

        updateAllUsedIcons();
        populateWaypoints();
    }

    public void updateWaypointPosition(WaypointLocation waypointToMove, int direction) {
        int waypointToMoveIndex = waypoints.indexOf(waypointToMove);

        // If waypoint is at the top/bottom of list or if it was selected but then filtered out, don't move it
        if (waypointToMoveIndex == -1
                || waypointToMoveIndex + direction < 0
                || waypointToMoveIndex + direction > waypoints.size() - 1) {
            return;
        }

        WaypointLocation waypointToSwap = waypoints.get(waypoints.indexOf(waypointToMove) + direction);

        Services.Waypoints.reorderWaypoints(waypointToMove, waypointToSwap);
        populateWaypoints();
    }

    public void toggleIcon(String iconId, boolean used) {
        filteredIcons.put(iconId, used);

        populateWaypoints();
    }

    public List<WaypointLocation> getWaypoints() {
        return Collections.unmodifiableList(waypoints);
    }

    private void renderScroll(PoseStack poseStack) {
        if (waypoints.size() <= MAX_WIDGETS_PER_PAGE) return;

        scrollY = getTranslationY()
                + 15
                + MathUtils.map(
                        waypointsScrollOffset, 0, getMaxScrollOffset(), 0, 186 - Texture.SCROLL_BUTTON.height());

        RenderUtils.drawTexturedRect(poseStack, Texture.SCROLL_BUTTON, getTranslationX() + SCROLL_RENDER_X, scrollY);
    }

    private void scroll(int newOffset) {
        waypointsScrollOffset = newOffset;
        int currentY = (int) (getTranslationY() + 16);

        for (AbstractWidget widget : waypointManagerWidgets) {
            int newY = currentY - waypointsScrollOffset;
            widget.setY(newY);
            widget.visible = (newY <= getTranslationY() + 16 + 179) && (newY + 20 >= getTranslationY() + 16);
            currentY += 20;
        }
    }

    private int getMaxScrollOffset() {
        return (waypointManagerWidgets.size() - MAX_WIDGETS_PER_PAGE) * 20;
    }

    private void populateWaypoints() {
        waypointManagerWidgets = new ArrayList<>();

        // Get full list of waypoints
        waypoints = Services.Waypoints.getWaypoints().stream()
                .filter(waypoint ->
                        searchMatches(waypoint.getAttributes().get().getLabel().get()))
                .collect(Collectors.toList());

        // No waypoints
        if (waypoints.isEmpty()) return;

        // Filter waypoints based on filtered icons
        waypoints = waypoints.stream()
                .filter(waypoint -> filteredIcons.get(
                        waypoint.getAttributes().get().getIconId().orElse(MapIcon.NO_ICON_ID)))
                .collect(Collectors.toList());

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
        searchInput.visible = !Services.Waypoints.getWaypoints().isEmpty();

        // No filtered waypoints
        if (waypoints.isEmpty()) return;

        // Sort waypoints
        if (sortOrder != null) {
            sortWaypoints();
        }

        int renderX = (int) (getTranslationX() + 12);
        int renderY = (int) (getTranslationY() + 16);

        for (WaypointLocation waypoint : waypoints) {
            WaypointManagerWidget waypointManagerWidget = new WaypointManagerWidget(
                    renderX, renderY, 320, 20, waypoint, this, selectionMode, selectedWaypoints.contains(waypoint));

            waypointManagerWidget.visible = renderY <= getTranslationY() + 16 + 179;

            waypointManagerWidgets.add(waypointManagerWidget);
            renderY += 20;
        }

        scroll(Math.min(waypointsScrollOffset, Math.max(0, getMaxScrollOffset())));
    }

    private void sortWaypoints() {
        // Sort waypoints, ignore case
        switch (sortOrder) {
            case ICON_ASC ->
                waypoints.sort(Comparator.comparing(
                        waypoint -> waypoint.getAttributes().get().getIconId().orElse(MapIcon.NO_ICON_ID)));
            case ICON_DESC ->
                waypoints.sort(Comparator.comparing(
                        waypoint -> waypoint.getAttributes().get().getIconId().orElse(MapIcon.NO_ICON_ID),
                        Comparator.reverseOrder()));
            case NAME_ASC ->
                waypoints.sort(Comparator.comparing(
                        waypoint -> waypoint.getAttributes().get().getLabel().get(), String.CASE_INSENSITIVE_ORDER));
            case NAME_DESC ->
                waypoints.sort(Comparator.comparing(
                        waypoint -> waypoint.getAttributes().get().getLabel().get(),
                        String.CASE_INSENSITIVE_ORDER.reversed()));
            case X_ASC ->
                waypoints.sort(
                        Comparator.comparing(waypoint -> waypoint.getLocation().x()));
            case X_DESC ->
                waypoints.sort(
                        Comparator.comparing(waypoint -> waypoint.getLocation().x(), Comparator.reverseOrder()));
            case Y_ASC ->
                waypoints.sort(
                        Comparator.comparing(waypoint -> waypoint.getLocation().y()));
            case Y_DESC ->
                waypoints.sort(
                        Comparator.comparing(waypoint -> waypoint.getLocation().y(), Comparator.reverseOrder()));
            case Z_ASC ->
                waypoints.sort(
                        Comparator.comparing(waypoint -> waypoint.getLocation().z()));
            case Z_DESC ->
                waypoints.sort(
                        Comparator.comparing(waypoint -> waypoint.getLocation().z(), Comparator.reverseOrder()));
        }
    }

    private void toggleMarkers(boolean addMarkers) {
        if (addMarkers) {
            selectedWaypoints.forEach(Services.UserMarker::addUserMarkedFeature);
        } else {
            selectedWaypoints.forEach(Services.UserMarker::removeUserMarkedFeature);
        }
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
        filteredIcons = Services.Waypoints.getWaypoints().stream()
                .filter(waypointLocation ->
                        waypointLocation.getAttributes().get().getIconId().isPresent())
                .map(waypointLocation ->
                        waypointLocation.getAttributes().get().getIconId().get())
                .distinct()
                .collect(Collectors.toMap(
                        Function.identity(),
                        id -> filteredIcons.getOrDefault(id, true),
                        (existing, replacement) -> existing,
                        HashMap::new));

        populateIcons();
    }

    private void populateIcons() {
        iconButtons.forEach(this::removeWidget);
        iconButtons = new ArrayList<>();

        if (nextIconButton != null && previousIconButton != null) {
            this.removeWidget(nextIconButton);
            this.removeWidget(previousIconButton);
        }

        if (filteredIcons.size() > 1) {
            int renderX = (int) (getTranslationX() + 15 + Texture.WAYPOINT_MANAGER_BACKGROUND.width() / 2f);

            if (filteredIcons.size() > MAX_ICONS_NO_PAGE) {
                previousIconButton = new Button.Builder(Component.literal("<"), (b) -> {
                            if (iconsScrollOffset - 1 < 0) {
                                iconsScrollOffset = filteredIcons.size() - 1;
                            } else {
                                iconsScrollOffset--;
                            }

                            populateIcons();
                        })
                        .pos(renderX, (int) (getTranslationY() - 25))
                        .size(20, 20)
                        .build();
                this.addRenderableWidget(previousIconButton);

                renderX += 20;
            }

            int iconIndex;
            int iconsToDisplay = Math.min(
                    filteredIcons.size(),
                    filteredIcons.size() > MAX_ICONS_NO_PAGE ? ICONS_PER_PAGE : MAX_ICONS_NO_PAGE);

            for (int i = 0; i < iconsToDisplay; i++) {
                iconIndex = (iconsScrollOffset + i) % filteredIcons.size();
                String iconId = filteredIcons.keySet().stream().toList().get(iconIndex);

                if (iconId.equals(MapIcon.NO_ICON_ID)) {
                    IconButton iconButton =
                            new IconButton(renderX, (int) (getTranslationY() - 25), 20, filteredIcons.get(iconId));

                    iconButtons.add(iconButton);

                    renderX += 20;
                    continue;
                }

                Optional<MapIcon> mapIconOpt = Services.MapData.getIcon(iconId);

                if (mapIconOpt.isPresent()) {
                    IconButton iconButton = new IconButton(
                            renderX, (int) (getTranslationY() - 25), 20, mapIconOpt.get(), filteredIcons.get(iconId));

                    iconButtons.add(iconButton);

                    renderX += 20;
                } else {
                    WynntilsMod.warn("Waypoint has icon ID " + iconId + " but failed to retrieve");
                }
            }

            if (filteredIcons.size() > MAX_ICONS_NO_PAGE) {
                nextIconButton = new Button.Builder(Component.literal(">"), (b) -> {
                            if (iconsScrollOffset + 1 >= filteredIcons.size()) {
                                iconsScrollOffset = 0;
                            } else {
                                iconsScrollOffset++;
                            }

                            populateIcons();
                        })
                        .pos(renderX, (int) (getTranslationY() - 25))
                        .size(20, 20)
                        .build();
                this.addRenderableWidget(nextIconButton);
            }
        }
    }

    private boolean searchMatches(String waypointName) {
        return StringUtils.partialMatch(waypointName, searchInput.getTextBoxInput());
    }

    private void updateSelectedWaypointPositions(int direction) {
        // Get selected waypoints in the same order they are in waypoints
        List<WaypointLocation> orderedWaypoints = waypoints.stream()
                .filter(waypoint -> selectedWaypoints.contains(waypoint))
                .collect(Collectors.toList());

        // If we are shifting waypoints down the list needs to be reversed
        if (direction == 1) {
            Collections.reverse(orderedWaypoints);
        }

        for (WaypointLocation selectedWaypoint : orderedWaypoints) {
            updateWaypointPosition(selectedWaypoint, direction);
        }
    }

    private void deleteSelectedWaypoints() {
        for (WaypointLocation waypoint : selectedWaypoints) {
            deleteWaypoint(waypoint);
        }

        McUtils.sendMessageToClient(Component.translatable(
                        "screens.wynntils.waypointManagementGui.deletedWaypoints", selectedWaypoints.size())
                .withStyle(ChatFormatting.GREEN));

        if (Services.Waypoints.getWaypoints().isEmpty()) {
            selectAllButton.active = false;
            iconSortButton.visible = false;
            nameSortButton.visible = false;
            xSortButton.visible = false;
            ySortButton.visible = false;
            zSortButton.visible = false;
        }

        // If any waypoint widgets were selected, deselect them all as they were deleted
        toggleSelectAll(false);
    }

    private void importFromClipboard() {
        int importedWaypoints = Services.Waypoints.importWaypoints();

        MutableComponent importTooltip = Component.translatable("screens.wynntils.waypointManagementGui.import.tooltip")
                .withStyle(ChatFormatting.WHITE)
                .append(Component.literal("\n"));

        // Still accept 0 as that was not a failure
        if (importedWaypoints >= 0) {
            // Enable search after importing new waypoints
            if (!Services.Waypoints.getWaypoints().isEmpty()) {
                searchInput.visible = true;
            }

            updateAllUsedIcons();
            populateWaypoints();

            importTooltip.append(Component.translatable("service.wynntils.waypoint.importSuccess", importedWaypoints)
                    .withStyle(ChatFormatting.GREEN));
        } else {
            importTooltip.append(Component.translatable("service.wynntils.waypoint.importError")
                    .withStyle(ChatFormatting.RED));
        }

        importButton.setTooltip(Tooltip.create(importTooltip));
    }

    private void exportToClipboard() {
        List<WaypointLocation> waypointsToExport =
                selectedWaypoints.isEmpty() ? Services.Waypoints.getWaypoints() : selectedWaypoints;

        McUtils.mc()
                .keyboardHandler
                .setClipboard(waypointsToExport.stream()
                        .map(Managers.Json.GSON::toJson)
                        .toList()
                        .toString());

        MutableComponent exportTooltip;

        if (selectedWaypoints.isEmpty()) {
            exportTooltip = Component.translatable("screens.wynntils.waypointManagementGui.exportAll.tooltip");
        } else {
            exportTooltip = Component.translatable(
                    "screens.wynntils.waypointManagementGui.exportSelected.tooltip", selectedWaypoints.size());
        }

        exportTooltip
                .append(Component.literal("\n"))
                .append(Component.translatable(
                                "screens.wynntils.waypointManagementGui.exportedWaypoints", waypointsToExport.size())
                        .withStyle(ChatFormatting.GREEN));

        exportButton.setTooltip(Tooltip.create(exportTooltip));
    }

    private void undoDelete() {
        List<WaypointLocation> allWaypoints = Services.Waypoints.getWaypoints();

        // Potentially a waypoint that was deleted had been imported so don't want a duplicate
        if (!allWaypoints.contains(deletedWaypoints.getLast())) {
            Services.Waypoints.addWaypointAtIndex(deletedWaypoints.getLast(), deletedIndexes.getLast());

            // Scroll up 1 waypoint widget if not already at the top of the list
            waypointsScrollOffset = Math.max(waypointsScrollOffset - 1, 0);

            updateAllUsedIcons();
            populateWaypoints();
        }

        deletedIndexes.removeLast();
        deletedWaypoints.removeLast();

        undoDeleteButton.active = !deletedIndexes.isEmpty();
    }

    private float getTranslationX() {
        return (this.width - Texture.WAYPOINT_MANAGER_BACKGROUND.width()) / 2f;
    }

    private float getTranslationY() {
        return (this.height - Texture.WAYPOINT_MANAGER_BACKGROUND.height()) / 2f;
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
