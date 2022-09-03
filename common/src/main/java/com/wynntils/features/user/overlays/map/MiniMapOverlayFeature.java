/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user.overlays.map;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import com.wynntils.core.config.Config;
import com.wynntils.core.config.ConfigHolder;
import com.wynntils.core.features.UserFeature;
import com.wynntils.core.features.overlays.Overlay;
import com.wynntils.core.features.overlays.OverlayPosition;
import com.wynntils.core.features.overlays.annotations.OverlayInfo;
import com.wynntils.core.features.overlays.sizes.GuiScaledOverlaySize;
import com.wynntils.core.webapi.WebManager;
import com.wynntils.core.webapi.profiles.MapProfile;
import com.wynntils.mc.event.RenderEvent;
import com.wynntils.mc.objects.CustomColor;
import com.wynntils.mc.render.FontRenderer;
import com.wynntils.mc.render.HorizontalAlignment;
import com.wynntils.mc.render.RenderUtils;
import com.wynntils.mc.render.TextRenderSetting;
import com.wynntils.mc.render.TextRenderTask;
import com.wynntils.mc.render.Texture;
import com.wynntils.mc.render.VerticalAlignment;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.wynn.utils.WynnUtils;
import net.minecraft.client.renderer.GameRenderer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

public class MiniMapOverlayFeature extends UserFeature {
    @OverlayInfo(renderType = RenderEvent.ElementType.GUI, renderAt = OverlayInfo.RenderState.Pre)
    public final MiniMapOverlay miniMapOverlay = new MiniMapOverlay();

    public static class MiniMapOverlay extends Overlay {

        private static final int DEFAULT_SIZE = 150;

        @Config
        public float scale = 1f;

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

        public MiniMapOverlay() {
            super(
                    new OverlayPosition(
                            0,
                            0,
                            VerticalAlignment.Top,
                            HorizontalAlignment.Left,
                            OverlayPosition.AnchorSection.TopLeft),
                    new GuiScaledOverlaySize(DEFAULT_SIZE, DEFAULT_SIZE));
        }

        @Override
        public void render(PoseStack poseStack, float partialTicks, Window window) {
            if (!WynnUtils.onWorld()) return;

            // TODO replace with generalized maps whenever that is done
            MapProfile map = WebManager.getMaps().get(0);

            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

            float width = getWidth();
            float height = getHeight();
            float renderX = getRenderX();
            float renderY = getRenderY();

            float centerX = renderX + width / 2;
            float centerZ = renderY + height / 2;
            float textureX = map.getTextureXPosition(McUtils.player().getX());
            float textureZ = map.getTextureZPosition(McUtils.player().getZ());

            // enable mask
            switch (maskType) {
                case Rectangular -> RenderUtils.enableScissor((int) renderX, (int) renderY, (int) width, (int) height);
                    // case Circle -> {
                    // TODO
                    // }
            }

            if (WebManager.isMapLoaded()) {
                renderMapQuad(map, poseStack, centerX, centerZ, textureX, textureZ, width, height);
            }

            // TODO minimap icons

            // TODO compass icon

            // cursor
            renderCursor(poseStack, centerX, centerZ);

            // disable mask & render border
            switch (maskType) {
                case Rectangular -> {
                    RenderSystem.disableScissor();
                    renderRectangularMapBorder(poseStack, renderX, renderY, width, height);
                }
                    // case Circle -> {
                    // TODO
                    // renderCircularMapBorder();
                    // }
            }

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
                                renderY + height + 6,
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
                    double toSquareScale = Math.min(width / Math.abs(northDX), height / Math.abs(northDY)) / 2;
                    northDX *= toSquareScale;
                    northDY *= toSquareScale;
                }

            } else {
                northDX = width / 2;
                northDY = height / 2;
            }

            FontRenderer.getInstance()
                    .renderText(
                            poseStack,
                            centerX + northDX,
                            centerZ + northDY,
                            new TextRenderTask("N", TextRenderSetting.CENTERED));

