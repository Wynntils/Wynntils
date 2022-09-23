/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.maps;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.webapi.WebManager;
import com.wynntils.core.webapi.profiles.MapProfile;
import com.wynntils.features.user.overlays.map.MapFeature;
import com.wynntils.features.user.overlays.map.PointerType;
import com.wynntils.mc.objects.CommonColors;
import com.wynntils.mc.objects.Location;
import com.wynntils.mc.render.RenderUtils;
import com.wynntils.mc.render.Texture;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.utils.KeyboardUtils;
import com.wynntils.utils.MathUtils;
import com.wynntils.wynn.model.CompassModel;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.sounds.SoundEvents;
import org.lwjgl.glfw.GLFW;

public class MainMapScreen extends Screen {
    private static final float SCREEN_SIDE_OFFSET = 10;
    private static final float BORDER_OFFSET = 7;

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

    // Zoom is the scaling of the map. The bigger the zoom, the less detailed the map becomes.
    private static final float MIN_ZOOM = 5.0f;
    private static final float MAX_ZOOM = 0.3f;
    private static final float ZOOM_FACTOR = 0.4f;
    private float currentZoom = 1f;

    private boolean dragging = false;
    private double lastMouseX = 0;
    private double lastMouseY = 0;

    public MainMapScreen() {
        super(new TextComponent("Main Map"));
        centerMapAroundPlayer();
    }

    @Override
    protected void init() {
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
                KeyboardUtils.isKeyDown(McUtils.mc().options.keyUp.key.getValue()));
        KeyMapping.set(
                McUtils.mc().options.keyRight.key,
                KeyboardUtils.isKeyDown(McUtils.mc().options.keyRight.key.getValue()));
        KeyMapping.set(
                McUtils.mc().options.keyJump.key,
                KeyboardUtils.isKeyDown(McUtils.mc().options.keyUp.key.getValue()));
        KeyMapping.set(
                McUtils.mc().options.keyShift.key,
                KeyboardUtils.isKeyDown(McUtils.mc().options.keyShift.key.getValue()));

        McUtils.mc().keyboardHandler.setSendRepeatsToGui(true);

        renderWidth = this.width - (SCREEN_SIDE_OFFSET) * 2f;
        renderHeight = this.height - (SCREEN_SIDE_OFFSET) * 2f;
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

        renderBackground(poseStack, renderWidth, renderHeight, renderX, renderY);

        if (!WebManager.isMapLoaded()) return;

        MapProfile map = WebManager.getMaps().get(0);
        float textureX = map.getTextureXPosition(mapCenterX);
        float textureZ = map.getTextureZPosition(mapCenterZ);

        renderMap(poseStack, map, textureX, textureZ);

        renderCursor(poseStack);
    }

    private void renderCursor(PoseStack poseStack) {
        double pX = McUtils.player().getX();
        double pZ = McUtils.player().getZ();

        double distanceX = pX - mapCenterX;
        double distanceZ = pZ - mapCenterZ;

        float cursorX = (float) (centerX + distanceX / currentZoom);
        float cursorZ = (float) (centerZ + distanceZ / currentZoom);
        RenderUtils.MapRenderer.renderCursor(
                poseStack, cursorX, cursorZ, 1.5f, false, CommonColors.WHITE, PointerType.Arrow);
    }

    private void renderMap(PoseStack poseStack, MapProfile map, float textureX, float textureZ) {
        RenderUtils.drawRect(
                poseStack,
                CommonColors.BLACK,
                renderX + renderedBorderXOffset,
                renderY + renderedBorderYOffset,
                0,
                mapWidth,
                mapHeight);
        RenderUtils.MapRenderer.renderMapQuad(
                map, poseStack, centerX, centerZ, textureX, textureZ, mapWidth, mapHeight, currentZoom, false, false);
    }

    private void updateMapCenterIfDragging(int mouseX, int mouseY) {
        if (dragging) {
            float zoomScale = currentZoom;
            updateMapCenter((float) (mapCenterX + (lastMouseX - mouseX) * zoomScale), (float)
                    (mapCenterZ + (lastMouseY - mouseY) * zoomScale));
        }
        lastMouseX = mouseX;
        lastMouseY = mouseY;
    }

    private static void renderBackground(PoseStack poseStack, float width, float height, float renderX, float renderY) {
        RenderUtils.drawScalingTexturedRect(
                poseStack,
                Texture.FULLSCREEN_MAP_BORDER.resource(),
                renderX,
                renderY,
                100,
                width,
                height,
                Texture.FULLSCREEN_MAP_BORDER.width(),
                Texture.FULLSCREEN_MAP_BORDER.height());
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        setZoom((float) (currentZoom - delta * ZOOM_FACTOR));

        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            centerMapAroundPlayer();
        } else if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            dragging = true;
            lastMouseX = mouseX;
            lastMouseY = mouseY;
        } else if (button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
            setCompassToMouseCoords(mouseX, mouseY);
        }

        return true;
    }

    private void setCompassToMouseCoords(double mouseX, double mouseY) {
        double gameX = (mouseX - centerX) * currentZoom + mapCenterX;
        double gameZ = (mouseY - centerZ) * currentZoom + mapCenterZ;
        Location compassLocation = new Location(gameX, 0, gameZ);
        CompassModel.setCompassLocation(compassLocation);

        McUtils.soundManager().play(SimpleSoundInstance.forUI(SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0F));
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
        this.currentZoom = MathUtils.clamp(zoomTargetDelta, MAX_ZOOM, MIN_ZOOM);
    }

    private void updateMapCenter(float newX, float newZ) {
        this.mapCenterX = newX;
        this.mapCenterZ = newZ;
    }

    public void setHoldingMapKey(boolean holdingMapKey) {
        this.holdingMapKey = holdingMapKey;
    }
}
