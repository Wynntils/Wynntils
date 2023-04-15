/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.maps;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Models;
import com.wynntils.core.text.CodedString;
import com.wynntils.features.map.GuildMapFeature;
import com.wynntils.models.items.items.gui.SeaskipperDestinationItem;
import com.wynntils.models.map.PoiLocation;
import com.wynntils.models.map.pois.SeaskipperPoi;
import com.wynntils.screens.base.widgets.BasicTexturedButton;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.MapRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import com.wynntils.utils.type.BoundingBox;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;

public final class SeaskipperMapScreen extends AbstractMapScreen {
    private final int MAP_CENTER_X = -240;
    private final int MAP_CENTER_Y = -3130;
    private SeaskipperPoi hoveredPoi;
    private boolean hideTerritoryBorders = false;
    private boolean renderAllDestinations = false;
    private boolean renderRoutes = false;

    private SeaskipperMapScreen() {}

    public static Screen create() {
        return new SeaskipperMapScreen();
    }

    @Override
    public void onClose() {
        Models.Seaskipper.closeScreen();
        super.onClose();
    }

    @Override
    protected void doInit() {
        super.doInit();

        this.addRenderableWidget(new BasicTexturedButton(
                width / 2 - Texture.MAP_BUTTONS_BACKGROUND.width() / 2 + 6 + 20 * 3,
                (int) (this.renderHeight
                        - this.renderedBorderYOffset
                        - Texture.MAP_BUTTONS_BACKGROUND.height() / 2
                        - 6),
                16,
                16,
                Texture.MAP_MANAGER_BUTTON,
                (b) -> toggleRoutes(),
                List.of(
                        Component.literal("[>] ")
                                .withStyle(ChatFormatting.LIGHT_PURPLE)
                                .append(Component.translatable("screens.wynntils.seaskipperMapGui.showRoutes.name")),
                        Component.translatable("screens.wynntils.seaskipperMapGui.showRoutes.description")
                                .withStyle(ChatFormatting.GRAY))));

        this.addRenderableWidget(new BasicTexturedButton(
                width / 2 - Texture.MAP_BUTTONS_BACKGROUND.width() / 2 + 6 + 20 * 2,
                (int) (this.renderHeight
                        - this.renderedBorderYOffset
                        - Texture.MAP_BUTTONS_BACKGROUND.height() / 2
                        - 6),
                16,
                16,
                Texture.MAP_ADD_BUTTON,
                (b) -> toggleDestinations(),
                List.of(
                        Component.literal("[>] ")
                                .withStyle(ChatFormatting.BLUE)
                                .append(Component.translatable(
                                        "screens.wynntils.seaskipperMapGui.showInaccessibleLocations.name")),
                        Component.translatable(
                                        "screens.wynntils.seaskipperMapGui.showInaccessibleLocations.description")
                                .withStyle(ChatFormatting.GRAY))));

        this.addRenderableWidget(new BasicTexturedButton(
                width / 2 - Texture.MAP_BUTTONS_BACKGROUND.width() / 2 + 6 + 20,
                (int) (this.renderHeight
                        - this.renderedBorderYOffset
                        - Texture.MAP_BUTTONS_BACKGROUND.height() / 2
                        - 6),
                16,
                16,
                Texture.MAP_OVERLAY_BUTTON,
                (b) -> toggleBorders(),
                List.of(
                        Component.literal("[>] ")
                                .withStyle(ChatFormatting.BLUE)
                                .append(Component.translatable("screens.wynntils.seaskipperMapGui.showBorders.name")),
                        Component.translatable("screens.wynntils.seaskipperMapGui.showBorders.description")
                                .withStyle(ChatFormatting.GRAY))));

        this.addRenderableWidget(new BasicTexturedButton(
                width / 2 - Texture.MAP_BUTTONS_BACKGROUND.width() / 2 + 6,
                (int) (this.renderHeight
                        - this.renderedBorderYOffset
                        - Texture.MAP_BUTTONS_BACKGROUND.height() / 2
                        - 6),
                16,
                16,
                Texture.MAP_BOAT_BUTTON,
                (b) -> Models.Seaskipper.purchaseBoat(),
                List.of(
                        Component.literal("[>] ")
                                .withStyle(ChatFormatting.LIGHT_PURPLE)
                                .append(Component.translatable("screens.wynntils.seaskipperMapGui.buyBoat.name")),
                        Component.translatable("screens.wynntils.seaskipperMapGui.buyBoat.description")
                                .withStyle(ChatFormatting.GRAY))));

        updateMapCenter(MAP_CENTER_X, MAP_CENTER_Y);
        setZoom(0.1f);
    }

