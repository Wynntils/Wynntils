/*
 * Copyright © Wynntils 2024-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.maps;

import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Models;
import com.wynntils.core.components.Services;
import com.wynntils.core.text.StyledText;
import com.wynntils.features.ui.CustomSeaskipperScreenFeature;
import com.wynntils.models.seaskipper.type.SeaskipperDestination;
import com.wynntils.models.seaskipper.type.SeaskipperDestinationArea;
import com.wynntils.screens.maps.widgets.MapButton;
import com.wynntils.screens.maps.widgets.SeaskipperDestinationButton;
import com.wynntils.screens.maps.widgets.SeaskipperTravelButton;
import com.wynntils.services.mapdata.attributes.impl.AbstractMapAreaAttributes;
import com.wynntils.services.mapdata.attributes.type.MapAttributes;
import com.wynntils.services.mapdata.features.type.MapFeature;
import com.wynntils.services.mapdata.providers.type.AbstractMapDataOverrideProvider;
import com.wynntils.utils.MathUtils;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.MapRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.joml.Vector2f;

public final class CustomSeaskipperScreen extends AbstractMapScreen {
    private static final String SEASKIPPER_LOCATION_BORDER_OVERIDE_PROVIDER_ID =
            "override:seaskipper_location_border_override";
    private static final SeaskipperLocationBorderOverrideProvider SEASKIPPER_LOCATION_BORDER_OVERRIDE_PROVIDER =
            new SeaskipperLocationBorderOverrideProvider();

    // Constants
    private static final int MAX_DESTINATIONS = 10;
    private static final int SCROLL_HEIGHT = 220;
    private static final int DEFAULT_ZOOM_LEVEL = 20;

    // Toggleable options
    private boolean hideTerritoryBorders = false;
    private boolean renderAllDestinations = false;
    private boolean renderRoutes = false;

    // Collections
    private List<SeaskipperDestinationButton> destinationButtons = new ArrayList<>();
    private List<SeaskipperDestination> availableDestinations = new ArrayList<>();

    // Seaskipper pois
    private SeaskipperDestination currentLocation = null;
    private SeaskipperDestination selectedDestination;

    // UI Size, position etc
    private boolean draggingScroll = false;
    private double currentUnusedScroll = 0;
    private float currentTextureScale;
    private float departureBoardY;
    private float destinationButtonsRenderX;
    private float scrollButtonRenderX;
    private float scrollButtonRenderY;
    private int scrollAreaHeight;
    private int scrollOffset = 0;

    private CustomSeaskipperScreen() {}

    public static Screen create() {
        return new CustomSeaskipperScreen();
    }

    @Override
    protected void doInit() {
        super.doInit();

        currentTextureScale = (float) this.height / (Texture.DESTINATION_LIST.height() + 49);

        float departureListWidth = Texture.DESTINATION_LIST.width() * currentTextureScale;

        renderX += departureListWidth;
        renderWidth -= departureListWidth;
        centerX += (departureListWidth) / 2f;
        renderedBorderXOffset -= 1;
        mapWidth -= (departureListWidth) - 5;
        departureBoardY = (this.height
                        - (Texture.DESTINATION_LIST.height() * currentTextureScale
                                + (Texture.TRAVEL_BUTTON.height() / 2f) * currentTextureScale))
                / 2;
        scrollButtonRenderX = 5 + departureListWidth * 0.933f;
        destinationButtonsRenderX = 5 + departureListWidth * 0.027f;
        scrollAreaHeight = (int) (SCROLL_HEIGHT * currentTextureScale);

        // region Map buttons
        addMapButton(new MapButton(
                Texture.WAYPOINT_MANAGER_ICON,
                (b) -> toggleRoutes(),
                List.of(
                        Component.literal("[>] ")
                                .withStyle(ChatFormatting.LIGHT_PURPLE)
                                .append(Component.translatable(
                                        "screens.wynntils.customSeaskipperScreen.showRoutes.name")),
                        Component.translatable("screens.wynntils.customSeaskipperScreen.showRoutes.description")
                                .withStyle(ChatFormatting.GRAY))));

        addMapButton(new MapButton(
                Texture.ADD_ICON,
                (b) -> toggleDestinations(),
                List.of(
                        Component.literal("[>] ")
                                .withStyle(ChatFormatting.BLUE)
                                .append(Component.translatable(
                                        "screens.wynntils.customSeaskipperScreen.showInaccessibleLocations.name")),
                        Component.translatable(
                                        "screens.wynntils.customSeaskipperScreen.showInaccessibleLocations.description")
                                .withStyle(ChatFormatting.GRAY))));

        addMapButton(new MapButton(
                Texture.OVERLAY_EXTRA_ICON,
                (b) -> toggleBorders(),
                List.of(
                        Component.literal("[>] ")
                                .withStyle(ChatFormatting.BLUE)
                                .append(Component.translatable(
                                        "screens.wynntils.customSeaskipperScreen.showBorders.name")),
                        Component.translatable("screens.wynntils.customSeaskipperScreen.showBorders.description")
                                .withStyle(ChatFormatting.GRAY))));

        addMapButton(new MapButton(
                Texture.BOAT_ICON,
                (b) -> Models.Seaskipper.purchaseBoat(),
                List.of(
                        Component.literal("[>] ")
                                .withStyle(ChatFormatting.LIGHT_PURPLE)
                                .append(Component.translatable("screens.wynntils.customSeaskipperScreen.buyBoat.name")),
                        Component.translatable("screens.wynntils.customSeaskipperScreen.buyBoat.description")
                                .withStyle(ChatFormatting.GRAY))));
        // endregion

        this.addRenderableWidget(new SeaskipperTravelButton(
                5,
                (int) (departureBoardY + Texture.DESTINATION_LIST.height() * currentTextureScale),
                (int) (Texture.TRAVEL_BUTTON.width() * currentTextureScale),
                (int) ((Texture.TRAVEL_BUTTON.height() / 2f) * currentTextureScale),
                this));

        // Only center the map and reload possible pois for first init
        if (firstInit) {
            centerMapAroundPlayer();
            setZoomLevel(DEFAULT_ZOOM_LEVEL);
            reloadDestinations();
            firstInit = false;
        }

        reloadButtons();
    }

    @Override
    public void removed() {
        Services.MapData.unregisterOverrideProvider(SEASKIPPER_LOCATION_BORDER_OVERIDE_PROVIDER_ID);
    }

    @Override
    public void doRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderMap(guiGraphics);

        RenderUtils.enableScissor(
                guiGraphics,
                (int) (renderX + renderedBorderXOffset),
                (int) (renderY + renderedBorderYOffset),
                (int) mapWidth,
                (int) mapHeight);

        renderMapFeatures(guiGraphics, mouseX, mouseY);

        if (renderRoutes) {
            renderSeaskipperPaths(guiGraphics);
        }

        renderCursor(
                guiGraphics,
                1.5f,
                Managers.Feature.getFeatureInstance(CustomSeaskipperScreenFeature.class)
                        .pointerColor
                        .get(),
                Managers.Feature.getFeatureInstance(CustomSeaskipperScreenFeature.class)
                        .pointerType
                        .get());

        RenderUtils.disableScissor(guiGraphics);

        renderBackground(guiGraphics, mouseX, mouseY, partialTick);

        renderCoordinates(guiGraphics, mouseX, mouseY);

        renderMapButtons(guiGraphics, mouseX, mouseY, partialTick);

        renderZoomWidgets(guiGraphics, mouseX, mouseY, partialTick);

        renderHoveredSeaskipperDestination(guiGraphics);

        renderTooltip(guiGraphics, mouseX, mouseY);

        RenderUtils.drawScalingTexturedRect(
                guiGraphics,
                Texture.DESTINATION_LIST,
                5,
                departureBoardY,
                Texture.DESTINATION_LIST.width() * currentTextureScale,
                Texture.DESTINATION_LIST.height() * currentTextureScale);

        renderScrollButton(guiGraphics);

        for (SeaskipperDestinationButton destinationButton : destinationButtons) {
            destinationButton.render(guiGraphics, mouseX, mouseY, partialTick);
        }
    }

    private void renderSeaskipperPaths(GuiGraphics guiGraphics) {
        List<SeaskipperDestinationArea> seaskipperDestinationsAreas = getRenderedMapFeatures()
                .filter(f -> f instanceof SeaskipperDestinationArea)
                .map(f -> (SeaskipperDestinationArea) f)
                .toList();

        SeaskipperDestinationArea currentLocationArea = seaskipperDestinationsAreas.stream()
                .filter(area -> area.getDestination().equals(currentLocation))
                .findFirst()
                .orElse(null);
        if (currentLocationArea == null) return;

        for (SeaskipperDestinationArea destinationArea : seaskipperDestinationsAreas) {
            if (destinationArea.getDestination() == currentLocation) continue;

            Vector2f firstCentroid = destinationArea.getBoundingPolygon().centroid();
            Vector2f secondCentroid = currentLocationArea.getBoundingPolygon().centroid();
            float firstWorldX = MapRenderer.getRenderX((int) firstCentroid.x(), mapCenterX, centerX, zoomRenderScale);
            float firstWorldZ = MapRenderer.getRenderZ((int) firstCentroid.y(), mapCenterZ, centerZ, zoomRenderScale);
            float secondWorldX = MapRenderer.getRenderX((int) secondCentroid.x(), mapCenterX, centerX, zoomRenderScale);
            float secondWorldZ = MapRenderer.getRenderZ((int) secondCentroid.y(), mapCenterZ, centerZ, zoomRenderScale);
            RenderUtils.drawLine(
                    guiGraphics,
                    CommonColors.DARK_GRAY.withAlpha(0.5f),
                    firstWorldX,
                    firstWorldZ,
                    secondWorldX,
                    secondWorldZ,
                    1);
        }
    }

    @Override
    public void onClose() {
        McUtils.player().closeContainer();
        super.onClose();
    }

    @Override
    public boolean doMouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        for (GuiEventListener child : children()) {
            if (child.isMouseOver((double) event.x(), (double) event.y())) {
                child.mouseClicked(event, isDoubleClick);
                return true;
            }
        }

        // If clicked on a destination and it is available, try and purchase a pass
        if (hoveredFeature instanceof SeaskipperDestinationArea seaskipperDestinationArea) {
            if (seaskipperDestinationArea.getDestination().isAvailable()
                    && !seaskipperDestinationArea.getDestination().equals(currentLocation)) {
                Models.Seaskipper.purchasePass(seaskipperDestinationArea.getDestination());
            }

            return true;
        }

        // If clicked on a destination button, set it as selected and zoom on the destination.
        // If the button was already selected, then try and purchase a pass
        for (SeaskipperDestinationButton destinationButton : destinationButtons) {
            if (destinationButton.isHovered() && selectedDestination != destinationButton.getDestination()) {
                selectedDestination = destinationButton.getDestination();
                zoomToDestination(selectedDestination);
                return true;
            } else if (destinationButton.isHovered() && selectedDestination == destinationButton.getDestination()) {
                Models.Seaskipper.purchasePass(destinationButton.getDestination());
                return true;
            }
        }

        if (!draggingScroll && (availableDestinations.size() > MAX_DESTINATIONS)) {
            if (MathUtils.isInside(
                    (int) event.x(),
                    (int) event.y(),
                    (int) scrollButtonRenderX,
                    (int) (scrollButtonRenderX + Texture.SCROLL_BUTTON.width() * currentTextureScale),
                    (int) scrollButtonRenderY,
                    (int) (scrollButtonRenderY + Texture.SCROLL_BUTTON.height() * currentTextureScale))) {
                draggingScroll = true;

                return true;
            }
        }

        return super.doMouseClicked(event, isDoubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (draggingScroll) {
            int startRenderY = (int) (departureBoardY + 4 * currentTextureScale * 0.933f);
            int scrollAreaStartY = startRenderY + 9;

            int newValue = Math.round(MathUtils.map(
                    (float) event.y(),
                    scrollAreaStartY,
                    scrollAreaStartY + scrollAreaHeight - Texture.SCROLL_BUTTON.height() * currentTextureScale,
                    0,
                    Math.max(0, availableDestinations.size() - MAX_DESTINATIONS)));

            scroll(newValue - scrollOffset);

            return true;
        }

        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        draggingScroll = false;
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY) {
        if (mouseX >= renderX) { // Scroll the map
            return super.mouseScrolled(mouseX, mouseY, deltaX, deltaY);
        } else { // Scroll the departure list
            if (Math.abs(deltaY) == 1.0) {
                scroll((int) -deltaY);
                return true;
            }

            // Account for scrollpad
            currentUnusedScroll -= deltaY / 5d;

            if (Math.abs(currentUnusedScroll) < 1) return true;

            int scroll = (int) (currentUnusedScroll);
            currentUnusedScroll = currentUnusedScroll % 1;

            scroll(scroll);

            return true;
        }
    }

    public void reloadDestinations() {
        List<SeaskipperDestination> destinations = Models.Seaskipper.getDestinations(false);
        currentLocation = destinations.stream()
                .filter(SeaskipperDestination::isPlayerInside)
                .findFirst()
                .orElse(null);

        // Available destinations to use for the departure list
        availableDestinations = destinations.stream()
                .filter(SeaskipperDestination::isAvailable)
                .sorted(Comparator.comparing(
                        destination -> destination.profile().destination(), String.CASE_INSENSITIVE_ORDER))
                .toList();

        reloadButtons();
    }

    public void travelToDestination() {
        if (selectedDestination == null) return;

        Models.Seaskipper.purchasePass(selectedDestination);
    }

    public SeaskipperDestination getSelectedDestination() {
        return selectedDestination;
    }

    @Override
    protected Stream<MapFeature> getRenderedMapFeatures() {
        return Services.MapData.getFeaturesForCategory("wynntils:fast-travel:seaskipper-destination")
                .filter(feature -> feature instanceof SeaskipperDestinationArea)
                .map(feature -> (SeaskipperDestinationArea) feature)
                .filter(area -> (area.getDestination().isAvailable() || renderAllDestinations)
                        || area.getDestination().isPlayerInside())
                .map(f -> f);
    }

    private void renderHoveredSeaskipperDestination(GuiGraphics guiGraphics) {
        if (!(hoveredFeature instanceof SeaskipperDestinationArea seaskipperDestinationArea)) return;
        SeaskipperDestination destination = seaskipperDestinationArea.getDestination();

        int xOffset = (int) (width - SCREEN_SIDE_OFFSET - 250);
        int yOffset = (int) (SCREEN_SIDE_OFFSET + 40);

        boolean isAccessible = destination.isAvailable();

        final float centerHeight = isAccessible ? 50 : 30;
        final int textureWidth = Texture.MAP_INFO_TOOLTIP_CENTER.width();

        RenderUtils.drawTexturedRect(guiGraphics, Texture.MAP_INFO_TOOLTIP_TOP, xOffset, yOffset);
        RenderUtils.drawTexturedRect(
                guiGraphics,
                Texture.MAP_INFO_TOOLTIP_CENTER,
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

        FontRenderer.getInstance()
                .renderText(
                        guiGraphics,
                        StyledText.fromComponent(Component.translatable(
                                "screens.wynntils.customSeaskipperScreen.level",
                                destination.profile().combatLevel())),
                        10 + xOffset,
                        10 + yOffset,
                        CommonColors.ORANGE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.TOP,
                        TextShadow.OUTLINE);

        float renderYOffset = 10 + yOffset;

        boolean origin = destination == currentLocation;

        if (isAccessible) {
            int price = destination.item().getPrice();

            CustomColor priceColor;
            Component travelComponent;

            if (Models.Emerald.getAmountInInventory() >= destination.item().getPrice()) {
                priceColor = CommonColors.GREEN;
                travelComponent = Component.translatable("screens.wynntils.customSeaskipperScreen.clickToGo");
            } else {
                priceColor = CommonColors.RED;
                travelComponent = Component.translatable("screens.wynntils.customSeaskipperScreen.cannotAfford");
            }

            renderYOffset += 10;

            FontRenderer.getInstance()
                    .renderText(
                            guiGraphics,
                            StyledText.fromComponent(
                                    Component.translatable("screens.wynntils.customSeaskipperScreen.cost", price)),
                            10 + xOffset,
                            10 + renderYOffset,
                            priceColor,
                            HorizontalAlignment.LEFT,
                            VerticalAlignment.TOP,
                            TextShadow.OUTLINE);

            renderYOffset += 20;

            FontRenderer.getInstance()
                    .renderText(
                            guiGraphics,
                            StyledText.fromComponent(travelComponent),
                            10 + xOffset,
                            10 + renderYOffset,
                            CommonColors.LIGHT_BLUE,
                            HorizontalAlignment.LEFT,
                            VerticalAlignment.TOP,
                            TextShadow.OUTLINE);
        } else if (origin) {
            renderYOffset += 10;

            FontRenderer.getInstance()
                    .renderText(
                            guiGraphics,
                            StyledText.fromComponent(
                                    Component.translatable("screens.wynntils.customSeaskipperScreen.origin")),
                            10 + xOffset,
                            10 + renderYOffset,
                            CommonColors.ORANGE,
                            HorizontalAlignment.LEFT,
                            VerticalAlignment.TOP,
                            TextShadow.OUTLINE);
        } else {
            renderYOffset += 10;

            FontRenderer.getInstance()
                    .renderText(
                            guiGraphics,
                            StyledText.fromComponent(
                                    Component.translatable("screens.wynntils.customSeaskipperScreen.inaccessible")),
                            10 + xOffset,
                            10 + renderYOffset,
                            CommonColors.GRAY,
                            HorizontalAlignment.LEFT,
                            VerticalAlignment.TOP,
                            TextShadow.OUTLINE);
        }

        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        guiGraphics,
                        StyledText.fromString(destination.profile().destination()),
                        7,
                        textureWidth,
                        Texture.MAP_INFO_TOOLTIP_TOP.height() + centerHeight,
                        Texture.MAP_INFO_TOOLTIP_TOP.height() + centerHeight + Texture.MAP_INFO_NAME_BOX.height(),
                        0,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.MIDDLE,
                        TextShadow.OUTLINE);
    }

    private void renderScrollButton(GuiGraphics guiGraphics) {
        if (availableDestinations.size() <= MAX_DESTINATIONS) return;

        scrollButtonRenderY = (int) (departureBoardY + 4 * currentTextureScale * 0.933f)
                + MathUtils.map(
                        scrollOffset,
                        0,
                        availableDestinations.size() - MAX_DESTINATIONS,
                        0,
                        scrollAreaHeight - Texture.SCROLL_BUTTON.height() * currentTextureScale);

        RenderUtils.drawScalingTexturedRect(
                guiGraphics,
                Texture.SCROLL_BUTTON,
                scrollButtonRenderX,
                scrollButtonRenderY,
                Texture.SCROLL_BUTTON.width() * currentTextureScale,
                Texture.SCROLL_BUTTON.height() * currentTextureScale);
    }

    private void scroll(int delta) {
        // Calculate how many destinations should be scrolled past
        scrollOffset =
                MathUtils.clamp(scrollOffset + delta, 0, Math.max(0, availableDestinations.size() - MAX_DESTINATIONS));

        reloadButtons();
    }

    private void zoomToDestination(SeaskipperDestination destination) {
        // Center on the destination
        updateMapCenter(destination.profile().getX(), destination.profile().getZ());

        setZoomLevel(DEFAULT_ZOOM_LEVEL);
    }

    private void reloadButtons() {
        for (AbstractWidget widget : destinationButtons) {
            this.removeWidget(widget);
        }

        destinationButtons = new ArrayList<>();

        int buttonY = (int) (departureBoardY + 4 * currentTextureScale * 0.933f);
        int currentDestination;

        int buttonHeight = (int) ((Texture.DESTINATION_BUTTON.height() / 2) * currentTextureScale);
        int totalGapSpace = scrollAreaHeight - (buttonHeight * MAX_DESTINATIONS);
        int buttonOffset = totalGapSpace / (MAX_DESTINATIONS - 1);

        for (int i = 0; i < MAX_DESTINATIONS; i++) {
            currentDestination = i + scrollOffset;

            if (currentDestination > availableDestinations.size() - 1) {
                break;
            }

            if (!availableDestinations.get(currentDestination).isAvailable()) continue;

            SeaskipperDestinationButton button = new SeaskipperDestinationButton(
                    (int) destinationButtonsRenderX,
                    buttonY,
                    (int) (Texture.DESTINATION_BUTTON.width() * currentTextureScale),
                    buttonHeight,
                    availableDestinations.get(currentDestination),
                    this);

            destinationButtons.add(button);

            buttonY += (int) ((Texture.DESTINATION_BUTTON.height() / 2) * currentTextureScale) + buttonOffset;
        }
    }

    private void toggleBorders() {
        hideTerritoryBorders = !hideTerritoryBorders;

        if (hideTerritoryBorders) {
            Services.MapData.registerOverrideProvider(
                    SEASKIPPER_LOCATION_BORDER_OVERIDE_PROVIDER_ID, SEASKIPPER_LOCATION_BORDER_OVERRIDE_PROVIDER);
        } else {
            Services.MapData.unregisterOverrideProvider(SEASKIPPER_LOCATION_BORDER_OVERIDE_PROVIDER_ID);
        }
    }

    private void toggleDestinations() {
        renderAllDestinations = !renderAllDestinations;

        reloadDestinations();
    }

    private void toggleRoutes() {
        renderRoutes = !renderRoutes;
    }

    private static final class SeaskipperLocationBorderOverrideProvider extends AbstractMapDataOverrideProvider {
        @Override
        public MapAttributes getOverrideAttributes(MapFeature mapFeature) {
            if (!(mapFeature instanceof SeaskipperDestinationArea seaskipperDestinationArea)) {
                return new AbstractMapAreaAttributes() {};
            }

            return new AbstractMapAreaAttributes() {
                @Override
                public Optional<Float> getBorderWidth() {
                    return Optional.of(0f);
                }
            };
        }

        @Override
        public Stream<String> getOverridenFeatureIds() {
            return Stream.empty();
        }

        @Override
        public Stream<String> getOverridenCategoryIds() {
            return Stream.of("wynntils:fast-travel:seaskipper-destination");
        }
    }
}
