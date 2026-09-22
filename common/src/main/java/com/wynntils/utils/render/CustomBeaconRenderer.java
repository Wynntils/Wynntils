/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BeaconRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;

public class CustomBeaconRenderer extends BeaconRenderer {
    // This is the same as BeaconRenderer.submitBeaconBeam with some changes to not complain about
    // non final variables in lambdas and changing the first RenderTypes.beaconBeam call to use true
    // for the translucent argument
    public static void submitBeaconBeam(
            final PoseStack poseStack,
            final SubmitNodeCollector submitNodeCollector,
            final Identifier beamLocation,
            final float scale,
            final float animationTime,
            final int beamStart,
            final int height,
            final int color,
            final float solidBeamRadius,
            final float beamGlowRadius) {
        int beamEnd = beamStart + height;
        poseStack.pushPose();
        poseStack.translate(0.5, 0.0, 0.5);
        float scroll = height < 0 ? animationTime : -animationTime;
        float texVOff = Mth.frac(scroll * 0.2F - Mth.floor(scroll * 0.1F));
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(animationTime * 2.25F - 45.0F));
        float wsx = -solidBeamRadius;
        float esz = -solidBeamRadius;
        float vv2 = -1.0F + texVOff;
        float vv1 = height * scale * (0.5F / solidBeamRadius) + vv2;

        submitNodeCollector.submitCustomGeometry(
                poseStack,
                RenderTypes.beaconBeam(beamLocation, true),
                (pose, buffer) -> renderPart(
                        pose,
                        buffer,
                        color,
                        beamStart,
                        beamEnd,
                        0.0F,
                        solidBeamRadius,
                        solidBeamRadius,
                        0.0F,
                        wsx,
                        0.0F,
                        0.0F,
                        esz,
                        0.0F,
                        1.0F,
                        vv1,
                        vv2));

        poseStack.popPose();

        float glowWnx = -beamGlowRadius;
        float glowWnz = -beamGlowRadius;
        float glowEnz = -beamGlowRadius;
        float glowWsx = -beamGlowRadius;
        float glowVv2 = -1.0F + texVOff;
        float glowVv1 = height * scale + glowVv2;

        submitNodeCollector.submitCustomGeometry(
                poseStack,
                RenderTypes.beaconBeam(beamLocation, true),
                (pose, buffer) -> renderPart(
                        pose,
                        buffer,
                        ARGB.color(32, color),
                        beamStart,
                        beamEnd,
                        glowWnx,
                        glowWnz,
                        beamGlowRadius,
                        glowEnz,
                        glowWsx,
                        beamGlowRadius,
                        beamGlowRadius,
                        beamGlowRadius,
                        0.0F,
                        1.0F,
                        glowVv1,
                        glowVv2));
        poseStack.popPose();
    }
}