    @Override
    public void doRender(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        updateMapCenterIfDragging(mouseX, mouseY);

        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        RenderSystem.enableDepthTest();

        renderMap(
                poseStack,
                Managers.Feature.getFeatureInstance(GuildMapFeature.class)
                        .renderUsingLinear
                        .get());

        RenderUtils.enableScissor(
                (int) (renderX + renderedBorderXOffset), (int) (renderY + renderedBorderYOffset), (int) mapWidth, (int)
                        mapHeight);

        renderPois(poseStack, mouseX, mouseY);

        renderCursor(
                poseStack,
                1.5f,
                Managers.Feature.getFeatureInstance(GuildMapFeature.class)
                        .pointerColor
                        .get(),
                Managers.Feature.getFeatureInstance(GuildMapFeature.class)
                        .pointerType
                        .get());

        RenderUtils.disableScissor();

        renderBackground(poseStack);

        renderCoordinates(poseStack, mouseX, mouseY);

        renderMapButtons(poseStack, mouseX, mouseY, partialTick);

        renderHoveredSeaskipperDestination(poseStack);
    }

    private void renderPois(PoseStack poseStack, int mouseX, int mouseY) {
        if (Models.Seaskipper.getSeaskipperPois().isEmpty()) return;
        if (Models.Seaskipper.getAvailableDestinations().isEmpty()) return;

        if (renderAllDestinations) {
            renderDestinations(
                    Models.Seaskipper.getSeaskipperPois(),
                    poseStack,
                    BoundingBox.centered(mapCenterX, mapCenterZ, width / currentZoom, height / currentZoom),
                    1,
                    mouseX,
                    mouseY);
        } else {
            renderDestinations(
                    Models.Seaskipper.getAvailableDestinations(),
                    poseStack,
                    BoundingBox.centered(mapCenterX, mapCenterZ, width / currentZoom, height / currentZoom),
                    1,
                    mouseX,
                    mouseY);
        }
    }

    private void renderDestinations(
            List<SeaskipperPoi> pois,
            PoseStack poseStack,
            BoundingBox textureBoundingBox,
            float poiScale,
            int mouseX,
            int mouseY) {
        hoveredPoi = null;

        List<SeaskipperPoi> filteredPois = getRenderedDestinations(pois, textureBoundingBox, poiScale, mouseX, mouseY);

        if (renderRoutes) {
            float poiRenderX =
                    MapRenderer.getRenderX(Models.Seaskipper.getCurrentPoi(), mapCenterX, centerX, currentZoom);
            float poiRenderZ =
                    MapRenderer.getRenderZ(Models.Seaskipper.getCurrentPoi(), mapCenterZ, centerZ, currentZoom);

            for (SeaskipperPoi poi : Models.Seaskipper.getAvailableDestinations()) {
                float x = MapRenderer.getRenderX(poi, mapCenterX, centerX, currentZoom);
                float z = MapRenderer.getRenderZ(poi, mapCenterZ, centerZ, currentZoom);

                RenderUtils.drawLine(
                        poseStack, CommonColors.DARK_GRAY.withAlpha(0.5f), poiRenderX, poiRenderZ, x, z, 0, 1);
            }
        }

        MultiBufferSource.BufferSource bufferSource =
                McUtils.mc().renderBuffers().bufferSource();

        for (int i = filteredPois.size() - 1; i >= 0; i--) {
            SeaskipperPoi poi = filteredPois.get(i);

            float poiRenderX = MapRenderer.getRenderX(poi, mapCenterX, centerX, currentZoom);
            float poiRenderZ = MapRenderer.getRenderZ(poi, mapCenterZ, centerZ, currentZoom);

            if (hideTerritoryBorders) {
                poi.renderAtWithoutBorders(poseStack, bufferSource, poiRenderX, poiRenderZ, currentZoom);
            } else {
                poi.renderAt(poseStack, bufferSource, poiRenderX, poiRenderZ, hoveredPoi == poi, poiScale, currentZoom);
            }
        }

        bufferSource.endBatch();
    }

    private List<SeaskipperPoi> getRenderedDestinations(
            List<SeaskipperPoi> pois, BoundingBox textureBoundingBox, float poiScale, int mouseX, int mouseY) {
        List<SeaskipperPoi> filteredPois = new ArrayList<>();

        for (int i = pois.size() - 1; i >= 0; i--) {
            SeaskipperPoi poi = pois.get(i);
            PoiLocation location = poi.getLocation();

            if (location == null) continue;

            float poiRenderX = MapRenderer.getRenderX(poi, mapCenterX, centerX, currentZoom);
            float poiRenderZ = MapRenderer.getRenderZ(poi, mapCenterZ, centerZ, currentZoom);

            float poiWidth = poi.getWidth(currentZoom, poiScale);
            float poiHeight = poi.getHeight(currentZoom, poiScale);

            BoundingBox filterBox = BoundingBox.centered(location.getX(), location.getZ(), poiWidth, poiHeight);
            BoundingBox mouseBox = BoundingBox.centered(poiRenderX, poiRenderZ, poiWidth, poiHeight);

            if (filterBox.intersects(textureBoundingBox)) {
                filteredPois.add(poi);
                if (hoveredPoi == null && mouseBox.contains(mouseX, mouseY)) {
                    hoveredPoi = poi;
                }
            }
        }

        if (hoveredPoi != null) {
            filteredPois.remove(hoveredPoi);
            filteredPois.add(0, hoveredPoi);
        }

        return filteredPois;
    }

