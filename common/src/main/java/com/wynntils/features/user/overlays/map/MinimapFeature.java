/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user.overlays.map;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.config.Config;
import com.wynntils.core.config.ConfigHolder;
import com.wynntils.core.features.UserFeature;
import com.wynntils.core.features.overlays.Overlay;
import com.wynntils.core.features.overlays.OverlayPosition;
import com.wynntils.core.features.overlays.annotations.OverlayInfo;
import com.wynntils.core.features.overlays.sizes.GuiScaledOverlaySize;
import com.wynntils.core.features.properties.FeatureCategory;
import com.wynntils.core.features.properties.FeatureInfo;
import com.wynntils.core.managers.Model;
import com.wynntils.gui.render.FontRenderer;
import com.wynntils.gui.render.HorizontalAlignment;
import com.wynntils.gui.render.MapRenderer;
import com.wynntils.gui.render.RenderUtils;
import com.wynntils.gui.render.TextRenderSetting;
import com.wynntils.gui.render.TextRenderTask;
import com.wynntils.gui.render.Texture;
import com.wynntils.gui.render.VerticalAlignment;
import com.wynntils.mc.event.RenderEvent;
import com.wynntils.mc.objects.CommonColors;
import com.wynntils.mc.objects.CustomColor;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.utils.MathUtils;
import com.wynntils.wynn.model.map.MapModel;
import com.wynntils.wynn.model.map.MapTexture;
import com.wynntils.wynn.utils.WynnUtils;
import java.util.List;
import java.util.Optional;

@FeatureInfo(category = FeatureCategory.MAP)
public class MinimapFeature extends UserFeature {
    @OverlayInfo(renderType = RenderEvent.ElementType.GUI, renderAt = OverlayInfo.RenderState.Pre)
    public final MinimapOverlay minimapOverlay = new MinimapOverlay();

    @Override
    public List<Class<? extends Model>> getModelDependencies() {
        return List.of(MapModel.class);
    }

    public static class MinimapOverlay extends Overlay {
        private static final int DEFAULT_SIZE = 150;

        @Config
        public float scale = 1f;

        @Config
        public float poiScale = 0.8f;

        @Config
        public float pointerScale = 1f;

        @Config
        public boolean followPlayerRotation = true;

        @Config
        public boolean renderUsingLinear = true;

        @Config
        public CustomColor pointerColor = new CustomColor(1f, 1f, 1f, 1f);

        @Config
        public MapMaskType maskType = MapMaskType.Rectangular;

        @Config
        public MapBorderType borderType = MapBorderType.Wynn;

        @Config
        public PointerType pointerType = PointerType.Arrow;

        @Config
        public CompassRenderType showCompass = CompassRenderType.All;

        @Config
        public boolean showCoords = true;

        public MinimapOverlay() {
            super(
                    new OverlayPosition(
                            5,
                            5,
                            VerticalAlignment.Top,
                            HorizontalAlignment.Left,
                            OverlayPosition.AnchorSection.TopLeft),
                    new GuiScaledOverlaySize(DEFAULT_SIZE, DEFAULT_SIZE));
        }

        @Override
        public void render(PoseStack poseStack, float partialTicks, Window window) {
            if (!WynnUtils.onWorld()) return;

            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

            float width = getWidth();
            float height = getHeight();
            float renderX = getRenderX();
            float renderY = getRenderY();

            float centerX = renderX + width / 2;
            float centerZ = renderY + height / 2;

            // enable mask
            switch (maskType) {
                case Rectangular -> RenderUtils.enableScissor((int) renderX, (int) renderY, (int) width, (int) height);
                case Circle -> RenderUtils.createMask(
                        poseStack, Texture.CIRCLE_MASK, (int) renderX, (int) renderY, (int) (renderX + width), (int)
                                (renderY + height));
            }

            // Always draw a black background to cover transparent map areas
            RenderUtils.drawRect(
                    poseStack,
                    CommonColors.BLACK,
                    renderX,
                    renderY,
                    0,
                    width,
                    height);

            Optional<MapTexture> mapOpt = MapModel.getMapForLocation(
                    (int) McUtils.player().getX(), (int) McUtils.player().getZ());
            if (mapOpt.isPresent()) {
                MapTexture map = mapOpt.get();
                float textureX = map.getTextureXPosition(McUtils.player().getX());
                float textureZ = map.getTextureZPosition(McUtils.player().getZ());
                MapRenderer.renderMapQuad(
                        map,
                        poseStack,
                        (float) McUtils.player().getX(),
                        (float) McUtils.player().getZ(),
                        centerX,
                        centerZ,
                        textureX,
                        textureZ,
                        width,
                        height,
                        this.scale,
                        this.poiScale,
                        null,
                        false,
                        this.followPlayerRotation,
                        this.renderUsingLinear);
            }

            // TODO minimap icons

            // TODO compass icon

            // cursor
            MapRenderer.renderCursor(
                    poseStack,
                    centerX,
                    centerZ,
                    this.pointerScale,
                    this.followPlayerRotation,
                    this.pointerColor,
                    this.pointerType);

            // disable mask & render border
            switch (maskType) {
                case Rectangular -> {
                    RenderSystem.disableScissor();
                }
                case Circle -> {
                    RenderUtils.clearMask();
                }
            }

            // render border
            renderMapBorder(poseStack, renderX, renderY, width, height);

            // Directional Text
            renderCardinalDirections(poseStack, width, height, centerX, centerZ);

            // Coordinates
            if (showCoords) {
                String coords = String.format(
                        "%s, %s, %s",
                        (int) McUtils.player().getX(), (int) McUtils.player().getY(), (int)
                                McUtils.player().getZ());

                FontRenderer.getInstance()
                        .renderText(
                                poseStack,
                                centerX,
                                renderY + height + 10 * height / DEFAULT_SIZE,
                                new TextRenderTask(
                                        coords,
                                        TextRenderSetting.CENTERED.withTextShadow(FontRenderer.TextShadow.OUTLINE)));
            }
        }

