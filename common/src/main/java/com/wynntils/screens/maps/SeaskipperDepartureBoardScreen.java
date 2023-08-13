/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.maps;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Models;
import com.wynntils.features.ui.CustomSeaskipperScreenFeature;
import com.wynntils.screens.maps.widgets.SeaskipperBoatButton;
import com.wynntils.screens.maps.widgets.SeaskipperDestinationButton;
import com.wynntils.screens.maps.widgets.SeaskipperTravelButton;
import com.wynntils.services.map.pois.Poi;
import com.wynntils.services.map.pois.SeaskipperDestinationPoi;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.mc.type.PoiLocation;
import com.wynntils.utils.render.MapRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.type.BoundingBox;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.MultiBufferSource;

public final class SeaskipperDepartureBoardScreen extends AbstractMapScreen {
    private static final float BORDER_OFFSET = 3;
    private static final float SCREEN_WIDTH_OFFSET = 3;
    private static final float ZOOM_SCALE = 650f;

    private final List<SeaskipperDestinationButton> destinationButtons = new ArrayList<>();

    private float currentTextureScale = 1f;
    private float destinationListWidth;
    private float destinationListY;
    private List<SeaskipperDestinationPoi> destinationPois = new ArrayList<>();
    private SeaskipperDestinationPoi currentLocationPoi = null;
    private SeaskipperDestinationPoi selectedDestination;

    private SeaskipperDepartureBoardScreen() {}

    public static Screen create() {
        return new SeaskipperDepartureBoardScreen();
    }

    @Override
    public void onClose() {
        McUtils.player().closeContainer();
        super.onClose();
    }

    @Override
    protected void doInit() {
        currentTextureScale = (float) this.height / (Texture.DESTINATION_LIST.height() + 49);

        reloadDestinationPois();

        renderX = Texture.DESTINATION_LIST.width() * currentTextureScale + SCREEN_WIDTH_OFFSET * 2f;

        renderWidth = this.width - renderX - SCREEN_WIDTH_OFFSET;

        float borderScaleX = (float) this.width / Texture.FULLSCREEN_MAP_BORDER.width();
        float borderScaleY = (float) this.height / Texture.FULLSCREEN_MAP_BORDER.height();

        renderedBorderXOffset = BORDER_OFFSET * borderScaleX;
        renderedBorderYOffset = BORDER_OFFSET * borderScaleY;

        mapWidth = renderWidth - renderedBorderXOffset * 2f;

        centerX = renderX + renderedBorderXOffset + mapWidth / 2f;

        float boatButtonWidth = Texture.BOAT_BUTTON.width() * currentTextureScale;
        float boatButtonHeight = Texture.BOAT_BUTTON.height() / 2f * currentTextureScale;

        destinationListWidth = Texture.DESTINATION_LIST.width() * currentTextureScale;

        this.addRenderableWidget(new SeaskipperBoatButton(
                (int) (destinationListWidth - boatButtonWidth + 5f),
                (int) (this.height - boatButtonHeight - 5f),
                (int) boatButtonWidth,
                (int) boatButtonHeight));

        float travelButtonWidth = Texture.TRAVEL_BUTTON.width() * currentTextureScale;
        float travelButtonHeight = Texture.TRAVEL_BUTTON.height() / 2f * currentTextureScale;

        this.addRenderableWidget(new SeaskipperTravelButton(
                5,
                (int) (this.height - travelButtonHeight - 5f),
                (int) travelButtonWidth,
                (int) travelButtonHeight,
                this));

        destinationListY =
                this.height - Texture.DESTINATION_LIST.height() * currentTextureScale - travelButtonHeight - 5f;

        renderY = destinationListY;

        renderHeight = Texture.DESTINATION_LIST.height() * currentTextureScale;

        mapHeight = renderHeight - renderedBorderYOffset * 2f;

        centerZ = renderY + renderedBorderYOffset + mapHeight / 2f;
    }

    @Override
    public void doRender(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        renderGradientBackground(poseStack);

        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        RenderSystem.enableDepthTest();

        renderMap(poseStack);

        RenderUtils.enableScissor(
                (int) (renderX + renderedBorderXOffset), (int) (renderY + renderedBorderYOffset), (int) mapWidth, (int)
                        mapHeight);

        renderDestinations(
                poseStack,
                BoundingBox.centered(mapCenterX, mapCenterZ, width / currentZoom, height / currentZoom),
                1,
                mouseX,
                mouseY);

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

        renderBackground(poseStack);

        RenderUtils.drawScalingTexturedRect(
                poseStack,
                Texture.DESTINATION_LIST.resource(),
                5,
                destinationListY,
                0,
                destinationListWidth,
                Texture.DESTINATION_LIST.height() * currentTextureScale,
                Texture.DESTINATION_LIST.width(),
                Texture.DESTINATION_LIST.height());

        renderWidgets(poseStack, mouseX, mouseY, partialTick);
    }

    private void renderDestinations(
            PoseStack poseStack, BoundingBox textureBoundingBox, float poiScale, int mouseX, int mouseY) {
        List<SeaskipperDestinationPoi> filteredPois =
                getRenderedDestinations(textureBoundingBox, poiScale, mouseX, mouseY);

        MultiBufferSource.BufferSource bufferSource =
                McUtils.mc().renderBuffers().bufferSource();

        for (int i = filteredPois.size() - 1; i >= 0; i--) {
            SeaskipperDestinationPoi poi = filteredPois.get(i);

            float poiRenderX = MapRenderer.getRenderX(poi, mapCenterX, centerX, currentZoom);
            float poiRenderZ = MapRenderer.getRenderZ(poi, mapCenterZ, centerZ, currentZoom);

            poi.renderAt(poseStack, bufferSource, poiRenderX, poiRenderZ, false, poiScale, currentZoom);
        }

        bufferSource.endBatch();
    }

