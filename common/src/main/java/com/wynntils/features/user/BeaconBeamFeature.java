/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.wynntils.core.components.Models;
import com.wynntils.core.config.Config;
import com.wynntils.core.features.UserFeature;
import com.wynntils.mc.event.RenderTileLevelLastEvent;
import com.wynntils.utils.MathUtils;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.mc.type.Location;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BeaconRenderer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class BeaconBeamFeature extends UserFeature {
    @Config
    public CustomColor waypointBeamColor = CommonColors.RED;

    @SubscribeEvent
    public void onRenderLevelLast(RenderTileLevelLastEvent event) {
        if (Models.Compass.getCompassLocation().isEmpty()) return;

        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource bufferSource =
                MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());

        Vec3 camera = event.getCamera().getPosition();
        Location location = Models.Compass.getCompassLocation().get();

        double dx = location.x - camera.x;
        double dy = location.y - camera.y;
        double dz = location.z - camera.z;

        double distance = MathUtils.magnitude(dx, dz);
        int maxDistance = McUtils.mc().options.renderDistance().get() * 16;

        if (distance > maxDistance) {
            double scale = maxDistance / distance;

            dx *= scale;
            dz *= scale;
        }

        float textureScale = 1f;

        if (distance <= 5) {
            textureScale = MathUtils.map((float) distance, 0f, 5f, 0f, 1f);
        }

        poseStack.pushPose();
        poseStack.translate(dx, dy, dz);

        BeaconRenderer.renderBeaconBeam(
                poseStack,
                bufferSource,
                BeaconRenderer.BEAM_LOCATION,
                event.getPartialTick(),
                textureScale,
                McUtils.player().level.getGameTime(),
                0,
                1024,
                waypointBeamColor.asFloatArray(),
                0.166f * textureScale,
                0.33f * textureScale);

        poseStack.popPose();

        bufferSource.endLastBatch();
    }
}
