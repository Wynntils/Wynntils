/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.overlays.minimap;

import com.mojang.blaze3d.platform.Window;
import com.wynntils.core.components.Services;
import com.wynntils.core.consumers.overlays.Overlay;
import com.wynntils.core.consumers.overlays.OverlayPosition;
import com.wynntils.core.consumers.overlays.OverlaySize;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.services.map.MapTexture;
import com.wynntils.utils.MathUtils;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.MapRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.TextRenderSetting;
import com.wynntils.utils.render.TextRenderTask;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.PointerType;
import com.wynntils.utils.render.type.VerticalAlignment;
import com.wynntils.utils.type.BoundingBox;
import java.util.List;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;

public class MinimapOverlay extends Overlay {
    private static final int DEFAULT_SIZE = 130;

    @Persisted
    private final Config<Float> zoomLevel = new Config<>(MapRenderer.DEFAULT_ZOOM_LEVEL);

    @Persisted
    private final Config<Float> poiScale = new Config<>(0.6f);

    @Persisted
    private final Config<Float> pointerScale = new Config<>(0.8f);

    @Persisted
    private final Config<Boolean> followPlayerRotation = new Config<>(true);

    @Persisted
    public final Config<UnmappedOption> hideWhenUnmapped = new Config<>(UnmappedOption.MINIMAP);

    @Persisted
    private final Config<CustomColor> pointerColor = new Config<>(new CustomColor(1f, 1f, 1f, 1f));

    @Persisted
    private final Config<MapMaskType> maskType = new Config<>(MapMaskType.RECTANGULAR);

    @Persisted
    private final Config<MapBorderType> borderType = new Config<>(MapBorderType.WYNN);

    @Persisted
    private final Config<PointerType> pointerType = new Config<>(PointerType.ARROW);

    @Persisted
    private final Config<CompassRenderType> showCompass = new Config<>(CompassRenderType.ALL);

    @Persisted
    private final Config<Boolean> renderRemoteFriendPlayers = new Config<>(true);

    @Persisted
    private final Config<Boolean> renderRemotePartyPlayers = new Config<>(true);

    @Persisted
    private final Config<Boolean> renderRemoteGuildPlayers = new Config<>(true);

    @Persisted
    public final Config<Float> remotePlayersHeadScale = new Config<>(0.4f);

    public MinimapOverlay() {
        super(
                new OverlayPosition(
                        5.25f,
                        5,
                        VerticalAlignment.TOP,
                        HorizontalAlignment.LEFT,
                        OverlayPosition.AnchorSection.TOP_LEFT),
                new OverlaySize(DEFAULT_SIZE, DEFAULT_SIZE));
    }

    private void setZoomLevel(float level) {
        // Clamp zoom levels to allowed interval
        float clampedLevel = MathUtils.clamp(level, 1, MapRenderer.ZOOM_LEVELS);

        // If the level is the same, do nothing (avoid recursion loop)
        if (clampedLevel == zoomLevel.get()) return;

        zoomLevel.setValue(clampedLevel);
    }

    public void adjustZoomLevel(int delta) {
        setZoomLevel(zoomLevel.get() + delta);
    }