            if (showCompass == CompassRenderType.All) {
                FontRenderer.getInstance()
                        .renderText(
                                poseStack,
                                centerX - northDY,
                                centerZ + northDX,
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
                                centerX + northDY,
                                centerZ - northDX,
                                new TextRenderTask("W", TextRenderSetting.CENTERED));
            }
        }

        private void renderCursor(PoseStack poseStack, float centerX, float centerZ) {
            if (!followPlayerRotation) {
                poseStack.pushPose();
                RenderUtils.rotatePose(
                        poseStack, centerX, centerZ, 180 + McUtils.player().getYRot());
            }

            RenderUtils.drawTexturedRectWithColor(
                    poseStack,
                    Texture.MAP_POINTERS.resource(),
                    pointerColor,
                    (int) (centerX - pointerType.width / 2),
                    (int) (centerZ - pointerType.height / 2),
                    0,
                    pointerType.width,
                    pointerType.height,
                    0,
                    pointerType.textureY,
                    pointerType.width,
                    pointerType.height,
                    Texture.MAP_POINTERS.width(),
                    Texture.MAP_POINTERS.height());

            if (!followPlayerRotation) {
                poseStack.popPose();
            }
        }

        private void renderRectangularMapBorder(
                PoseStack poseStack, float renderX, float renderY, float width, float height) {
            Texture texture = borderType.texture();
            int grooves = borderType.groovesSize();
            int tx1 = borderType.tx1();
            int ty1 = borderType.ty1();
            int tx2 = borderType.tx2();
            int ty2 = borderType.ty2();

            // Scale to stay the same.
            float groovesWidth = grooves * width / DEFAULT_SIZE;
            float groovesHeight = grooves * height / DEFAULT_SIZE;

            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, texture.resource());

            // TODO remove int casts with settings pr merge
            RenderUtils.drawTexturedRect(
                    poseStack,
                    texture.resource(),
                    (int) (renderX - groovesWidth),
                    (int) (renderY - groovesHeight),
                    0,
                    (int) (width + 2 * groovesWidth),
                    (int) (height + 2 * groovesHeight),
                    tx1,
                    ty1,
                    tx2 - tx1,
                    ty2 - ty1,
                    texture.width(),
                    texture.height());
        }

        private void renderMapQuad(
                MapProfile map,
                PoseStack poseStack,
                float centerX,
                float centerZ,
                float textureX,
                float textureZ,
                float width,
                float height) {
            // enable rotation if necessary
            if (followPlayerRotation) {
                poseStack.pushPose();
                RenderUtils.rotatePose(
                        poseStack, centerX, centerZ, 180 - McUtils.player().getYRot());
            }

            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, map.resource());

            // clamp map rendering
            int option = renderUsingLinear ? GL11.GL_LINEAR : GL11.GL_NEAREST;
            RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, option);

            RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL13.GL_CLAMP_TO_BORDER);
            RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL13.GL_CLAMP_TO_BORDER);

            float uScale = 1f / map.getTextureWidth();
            float vScale = 1f / map.getTextureHeight();

            // avoid rotational overpass - This is a rather loose oversizing, if possible later
            // use trignometry, etc. to find a better one
            float extraFactor = 1F;
            if (followPlayerRotation && maskType == MapMaskType.Rectangular) {
                // 1.5 > sqrt(2);
                extraFactor = 1.5F;

                if (width > height) {
                    extraFactor *= width / height;
                } else {
                    extraFactor *= height / width;
                }
            }

            float halfRenderedWidth = width / 2 * extraFactor;
            float halfRenderedHeight = height / 2 * extraFactor;
            float halfTextureWidth = halfRenderedWidth * scale;
            float halfTextureHeight = halfRenderedHeight * scale;

            Matrix4f matrix = poseStack.last().pose();

            // TODO replace with RenderUtils after settings pr is merged
            BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
            bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
            bufferBuilder
                    .vertex(matrix, (centerX - halfRenderedWidth), (centerZ + halfRenderedHeight), 0)
                    .uv((textureX - halfTextureWidth) * uScale, (textureZ + halfTextureHeight) * vScale)
                    .endVertex();
            bufferBuilder
                    .vertex(matrix, (centerX + halfRenderedWidth), (centerZ + halfRenderedHeight), 0)
                    .uv((textureX + halfTextureWidth) * uScale, (textureZ + halfTextureHeight) * vScale)
                    .endVertex();
            bufferBuilder
                    .vertex(matrix, (centerX + halfRenderedWidth), (centerZ - halfRenderedHeight), 0)
                    .uv((textureX + halfTextureWidth) * uScale, (textureZ - halfTextureHeight) * vScale)
                    .endVertex();
            bufferBuilder
                    .vertex(matrix, (centerX - halfRenderedWidth), (centerZ - halfRenderedHeight), 0)
                    .uv((textureX - halfTextureWidth) * uScale, (textureZ - halfTextureHeight) * vScale)
                    .endVertex();
            bufferBuilder.end();
            BufferUploader.end(bufferBuilder);

            // disable rotation if necessary
            if (followPlayerRotation) {
                poseStack.popPose();
            }
        }

        @Override
        protected void onConfigUpdate(ConfigHolder configHolder) {}
    }

    public enum CompassRenderType {
        None,
        North,
        All
    }
}
