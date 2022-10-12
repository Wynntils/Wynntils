/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.gui.screens.maps;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.features.user.overlays.map.MapFeature;
import com.wynntils.gui.render.FontRenderer;
import com.wynntils.gui.render.HorizontalAlignment;
import com.wynntils.gui.render.MapRenderer;
import com.wynntils.gui.render.RenderUtils;
import com.wynntils.gui.render.Texture;
import com.wynntils.gui.render.VerticalAlignment;
import com.wynntils.mc.objects.CommonColors;
import com.wynntils.mc.objects.Location;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.utils.BoundingBox;
import com.wynntils.utils.KeyboardUtils;
import com.wynntils.utils.MathUtils;
import com.wynntils.wynn.model.CompassModel;
import com.wynntils.wynn.model.map.MapModel;
import com.wynntils.wynn.model.map.MapTexture;
import com.wynntils.wynn.model.map.poi.Poi;
import com.wynntils.wynn.model.map.poi.WaypointPoi;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.sounds.SoundEvents;
import org.lwjgl.glfw.GLFW;

public class MainMapScreen extends Screen {
    private static final float SCREEN_SIDE_OFFSET = 10;
    private static final float BORDER_OFFSET = 6;

    private boolean holdingMapKey = false;

    private float renderWidth;
    private float renderHeight;
    private float renderX;
    private float renderY;

    private float renderedBorderXOffset;
    private float renderedBorderYOffset;

    private float mapWidth;
    private float mapHeight;
    private float centerX;
    private float centerZ;

    private float mapCenterX;
    private float mapCenterZ;

    // Zoom is the scaling of the map. The bigger the zoom, the more detailed the map becomes.
    private static final float MIN_ZOOM = 0.2f;
    private static final float MAX_ZOOM = 3f;
    private static final float MOUSE_SCROLL_ZOOM_FACTOR = 0.04f;
    private float currentZoom = 1f;

    private boolean dragging = false;
    private double lastMouseX = 0;
    private double lastMouseY = 0;

    private Poi hovered = null;

    public MainMapScreen() {
        super(new TextComponent("Main Map"));
        centerMapAroundPlayer();
    }

    public MainMapScreen(float mapCenterX, float mapCenterZ) {
        super(new TextComponent("Main Map"));
        updateMapCenter(mapCenterX, mapCenterZ);
    }

    @Override
    protected void init() {
        McUtils.mc().keyboardHandler.setSendRepeatsToGui(true);

        // FIXME: Figure out a way to not need this.
        //        At the moment, this is needed for Minecraft not to forget we hold keys when we open the GUI...
        KeyMapping.set(
                McUtils.mc().options.keyUp.key,
                KeyboardUtils.isKeyDown(McUtils.mc().options.keyUp.key.getValue()));
        KeyMapping.set(
                McUtils.mc().options.keyDown.key,
                KeyboardUtils.isKeyDown(McUtils.mc().options.keyDown.key.getValue()));
        KeyMapping.set(
                McUtils.mc().options.keyLeft.key,
                KeyboardUtils.isKeyDown(McUtils.mc().options.keyLeft.key.getValue()));
        KeyMapping.set(
                McUtils.mc().options.keyRight.key,
                KeyboardUtils.isKeyDown(McUtils.mc().options.keyRight.key.getValue()));
        KeyMapping.set(
                McUtils.mc().options.keyJump.key,
                KeyboardUtils.isKeyDown(McUtils.mc().options.keyJump.key.getValue()));
        KeyMapping.set(
                McUtils.mc().options.keyShift.key,
                KeyboardUtils.isKeyDown(McUtils.mc().options.keyShift.key.getValue()));

        renderWidth = this.width - SCREEN_SIDE_OFFSET * 2f;
        renderHeight = this.height - SCREEN_SIDE_OFFSET * 2f;
        renderX = SCREEN_SIDE_OFFSET;
        renderY = SCREEN_SIDE_OFFSET;

        float borderScaleX = (float) this.width / Texture.FULLSCREEN_MAP_BORDER.width();
        float borderScaleY = (float) this.height / Texture.FULLSCREEN_MAP_BORDER.height();

        renderedBorderXOffset = BORDER_OFFSET * borderScaleX;
        renderedBorderYOffset = BORDER_OFFSET * borderScaleY;

        mapWidth = renderWidth - renderedBorderXOffset * 2f;
        mapHeight = renderHeight - renderedBorderYOffset * 2f;
        centerX = renderX + renderedBorderXOffset + mapWidth / 2f;
        centerZ = renderY + renderedBorderYOffset + mapHeight / 2f;
    }

