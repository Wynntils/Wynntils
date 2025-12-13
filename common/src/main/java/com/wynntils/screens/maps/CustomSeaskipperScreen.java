/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.maps;

import com.mojang.blaze3d.platform.cursor.CursorTypes;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.features.ui.CustomSeaskipperScreenFeature;
import com.wynntils.screens.base.widgets.BasicTexturedButton;
import com.wynntils.screens.maps.widgets.SeaskipperDestinationButton;
import com.wynntils.screens.maps.widgets.SeaskipperTravelButton;
import com.wynntils.services.map.pois.SeaskipperDestinationPoi;
import com.wynntils.utils.MathUtils;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.mc.type.PoiLocation;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.MapRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import com.wynntils.utils.type.BoundingBox;
import com.wynntils.utils.type.BoundingShape;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public final class CustomSeaskipperScreen extends AbstractMapScreen {
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
    private List<SeaskipperDestinationPoi> availablePois = new ArrayList<>();
    private List<SeaskipperDestinationPoi> destinationPois = new ArrayList<>();

    // Seaskipper pois
    private SeaskipperDestinationPoi currentLocationPoi = null;
    private SeaskipperDestinationPoi hoveredPoi;
    private SeaskipperDestinationPoi selectedPoi;

    // UI Size, position etc
    private boolean draggingScroll = false;
    private boolean firstInit = true;
    private double currentUnusedScroll = 0;
    private float currentTextureScale;
    private int departureBoardY;
    private int destinationButtonsRenderX;
    private int scrollButtonRenderX;
    private int scrollButtonRenderY;
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
        departureBoardY = (int) ((this.height
                        - (Texture.DESTINATION_LIST.height() * currentTextureScale
                                + (Texture.TRAVEL_BUTTON.height() / 2) * currentTextureScale))
                / 2);
        scrollButtonRenderX = (int) (5 + departureListWidth * 0.933f);
        destinationButtonsRenderX = (int) (5 + departureListWidth * 0.027f);
        scrollAreaHeight = (int) (SCROLL_HEIGHT * currentTextureScale);

        // region Map buttons
        this.addRenderableWidget(new BasicTexturedButton(
                (int) (width / 2 - Texture.MAP_BUTTONS_BACKGROUND.width() / 2 + 7 + 20 * 3 + (departureListWidth) / 2),
                (int) (this.renderHeight
                        - this.renderedBorderYOffset
                        - Texture.MAP_BUTTONS_BACKGROUND.height() / 2
                        - 8),
                12,
                16,
                Texture.WAYPOINT_MANAGER_ICON,
                (b) -> toggleRoutes(),
                List.of(
                        Component.literal("[>] ")
                                .withStyle(ChatFormatting.LIGHT_PURPLE)
                                .append(Component.translatable(
                                        "screens.wynntils.customSeaskipperScreen.showRoutes.name")),
                        Component.translatable("screens.wynntils.customSeaskipperScreen.showRoutes.description")
                                .withStyle(ChatFormatting.GRAY))));

        this.addRenderableWidget(new BasicTexturedButton(
                (int) (width / 2 - Texture.MAP_BUTTONS_BACKGROUND.width() / 2 + 6 + 20 * 2 + (departureListWidth) / 2),
                (int) (this.renderHeight
                        - this.renderedBorderYOffset
                        - Texture.MAP_BUTTONS_BACKGROUND.height() / 2
                        - 7),
                14,
                14,
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

        this.addRenderableWidget(new BasicTexturedButton(
                (int) (width / 2 - Texture.MAP_BUTTONS_BACKGROUND.width() / 2 + 5 + 20 + (departureListWidth) / 2),
                (int) (this.renderHeight
                        - this.renderedBorderYOffset
                        - Texture.MAP_BUTTONS_BACKGROUND.height() / 2
                        - 8),
                16,
                16,
                Texture.OVERLAY_EXTRA_ICON,
                (b) -> toggleBorders(),
                List.of(
                        Component.literal("[>] ")
                                .withStyle(ChatFormatting.BLUE)
                                .append(Component.translatable(
                                        "screens.wynntils.customSeaskipperScreen.showBorders.name")),
                        Component.translatable("screens.wynntils.customSeaskipperScreen.showBorders.description")
                                .withStyle(ChatFormatting.GRAY))));

        this.addRenderableWidget(new BasicTexturedButton(
                (int) (width / 2 - Texture.MAP_BUTTONS_BACKGROUND.width() / 2 + 6 + (departureListWidth) / 2),
                (int) (this.renderHeight
                        - this.renderedBorderYOffset
                        - Texture.MAP_BUTTONS_BACKGROUND.height() / 2
                        - 7),
                15,
                14,
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
                (int) ((Texture.TRAVEL_BUTTON.height() / 2) * currentTextureScale),
                this));

        // Only center the map and reload possible pois for first init
        if (firstInit) {
            centerMapAroundPlayer();
            setZoomLevel(DEFAULT_ZOOM_LEVEL);
            reloadDestinationPois();
            firstInit = false;
        }

        reloadButtons();
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

        renderPois(guiGraphics, mouseX, mouseY);

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

        renderMapBorder(guiGraphics);

        renderCoordinates(guiGraphics, mouseX, mouseY);

        renderMapButtons(guiGraphics, mouseX, mouseY, partialTick);

        renderHoveredSeaskipperDestination(guiGraphics);

        renderTooltip(guiGraphics, mouseX, mouseY);

        RenderUtils.drawScalingTexturedRect(
                guiGraphics,
                Texture.DESTINATION_LIST.identifier(),
                5,
                departureBoardY,
                Texture.DESTINATION_LIST.width() * currentTextureScale,
                Texture.DESTINATION_LIST.height() * currentTextureScale,
                Texture.DESTINATION_LIST.width(),
                Texture.DESTINATION_LIST.height());

        renderScrollButton(guiGraphics);

        for (SeaskipperDestinationButton destinationButton : destinationButtons) {
            destinationButton.render(guiGraphics, mouseX, mouseY, partialTick);
        }

        if (isPanning) {
            guiGraphics.requestCursor(CursorTypes.RESIZE_ALL);
        } else if (draggingScroll) {
            guiGraphics.requestCursor(CursorTypes.RESIZE_NS);
        } else if (this.hoveredPoi != null
                || MathUtils.isInside(
                        mouseX,
                        mouseY,
                        scrollButtonRenderX,
                        (int) (scrollButtonRenderX + Texture.SCROLL_BUTTON.width() * currentTextureScale),
                        scrollButtonRenderY,
                        (int) (scrollButtonRenderY + Texture.SCROLL_BUTTON.height() * currentTextureScale))) {
            guiGraphics.requestCursor(CursorTypes.POINTING_HAND);
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
            if (child.isMouseOver(event.x(), event.y())) {
                child.mouseClicked(event, isDoubleClick);
                return true;
            }
        }

        // If clicked on a destinationPoi and it is available, try and purchase a pass
        for (SeaskipperDestinationPoi poi : destinationPois) {
            if (hoveredPoi == poi) {
                if (poi.isAvailable()) {
                    Models.Seaskipper.purchasePass(poi.getDestination());
                }

                return true;
            }
        }

        // If clicked on a destination button, set it as selected and zoom on the destination.
        // If the button was already selected, then try and purchase a pass
        for (SeaskipperDestinationButton destinationButton : destinationButtons) {
            if (destinationButton.isHovered() && selectedPoi != destinationButton.getDestination()) {
                selectedPoi = destinationButton.getDestination();
                zoomToDestination(selectedPoi);
                return true;
            } else if (destinationButton.isHovered() && selectedPoi == destinationButton.getDestination()) {
                Models.Seaskipper.purchasePass(
                        destinationButton.getDestination().getDestination());
                return true;
            }
        }

        if (!draggingScroll && (availablePois.size() > MAX_DESTINATIONS)) {
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
                    Math.max(0, availablePois.size() - MAX_DESTINATIONS)));

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

    public void reloadDestinationPois() {
        destinationPois = new ArrayList<>();

        destinationPois.addAll(Models.Seaskipper.getPois(renderAllDestinations));

        currentLocationPoi = destinationPois.stream()
                .filter(SeaskipperDestinationPoi::isPlayerInside)
                .findFirst()
                .orElse(null);

        // Available pois to use for the departure list
        availablePois = destinationPois.stream()
                .filter(SeaskipperDestinationPoi::isAvailable)
                .sorted(Comparator.comparing(SeaskipperDestinationPoi::getName, String.CASE_INSENSITIVE_ORDER))
                .toList();

        reloadButtons();
    }

    public void travelToDestination() {
        if (selectedPoi == null) return;

        Models.Seaskipper.purchasePass(selectedPoi.getDestination());
    }

    public SeaskipperDestinationPoi getSelectedDestination() {
        return selectedPoi;
    }

    private void renderPois(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        renderDestinations(
                destinationPois,
                guiGraphics,
                BoundingBox.centered(mapCenterX, mapCenterZ, width / zoomRenderScale, height / zoomRenderScale),
                1,
                mouseX,
                mouseY);
    }

    private void renderDestinations(
            List<SeaskipperDestinationPoi> pois,
            GuiGraphics guiGraphics,
            BoundingBox textureBoundingBox,
            float poiScale,
            int mouseX,
            int mouseY) {
        hoveredPoi = null;

        List<SeaskipperDestinationPoi> filteredPois =
                getRenderedDestinations(pois, textureBoundingBox, poiScale, mouseX, mouseY);

        if (renderRoutes) {
            float poiRenderX = MapRenderer.getRenderX(currentLocationPoi, mapCenterX, centerX, zoomRenderScale);
            float poiRenderZ = MapRenderer.getRenderZ(currentLocationPoi, mapCenterZ, centerZ, zoomRenderScale);

            for (SeaskipperDestinationPoi poi : destinationPois.stream()
                    .filter(SeaskipperDestinationPoi::isAvailable)
                    .toList()) {
                float x = MapRenderer.getRenderX(poi, mapCenterX, centerX, zoomRenderScale);
                float z = MapRenderer.getRenderZ(poi, mapCenterZ, centerZ, zoomRenderScale);

                RenderUtils.drawLine(
                        guiGraphics, CommonColors.DARK_GRAY.withAlpha(0.5f), poiRenderX, poiRenderZ, x, z, 1);
            }
        }

        for (int i = filteredPois.size() - 1; i >= 0; i--) {
            SeaskipperDestinationPoi poi = filteredPois.get(i);

            float poiRenderX = MapRenderer.getRenderX(poi, mapCenterX, centerX, zoomRenderScale);
            float poiRenderZ = MapRenderer.getRenderZ(poi, mapCenterZ, centerZ, zoomRenderScale);

            if (hideTerritoryBorders) {
                poi.renderAtWithoutBorders(guiGraphics, poiRenderX, poiRenderZ, zoomRenderScale);
            } else {
                poi.renderAt(
                        guiGraphics,
                        poiRenderX,
                        poiRenderZ,
                        hoveredPoi == poi,
                        poiScale,
                        zoomRenderScale,
                        zoomLevel,
                        true);
            }
        }
    }

    private List<SeaskipperDestinationPoi> getRenderedDestinations(
            List<SeaskipperDestinationPoi> pois,
            BoundingBox textureBoundingBox,
            float poiScale,
            int mouseX,
            int mouseY) {
        List<SeaskipperDestinationPoi> filteredPois = new ArrayList<>();

        for (int i = pois.size() - 1; i >= 0; i--) {
            SeaskipperDestinationPoi poi = pois.get(i);
            PoiLocation location = poi.getLocation();

            if (location == null) continue;

            float poiRenderX = MapRenderer.getRenderX(poi, mapCenterX, centerX, zoomRenderScale);
            float poiRenderZ = MapRenderer.getRenderZ(poi, mapCenterZ, centerZ, zoomRenderScale);

            float poiWidth = poi.getWidth(zoomRenderScale, poiScale);
            float poiHeight = poi.getHeight(zoomRenderScale, poiScale);

            BoundingBox filterBox = BoundingBox.centered(location.getX(), location.getZ(), poiWidth, poiHeight);
            BoundingBox mouseBox = BoundingBox.centered(poiRenderX, poiRenderZ, poiWidth, poiHeight);

            if (BoundingShape.intersects(filterBox, textureBoundingBox)) {
                filteredPois.add(poi);
                if (hoveredPoi == null && mouseBox.contains(mouseX, mouseY)) {
                    hoveredPoi = poi;
                }
            }
        }

        if (hoveredPoi != null) {
            filteredPois.remove(hoveredPoi);
            filteredPois.addFirst(hoveredPoi);
        }

        return filteredPois;
    }

    private void renderHoveredSeaskipperDestination(GuiGraphics guiGraphics) {
        if (hoveredPoi == null) return;

        int xOffset = (int) (width - SCREEN_SIDE_OFFSET - 250);
        int yOffset = (int) (SCREEN_SIDE_OFFSET + 40);

        boolean isAccessible = hoveredPoi.isAvailable();

        final float centerHeight = isAccessible ? 50 : 30;
        final int textureWidth = Texture.MAP_INFO_TOOLTIP_CENTER.width();

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

        FontRenderer.getInstance()
                .renderText(
                        guiGraphics,
                        StyledText.fromComponent(Component.translatable(
                                "screens.wynntils.customSeaskipperScreen.level", hoveredPoi.getLevel())),
                        10 + xOffset,
                        10 + yOffset,
                        CommonColors.ORANGE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.TOP,
                        TextShadow.OUTLINE);

        float renderYOffset = 10 + yOffset;

        boolean origin = hoveredPoi == currentLocationPoi;

        if (isAccessible) {
            int price = hoveredPoi.getDestination().item().getPrice();

            CustomColor priceColor;
            Component travelComponent;

            if (Models.Emerald.getAmountInInventory()
                    >= hoveredPoi.getDestination().item().getPrice()) {
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
                        StyledText.fromString(hoveredPoi.getName()),
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

    private void renderScrollButton(GuiGraphics guiGraphics) {
        if (availablePois.size() <= MAX_DESTINATIONS) return;

        scrollButtonRenderY = (int) ((int) (departureBoardY + 4 * currentTextureScale * 0.933f)
                + MathUtils.map(
                        scrollOffset,
                        0,
                        availablePois.size() - MAX_DESTINATIONS,
                        0,
                        scrollAreaHeight - Texture.SCROLL_BUTTON.height() * currentTextureScale));

        RenderUtils.drawScalingTexturedRect(
                guiGraphics,
                Texture.SCROLL_BUTTON.identifier(),
                scrollButtonRenderX,
                scrollButtonRenderY,
                (int) (Texture.SCROLL_BUTTON.width() * currentTextureScale),
                (int) (Texture.SCROLL_BUTTON.height() * currentTextureScale),
                Texture.SCROLL_BUTTON.width(),
                Texture.SCROLL_BUTTON.height());
    }

    private void scroll(int delta) {
        // Calculate how many destinations should be scrolled past
        scrollOffset = MathUtils.clamp(scrollOffset + delta, 0, Math.max(0, availablePois.size() - MAX_DESTINATIONS));

        reloadButtons();
    }

    private void zoomToDestination(SeaskipperDestinationPoi destination) {
        // Center on the destination
        updateMapCenter(
                destination.getLocation().getX(), destination.getLocation().getZ());

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

            if (currentDestination > availablePois.size() - 1) {
                break;
            }

            if (!availablePois.get(currentDestination).isAvailable()) continue;

            SeaskipperDestinationButton button = new SeaskipperDestinationButton(
                    (int) destinationButtonsRenderX,
                    buttonY,
                    (int) (Texture.DESTINATION_BUTTON.width() * currentTextureScale),
                    buttonHeight,
                    availablePois.get(currentDestination),
                    this);

            destinationButtons.add(button);

            buttonY += (int) ((Texture.DESTINATION_BUTTON.height() / 2) * currentTextureScale) + buttonOffset;
        }
    }

    private void toggleBorders() {
        hideTerritoryBorders = !hideTerritoryBorders;
    }

    private void toggleDestinations() {
        renderAllDestinations = !renderAllDestinations;

        reloadDestinationPois();
    }

    private void toggleRoutes() {
        renderRoutes = !renderRoutes;
    }
}