    private void renderHoveredSeaskipperDestination(PoseStack poseStack) {
        if (hoveredPoi == null) return;

        poseStack.pushPose();
        poseStack.translate(width - SCREEN_SIDE_OFFSET - 250, SCREEN_SIDE_OFFSET + 40, 101);

        final float centerHeight = 40;
        final int textureWidth = Texture.TERRITORY_TOOLTIP_CENTER.width();

        RenderUtils.drawTexturedRect(poseStack, Texture.TERRITORY_TOOLTIP_TOP, 0, 0);
        RenderUtils.drawTexturedRect(
                poseStack,
                Texture.TERRITORY_TOOLTIP_CENTER.resource(),
                0,
                Texture.TERRITORY_TOOLTIP_TOP.height(),
                textureWidth,
                centerHeight,
                textureWidth,
                Texture.TERRITORY_TOOLTIP_CENTER.height());
        RenderUtils.drawTexturedRect(
                poseStack, Texture.TERRITORY_NAME_BOX, 0, Texture.TERRITORY_TOOLTIP_TOP.height() + centerHeight);

        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        CodedString.fromString("Level %d".formatted(hoveredPoi.getLevel())),
                        10,
                        10,
                        CommonColors.ORANGE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.TOP,
                        TextShadow.OUTLINE);

        float renderYOffset = 10;

        boolean isAccessible = false;
        boolean origin = false;
        int price = 0;

        for (SeaskipperDestinationItem destinationItem :
                Models.Seaskipper.getDestinations().keySet()) {
            String destination = destinationItem.getDestination();

            if (hoveredPoi.getName().equals(destination)) {
                isAccessible = true;
                price = destinationItem.getPrice();
                break;
            }

            if (hoveredPoi.getName().equals(Models.Seaskipper.getCurrentPoi().getName())) {
                origin = true;
            }
        }

        if (isAccessible) {
            renderYOffset += 10;

            FontRenderer.getInstance()
                    .renderText(
                            poseStack,
                            CodedString.fromString("Cost: %d²".formatted(price)),
                            10,
                            10 + renderYOffset,
                            CommonColors.GREEN,
                            HorizontalAlignment.LEFT,
                            VerticalAlignment.TOP,
                            TextShadow.OUTLINE);

            renderYOffset += 10;

            FontRenderer.getInstance()
                    .renderText(
                            poseStack,
                            CodedString.fromString("Click to go here!"),
                            10,
                            10 + renderYOffset,
                            CommonColors.BLUE,
                            HorizontalAlignment.LEFT,
                            VerticalAlignment.TOP,
                            TextShadow.OUTLINE);
        } else if (origin) {
            renderYOffset += 20;

            FontRenderer.getInstance()
                    .renderText(
                            poseStack,
                            CodedString.fromString("Origin"),
                            10,
                            10 + renderYOffset,
                            CommonColors.RED,
                            HorizontalAlignment.LEFT,
                            VerticalAlignment.TOP,
                            TextShadow.OUTLINE);
        } else {
            renderYOffset += 20;

            FontRenderer.getInstance()
                    .renderText(
                            poseStack,
                            CodedString.fromString("Inaccessible"),
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
                        CodedString.fromString(hoveredPoi.getName()),
                        7,
                        textureWidth,
                        Texture.TERRITORY_TOOLTIP_TOP.height() + centerHeight,
                        Texture.TERRITORY_TOOLTIP_TOP.height() + centerHeight + Texture.TERRITORY_NAME_BOX.height(),
                        0,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.MIDDLE,
                        TextShadow.OUTLINE);

        poseStack.popPose();
    }

    @Override
    public boolean doMouseClicked(double mouseX, double mouseY, int button) {
        for (GuiEventListener child : children()) {
            if (child.isMouseOver(mouseX, mouseY)) {
                child.mouseClicked(mouseX, mouseY, button);
                return true;
            }
        }

        for (SeaskipperPoi poi : Models.Seaskipper.getSeaskipperPois()) {
            if (poi.isSelected(mouseX, mouseY)) {
                Models.Seaskipper.purchasePass(poi.getName());

                return true;
            }
        }

        return super.doMouseClicked(mouseX, mouseY, button);
    }

    private void toggleBorders() {
        hideTerritoryBorders = !hideTerritoryBorders;
    }

    private void toggleDestinations() {
        renderAllDestinations = !renderAllDestinations;
    }

    private void toggleRoutes() {
        renderRoutes = !renderRoutes;
    }
}
