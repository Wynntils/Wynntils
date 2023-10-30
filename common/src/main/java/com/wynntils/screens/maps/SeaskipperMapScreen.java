/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.maps;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.features.ui.CustomSeaskipperScreenFeature;
import com.wynntils.screens.base.widgets.BasicTexturedButton;
import com.wynntils.services.map.pois.SeaskipperDestinationPoi;
import com.wynntils.utils.colors.CommonColors;
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
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;

public final class SeaskipperMapScreen extends AbstractMapScreen {
    private static final int MAP_CENTER_X = -240;
    private static final int MAP_CENTER_Y = -3130;

    private boolean hideTerritoryBorders = false;
    private boolean renderAllDestinations = false;
    private boolean renderRoutes = false;

    private List<SeaskipperDestinationPoi> destinationPois = new ArrayList<>();
    private SeaskipperDestinationPoi currentLocationPoi = null;

    private SeaskipperDestinationPoi hoveredPoi;

    private boolean firstInit = true;

    private SeaskipperMapScreen() {}

    public static Screen create() {
        return new SeaskipperMapScreen();
    }

    @Override
    public void onClose() {
        McUtils.player().closeContainer();
        super.onClose();
    }

    @Override
    protected void doInit() {
        super.doInit();

        reloadDestinationPois();

        this.addRenderableWidget(new BasicTexturedButton(
                width / 2 - Texture.MAP_BUTTONS_BACKGROUND.width() / 2 + 6 + 20 * 3,
                (int) (this.renderHeight
                        - this.renderedBorderYOffset
                        - Texture.MAP_BUTTONS_BACKGROUND.height() / 2
                        - 6),
                16,
                16,
                Texture.WAYPOINT_MANAGER_ICON,
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
                Texture.ADD_ICON,
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
                Texture.OVERLAY_EXTRA_ICON,
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
                Texture.BOAT_ICON,
                (b) -> Models.Seaskipper.purchaseBoat(),
                List.of(
                        Component.literal("[>] ")
                                .withStyle(ChatFormatting.LIGHT_PURPLE)
                                .append(Component.translatable("screens.wynntils.seaskipperMapGui.buyBoat.name")),
                        Component.translatable("screens.wynntils.seaskipperMapGui.buyBoat.description")
                                .withStyle(ChatFormatting.GRAY))));

        if (firstInit) {
            updateMapCenter(MAP_CENTER_X, MAP_CENTER_Y);
            setZoom(0.1f);
            firstInit = false;
        }
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

        renderPois(poseStack, mouseX, mouseY);

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

        renderHoveredSeaskipperDestination(poseStack);

        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    private void renderPois(PoseStack poseStack, int mouseX, int mouseY) {
        renderDestinations(
                destinationPois,
                poseStack,
                BoundingBox.centered(mapCenterX, mapCenterZ, width / currentZoom, height / currentZoom),
                1,
                mouseX,
                mouseY);
    }

    private void renderDestinations(
            List<SeaskipperDestinationPoi> pois,
            PoseStack poseStack,
            BoundingBox textureBoundingBox,
            float poiScale,
            int mouseX,
            int mouseY) {
        hoveredPoi = null;

        List<SeaskipperDestinationPoi> filteredPois =
                getRenderedDestinations(pois, textureBoundingBox, poiScale, mouseX, mouseY);

        if (renderRoutes) {
            float poiRenderX = MapRenderer.getRenderX(currentLocationPoi, mapCenterX, centerX, currentZoom);
            float poiRenderZ = MapRenderer.getRenderZ(currentLocationPoi, mapCenterZ, centerZ, currentZoom);

            for (SeaskipperDestinationPoi poi : destinationPois.stream()
                    .filter(SeaskipperDestinationPoi::isAvailable)
                    .toList()) {
                float x = MapRenderer.getRenderX(poi, mapCenterX, centerX, currentZoom);
                float z = MapRenderer.getRenderZ(poi, mapCenterZ, centerZ, currentZoom);

                RenderUtils.drawLine(
                        poseStack, CommonColors.DARK_GRAY.withAlpha(0.5f), poiRenderX, poiRenderZ, x, z, 0, 1);
            }
        }

        MultiBufferSource.BufferSource bufferSource =
                McUtils.mc().renderBuffers().bufferSource();

        for (int i = filteredPois.size() - 1; i >= 0; i--) {
            SeaskipperDestinationPoi poi = filteredPois.get(i);

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

            float poiRenderX = MapRenderer.getRenderX(poi, mapCenterX, centerX, currentZoom);
            float poiRenderZ = MapRenderer.getRenderZ(poi, mapCenterZ, centerZ, currentZoom);

            float poiWidth = poi.getWidth(currentZoom, poiScale);
            float poiHeight = poi.getHeight(currentZoom, poiScale);

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
            filteredPois.add(0, hoveredPoi);
        }

        return filteredPois;
    }

    private void renderHoveredSeaskipperDestination(PoseStack poseStack) {
        if (hoveredPoi == null) return;

        poseStack.pushPose();
        poseStack.translate(width - SCREEN_SIDE_OFFSET - 250, SCREEN_SIDE_OFFSET + 40, 101);

        boolean isAccessible = hoveredPoi.isAvailable();

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
                        StyledText.fromString("Level %d".formatted(hoveredPoi.getLevel())),
                        10,
                        10,
                        CommonColors.ORANGE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.TOP,
                        TextShadow.OUTLINE);

        float renderYOffset = 10;

        boolean origin = hoveredPoi == currentLocationPoi;

        if (isAccessible) {
            int price = hoveredPoi.getDestination().item().getPrice();

            renderYOffset += 10;

            FontRenderer.getInstance()
                    .renderText(
                            poseStack,
                            StyledText.fromString("Cost: %d²".formatted(price)),
                            10,
                            10 + renderYOffset,
                            CommonColors.GREEN,
                            HorizontalAlignment.LEFT,
                            VerticalAlignment.TOP,
                            TextShadow.OUTLINE);

            renderYOffset += 20;

            FontRenderer.getInstance()
                    .renderText(
                            poseStack,
                            StyledText.fromString("Click to go here!"),
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
                            StyledText.fromString("Origin"),
                            10,
                            10 + renderYOffset,
                            CommonColors.RED,
                            HorizontalAlignment.LEFT,
                            VerticalAlignment.TOP,
                            TextShadow.OUTLINE);
        } else {
            renderYOffset += 10;

            FontRenderer.getInstance()
                    .renderText(
                            poseStack,
                            StyledText.fromString("Inaccessible"),
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
                        StyledText.fromString(hoveredPoi.getName()),
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

    @Override
    public boolean doMouseClicked(double mouseX, double mouseY, int button) {
        for (GuiEventListener child : children()) {
            if (child.isMouseOver(mouseX, mouseY)) {
                child.mouseClicked(mouseX, mouseY, button);
                return true;
            }
        }

        for (SeaskipperDestinationPoi poi : destinationPois) {
            if (poi.isSelected(mouseX, mouseY)) {
                if (poi.isAvailable()) {
                    Models.Seaskipper.purchasePass(poi.getDestination());
                }

                return true;
            }
        }

        return super.doMouseClicked(mouseX, mouseY, button);
    }

    public void reloadDestinationPois() {
        destinationPois = new ArrayList<>();

        destinationPois.addAll(Models.Seaskipper.getPois(renderAllDestinations));

        currentLocationPoi = destinationPois.stream()
                .filter(SeaskipperDestinationPoi::isPlayerInside)
                .findFirst()
                .orElse(null);
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