    private List<SeaskipperDestinationPoi> getRenderedDestinations(
            BoundingBox textureBoundingBox, float poiScale, int mouseX, int mouseY) {
        List<SeaskipperDestinationPoi> filteredPois = new ArrayList<>();

        for (int i = destinationPois.size() - 1; i >= 0; i--) {
            SeaskipperDestinationPoi poi = destinationPois.get(i);
            PoiLocation location = poi.getLocation();

            if (location == null) continue;

            float poiWidth = poi.getWidth(currentZoom, poiScale);
            float poiHeight = poi.getHeight(currentZoom, poiScale);

            BoundingBox filterBox = BoundingBox.centered(location.getX(), location.getZ(), poiWidth, poiHeight);

            if (filterBox.intersects(textureBoundingBox)) {
                filteredPois.add(poi);
            }
        }

        return filteredPois;
    }

    private void renderWidgets(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        for (Renderable renderable : this.renderables) {
            renderable.render(poseStack, mouseX, mouseY, partialTicks);
        }

        reloadButtons();

        for (SeaskipperDestinationButton destinationButton : destinationButtons) {
            destinationButton.render(poseStack, mouseX, mouseY, partialTicks);
        }
    }

    private void reloadButtons() {
        destinationButtons.clear();

        // This gets the job done for different GUI scales, but anything above scale 1 has the right column buttons
        // slightly more to the left and all buttons below row 1 are slightly higher
        int buttonWidth = (int) (Texture.DESTINATION_BUTTON.width() * currentTextureScale);
        int buttonHeight = (int) (Texture.DESTINATION_BUTTON.height() / 2f * currentTextureScale);
        int leftX = (int) (5 + (4 * currentTextureScale));
        int rightX = (int) (7 * currentTextureScale) + 7;
        int rowOffset = 5;
        int yOffset = (int) (destinationListY + rowOffset * currentTextureScale);
        int buttonIndex = 0;

        for (SeaskipperDestinationPoi poi : destinationPois) {
            if (poi == currentLocationPoi) continue;

            boolean selected = selectedDestination == poi;

            SeaskipperDestinationButton newButton;

            if (buttonIndex == 0) {
                newButton = new SeaskipperDestinationButton(
                        leftX,
                        (yOffset + (destinationButtons.size() * buttonHeight)),
                        buttonWidth,
                        buttonHeight,
                        selected,
                        poi);
            } else if (buttonIndex == 1) {
                newButton = new SeaskipperDestinationButton(
                        (rightX + buttonWidth),
                        (yOffset + ((destinationButtons.size() - 1) * buttonHeight)),
                        buttonWidth,
                        buttonHeight,
                        selected,
                        poi);

                rowOffset += 2;
                yOffset = (int) (destinationListY + rowOffset * currentTextureScale);
            } else if (buttonIndex % 2 == 0) {
                newButton = new SeaskipperDestinationButton(
                        leftX,
                        (yOffset + ((destinationButtons.size() - (buttonIndex / 2)) * buttonHeight)),
                        buttonWidth,
                        buttonHeight,
                        selected,
                        poi);
            } else {
                newButton = new SeaskipperDestinationButton(
                        (rightX + buttonWidth),
                        (yOffset + ((destinationButtons.size() - (buttonIndex / 2) - 1) * buttonHeight)),
                        buttonWidth,
                        buttonHeight,
                        selected,
                        poi);

                rowOffset += 2;
                yOffset = (int) (destinationListY + rowOffset * currentTextureScale);
            }

            buttonIndex++;

            destinationButtons.add(newButton);
        }
    }

    public void travelToDestination() {
        if (selectedDestination != null) {
            Models.Seaskipper.purchasePass(selectedDestination.getDestination());
        }
    }

    private void zoomToDestination(Poi destination) {
        updateMapCenter(
                destination.getLocation().getX(), destination.getLocation().getZ());

        float zoomLevel = McUtils.mc().getWindow().getGuiScaledHeight() / ZOOM_SCALE;

        setZoom(zoomLevel);
    }

    @Override
    public boolean doMouseClicked(double mouseX, double mouseY, int button) {
        for (GuiEventListener child : children()) {
            if (child.isMouseOver(mouseX, mouseY)) {
                child.mouseClicked(mouseX, mouseY, button);
                return true;
            }
        }

        for (SeaskipperDestinationButton destinationButton : destinationButtons) {
            if (destinationButton.isMouseOver(mouseX, mouseY)) {
                destinationButton.mouseClicked(mouseX, mouseY, button);

                if (selectedDestination != null
                        && selectedDestination.getDestination()
                                == destinationButton.getDestination().getDestination()) {
                    Models.Seaskipper.purchasePass(selectedDestination.getDestination());
                } else {
                    selectedDestination = destinationButton.getDestination();

                    zoomToDestination(selectedDestination);
                }

                break;
            }
        }

        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        // Disallow zooming
        return true;
    }

    public void reloadDestinationPois() {
        destinationPois = new ArrayList<>();

        destinationPois.addAll(Models.Seaskipper.getPois(false));

        currentLocationPoi = destinationPois.stream()
                .filter(SeaskipperDestinationPoi::isPlayerInside)
                .findFirst()
                .orElse(null);

        zoomToDestination(currentLocationPoi);
    }

    public boolean hasSelectedDestination() {
        return selectedDestination != null;
    }
}