    @Override
    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker, Window window) {
        float width = getWidth();
        float height = getHeight();
        float renderX = getRenderX();
        float renderY = getRenderY();

        float centerX = renderX + width / 2;
        float centerZ = renderY + height / 2;

        double playerX = McUtils.player().getX();
        double playerZ = McUtils.player().getZ();

        final float zoomRenderScale = 1.0f / MapRenderer.getZoomRenderScaleFromLevel(zoomLevel.get());

        // avoid rotational overpass - This is a rather loose oversizing, if possible later
        // use trigonometry, etc. to find a better one
        float extraFactor = 1f;
        if (followPlayerRotation.get()) {
            // 1.5 > sqrt(2);
            extraFactor = 1.5F;

            if (width > height) {
                extraFactor *= width / height;
            } else {
                extraFactor *= height / width;
            }
        }

        float worldWidth = width * (1f / zoomRenderScale) * extraFactor;
        float worldHeight = height * (1f / zoomRenderScale) * extraFactor;

        BoundingBox visibleWorldBox = BoundingBox.centered((float) playerX, (float) playerZ, worldWidth, worldHeight);

        List<MapTexture> maps = Services.Map.getMapsForBoundingBox(visibleWorldBox);

        if (hideWhenUnmapped.get() != UnmappedOption.NEITHER && maps.isEmpty()) return;

        // FIXME: Reimplement circle mask
        // enable mask
        switch (maskType.get()) {
            case RECTANGULAR ->
                RenderUtils.enableScissor(guiGraphics, (int) renderX, (int) renderY, (int) width, (int) height);
        }

        // Always draw a black background to cover transparent map areas
        RenderUtils.drawRect(guiGraphics, CommonColors.BLACK, (int) renderX, (int) renderY, (int) width, (int) height);

        // enable rotation if necessary
        if (followPlayerRotation.get()) {
            guiGraphics.pose().pushMatrix();
            RenderUtils.rotatePose(
                    guiGraphics.pose(),
                    centerX,
                    centerZ,
                    180 - McUtils.mc().gameRenderer.getMainCamera().yRot());
        }

        for (MapTexture map : maps) {
            MapRenderer.renderMapTile(
                    guiGraphics,
                    map,
                    (float) playerX,
                    (float) playerZ,
                    centerX,
                    centerZ,
                    zoomRenderScale,
                    visibleWorldBox);
        }

        // disable rotation if necessary
        if (followPlayerRotation.get()) {
            guiGraphics.pose().popMatrix();
        }

        renderPois(
                guiGraphics,
                centerX,
                centerZ,
                width,
                height,
                playerX,
                playerZ,
                zoomRenderScale,
                zoomLevel.get(),
                visibleWorldBox);

        // cursor
        MapRenderer.renderCursor(
                guiGraphics,
                centerX,
                centerZ,
                this.pointerScale.get(),
                this.pointerColor.get(),
                this.pointerType.get(),
                followPlayerRotation.get());

        // FIXME: Reimplement circle mask
        // disable mask & render border
        switch (maskType.get()) {
            case RECTANGULAR -> RenderUtils.disableScissor(guiGraphics);
        }

        // render border
        renderMapBorder(guiGraphics, renderX, renderY, width, height);

        // Directional Text
        renderCardinalDirections(guiGraphics, width, height, centerX, centerZ);
    }

    private void renderPois(
            GuiGraphics guiGraphics,
            float centerX,
            float centerZ,
            float width,
            float height,
            double playerX,
            double playerZ,
            float zoomRenderScale,
            float zoomLevel,
            BoundingBox visibleWorldBox) {
        // FIXME: Re-implement POI rendering for mapdata system
        // The POI system was replaced by MapFeatureRenderer in the mapdata branch
    }

    // FIXME: Re-implement player minimap POIs for mapdata system

    private void renderCardinalDirections(
            GuiGraphics guiGraphics, float width, float height, float centerX, float centerZ) {
        if (showCompass.get() == CompassRenderType.NONE) return;

        float northDX;
        float northDY;

        if (followPlayerRotation.get()) {
            float yawRadians = (float)
                    Math.toRadians(McUtils.mc().gameRenderer.getMainCamera().yRot());
            northDX = (float) StrictMath.sin(yawRadians);
            northDY = (float) StrictMath.cos(yawRadians);

            double toBorderScaleNorth = 1;

            if (maskType.get() == MapMaskType.RECTANGULAR) {
                toBorderScaleNorth = Math.min(width / Math.abs(northDX), height / Math.abs(northDY)) / 2;
            }
            // FIXME: Reimplement circle mask
            //            } else if (maskType.get() == MapMaskType.CIRCLE) {
            //                toBorderScaleNorth = width / (MathUtils.magnitude(northDX, northDY * width / height)) / 2;
            //            }

            northDX *= toBorderScaleNorth;
            northDY *= toBorderScaleNorth;

        } else {
            northDX = 0;
            northDY = -height / 2;
        }

        FontRenderer.getInstance()
                .renderText(
                        guiGraphics,
                        centerX + northDX,
                        centerZ + northDY,
                        new TextRenderTask("N", TextRenderSetting.CENTERED));

        if (showCompass.get() == CompassRenderType.NORTH) return;

        // we can't do manipulations from north to east as it might not be square
        float eastDX;
        float eastDY;

        if (followPlayerRotation.get()) {
            eastDX = -northDY;
            eastDY = northDX;

            double toBorderScaleEast = 1f;

            if (maskType.get() == MapMaskType.RECTANGULAR) {
                toBorderScaleEast = Math.min(width / Math.abs(northDY), height / Math.abs(northDX)) / 2;
            }
            // FIXME: Reimplement circle mask
            //            } else if (maskType.get() == MapMaskType.CIRCLE) {
            //                toBorderScaleEast = width / (MathUtils.magnitude(eastDX, eastDY * width / height)) / 2;
            //            }

            eastDX *= toBorderScaleEast;
            eastDY *= toBorderScaleEast;
        } else {
            eastDX = width / 2;
            eastDY = 0;
        }

        FontRenderer.getInstance()
                .renderText(
                        guiGraphics,
                        centerX + eastDX,
                        centerZ + eastDY,
                        new TextRenderTask("E", TextRenderSetting.CENTERED));
        FontRenderer.getInstance()
                .renderText(
                        guiGraphics,
                        centerX - northDX,
                        centerZ - northDY,
                        new TextRenderTask("S", TextRenderSetting.CENTERED));
        FontRenderer.getInstance()
                .renderText(
                        guiGraphics,
                        centerX - eastDX,
                        centerZ - eastDY,
                        new TextRenderTask("W", TextRenderSetting.CENTERED));
    }

    private void renderMapBorder(GuiGraphics guiGraphics, float renderX, float renderY, float width, float height) {
        Texture texture = borderType.get().texture();
        int grooves = borderType.get().groovesSize();
        BorderInfo borderInfo = borderType.get().square();
        // FIXME: Reimplement circle mask
        //        BorderInfo borderInfo = maskType.get() == MapMaskType.CIRCLE
        //                ? borderType.get().circle()
        //                : borderType.get().square();
        int tx1 = borderInfo.tx1();
        int ty1 = borderInfo.ty1();
        int tx2 = borderInfo.tx2();
        int ty2 = borderInfo.ty2();

        // Scale to stay the same.
        float groovesWidth = grooves * width / DEFAULT_SIZE;
        float groovesHeight = grooves * height / DEFAULT_SIZE;

        RenderUtils.drawTexturedRect(
                guiGraphics,
                texture,
                renderX - groovesWidth,
                renderY - groovesHeight,
                width + 2 * groovesWidth,
                height + 2 * groovesHeight,
                tx1,
                ty1,
                tx2 - tx1,
                ty2 - ty1,
                texture.width(),
                texture.height());
    }

    @Override
    protected void onConfigUpdate(Config<?> config) {
        if (config == zoomLevel) {
            // Make sure it is a valid level
            setZoomLevel(zoomLevel.get());
        }
    }

    public enum UnmappedOption {
        MINIMAP,
        MINIMAP_AND_COORDS,
        NEITHER
    }

    private enum CompassRenderType {
        NONE,
        NORTH,
        ALL
    }

    private enum MapMaskType {
        RECTANGULAR,
        // FIXME: Reimplement circle mask
        //        CIRCLE
    }

    private enum MapBorderType {
        GILDED(Texture.GILDED_MAP_TEXTURES, new BorderInfo(0, 262, 262, 524), new BorderInfo(0, 0, 262, 262), 1),
        PAPER(Texture.PAPER_MAP_TEXTURES, new BorderInfo(0, 0, 217, 217), new BorderInfo(0, 217, 217, 438), 3),
        WYNN(Texture.WYNN_MAP_TEXTURES, new BorderInfo(0, 0, 112, 112), new BorderInfo(0, 112, 123, 235), 3);

        private final Texture texture;
        private final BorderInfo square;
        private final BorderInfo circle;
        private final int groovesSize;

        MapBorderType(Texture texture, BorderInfo square, BorderInfo circle, int groovesSize) {
            this.texture = texture;
            this.square = square;
            this.circle = circle;
            this.groovesSize = groovesSize;
        }

        private Texture texture() {
            return texture;
        }

        private int groovesSize() {
            return groovesSize;
        }

        private BorderInfo square() {
            return square;
        }

        private BorderInfo circle() {
            return circle;
        }
    }

    private record BorderInfo(int tx1, int ty1, int tx2, int ty2) {}
}