        private void renderCardinalDirections(
                PoseStack poseStack, float width, float height, float centerX, float centerZ) {
            if (showCompass == CompassRenderType.None) return;

            float northDX;
            float northDY;

            if (followPlayerRotation) {
                float yawRadians = (float) Math.toRadians(McUtils.player().getYRot());
                northDX = (float) StrictMath.sin(yawRadians);
                northDY = (float) StrictMath.cos(yawRadians);
                if (maskType == MapMaskType.Rectangular) {
                    // Scale as necessary
                    double toSquareScaleNorth = Math.min(width / Math.abs(northDX), height / Math.abs(northDY)) / 2;
                    northDX *= toSquareScaleNorth;
                    northDY *= toSquareScaleNorth;
                } else if (maskType == MapMaskType.Circle) {
                    double toSquareScaleNorth = width / (MathUtils.magnitude(northDX, northDY * width / height)) / 2;
                    northDX *= toSquareScaleNorth;
                    northDY *= toSquareScaleNorth;
                }
            } else {
                northDX = 0;
                northDY = -height / 2;
            }

            FontRenderer.getInstance()
                    .renderText(
                            poseStack,
                            centerX + northDX,
                            centerZ + northDY,
                            new TextRenderTask("N", TextRenderSetting.CENTERED));

            if (showCompass == CompassRenderType.North) return;

            // we can't do manipulations from north to east as it might not be square
            float eastDX;
            float eastDY;

            if (followPlayerRotation) {
                eastDX = -northDY;
                eastDY = northDX;

                if (maskType == MapMaskType.Rectangular) {
                    // Scale as necessary
                    double toSquareScaleEast = Math.min(width / Math.abs(northDY), height / Math.abs(northDX)) / 2;
                    eastDX *= toSquareScaleEast;
                    eastDY *= toSquareScaleEast;
                } else if (maskType == MapMaskType.Circle) {
                    double toSquareScaleEast = width / (MathUtils.magnitude(eastDX, eastDY * width / height)) / 2;
                    eastDX *= toSquareScaleEast;
                    eastDY *= toSquareScaleEast;
                }
            } else {
                eastDX = width / 2;
                eastDY = 0;
            }

            FontRenderer.getInstance()
                    .renderText(
                            poseStack,
                            centerX + eastDX,
                            centerZ + eastDY,
                            new TextRenderTask("E", TextRenderSetting.CENTERED));
            FontRenderer.getInstance()
                    .renderText(
                            poseStack,
                            centerX - northDX,
                            centerZ - northDY,
                            new TextRenderTask("S", TextRenderSetting.CENTERED));
            FontRenderer.getInstance()
                    .renderText(
                            poseStack,
                            centerX - eastDX,
                            centerZ - eastDY,
                            new TextRenderTask("W", TextRenderSetting.CENTERED));
        }

        private void renderMapBorder(PoseStack poseStack, float renderX, float renderY, float width, float height) {
            Texture texture = borderType.texture();
            int grooves = borderType.groovesSize();
            MapBorderType.BorderInfo borderInfo =
                    maskType == MapMaskType.Circle ? borderType.circle() : borderType.square();
            int tx1 = borderInfo.tx1();
            int ty1 = borderInfo.ty1();
            int tx2 = borderInfo.tx2();
            int ty2 = borderInfo.ty2();

            // Scale to stay the same.
            float groovesWidth = grooves * width / DEFAULT_SIZE;
            float groovesHeight = grooves * height / DEFAULT_SIZE;

            RenderUtils.drawTexturedRect(
                    poseStack,
                    texture.resource(),
                    renderX - groovesWidth,
                    renderY - groovesHeight,
                    0,
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
        protected void onConfigUpdate(ConfigHolder configHolder) {}
    }

    public enum CompassRenderType {
        None,
        North,
        All
    }

    public enum MapMaskType {
        Rectangular,
        Circle
    }
}
