/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.wynntils.utils.MathUtils;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BeaconRenderer;
import net.minecraft.resources.ResourceLocation;

public final class CustomBeaconRenderer {
    public static void renderBeaconBeam(
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            ResourceLocation beamLocation,
            float partialTick,
            float textureScale,
            long gameTime,
            int yOffset,
            int height,
            float[] colors,
            float alpha,
            float beamRadius,
            float glowRadius) {
        int renderY = yOffset + height;
        poseStack.pushPose();
        poseStack.translate(0.5, 0.0, 0.5);
        float f = (float) Math.floorMod(gameTime, 40) + partialTick;
        float g = height < 0 ? f : -f;
        float h = MathUtils.frac(g * 0.2F - (float) MathUtils.floor(g * 0.1F));
        float red = colors[0];
        float green = colors[1];
        float blue = colors[2];
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(f * 2.25F - 45.0F));
        float maxV = -1.0F + h;
        float minV = (float) height * textureScale * (0.5F / beamRadius) + maxV;
        BeaconRenderer.renderPart(
                poseStack,
                bufferSource.getBuffer(RenderType.beaconBeam(beamLocation, true)),
                red,
                green,
                blue,
                alpha,
                yOffset,
                renderY,
                0.0F,
                beamRadius,
                beamRadius,
                0.0F,
                -beamRadius,
                0.0F,
                0.0F,
                -beamRadius,
                0.0F,
                1.0F,
                minV,
                maxV);
        poseStack.popPose();
        maxV = -1.0F + h;
        minV = (float) height * textureScale + maxV;
        BeaconRenderer.renderPart(
                poseStack,
                bufferSource.getBuffer(RenderType.beaconBeam(beamLocation, true)),
                red,
                green,
                blue,
                MathUtils.map(alpha, 0.0f, 1.0f, 0f, 0.125F),
                yOffset,
                renderY,
                -glowRadius,
                -glowRadius,
                glowRadius,
                -glowRadius,
                -beamRadius,
                glowRadius,
                glowRadius,
                glowRadius,
                0.0F,
                1.0F,
                minV,
                maxV);
        poseStack.popPose();
    }
}
