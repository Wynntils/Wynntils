/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.map;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.wynntils.core.components.Models;
import com.wynntils.core.config.Category;
import com.wynntils.core.config.Config;
import com.wynntils.core.config.ConfigCategory;
import com.wynntils.core.config.RegisterConfig;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.mc.event.RenderTileLevelLastEvent;
import com.wynntils.utils.MathUtils;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.mc.type.Location;
import com.wynntils.utils.render.CustomBeaconRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BeaconRenderer;
import net.minecraft.core.Position;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@ConfigCategory(Category.MAP)
public class BeaconBeamFeature extends Feature {
    @RegisterConfig
    public final Config<CustomColor> waypointBeamColor = new Config<>(CommonColors.RED);

    @SubscribeEvent
    public void onRenderLevelLast(RenderTileLevelLastEvent event) {
        if (Models.Compass.getCompassLocation().isEmpty()) return;

        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource bufferSource =
                MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());

        Position camera = event.getCamera().getPosition();
        Location location = Models.Compass.getCompassLocation().get();

        double dx = location.x - camera.x();
        double dy = location.y - camera.y();
        double dz = location.z - camera.z();

        double distance = MathUtils.magnitude(dx, dz);
        int maxDistance = McUtils.options().renderDistance().get() * 16;

        if (distance > maxDistance) {
            double scale = maxDistance / distance;

            dx *= scale;
            dz *= scale;
        }

        float alpha = 1f;

        if (distance <= 7) {
            alpha = MathUtils.clamp(MathUtils.map((float) distance, 2f, 7f, 0f, 1f), 0f, 1f);
        }

        poseStack.pushPose();
        poseStack.translate(dx, dy, dz);

        CustomBeaconRenderer.renderBeaconBeam(
                poseStack,
                bufferSource,
                BeaconRenderer.BEAM_LOCATION,
                event.getPartialTick(),
                1f,
                McUtils.player().level.getGameTime(),
                0,
                1024,
                waypointBeamColor.get().asFloatArray(),
                alpha,
                0.166f,
                0.33f);

        poseStack.popPose();

        bufferSource.endLastBatch();
    }
}
