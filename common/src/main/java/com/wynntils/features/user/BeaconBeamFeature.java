/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.config.Config;
import com.wynntils.core.features.UserFeature;
import com.wynntils.core.managers.Model;
import com.wynntils.core.managers.Models;
import com.wynntils.mc.event.RenderTileLevelLastEvent;
import com.wynntils.mc.objects.CommonColors;
import com.wynntils.mc.objects.CustomColor;
import com.wynntils.mc.objects.Location;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.utils.MathUtils;
import java.util.List;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BeaconRenderer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class BeaconBeamFeature extends UserFeature {
    @Config
    public CustomColor waypointBeamColor = CommonColors.RED;

    @Override
    public List<Class<? extends Model>> getModelDependencies() {
        return List.of(Models.Compass.getClass());
    }

    @SubscribeEvent
    public void onRenderLevelLast(RenderTileLevelLastEvent event) {
        if (Models.Compass.getCompassLocation().isEmpty()) return;

        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource bufferSource =
                McUtils.mc().renderBuffers().bufferSource();

        Vec3 camera = event.getCamera().getPosition();
        Location location = Models.Compass.getCompassLocation().get();

        double dx = location.x - camera.x;
        double dy = location.y - camera.y;
        double dz = location.z - camera.z;

        double distance = MathUtils.magnitude(dx, dz);
        int maxDistance = McUtils.mc().options.renderDistance * 16;

        if (distance > maxDistance) {
            double scale = maxDistance / distance;

            dx *= scale;
            dz *= scale;
        }

        poseStack.pushPose();
        poseStack.translate(dx, dy, dz);

        BeaconRenderer.renderBeaconBeam(
                poseStack,
                bufferSource,
                BeaconRenderer.BEAM_LOCATION,
                event.getPartialTick(),
                1f,
                McUtils.player().level.getGameTime(),
                0,
                1024,
                waypointBeamColor.asFloatArray(),
                0.166f,
                0.33f);

        poseStack.popPose();

        bufferSource.endLastBatch();
    }
}
