/*
 * Copyright © Wynntils 2024-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.territorymanagement;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Models;
import com.wynntils.core.components.Services;
import com.wynntils.core.persisted.storage.Storage;
import com.wynntils.core.text.StyledText;
import com.wynntils.features.map.GuildMapFeature;
import com.wynntils.features.ui.CustomTerritoryManagementScreenFeature;
import com.wynntils.handlers.wrappedscreen.WrappedScreen;
import com.wynntils.handlers.wrappedscreen.type.WrappedScreenInfo;
import com.wynntils.models.emeralds.type.EmeraldUnits;
import com.wynntils.models.items.items.gui.TerritoryItem;
import com.wynntils.models.territories.TerritoryInfo;
import com.wynntils.models.territories.profile.TerritoryProfile;
import com.wynntils.models.territories.type.GuildResource;
import com.wynntils.models.territories.type.TerritoryUpgrade;
import com.wynntils.screens.base.TooltipProvider;
import com.wynntils.screens.base.widgets.BasicTexturedButton;
import com.wynntils.screens.base.widgets.ItemFilterUIButton;
import com.wynntils.screens.base.widgets.ItemSearchWidget;
import com.wynntils.screens.maps.AbstractMapScreen;
import com.wynntils.screens.maps.widgets.MapButton;
import com.wynntils.screens.territorymanagement.mapdata.ManageTerritoryArea;
import com.wynntils.screens.territorymanagement.mapdata.ManageTerritoryProvider;
import com.wynntils.screens.territorymanagement.widgets.GuildOverallProductionWidget;
import com.wynntils.screens.territorymanagement.widgets.TerritoryApplyLoadoutButton;
import com.wynntils.screens.territorymanagement.widgets.TerritoryHighlightLegendWidget;
import com.wynntils.screens.territorymanagement.widgets.TerritoryWidget;
import com.wynntils.screens.territorymanagement.widgets.quickfilters.TerritoryBonusesQuickFilterWidget;
import com.wynntils.screens.territorymanagement.widgets.quickfilters.TerritoryDefenseQuickFilterWidget;
import com.wynntils.screens.territorymanagement.widgets.quickfilters.TerritoryProducesQuickFilterWidget;
import com.wynntils.screens.territorymanagement.widgets.quickfilters.TerritoryQuickFilterWidget;
import com.wynntils.screens.territorymanagement.widgets.quicksorts.TerritoryDefenseQuickSortWidget;
import com.wynntils.screens.territorymanagement.widgets.quicksorts.TerritoryOverallProductionQuickSortWidget;
import com.wynntils.screens.territorymanagement.widgets.quicksorts.TerritoryQuickSortWidget;
import com.wynntils.screens.territorymanagement.widgets.quicksorts.TerritoryTreasuryQuickSortWidget;
import com.wynntils.services.itemfilter.type.ItemProviderType;
import com.wynntils.services.map.type.TerritoryInfoType;
import com.wynntils.services.mapdata.MapFeatureRenderer;
import com.wynntils.services.mapdata.features.type.MapFeature;
import com.wynntils.utils.MathUtils;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.mc.type.PoiLocation;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.MapRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.state.ColoredLineBatchRenderState;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import com.wynntils.utils.type.CappedValue;
import com.wynntils.utils.type.Pair;
import com.wynntils.utils.wynn.ContainerUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3x2f;
import org.joml.Vector2f;
import org.lwjgl.glfw.GLFW;

public class TerritoryManagementScreen extends AbstractMapScreen implements WrappedScreen {
    // Constants
    // The render area is the area where the territories are rendered
    private static final Pair<Integer, Integer> RENDER_AREA_POSITION = new Pair<>(9, 16);
    private static final Pair<Integer, Integer> RENDER_AREA_SIZE = new Pair<>(221, 110);
    private static final int TERRITORY_SIZE = 20;
    private static final int TERRITORIES_PER_ROW = RENDER_AREA_SIZE.a() / TERRITORY_SIZE;
    private static final int BACK_BUTTON_SLOT = 18;
    private static final int APPLY_BUTTON_SLOT = 0;
    private static final int LOADOUT_BUTTON_SLOT = 36;
    private static final int QUICK_FILTER_WIDTH = 150;
    private static final Set<TerritoryUpgrade> DEFENSE_UPGRADES = Set.of(
            TerritoryUpgrade.DAMAGE,
            TerritoryUpgrade.ATTACK,
            TerritoryUpgrade.HEALTH,
            TerritoryUpgrade.DEFENCE,
            TerritoryUpgrade.STRONGER_MINIONS,
            TerritoryUpgrade.TOWER_MULTI_ATTACKS,
            TerritoryUpgrade.TOWER_AURA,
            TerritoryUpgrade.TOWER_VOLLEY);

    // Map mode
    private static TerritoryInfoType infoType = TerritoryInfoType.DEFENSE;
    private boolean mapMode = false;
    private MapButton infoTypeButton;

    // Territory items
    private List<Pair<ItemStack, TerritoryItem>> territoryItems = new ArrayList<>();

    // Widgets
    private final List<AbstractWidget> renderAreaWidgets = new ArrayList<>();
    private final List<TerritoryQuickFilterWidget> quickFilters = new ArrayList<>();
    private final List<TerritoryQuickSortWidget> quickSorts = new ArrayList<>();
    private ItemSearchWidget itemSearchWidget;

    // Scroll fields
    // Scroll offset is measured in pixels
    private float scrollOffset = 0;
    private boolean draggingScroll = false;

    // WrappedScreen fields
    private final WrappedScreenInfo wrappedScreenInfo;
    private final TerritoryManagementHolder holder;

    public TerritoryManagementScreen(WrappedScreenInfo wrappedScreenInfo, TerritoryManagementHolder holder) {
        super();
        this.wrappedScreenInfo = wrappedScreenInfo;
        this.holder = holder;
    }

    public void setMapMode(boolean mapMode) {
        this.mapMode = mapMode;
    }

    @Override
    protected boolean shouldRenderMapControls() {
        return mapMode;
    }

    public void setMapPosition(float centerX, float centerZ, float zoomLevel) {
        this.setZoomLevel(zoomLevel);
        this.updateMapCenter(centerX, centerZ);
    }

    @Override
    public WrappedScreenInfo getWrappedScreenInfo() {
        return wrappedScreenInfo;
    }

    @Override
    protected void doInit() {
        super.doInit();

        if (!mapMode) {
            initListScreen();
        } else {
            initMapScreen();
        }

        this.addRenderableOnly(new GuildOverallProductionWidget(
                mapMode ? (int) SCREEN_SIDE_OFFSET + 10 : getRenderX() - 190,
                mapMode ? (int) SCREEN_SIDE_OFFSET + 35 : getRenderY() + 10,
                200,
                150,
                holder));

        if (firstInit) {
            // When outside the main map, center to the middle of the map
            if (!isPlayerInsideMainArea()) {
                centerMapOnWorld();
            }

            firstInit = false;
        }

        updateTerritoryItems();
    }

    private void initListScreen() {
        ItemSearchWidget oldWidget = itemSearchWidget;

        itemSearchWidget = new ItemSearchWidget(
                getRenderX(),
                getRenderY() - 20,
                Texture.TERRITORY_MANAGEMENT_BACKGROUND.width() - 25,
                20,
                List.of(ItemProviderType.TERRITORY),
                true,
                (q) -> populateRenderAreaWidgets(),
                this);
        this.addRenderableWidget(itemSearchWidget);
        itemSearchWidget.setTextBoxInput(
                oldWidget == null ? "" : oldWidget.getSearchQuery().queryString());

        this.addRenderableWidget(new ItemFilterUIButton(
                getRenderX() + Texture.TERRITORY_MANAGEMENT_BACKGROUND.width() - 20,
                getRenderY() - 20,
                itemSearchWidget,
                this,
                true,
                List.of(ItemProviderType.TERRITORY)));

        // Territory Highlighter Legend
        this.addRenderableWidget(new TerritoryHighlightLegendWidget(
                getRenderX(),
                getRenderY() + Texture.TERRITORY_MANAGEMENT_BACKGROUND.height() + 5,
                Texture.TERRITORY_MANAGEMENT_BACKGROUND.width(),
                110,
                holder));

        // Back button in the sidebar
        this.addRenderableWidget(new BasicTexturedButton(
                getRenderX() - 20,
                getRenderY() + 5,
                Texture.ARROW_LEFT_ICON.width(),
                Texture.ARROW_LEFT_ICON.height(),
                Texture.ARROW_LEFT_ICON,
                (button) -> ContainerUtils.clickOnSlot(
                        BACK_BUTTON_SLOT,
                        wrappedScreenInfo.containerId(),
                        button,
                        wrappedScreenInfo.containerMenu().getItems()),
                List.of(Component.translatable("gui.back").withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD)),
                false));

        // Territory production tooltip disable button
        this.addRenderableWidget(new BasicTexturedButton(
                getRenderX() - 20,
                getRenderY() + 75,
                Texture.DEFENSE_FILTER_ICON.width(),
                Texture.DEFENSE_FILTER_ICON.height(),
                Texture.DEFENSE_FILTER_ICON,
                (button) -> {
                    Storage<Boolean> screenTerritoryProductionTooltip = Managers.Feature.getFeatureInstance(
                                    CustomTerritoryManagementScreenFeature.class)
                            .screenTerritoryProductionTooltip;
                    screenTerritoryProductionTooltip.store(!screenTerritoryProductionTooltip.get());
                },
                List.of(
                        Component.translatable(
                                        "feature.wynntils.customTerritoryManagementScreen.disableTerritoryProductionTooltip")
                                .withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD),
                        Component.translatable(
                                        "feature.wynntils.customTerritoryManagementScreen.territoryProductionHelper1")
                                .withStyle(ChatFormatting.GRAY),
                        Component.translatable(
                                        "feature.wynntils.customTerritoryManagementScreen.territoryProductionHelper2")
                                .withStyle(ChatFormatting.GRAY)),
                false));

        // Highlight legend disable button
        this.addRenderableWidget(new BasicTexturedButton(
                getRenderX() - 17,
                getRenderY() + 95,
                Texture.HELP_ICON.width(),
                Texture.HELP_ICON.height(),
                Texture.HELP_ICON,
                (button) -> {
                    Storage<Boolean> screenHighlightLegend = Managers.Feature.getFeatureInstance(
                                    CustomTerritoryManagementScreenFeature.class)
                            .screenHighlightLegend;
                    screenHighlightLegend.store(!screenHighlightLegend.get());
                },
                List.of(Component.translatable(
                                "feature.wynntils.customTerritoryManagementScreen.disableHighlightLegend")
                        .withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD)),
                false));

        if (!holder.isSelectionMode()) {
            // Loadout button in the sidebar
            this.addRenderableWidget(new BasicTexturedButton(
                    getRenderX() - 20,
                    getRenderY() + 115,
                    Texture.TERRITORY_LOADOUT.width(),
                    Texture.TERRITORY_LOADOUT.height(),
                    Texture.TERRITORY_LOADOUT,
                    (button) -> ContainerUtils.clickOnSlot(
                            LOADOUT_BUTTON_SLOT,
                            wrappedScreenInfo.containerId(),
                            button,
                            wrappedScreenInfo.containerMenu().getItems()),
                    List.of(
                            Component.translatable("feature.wynntils.customTerritoryManagementScreen.loadouts")
                                    .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD),
                            Component.empty(),
                            Component.translatable(
                                            "feature.wynntils.customTerritoryManagementScreen.loadouts.description")
                                    .withStyle(ChatFormatting.GRAY),
                            Component.empty(),
                            Component.translatable(
                                            "feature.wynntils.customTerritoryManagementScreen.loadouts.clickToOpen")
                                    .withStyle(ChatFormatting.GREEN))));
        } else {
            // Apply selection button in the sidebar
            this.addRenderableWidget(new TerritoryApplyLoadoutButton(
                    getRenderX() - 20,
                    getRenderY() + 115,
                    Texture.CHECKMARK_YELLOW.width(),
                    Texture.CHECKMARK_YELLOW.height(),
                    holder::getApplyButtonTexture,
                    (button) -> ContainerUtils.clickOnSlot(
                            APPLY_BUTTON_SLOT,
                            wrappedScreenInfo.containerId(),
                            button,
                            wrappedScreenInfo.containerMenu().getItems()),
                    List.of(
                            Component.translatable("feature.wynntils.customTerritoryManagementScreen.applySelection")
                                    .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD),
                            Component.empty(),
                            Component.translatable(
                                            "feature.wynntils.customTerritoryManagementScreen.applySelection.description")
                                    .withStyle(ChatFormatting.GRAY),
                            Component.empty(),
                            Component.translatable(
                                            "feature.wynntils.customTerritoryManagementScreen.applySelection.clickToConfirm")
                                    .withStyle(ChatFormatting.GREEN))));
        }

        // Quick filters
        quickFilters.clear();

        quickFilters.add(new TerritoryBonusesQuickFilterWidget(
                getRenderX() + Texture.TERRITORY_MANAGEMENT_BACKGROUND.width() + 5,
                getRenderY() + 20,
                QUICK_FILTER_WIDTH,
                10,
                this));

        quickFilters.add(new TerritoryDefenseQuickFilterWidget(
                getRenderX() + Texture.TERRITORY_MANAGEMENT_BACKGROUND.width() + 5,
                getRenderY() + 35,
                QUICK_FILTER_WIDTH,
                10,
                this));

        quickFilters.add(new TerritoryProducesQuickFilterWidget(
                getRenderX() + Texture.TERRITORY_MANAGEMENT_BACKGROUND.width() + 5,
                getRenderY() + 50,
                QUICK_FILTER_WIDTH,
                10,
                this));

        // Quick sorts
        quickSorts.clear();

        quickSorts.add(new TerritoryTreasuryQuickSortWidget(
                getRenderX() + Texture.TERRITORY_MANAGEMENT_BACKGROUND.width() + 5,
                getRenderY() + 85,
                QUICK_FILTER_WIDTH,
                10,
                this));

        quickSorts.add(new TerritoryDefenseQuickSortWidget(
                getRenderX() + Texture.TERRITORY_MANAGEMENT_BACKGROUND.width() + 5,
                getRenderY() + 100,
                QUICK_FILTER_WIDTH,
                10,
                this));

        quickSorts.add(new TerritoryOverallProductionQuickSortWidget(
                getRenderX() + Texture.TERRITORY_MANAGEMENT_BACKGROUND.width() + 5,
                getRenderY() + 115,
                QUICK_FILTER_WIDTH,
                10,
                this));
    }

    private void initMapScreen() {
        addMapButton(new MapButton(
                Texture.ARROW_LEFT_ICON,
                (button) -> ContainerUtils.clickOnSlot(
                        BACK_BUTTON_SLOT,
                        wrappedScreenInfo.containerId(),
                        button,
                        wrappedScreenInfo.containerMenu().getItems()),
                List.of(Component.literal("[>] ")
                        .withStyle(ChatFormatting.GRAY)
                        .append(Component.translatable("gui.back")))));
        addMapButton(new MapButton(
                Texture.DEFENSE_FILTER_ICON,
                (button) -> {
                    Storage<Boolean> screenTerritoryProductionTooltip = Managers.Feature.getFeatureInstance(
                                    CustomTerritoryManagementScreenFeature.class)
                            .screenTerritoryProductionTooltip;
                    screenTerritoryProductionTooltip.store(!screenTerritoryProductionTooltip.get());
                },
                List.of(
                        Component.literal("[>] ")
                                .withStyle(ChatFormatting.BLUE)
                                .append(
                                        Component.translatable(
                                                "feature.wynntils.customTerritoryManagementScreen.disableTerritoryProductionTooltip")),
                        Component.translatable(
                                        "feature.wynntils.customTerritoryManagementScreen.territoryProductionHelper1")
                                .withStyle(ChatFormatting.GRAY),
                        Component.translatable(
                                        "feature.wynntils.customTerritoryManagementScreen.territoryProductionHelper2")
                                .withStyle(ChatFormatting.GRAY))));
        infoTypeButton = new MapButton(
                Texture.OVERLAY_EXTRA_ICON,
                (b) -> {
                    if (b == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                        setInfoType(infoType.getNext());
                    } else if (b == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                        setInfoType(infoType.getPrevious());
                    }
                },
                getCompleteInfoTypeTooltip());
        addMapButton(infoTypeButton);

        if (!holder.isSelectionMode()) {
            addMapButton(new MapButton(
                    Texture.TERRITORY_LOADOUT,
                    (button) -> ContainerUtils.clickOnSlot(
                            LOADOUT_BUTTON_SLOT,
                            wrappedScreenInfo.containerId(),
                            button,
                            wrappedScreenInfo.containerMenu().getItems()),
                    List.of(
                            Component.literal("[>] ")
                                    .withStyle(ChatFormatting.GOLD)
                                    .append(Component.translatable(
                                            "feature.wynntils.customTerritoryManagementScreen.loadouts")),
                            Component.translatable(
                                            "feature.wynntils.customTerritoryManagementScreen.loadouts.description")
                                    .withStyle(ChatFormatting.GRAY),
                            Component.translatable(
                                            "feature.wynntils.customTerritoryManagementScreen.loadouts.clickToOpen")
                                    .withStyle(ChatFormatting.GREEN))));
        } else {
            addMapButton(new MapButton(
                    Texture.CHECKMARK_YELLOW,
                    (button) -> {
                        holder.saveMapPos();
                        ContainerUtils.clickOnSlot(
                                APPLY_BUTTON_SLOT,
                                wrappedScreenInfo.containerId(),
                                button,
                                wrappedScreenInfo.containerMenu().getItems());
                    },
                    List.of(
                            Component.literal("[>] ")
                                    .withStyle(ChatFormatting.GOLD)
                                    .append(Component.translatable(
                                            "feature.wynntils.customTerritoryManagementScreen.applySelection")),
                            Component.translatable(
                                            "feature.wynntils.customTerritoryManagementScreen.applySelection.description")
                                    .withStyle(ChatFormatting.GRAY),
                            Component.translatable(
                                            "feature.wynntils.customTerritoryManagementScreen.applySelection.clickToConfirm")
                                    .withStyle(ChatFormatting.GREEN))));
        }

        addMapButton(new MapButton(
                Texture.HELP_ICON,
                (b) -> {},
                List.of(
                        Component.literal("[>] ")
                                .withStyle(ChatFormatting.YELLOW)
                                .append(Component.translatable(
                                        "feature.wynntils.customTerritoryManagementScreen.help.name")),
                        Component.literal("- ")
                                .withStyle(ChatFormatting.GRAY)
                                .append(Component.translatable(
                                        "feature.wynntils.customTerritoryManagementScreen.help.description1")),
                        Component.literal("- ")
                                .withStyle(ChatFormatting.GRAY)
                                .append(Component.translatable(
                                        "feature.wynntils.customTerritoryManagementScreen.help.description2")))));
    }

    @Override
    public void doRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (!this.mapMode) {
            renderListScreen(guiGraphics, mouseX, mouseY, partialTick);
        } else {
            renderMapScreen(guiGraphics, mouseX, mouseY, partialTick);
        }

        // Render widget tooltip
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    private void renderListScreen(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Screen background
        RenderUtils.drawTexturedRect(guiGraphics, Texture.TERRITORY_MANAGEMENT_BACKGROUND, getRenderX(), getRenderY());
        RenderUtils.drawTexturedRect(guiGraphics, Texture.TERRITORY_SIDEBAR, getRenderX() - 22, getRenderY());

        // Render title
        FontRenderer.getInstance()
                .renderText(
                        guiGraphics,
                        StyledText.fromComponent(wrappedScreenInfo.screen().getTitle()),
                        getRenderX() + 8,
                        getRenderY() + 9,
                        CommonColors.TITLE_GRAY,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NONE);

        // Render the widgets
        renderWidgets(guiGraphics, mouseX, mouseY, partialTick);

        // Render scroll button
        renderScrollButton(guiGraphics);

        // Render quick filters
        renderQuickFiltersAndSorts(guiGraphics, mouseX, mouseY, partialTick);
    }

    private void renderMapScreen(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderMap(guiGraphics);

        RenderUtils.enableScissor(
                guiGraphics,
                (int) (renderX + renderedBorderXOffset),
                (int) (renderY + renderedBorderYOffset),
                (int) mapWidth,
                (int) mapHeight);

        renderTerritoryAreaPaths(guiGraphics);

        renderMapFeatures(guiGraphics, mouseX, mouseY);

        renderTerritoryDecorations(guiGraphics);

        renderCursor(
                guiGraphics,
                1.5f,
                Managers.Feature.getFeatureInstance(GuildMapFeature.class)
                        .pointerColor
                        .get(),
                Managers.Feature.getFeatureInstance(GuildMapFeature.class)
                        .pointerType
                        .get());

        RenderUtils.disableScissor(guiGraphics);

        renderMapBorder(guiGraphics);

        renderCoordinates(guiGraphics, mouseX, mouseY);

        renderMapButtons(guiGraphics, mouseX, mouseY, partialTick);

        renderZoomWidgets(guiGraphics, mouseX, mouseY, partialTick);

        renderHoveredTerritoryInfo(guiGraphics);

        if (isPanning) {
            guiGraphics.requestCursor(CursorTypes.RESIZE_ALL);
        } else if (holdingZoomHandle) {
            guiGraphics.requestCursor(CursorTypes.RESIZE_NS);
        } else if (hoveredFeature != null || isMouseOverZoomHandle(mouseX, mouseY)) {
            guiGraphics.requestCursor(CursorTypes.POINTING_HAND);
        }
    }

    private void renderTerritoryAreaPaths(GuiGraphics guiGraphics) {
        List<ManageTerritoryArea> territoryAreas = getRenderedMapFeatures()
                .filter(f -> f instanceof ManageTerritoryArea)
                .map(f -> (ManageTerritoryArea) f)
                .toList();

        Map<String, ManageTerritoryArea> territoryAreasByName = territoryAreas.stream()
                .collect(Collectors.toMap(
                        area -> area.getTerritoryProfile().getName(), area -> area, (first, second) -> first));

        // Accumulate every trading-route line into a single batch, submitted once below, instead of
        // one GuiElementRenderState submission per route
        List<ColoredLineBatchRenderState.Segment> segments = new ArrayList<>();

        for (ManageTerritoryArea territoryArea : territoryAreas) {
            TerritoryInfo territoryInfo = Models.Territory.getTerritoryInfo(
                    territoryArea.getTerritoryProfile().getName());
            if (territoryInfo == null) continue;

            for (String tradingRoute : territoryInfo.getTradingRoutes()) {
                ManageTerritoryArea destination = territoryAreasByName.get(tradingRoute);

                if (destination == null) continue;

                Vector2f firstCentroid = territoryArea.getBoundingPolygon().centroid();
                Vector2f secondCentroid = destination.getBoundingPolygon().centroid();
                float firstWorldX =
                        MapRenderer.getRenderX((int) firstCentroid.x(), mapCenterX, centerX, zoomRenderScale);
                float firstWorldZ =
                        MapRenderer.getRenderZ((int) firstCentroid.y(), mapCenterZ, centerZ, zoomRenderScale);
                float secondWorldX =
                        MapRenderer.getRenderX((int) secondCentroid.x(), mapCenterX, centerX, zoomRenderScale);
                float secondWorldZ =
                        MapRenderer.getRenderZ((int) secondCentroid.y(), mapCenterZ, centerZ, zoomRenderScale);
                segments.add(new ColoredLineBatchRenderState.Segment(
                        firstWorldX,
                        firstWorldZ,
                        secondWorldX,
                        secondWorldZ,
                        1,
                        CommonColors.DARK_GRAY.withAlpha(0.5f)));
            }
        }

        if (segments.isEmpty()) return;

        guiGraphics.guiRenderState.submitGuiElement(new ColoredLineBatchRenderState(
                RenderPipelines.GUI,
                TextureSetup.noTexture(),
                new Matrix3x2f(guiGraphics.pose()),
                segments,
                guiGraphics.scissorStack.peek()));
    }

    private void renderTerritoryDecorations(GuiGraphics guiGraphics) {
        List<ManageTerritoryArea> territoryAreas = getRenderedMapFeatures()
                .filter(f -> f instanceof ManageTerritoryArea)
                .map(f -> (ManageTerritoryArea) f)
                .toList();

        for (ManageTerritoryArea territoryArea : territoryAreas) {
            TerritoryItem territoryItem = territoryArea.getTerritoryItem();
            TerritoryProfile territoryProfile = territoryArea.getTerritoryProfile();

            final int width = Math.abs(territoryProfile.getEndX() - territoryProfile.getStartX());
            final int height = Math.abs(territoryProfile.getEndZ() - territoryProfile.getStartZ());
            final float renderWidth = width * zoomRenderScale;
            final float renderHeight = height * zoomRenderScale;

            Vector2f centroid = territoryArea.getBoundingPolygon().centroid();
            float centerScreenX = MapRenderer.getRenderX((int) centroid.x(), mapCenterX, centerX, zoomRenderScale);
            float centerScreenZ = MapRenderer.getRenderZ((int) centroid.y(), mapCenterZ, centerZ, zoomRenderScale);

            final float actualRenderX = centerScreenX - renderWidth / 2f;
            final float actualRenderZ = centerScreenZ - renderHeight / 2f;

            // Render the territory production type icons
            Set<GuildResource> productionTypes = territoryItem.getProduction().keySet().stream()
                    .filter(GuildResource::isMaterialResource)
                    .collect(Collectors.toUnmodifiableSet());

            if (!productionTypes.isEmpty() && zoomRenderScale >= 0.2f) {
                int iconYOffset = 4;
                if (territoryItem.isSelected() || territoryItem.isPending() || territoryItem.isHeadquarters()) {
                    iconYOffset += 4;
                }

                if (productionTypes.size() <= 2) {
                    GuildResource productionType = productionTypes.iterator().next();
                    String symbol = productionType.getPrettySymbol().trim();
                    symbol += productionTypes.size() == 2
                            ? productionTypes
                                    .iterator()
                                    .next()
                                    .getPrettySymbol()
                                    .trim()
                            : "";
                    if (territoryItem.getDoubleResource()) {
                        symbol += symbol;
                    }

                    FontRenderer.getInstance()
                            .renderAlignedTextInBox(
                                    guiGraphics,
                                    StyledText.fromString(symbol),
                                    actualRenderX,
                                    actualRenderX + renderWidth + (territoryItem.getDoubleEmeralds() ? 8 : 0),
                                    actualRenderZ + renderHeight / 2 + iconYOffset,
                                    actualRenderZ + renderHeight,
                                    0,
                                    CommonColors.WHITE,
                                    HorizontalAlignment.CENTER,
                                    VerticalAlignment.TOP,
                                    TextShadow.NORMAL);

                    // Render E icon separately so we can shift it down to be aligned
                    if (territoryItem.getDoubleEmeralds()) {
                        FontRenderer.getInstance()
                                .renderAlignedTextInBox(
                                        guiGraphics,
                                        StyledText.fromComponent(Component.literal(EmeraldUnits.EMERALD.getSymbol())
                                                .withStyle(ChatFormatting.GREEN)),
                                        actualRenderX,
                                        actualRenderX + renderWidth - 8,
                                        actualRenderZ + renderHeight / 2 + iconYOffset + 1,
                                        actualRenderZ + renderHeight,
                                        0,
                                        CommonColors.WHITE,
                                        HorizontalAlignment.CENTER,
                                        VerticalAlignment.TOP,
                                        TextShadow.NORMAL);
                    }
                } else {
                    int i = 0;
                    for (GuildResource productionType : productionTypes) {
                        String symbol = productionType.getPrettySymbol().trim();
                        FontRenderer.getInstance()
                                .renderText(
                                        guiGraphics,
                                        StyledText.fromString(symbol),
                                        actualRenderX + renderWidth / 2 + i * 8 - 12,
                                        actualRenderZ + renderHeight / 2 + iconYOffset,
                                        CommonColors.WHITE,
                                        HorizontalAlignment.CENTER,
                                        VerticalAlignment.TOP,
                                        TextShadow.NORMAL);
                        i++;
                    }
                }
            }

            if (territoryItem.isPending() && !territoryItem.isSelected()) {
                RenderUtils.drawTexturedRect(
                        guiGraphics,
                        Texture.CHECKMARK_GRAY,
                        actualRenderX + renderWidth / 2f - Texture.CHECKMARK_GRAY.width() / 2f,
                        actualRenderZ + renderHeight / 2f - Texture.CHECKMARK_GRAY.height() / 2f);
            } else if (!territoryItem.isPending() && territoryItem.isSelected()) {
                RenderUtils.drawTexturedRect(
                        guiGraphics,
                        Texture.CHECKMARK_GREEN,
                        actualRenderX + renderWidth / 2f - Texture.CHECKMARK_GREEN.width() / 2f,
                        actualRenderZ + renderHeight / 2f - Texture.CHECKMARK_GREEN.height() / 2f);
            } else if (territoryItem.isPending() && territoryItem.isSelected()) {
                RenderUtils.drawTexturedRect(
                        guiGraphics,
                        Texture.CHECKMARK_YELLOW,
                        actualRenderX + renderWidth / 2f - Texture.CHECKMARK_YELLOW.width() / 2f,
                        actualRenderZ + renderHeight / 2f - Texture.CHECKMARK_YELLOW.height() / 2f);
            } else if (territoryItem.isHeadquarters()) {
                RenderUtils.drawTexturedRect(
                        guiGraphics,
                        Texture.GUILD_HEADQUARTERS,
                        actualRenderX + renderWidth / 2f - Texture.GUILD_HEADQUARTERS.width() / 2f,
                        actualRenderZ + renderHeight / 2f - Texture.GUILD_HEADQUARTERS.height() / 2f);
            }
        }
    }

    @Override
    protected Stream<MapFeature> getRenderedMapFeatures() {
        return Stream.concat(
                Services.MapData.getFeaturesForCategory("wynntils:guild:managed-territory"),
                Services.MapData.getFeaturesForCategory("wynntils:personal:user-marker"));
    }

    @Override
    protected Optional<MapFeatureRenderer.OverridenAreaColors> getOverridenAreaColors(MapFeature feature) {
        if (!(feature instanceof ManageTerritoryArea territoryArea)) return Optional.empty();

        return Optional.of(new MapFeatureRenderer.OverridenAreaColors(
                territoryArea.getFillColors(), territoryArea.getBorderColors()));
    }

    private void renderWidgets(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Render territory widgets in the render area
        RenderUtils.enableScissor(
                guiGraphics,
                getRenderX() + RENDER_AREA_POSITION.a(),
                getRenderY() + RENDER_AREA_POSITION.b(),
                RENDER_AREA_SIZE.a(),
                RENDER_AREA_SIZE.b());

        // Render the render main area widgets
        for (AbstractWidget widget : renderAreaWidgets) {
            widget.render(guiGraphics, mouseX, mouseY, partialTick);
        }

        RenderUtils.disableScissor(guiGraphics);

        // Render normal widgets
        renderables.forEach(widget -> widget.render(guiGraphics, mouseX, mouseY, partialTick));
    }

    private void renderScrollButton(GuiGraphics guiGraphics) {
        float renderY = MathUtils.map(
                scrollOffset,
                0,
                getMaxScrollOffset(),
                getRenderY() + RENDER_AREA_POSITION.b(),
                getRenderY() + RENDER_AREA_POSITION.b() + RENDER_AREA_SIZE.b());
        RenderUtils.drawTexturedRect(
                guiGraphics,
                Texture.SCROLLBAR_BUTTON,
                getRenderX()
                        + RENDER_AREA_POSITION.a()
                        + RENDER_AREA_SIZE.a()
                        + 10f
                        - Texture.SCROLL_BUTTON.width() / 2f,
                renderY - Texture.SCROLLBAR_BUTTON.height() / 2f);
    }

    private void renderQuickFiltersAndSorts(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int xOffset = getRenderX() + Texture.TERRITORY_MANAGEMENT_BACKGROUND.width() + 5;

        RenderUtils.drawRect(
                guiGraphics,
                CommonColors.BLACK.withAlpha(80),
                xOffset,
                getRenderY(),
                QUICK_FILTER_WIDTH,
                Texture.TERRITORY_MANAGEMENT_BACKGROUND.height());

        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        guiGraphics,
                        StyledText.fromComponent(
                                Component.translatable("feature.wynntils.customTerritoryManagementScreen.filters")),
                        xOffset,
                        QUICK_FILTER_WIDTH + xOffset,
                        5 + getRenderY(),
                        0,
                        CommonColors.WHITE,
                        HorizontalAlignment.CENTER,
                        TextShadow.OUTLINE);

        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        guiGraphics,
                        StyledText.fromComponent(
                                Component.translatable("feature.wynntils.customTerritoryManagementScreen.sorts")),
                        xOffset,
                        QUICK_FILTER_WIDTH + xOffset,
                        70 + getRenderY(),
                        0,
                        CommonColors.WHITE,
                        HorizontalAlignment.CENTER,
                        TextShadow.OUTLINE);

        for (TerritoryQuickFilterWidget quickFilter : quickFilters) {
            quickFilter.render(guiGraphics, mouseX, mouseY, partialTick);
        }

        for (TerritoryQuickSortWidget quickSort : quickSorts) {
            quickSort.render(guiGraphics, mouseX, mouseY, partialTick);
        }

        if (draggingScroll) {
            guiGraphics.requestCursor(CursorTypes.RESIZE_NS);
        } else {
            float scrollX = getRenderX()
                    + RENDER_AREA_POSITION.a()
                    + RENDER_AREA_SIZE.a()
                    + 10f
                    - Texture.SCROLL_BUTTON.width() / 2f;
            float scrollY = MathUtils.map(
                    scrollOffset,
                    0,
                    getMaxScrollOffset(),
                    getRenderY() + RENDER_AREA_POSITION.b(),
                    getRenderY() + RENDER_AREA_POSITION.b() + RENDER_AREA_SIZE.b());
            if (mouseX >= scrollX
                    && mouseX <= scrollX + Texture.SCROLL_BUTTON.width()
                    && mouseY >= scrollY - Texture.SCROLL_BUTTON.height() / 2f
                    && mouseY <= scrollY + Texture.SCROLL_BUTTON.height() / 2f) {
                guiGraphics.requestCursor(CursorTypes.POINTING_HAND);
            }
        }
    }

    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        AbstractWidget hoveredWidget = getHoveredWidget(mouseX, mouseY);
        if (hoveredWidget == null) return;
        if (!(hoveredWidget instanceof TooltipProvider tooltipProvider)) return;

        List<Component> tooltipLines = tooltipProvider.getTooltipLines();
        if (tooltipLines.isEmpty()) return;

        guiGraphics.setTooltipForNextFrame(
                Lists.transform(tooltipLines, Component::getVisualOrderText), mouseX, mouseY);
    }

    @Override
    public boolean doMouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        if (!this.mapMode) {
            // Render area widgets need to handle the scroll offset
            // Check if mouse is over the render area
            if (event.x() >= getRenderX() + RENDER_AREA_POSITION.a()
                    && event.x() <= getRenderX() + RENDER_AREA_POSITION.a() + RENDER_AREA_SIZE.a()
                    && event.y() >= getRenderY() + RENDER_AREA_POSITION.b()
                    && event.y() <= getRenderY() + RENDER_AREA_POSITION.b() + RENDER_AREA_SIZE.b()) {
                for (AbstractWidget widget : renderAreaWidgets) {
                    if (widget.isMouseOver(event.x(), event.y())) {
                        return widget.mouseClicked(
                                new MouseButtonEvent(event.x(), event.y(), event.buttonInfo()), isDoubleClick);
                    }
                }
            }

            for (TerritoryQuickFilterWidget quickFilter : quickFilters) {
                if (quickFilter.isMouseOver(event.x(), event.y())) {
                    return quickFilter.mouseClicked(event, isDoubleClick);
                }
            }

            for (TerritoryQuickSortWidget quickSort : quickSorts) {
                if (quickSort.isMouseOver(event.x(), event.y())) {
                    return quickSort.mouseClicked(event, isDoubleClick);
                }
            }

            // Check if the scroll button was clicked
            float scrollX = getRenderX()
                    + RENDER_AREA_POSITION.a()
                    + RENDER_AREA_SIZE.a()
                    + 10f
                    - Texture.SCROLL_BUTTON.width() / 2f;
            float scrollY = MathUtils.map(
                    scrollOffset,
                    0,
                    getMaxScrollOffset(),
                    getRenderY() + RENDER_AREA_POSITION.b(),
                    getRenderY() + RENDER_AREA_POSITION.b() + RENDER_AREA_SIZE.b());
            if (event.x() >= scrollX
                    && event.x() <= scrollX + Texture.SCROLL_BUTTON.width()
                    && event.y() >= scrollY - Texture.SCROLL_BUTTON.height() / 2f
                    && event.y() <= scrollY + Texture.SCROLL_BUTTON.height() / 2f) {
                draggingScroll = true;
                return true;
            }
        } else {
            for (GuiEventListener child :
                    Stream.concat(children().stream(), mapButtons.stream()).toList()) {
                if (child.isMouseOver(event.x(), event.y())) {
                    child.mouseClicked(event, isDoubleClick);
                    return true;
                }
            }

            if (event.button() == GLFW.GLFW_MOUSE_BUTTON_RIGHT
                    && hoveredFeature instanceof ManageTerritoryArea manageTerritoryArea) {
                holder.saveMapPos();
                manageTerritoryArea.onClick();
            }
        }

        return super.doMouseClicked(event, isDoubleClick);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        draggingScroll = false;
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (!this.mapMode) {
            // Scroll the render area
            setScrollOffset((float) (scrollOffset - Math.signum(scrollY) * 10f));
            scrollAreaWidgets(scrollOffset);
        } else {
            return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        }
        return true;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (!this.mapMode && draggingScroll) {
            // Calculate the new scroll offset
            float newScrollOffset = MathUtils.map(
                    (float) event.y(),
                    getRenderY() + RENDER_AREA_POSITION.b(),
                    getRenderY() + RENDER_AREA_POSITION.b() + RENDER_AREA_SIZE.b(),
                    0,
                    getMaxScrollOffset());
            setScrollOffset(newScrollOffset);
            scrollAreaWidgets(scrollOffset);
            return true;
        }

        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (mapMode) {
            switch (event.key()) {
                case GLFW.GLFW_KEY_1 -> setInfoType(TerritoryInfoType.DEFENSE);
                case GLFW.GLFW_KEY_2 -> setInfoType(TerritoryInfoType.PRODUCTION);
                case GLFW.GLFW_KEY_3 -> setInfoType(TerritoryInfoType.TREASURY);
                case GLFW.GLFW_KEY_4 -> setInfoType(TerritoryInfoType.SEEKING);
                case GLFW.GLFW_KEY_H -> centerOnHeadquarters();
            }
        }

        return keyPressedParent(event);
    }

    @Override
    public void onClose() {
        holder.resetMapPos();
        super.onClose();
    }

    public void updateTerritoryItems() {
        territoryItems = holder.territoryItems().stream().toList();

        if (!this.mapMode) {
            populateRenderAreaWidgets();
            scrollOffset = Math.min(getMaxScrollOffset(), scrollOffset);
        } else {
            ManageTerritoryProvider.updateFeatures(holder);
        }
    }

    public void updateSearchFromQuickFilters() {
        itemSearchWidget.setTextBoxInput(Stream.concat(
                        quickFilters.stream().map(TerritoryQuickFilterWidget::getItemSearchQuery),
                        quickSorts.stream().map(TerritoryQuickSortWidget::getItemSearchQuery))
                .collect(Collectors.joining(" "))
                .trim()
                .replaceAll("\\s+", " "));
    }

    private void setInfoType(TerritoryInfoType type) {
        infoType = type;
        infoTypeButton.setTooltip(getCompleteInfoTypeTooltip());
        ManageTerritoryProvider.refreshColors();
    }

    private void populateRenderAreaWidgets() {
        List<Pair<ItemStack, TerritoryItem>> filteredItems = Services.ItemFilter.filterAndSort(
                        itemSearchWidget.getSearchQuery(),
                        territoryItems.stream().map(Pair::a).toList())
                .stream()
                .map(item -> Pair.of(
                        item, Models.Item.asWynnItem(item, TerritoryItem.class).orElseThrow()))
                .toList();

        renderAreaWidgets.clear();

        int x = getRenderX() + RENDER_AREA_POSITION.a();
        int y = getRenderY() + RENDER_AREA_POSITION.b();

        // Populate the render area with territory widgets
        // (even if they are out of bounds, they will be clipped,
        // but we need to render them to be able to scroll)
        int xOffset = (RENDER_AREA_SIZE.a() - TERRITORIES_PER_ROW * TERRITORY_SIZE) / 2;
        int yOffset = 0;
        for (Pair<ItemStack, TerritoryItem> territoryItemPair : filteredItems) {
            renderAreaWidgets.add(new TerritoryWidget(
                    x + xOffset,
                    y + yOffset,
                    TERRITORY_SIZE,
                    TERRITORY_SIZE,
                    holder,
                    holder.getTerritoryColor(territoryItemPair.b()),
                    territoryItemPair.a(),
                    territoryItemPair.b()));

            xOffset += TERRITORY_SIZE;
            if (xOffset >= TERRITORY_SIZE * TERRITORIES_PER_ROW) {
                xOffset = (RENDER_AREA_SIZE.a() - TERRITORIES_PER_ROW * TERRITORY_SIZE) / 2;
                yOffset += TERRITORY_SIZE;
            }
        }

        scrollAreaWidgets(scrollOffset);
    }

    private void renderHoveredTerritoryInfo(GuiGraphics guiGraphics) {
        if (!(hoveredFeature instanceof ManageTerritoryArea territoryArea)) return;

        int xOffset = (int) (width - SCREEN_SIDE_OFFSET - 250);
        int yOffset = (int) (SCREEN_SIDE_OFFSET + 40);

        renderTerritoryTooltip(guiGraphics, xOffset, yOffset, territoryArea);
    }

    private void renderTerritoryTooltip(
            GuiGraphics guiGraphics, int xOffset, int yOffset, ManageTerritoryArea territoryArea) {
        final TerritoryItem item = territoryArea.getTerritoryItem();

        final List<Component> tooltipLines = new ArrayList<>();

        for (GuildResource value : GuildResource.values()) {
            int generation = item.getProduction().getOrDefault(value, 0);
            CappedValue storage = item.getStorage().get(value);

            if (generation != 0) {
                StyledText formattedGenerated = StyledText.fromString(
                        "%s+%d %s per Hour".formatted(value.getPrettySymbol(), generation, value.getName()));

                tooltipLines.add(formattedGenerated.getComponent());
            }

            if (storage != null) {
                StyledText formattedStored = StyledText.fromString("%s%d/%d %s stored"
                        .formatted(value.getPrettySymbol(), storage.current(), storage.max(), value.getName()));

                tooltipLines.add(formattedStored.getComponent());
            }
        }

        tooltipLines.add(Component.literal(""));

        StyledText defences = StyledText.fromString(ChatFormatting.GRAY
                + "Territory Defences: %s"
                        .formatted(item.getDefenseDifficulty().getDefenceColor()
                                + item.getDefenseDifficulty().getAsString()));
        tooltipLines.add(defences.getComponent());

        TerritoryProfile territoryProfile = territoryArea.getTerritoryProfile();
        TerritoryInfo territoryInfo = Models.Territory.getTerritoryInfo(item.getName());

        String timeHeldString =
                territoryInfo != null && territoryProfile.getGuild().equals(territoryInfo.getGuildName())
                        ? territoryProfile.getTimeAcquiredColor() + territoryProfile.getReadableRelativeTimeAcquired()
                        : "-";
        tooltipLines.add(StyledText.fromString(ChatFormatting.GRAY + "Time Held: " + timeHeldString)
                .getComponent());

        if (territoryInfo != null && item.getTreasuryBonus() > 0) {
            tooltipLines.add(Component.literal(""));

            StyledText treasury = StyledText.fromString(ChatFormatting.LIGHT_PURPLE
                    + "✦ Treasury Bonus: " + ChatFormatting.WHITE + "%.1f%%".formatted(item.getTreasuryBonus())
                    + ChatFormatting.GRAY
                    + " (%s)"
                            .formatted(territoryInfo.getTreasury().getTreasuryColor()
                                    + territoryInfo.getTreasury().getAsString()
                                    + ChatFormatting.GRAY));
            tooltipLines.add(treasury.getComponent());
        }

        List<Component> defenseLines = new ArrayList<>();
        List<Component> bonusLines = new ArrayList<>();

        Map<TerritoryUpgrade, Integer> territoryUpgrades = item.getUpgrades();

        for (TerritoryUpgrade upgrade : TerritoryUpgrade.values()) {
            if (territoryUpgrades.getOrDefault(upgrade, 0) == 0) continue;
            Component upgradeLine = Component.literal("- ")
                    .withStyle(ChatFormatting.LIGHT_PURPLE)
                    .append(Component.literal(upgrade.getName()).withStyle(ChatFormatting.GRAY))
                    .append(Component.literal(" [Lv. %s]".formatted(territoryUpgrades.get(upgrade)))
                            .withStyle(ChatFormatting.DARK_GRAY));
            if (DEFENSE_UPGRADES.contains(upgrade)) {
                defenseLines.add(upgradeLine);
            } else {
                bonusLines.add(upgradeLine);
            }
        }

        if (!defenseLines.isEmpty()) {
            tooltipLines.add(Component.literal(""));
            tooltipLines.add(Component.literal("Defenses:").withStyle(ChatFormatting.LIGHT_PURPLE));
            tooltipLines.addAll(defenseLines);
        }

        if (!bonusLines.isEmpty()) {
            tooltipLines.add(Component.literal(""));
            tooltipLines.add(Component.literal("Bonuses:").withStyle(ChatFormatting.LIGHT_PURPLE));
            tooltipLines.addAll(bonusLines);
        }

        final int textureWidth = Texture.MAP_INFO_TOOLTIP_CENTER.width();

        final float centerHeight = (tooltipLines.size()) * 10 + 5;

        RenderUtils.drawTexturedRect(guiGraphics, Texture.MAP_INFO_TOOLTIP_TOP, xOffset, yOffset);
        RenderUtils.drawScalingTexturedRect(
                guiGraphics,
                Texture.MAP_INFO_TOOLTIP_CENTER.identifier(),
                xOffset,
                Texture.MAP_INFO_TOOLTIP_TOP.height() + yOffset,
                textureWidth,
                centerHeight,
                textureWidth,
                Texture.MAP_INFO_TOOLTIP_CENTER.height());
        RenderUtils.drawTexturedRect(
                guiGraphics,
                Texture.MAP_INFO_NAME_BOX,
                xOffset,
                Texture.MAP_INFO_TOOLTIP_TOP.height() + centerHeight + yOffset);

        float renderYOffset = 10 + yOffset;

        for (Component line : tooltipLines) {
            FontRenderer.getInstance()
                    .renderText(
                            guiGraphics,
                            StyledText.fromComponent(line),
                            10f + xOffset,
                            renderYOffset,
                            CommonColors.WHITE,
                            HorizontalAlignment.LEFT,
                            VerticalAlignment.TOP,
                            TextShadow.OUTLINE,
                            1.0f);
            renderYOffset += 10;
        }

        // Territory name
        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        guiGraphics,
                        StyledText.fromString(item.getName()),
                        7 + xOffset,
                        textureWidth + xOffset,
                        Texture.MAP_INFO_TOOLTIP_TOP.height() + centerHeight + yOffset,
                        Texture.MAP_INFO_TOOLTIP_TOP.height()
                                + centerHeight
                                + Texture.MAP_INFO_NAME_BOX.height()
                                + yOffset,
                        0,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.MIDDLE,
                        TextShadow.OUTLINE);
    }

    private void centerOnHeadquarters() {
        Optional<Pair<ItemStack, TerritoryItem>> hqItem = territoryItems.stream()
                .filter((item) -> item.b().isHeadquarters())
                .findFirst();
        if (hqItem.isPresent()) {
            TerritoryProfile territoryProfile =
                    Models.Territory.getTerritoryProfile(hqItem.get().b().getName());
            // The API territory list may not have loaded yet, or may not know this territory
            if (territoryProfile == null) return;

            PoiLocation location = territoryProfile.getCenterLocation();
            updateMapCenter(location.getX(), location.getZ());
        }
    }

    private AbstractWidget getHoveredWidget(int mouseX, int mouseY) {
        for (AbstractWidget widget : renderAreaWidgets) {
            if (widget.isMouseOver(mouseX, mouseY)
                    && mouseY >= getRenderY() + RENDER_AREA_POSITION.b()
                    && mouseY <= getRenderY() + RENDER_AREA_POSITION.b() + RENDER_AREA_SIZE.b()) {
                return widget;
            }
        }

        for (GuiEventListener child : children) {
            if (child instanceof TooltipProvider tooltipProvider) {
                AbstractWidget widget = (AbstractWidget) child;
                if (widget.isMouseOver(mouseX, mouseY)) {
                    return widget;
                }
            }
        }

        for (AbstractWidget button : mapButtons) {
            if (button.isMouseOver(mouseX, mouseY)) {
                return button;
            }
        }

        return null;
    }

    private void scrollAreaWidgets(float newOffset) {
        scrollOffset = newOffset;

        int y = getRenderY() + RENDER_AREA_POSITION.b();
        int xOffset = (RENDER_AREA_SIZE.a() - TERRITORIES_PER_ROW * TERRITORY_SIZE) / 2;
        int yOffset = 0;
        for (AbstractWidget areaWidget : renderAreaWidgets) {
            int newY = (int) (y + yOffset - scrollOffset);

            areaWidget.setY(newY);

            xOffset += TERRITORY_SIZE;
            if (xOffset >= TERRITORY_SIZE * TERRITORIES_PER_ROW) {
                xOffset = (RENDER_AREA_SIZE.a() - TERRITORIES_PER_ROW * TERRITORY_SIZE) / 2;
                yOffset += TERRITORY_SIZE;
            }
        }
    }

    private void setScrollOffset(float scrollOffset) {
        this.scrollOffset = scrollOffset;
        this.scrollOffset = Math.max(0, this.scrollOffset);
        this.scrollOffset = Math.min(getMaxScrollOffset(), this.scrollOffset);
    }

    private int getMaxScrollOffset() {
        int totalHeight = TERRITORY_SIZE * ((renderAreaWidgets.size() + TERRITORIES_PER_ROW - 1) / TERRITORIES_PER_ROW);
        return Math.max(0, totalHeight - RENDER_AREA_SIZE.b());
    }

    private List<Component> getCompleteInfoTypeTooltip() {
        return List.of(
                Component.literal("[>] ")
                        .withStyle(ChatFormatting.RED)
                        .append(Component.translatable(
                                "feature.wynntils.customTerritoryManagementScreen.cycleInfoType.name")),
                Component.translatable("feature.wynntils.customTerritoryManagementScreen.cycleInfoType.description")
                        .withStyle(ChatFormatting.GRAY),
                Component.translatable("feature.wynntils.customTerritoryManagementScreen.cycleInfoType.description2")
                        .withStyle(ChatFormatting.GRAY)
                        .append(infoType.asComponent()));
    }

    private int getRenderX() {
        return (this.width - Texture.TERRITORY_MANAGEMENT_BACKGROUND.width()) / 2;
    }

    private int getRenderY() {
        return (this.height - Texture.TERRITORY_MANAGEMENT_BACKGROUND.height()) / 2;
    }

    public float getMapCenterX() {
        return this.mapCenterX;
    }

    public float getMapCenterZ() {
        return this.mapCenterZ;
    }

    public float getZoomLevel() {
        return this.zoomLevel;
    }

    public TerritoryInfoType getInfoType() {
        return this.infoType;
    }
}
