package com.wynntils.features.user.overlays;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
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
import com.wynntils.mc.render.HorizontalAlignment;
import com.wynntils.mc.render.VerticalAlignment;
import com.wynntils.mc.utils.McUtils;
import net.minecraft.client.renderer.GameRenderer;

public class MiniMapOverlayFeature extends UserFeature {
    @OverlayInfo(renderType = RenderEvent.ElementType.GUI)
    public final MiniMapOverlay miniMapOverlay = new MiniMapOverlay();

    public static class MiniMapOverlay extends Overlay {

        @Config
        public float scale = 1f;

        @Config
        public boolean followPlayerRotation = true;

        public MiniMapOverlay() {
            super(
                    new OverlayPosition(
                            50,
                            50,
                            VerticalAlignment.Middle,
                            HorizontalAlignment.Center,
                            OverlayPosition.AnchorSection.TopLeft),
                    new GuiScaledOverlaySize(100, 100));
        }

        @Override
        public void render(PoseStack poseStack, float partialTicks, Window window) {
            MapProfile map = WebManager.getMaps().get(0);

            float uScale = 1f / map.getTextureWidth();
            float vScale = 1f / map.getTextureHeight();

            float renderX = getRenderX();
            float renderY = getRenderY();

            // Render Minimap

            float halfMapWidth = getWidth()/2;
            float halfMapHeight = getHeight()/2;

            float halfTextureWidth = scale * halfMapWidth;
            float halfTextureHeight = scale * halfMapHeight;
            float textureX = map.getTextureXPosition(McUtils.player().getX());
            float textureZ = map.getTextureZPosition(McUtils.player().getZ());

            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, map.resource());

            // TODO masking
            {

                // TODO tex parameters with clamping

                //TODO find better alternative for rotations
                if (followPlayerRotation) {
                    poseStack.pushPose();
                    poseStack.translate(renderX, renderY, 0);
                    // See Quaternion#fromXYZ
                    poseStack.mulPose(new Quaternion(0F, (float) StrictMath.sin((Math.PI - Math.toRadians(McUtils.player().getYRot()) / 2F)), 0, (float) StrictMath.cos(Math.PI - Math.toRadians(McUtils.player().getYRot()) / 2F)));
                    poseStack.translate(-renderX, -renderY, 0);
                }

                Matrix4f matrix = poseStack.last().pose();
                {
                    BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
                    bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
                    bufferBuilder
                            .vertex(matrix, (renderX - halfMapWidth), (renderY + halfMapHeight), 0)
                            .uv((textureX - halfTextureWidth) * uScale, (textureZ + halfTextureHeight) * vScale)
                            .endVertex();
                    bufferBuilder
                            .vertex(matrix, (renderX + halfMapWidth), (renderY + halfMapHeight), 0)
                            .uv((textureX + halfTextureWidth) * uScale, (textureZ + halfTextureHeight) * vScale)
                            .endVertex();
                    bufferBuilder
                            .vertex(matrix, (renderX + halfMapWidth), (renderY - halfMapHeight), 0)
                            .uv((textureX + halfTextureWidth) * uScale, (textureZ - halfTextureHeight) * vScale)
                            .endVertex();
                    bufferBuilder
                            .vertex(matrix, (renderX - halfMapWidth), (renderY - halfMapHeight), 0)
                            .uv((textureX - halfTextureWidth) * uScale, (textureZ - halfTextureHeight) * vScale)
                            .endVertex();
                    bufferBuilder.end();
                    BufferUploader.end(bufferBuilder);
                }

                if (followPlayerRotation) {
                    poseStack.popPose();
                }

                // TODO minimap icons

                // TODO compass icon

            }

            // TODO cursor

            // TODO Render Border

            // TODO Directional Text

            // TODO Coords



        }

        @Override
        protected void onConfigUpdate(ConfigHolder configHolder) {
        }

    }

}