    @Override
    public void onClose() {
        McUtils.mc().keyboardHandler.setSendRepeatsToGui(false);
        super.onClose();
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        if (holdingMapKey && !MapFeature.INSTANCE.openMapKeybind.getKeyMapping().isDown()) {
            this.onClose();
            return;
        }

        updateMapCenterIfDragging(mouseX, mouseY);

        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        RenderSystem.enableDepthTest();

        renderMap(poseStack, mouseX, mouseY);
        renderBackground(poseStack);

        renderCursor(poseStack);

        renderCoordinates(poseStack, mouseX, mouseY);
    }

    private void renderCoordinates(PoseStack poseStack, int mouseX, int mouseY) {
        int gameX = (int) ((mouseX - centerX) / currentZoom + mapCenterX);
        int gameZ = (int) ((mouseY - centerZ) / currentZoom + mapCenterZ);

        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        gameX + ", " + gameZ,
                        this.centerX,
                        this.renderHeight - this.renderedBorderYOffset - 10,
                        CommonColors.WHITE,
                        HorizontalAlignment.Center,
                        VerticalAlignment.Top,
                        FontRenderer.TextShadow.OUTLINE);
    }

    private void renderCursor(PoseStack poseStack) {
        double pX = McUtils.player().getX();
        double pZ = McUtils.player().getZ();

        double distanceX = pX - mapCenterX;
        double distanceZ = pZ - mapCenterZ;

        float cursorX = (float) (centerX + distanceX * currentZoom);
        float cursorZ = (float) (centerZ + distanceZ * currentZoom);
        MapRenderer.renderCursor(
                poseStack,
                cursorX,
                cursorZ,
                MapFeature.INSTANCE.playerPointerScale,
                MapFeature.INSTANCE.pointerColor,
                MapFeature.INSTANCE.pointerType);
    }

    private void renderMap(PoseStack poseStack, int mouseX, int mouseY) {
        RenderUtils.enableScissor(
                (int) (renderX + renderedBorderXOffset), (int) (renderY + renderedBorderYOffset), (int) mapWidth, (int)
                        mapHeight);

        // Background black void color
        RenderUtils.drawRect(
                poseStack,
                CommonColors.BLACK,
                renderX + renderedBorderXOffset,
                renderY + renderedBorderYOffset,
                0,
                mapWidth,
                mapHeight);

        BoundingBox textureBoundingBox =
                BoundingBox.centered(mapCenterX, mapCenterZ, width / currentZoom, height / currentZoom);

        List<MapTexture> maps = MapModel.getMapsForBoundingBox(textureBoundingBox);
        for (MapTexture map : maps) {
            float textureX = map.getTextureXPosition(mapCenterX);
            float textureZ = map.getTextureZPosition(mapCenterZ);

            MapRenderer.renderMapQuad(
                    map,
                    poseStack,
                    centerX,
                    centerZ,
                    textureX,
                    textureZ,
                    mapWidth,
                    mapHeight,
                    1f / currentZoom,
                    MapFeature.INSTANCE.renderUsingLinear);
        }

        hovered = null;

        List<Poi> pois = MapModel.getAllPois()
                .sorted(Comparator.comparing(poi -> poi.getLocation().getY()))
                .toList();

        List<Poi> filteredPois = new ArrayList<>();

        // Filter and find hovered
        for (int i = pois.size() - 1; i >= 0; i--) {
            Poi poi = pois.get(i);

            float poiRenderX = MapRenderer.getRenderX(poi, mapCenterX, centerX, currentZoom);
            float poiRenderZ = MapRenderer.getRenderZ(poi, mapCenterZ, centerZ, currentZoom);

            float poiWidth = poi.getWidth() * MapFeature.INSTANCE.poiScale;
            float poiHeight = poi.getHeight() * MapFeature.INSTANCE.poiScale;

            BoundingBox filterBox = BoundingBox.centered(
                    poi.getLocation().getX(), poi.getLocation().getZ(), poiWidth, poiHeight);
            BoundingBox mouseBox = BoundingBox.centered(poiRenderX, poiRenderZ, poiWidth, poiHeight);

            if (filterBox.intersects(textureBoundingBox)) {
                filteredPois.add(poi);
                if (hovered == null && mouseBox.contains(mouseX, mouseY)) {
                    hovered = poi;
                }
            }
        }

        // Reverse and Render
        for (int i = filteredPois.size() - 1; i >= 0; i--) {
            Poi poi = filteredPois.get(i);

            float poiRenderX = MapRenderer.getRenderX(poi, mapCenterX, centerX, currentZoom);
            float poiRenderZ = MapRenderer.getRenderZ(poi, mapCenterZ, centerZ, currentZoom);

            poi.renderAt(poseStack, poiRenderX, poiRenderZ, hovered == poi, MapFeature.INSTANCE.poiScale);
        }

        RenderSystem.disableScissor();
    }

    private void updateMapCenterIfDragging(int mouseX, int mouseY) {
        if (dragging) {
            updateMapCenter((float) (mapCenterX + (lastMouseX - mouseX) / currentZoom), (float)
                    (mapCenterZ + (lastMouseY - mouseY) / currentZoom));
        }
        lastMouseX = mouseX;
        lastMouseY = mouseY;
    }

    @Override
    public void renderBackground(PoseStack poseStack) {
        RenderUtils.drawScalingTexturedRect(
                poseStack,
                Texture.FULLSCREEN_MAP_BORDER.resource(),
                renderX,
                renderY,
                100,
                renderWidth,
                renderHeight,
                Texture.FULLSCREEN_MAP_BORDER.width(),
                Texture.FULLSCREEN_MAP_BORDER.height());
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        double newZoom = currentZoom + delta * MOUSE_SCROLL_ZOOM_FACTOR * currentZoom;
        setZoom((float) newZoom);

        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            if (McUtils.mc().player.isShiftKeyDown()
                    && CompassModel.getCompassLocation().isPresent()) {
                Location location = CompassModel.getCompassLocation().get();
                updateMapCenter((float) location.x, (float) location.z);
                return true;
            }

            centerMapAroundPlayer();
        } else if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            if (hovered instanceof WaypointPoi) {
                CompassModel.reset();
                return true;
            }

            if (hovered != null) {
                McUtils.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP);
                CompassModel.setCompassLocation(new Location(hovered.getLocation()));
                return true;
            }

            dragging = true;
            lastMouseX = mouseX;
            lastMouseY = mouseY;
        } else if (button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
            setCompassToMouseCoords(mouseX, mouseY);
        }

        return true;
    }

    private void setCompassToMouseCoords(double mouseX, double mouseY) {
        double gameX = (mouseX - centerX) / currentZoom + mapCenterX;
        double gameZ = (mouseY - centerZ) / currentZoom + mapCenterZ;
        Location compassLocation = new Location(gameX, 0, gameZ);
        CompassModel.setCompassLocation(compassLocation);

        McUtils.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            dragging = false;
        }

        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            this.onClose();
            return true;
        }

        // Pass along key press to move
        InputConstants.Key key = InputConstants.getKey(keyCode, scanCode);
        KeyMapping.set(key, true);

        return false;
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        // Pass along key press to move
        InputConstants.Key key = InputConstants.getKey(keyCode, scanCode);
        KeyMapping.set(key, false);

        return false;
    }

    private void centerMapAroundPlayer() {
        updateMapCenter(
                (float) McUtils.player().getX(), (float) McUtils.player().getZ());
    }

    private void setZoom(float zoomTargetDelta) {
        this.currentZoom = MathUtils.clamp(zoomTargetDelta, MIN_ZOOM, MAX_ZOOM);
    }

    private void updateMapCenter(float newX, float newZ) {
        this.mapCenterX = newX;
        this.mapCenterZ = newZ;
    }

    public void setHoldingMapKey(boolean holdingMapKey) {
        this.holdingMapKey = holdingMapKey;
    }

    public void setHovered(Poi hovered) {
        this.hovered = hovered;
    }
}
