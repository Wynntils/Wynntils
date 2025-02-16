/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.maps;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.features.ui.CustomSeaskipperScreenFeature;
import com.wynntils.models.seaskipper.type.SeaskipperDestination;
import com.wynntils.screens.maps.widgets.MapButton;
import com.wynntils.screens.maps.widgets.SeaskipperDestinationButton;
import com.wynntils.screens.maps.widgets.SeaskipperTravelButton;
import com.wynntils.services.mapdata.features.type.MapFeature;
import com.wynntils.utils.MathUtils;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
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
    private List<SeaskipperDestination> availableDestinations = new ArrayList<>();
    private List<SeaskipperDestination> destinationDestinations = new ArrayList<>();

    // Seaskipper pois
    private SeaskipperDestination currentLocation = null;
    private SeaskipperDestination hoveredDestionation;
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
    public void doRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        PoseStack poseStack = guiGraphics.pose();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        RenderSystem.enableDepthTest();

        renderMap(poseStack);

        RenderUtils.enableScissor(
                (int) (renderX + renderedBorderXOffset), (int) (renderY + renderedBorderYOffset), (int) mapWidth, (int)
                        mapHeight);

        renderCursor(
                poseStack,
                1.5f,
                Managers.Feature.getFeatureInstance(CustomSeaskipperScreenFeature.class)
                        .pointerColor
                        .get(),
                Managers.Feature.getFeatureInstance(CustomSeaskipperScreenFeature.class)
                        .pointerType
                        .get());

        RenderUtils.disableScissor();

        renderBackground(guiGraphics, mouseX, mouseY, partialTick);

        renderCoordinates(poseStack, mouseX, mouseY);

        renderMapButtons(guiGraphics, mouseX, mouseY, partialTick);

        renderZoomWidgets(guiGraphics, mouseX, mouseY, partialTick);

        renderHoveredSeaskipperDestination(poseStack);

        renderTooltip(guiGraphics, mouseX, mouseY);

        RenderUtils.drawScalingTexturedRect(
                poseStack,
                Texture.DESTINATION_LIST.resource(),
                5,
                departureBoardY,
                0,
                Texture.DESTINATION_LIST.width() * currentTextureScale,
                Texture.DESTINATION_LIST.height() * currentTextureScale,
                Texture.DESTINATION_LIST.width(),
                Texture.DESTINATION_LIST.height());

        renderScrollButton(poseStack);

        for (SeaskipperDestinationButton destinationButton : destinationButtons) {
            destinationButton.render(guiGraphics, mouseX, mouseY, partialTick);
        }
    }

    @Override
    public void onClose() {
        McUtils.player().closeContainer();
        super.onClose();
    }

    @Override
    public boolean doMouseClicked(double mouseX, double mouseY, int button) {
        for (GuiEventListener child : children()) {
            if (child.isMouseOver(mouseX, mouseY)) {
                child.mouseClicked(mouseX, mouseY, button);
                return true;
            }
        }

        // If clicked on a destination and it is available, try and purchase a pass
        for (SeaskipperDestination destination : destinationDestinations) {
            if (hoveredDestionation == destination) {
                if (destination.isAvailable()) {
                    Models.Seaskipper.purchasePass(destination);
                }

                return true;
            }
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
                    (int) mouseX,
                    (int) mouseY,
                    (int) scrollButtonRenderX,
                    (int) (scrollButtonRenderX + Texture.SCROLL_BUTTON.width() * currentTextureScale),
                    (int) scrollButtonRenderY,
                    (int) (scrollButtonRenderY + Texture.SCROLL_BUTTON.height() * currentTextureScale))) {
                draggingScroll = true;

                return true;
            }
        }

        return super.doMouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (draggingScroll) {
            int startRenderY = (int) (departureBoardY + 4 * currentTextureScale * 0.933f);
            int scrollAreaStartY = startRenderY + 9;

            int newValue = Math.round(MathUtils.map(
                    (float) mouseY,
                    scrollAreaStartY,
                    scrollAreaStartY + scrollAreaHeight - Texture.SCROLL_BUTTON.height() * currentTextureScale,
                    0,
                    Math.max(0, availableDestinations.size() - MAX_DESTINATIONS)));

            scroll(newValue - scrollOffset);

            return true;
        }

        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        draggingScroll = false;
        return super.mouseReleased(mouseX, mouseY, button);
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
        destinationDestinations = new ArrayList<>();

        destinationDestinations.addAll(Models.Seaskipper.getDestinations(renderAllDestinations));

        currentLocation = destinationDestinations.stream()
                .filter(SeaskipperDestination::isPlayerInside)
                .findFirst()
                .orElse(null);

        // Available pois to use for the departure list
        availableDestinations = destinationDestinations.stream()
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
        return Stream.empty();
    }

    //    private void renderPois(PoseStack poseStack, int mouseX, int mouseY) {
    //        renderDestinations(
    //                destinationDestinations,
    //                poseStack,
    //                BoundingBox.centered(mapCenterX, mapCenterZ, width / zoomRenderScale, height / zoomRenderScale),
    //                1,
    //                mouseX,
    //                mouseY);
    //    }

    //    private void renderDestinations(
    //            List<SeaskipperDestination> destinations,
    //            PoseStack poseStack,
    //            BoundingBox textureBoundingBox,
    //            float poiScale,
    //            int mouseX,
    //            int mouseY) {
    //        hoveredDestionation = null;
    //
    //        List<SeaskipperDestination> filteredDestinations =
    //                getRenderedDestinations(destinations, textureBoundingBox, poiScale, mouseX, mouseY);
    //
    //        if (renderRoutes) {
    //            float poiRenderX = MapRenderer.getRenderX(currentLocation, mapCenterX, centerX, zoomRenderScale);
    //            float poiRenderZ = MapRenderer.getRenderZ(currentLocation, mapCenterZ, centerZ, zoomRenderScale);
    //
    //            for (SeaskipperDestinationPoi poi : destinationDestinations.stream()
    //                    .filter(SeaskipperDestinationPoi::isAvailable)
    //                    .toList()) {
    //                float x = MapRenderer.getRenderX(poi, mapCenterX, centerX, zoomRenderScale);
    //                float z = MapRenderer.getRenderZ(poi, mapCenterZ, centerZ, zoomRenderScale);
    //
    //                RenderUtils.drawLine(
    //                        poseStack, CommonColors.DARK_GRAY.withAlpha(0.5f), poiRenderX, poiRenderZ, x, z, 0, 1);
    //            }
    //        }
    //
    //        MultiBufferSource.BufferSource bufferSource =
    //                McUtils.mc().renderBuffers().bufferSource();
    //
    //        for (int i = filteredDestinations.size() - 1; i >= 0; i--) {
    //            SeaskipperDestinationPoi poi = filteredDestinations.get(i);
    //
    //            float poiRenderX = MapRenderer.getRenderX(poi, mapCenterX, centerX, zoomRenderScale);
    //            float poiRenderZ = MapRenderer.getRenderZ(poi, mapCenterZ, centerZ, zoomRenderScale);
    //
    //            if (hideTerritoryBorders) {
    //                poi.renderAtWithoutBorders(poseStack, bufferSource, poiRenderX, poiRenderZ, zoomRenderScale);
    //            } else {
    //                poi.renderAt(
    //                        poseStack,
    //                        bufferSource,
    //                        poiRenderX,
    //                        poiRenderZ,
    //                        hoveredDestionation == poi,
    //                        poiScale,
    //                        zoomRenderScale,
    //                        zoomLevel,
    //                        true);
    //            }
    //        }
    //
    //        bufferSource.endBatch();
    //    }

    //    private List<SeaskipperDestinationPoi> getRenderedDestinations(
    //            List<SeaskipperDestinationPoi> pois,
    //            BoundingBox textureBoundingBox,
    //            float poiScale,
    //            int mouseX,
    //            int mouseY) {
    //        List<SeaskipperDestinationPoi> filteredPois = new ArrayList<>();
    //
    //        for (int i = pois.size() - 1; i >= 0; i--) {
    //            SeaskipperDestinationPoi poi = pois.get(i);
    //            PoiLocation location = poi.getLocation();
    //
    //            if (location == null) continue;
    //
    //            float poiRenderX = MapRenderer.getRenderX(poi, mapCenterX, centerX, zoomRenderScale);
    //            float poiRenderZ = MapRenderer.getRenderZ(poi, mapCenterZ, centerZ, zoomRenderScale);
    //
    //            float poiWidth = poi.getWidth(zoomRenderScale, poiScale);
    //            float poiHeight = poi.getHeight(zoomRenderScale, poiScale);
    //
    //            BoundingBox filterBox = BoundingBox.centered(location.getX(), location.getZ(), poiWidth, poiHeight);
    //            BoundingBox mouseBox = BoundingBox.centered(poiRenderX, poiRenderZ, poiWidth, poiHeight);
    //
    //            if (BoundingShape.intersects(filterBox, textureBoundingBox)) {
    //                filteredPois.add(poi);
    //                if (hoveredDestionation == null && mouseBox.contains(mouseX, mouseY)) {
    //                    hoveredDestionation = poi;
    //                }
    //            }
    //        }
    //
    //        if (hoveredDestionation != null) {
    //            filteredPois.remove(hoveredDestionation);
    //            filteredPois.addFirst(hoveredDestionation);
    //        }
    //
    //        return filteredPois;
    //    }

    private void renderHoveredSeaskipperDestination(PoseStack poseStack) {
        if (hoveredDestionation == null) return;

        poseStack.pushPose();
        poseStack.translate(width - SCREEN_SIDE_OFFSET - 250, SCREEN_SIDE_OFFSET + 40, 101);

        boolean isAccessible = hoveredDestionation.isAvailable();

        final float centerHeight = isAccessible ? 50 : 30;
        final int textureWidth = Texture.MAP_INFO_TOOLTIP_CENTER.width();

        RenderUtils.drawTexturedRect(poseStack, Texture.MAP_INFO_TOOLTIP_TOP, 0, 0);
        RenderUtils.drawTexturedRect(
                poseStack,
                Texture.MAP_INFO_TOOLTIP_CENTER.resource(),
                0,
                Texture.MAP_INFO_TOOLTIP_TOP.height(),
                textureWidth,
                centerHeight,
                textureWidth,
                Texture.MAP_INFO_TOOLTIP_CENTER.height());
        RenderUtils.drawTexturedRect(
                poseStack, Texture.MAP_INFO_NAME_BOX, 0, Texture.MAP_INFO_TOOLTIP_TOP.height() + centerHeight);

        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        StyledText.fromComponent(Component.translatable(
                                "screens.wynntils.customSeaskipperScreen.level",
                                hoveredDestionation.profile().combatLevel())),
                        10,
                        10,
                        CommonColors.ORANGE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.TOP,
                        TextShadow.OUTLINE);

        float renderYOffset = 10;

        boolean origin = hoveredDestionation == currentLocation;

        if (isAccessible) {
            int price = hoveredDestionation.item().getPrice();

            CustomColor priceColor;
            Component travelComponent;

            if (Models.Emerald.getAmountInInventory()
                    >= hoveredDestionation.item().getPrice()) {
                priceColor = CommonColors.GREEN;
                travelComponent = Component.translatable("screens.wynntils.customSeaskipperScreen.clickToGo");
            } else {
                priceColor = CommonColors.RED;
                travelComponent = Component.translatable("screens.wynntils.customSeaskipperScreen.cannotAfford");
            }

            renderYOffset += 10;

            FontRenderer.getInstance()
                    .renderText(
                            poseStack,
                            StyledText.fromComponent(
                                    Component.translatable("screens.wynntils.customSeaskipperScreen.cost", price)),
                            10,
                            10 + renderYOffset,
                            priceColor,
                            HorizontalAlignment.LEFT,
                            VerticalAlignment.TOP,
                            TextShadow.OUTLINE);

            renderYOffset += 20;

            FontRenderer.getInstance()
                    .renderText(
                            poseStack,
                            StyledText.fromComponent(travelComponent),
                            10,
                            10 + renderYOffset,
                            CommonColors.LIGHT_BLUE,
                            HorizontalAlignment.LEFT,
                            VerticalAlignment.TOP,
                            TextShadow.OUTLINE);
        } else if (origin) {
            renderYOffset += 10;

            FontRenderer.getInstance()
                    .renderText(
                            poseStack,
                            StyledText.fromComponent(
                                    Component.translatable("screens.wynntils.customSeaskipperScreen.origin")),
                            10,
                            10 + renderYOffset,
                            CommonColors.ORANGE,
                            HorizontalAlignment.LEFT,
                            VerticalAlignment.TOP,
                            TextShadow.OUTLINE);
        } else {
            renderYOffset += 10;

            FontRenderer.getInstance()
                    .renderText(
                            poseStack,
                            StyledText.fromComponent(
                                    Component.translatable("screens.wynntils.customSeaskipperScreen.inaccessible")),
                            10,
                            10 + renderYOffset,
                            CommonColors.GRAY,
                            HorizontalAlignment.LEFT,
                            VerticalAlignment.TOP,
                            TextShadow.OUTLINE);
        }

        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        poseStack,
                        StyledText.fromString(hoveredDestionation.profile().destination()),
                        7,
                        textureWidth,
                        Texture.MAP_INFO_TOOLTIP_TOP.height() + centerHeight,
                        Texture.MAP_INFO_TOOLTIP_TOP.height() + centerHeight + Texture.MAP_INFO_NAME_BOX.height(),
                        0,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.MIDDLE,
                        TextShadow.OUTLINE);

        poseStack.popPose();
    }

    private void renderScrollButton(PoseStack poseStack) {
        if (availableDestinations.size() <= MAX_DESTINATIONS) return;

        scrollButtonRenderY = (int) (departureBoardY + 4 * currentTextureScale * 0.933f)
                + MathUtils.map(
                        scrollOffset,
                        0,
                        availableDestinations.size() - MAX_DESTINATIONS,
                        0,
                        scrollAreaHeight - Texture.SCROLL_BUTTON.height() * currentTextureScale);

        RenderUtils.drawScalingTexturedRect(
                poseStack,
                Texture.SCROLL_BUTTON.resource(),
                scrollButtonRenderX,
                scrollButtonRenderY,
                0,
                Texture.SCROLL_BUTTON.width() * currentTextureScale,
                Texture.SCROLL_BUTTON.height() * currentTextureScale,
                Texture.SCROLL_BUTTON.width(),
                Texture.SCROLL_BUTTON.height());
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
    }

    private void toggleDestinations() {
        renderAllDestinations = !renderAllDestinations;

        reloadDestinations();
    }

    private void toggleRoutes() {
        renderRoutes = !renderRoutes;
    }
}
