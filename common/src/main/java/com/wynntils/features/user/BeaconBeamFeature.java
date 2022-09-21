/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.wynntils.core.config.Config;
import com.wynntils.core.features.UserFeature;
import com.wynntils.core.managers.Model;
import com.wynntils.mc.event.RenderLevelLastEvent;
import com.wynntils.mc.objects.CommonColors;
import com.wynntils.mc.objects.CustomColor;
import com.wynntils.mc.objects.Location;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.wynn.event.TrackedQuestUpdateEvent;
import com.wynntils.wynn.model.CompassModel;
import com.wynntils.wynn.model.scoreboard.quests.QuestInfo;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BeaconRenderer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class BeaconBeamFeature extends UserFeature {

    private static final Pattern COORDINATE_PATTERN = Pattern.compile(".*\\[(-?\\d+), ?(-?\\d+), ?(-?\\d+)\\].*");

    @Config
    public CustomColor waypointBeamColor = CommonColors.RED;

    @Config
    public boolean autoTrackQuestCoordinates = true;

    @Override
    protected void onInit(
            ImmutableList.Builder<Condition> conditions, ImmutableList.Builder<Class<? extends Model>> dependencies) {
        dependencies.add(CompassModel.class);
    }

    @SubscribeEvent
    public void onTrackedQuestUpdate(TrackedQuestUpdateEvent event) {
        if (event.getQuestInfo() == null) return;

        QuestInfo questInfo = event.getQuestInfo();

        Matcher matcher = COORDINATE_PATTERN.matcher(questInfo.description());
        if (!matcher.matches()) return;

        Location parsedLocation = new Location(
                Integer.parseInt(matcher.group(1)),
                Integer.parseInt(matcher.group(2)),
                Integer.parseInt(matcher.group(3)));

        CompassModel.setCompassLocation(parsedLocation);
    }

    @SubscribeEvent
    public void onRenderLevelLast(RenderLevelLastEvent event) {
        if (McUtils.player() == null || CompassModel.getCompassLocation().isEmpty()) return;

        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource bufferSource =
                MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());

        Player player = McUtils.player();

        poseStack.pushPose();
        float partials = event.getPartialTick();
        Vec3 vec = McUtils.mc().getCameraEntity().getEyePosition(partials);
        poseStack.translate(-vec.x, -vec.y, -vec.z);

        Location location = CompassModel.getCompassLocation().get();
        poseStack.translate(location.x, location.y, location.z);

        BeaconRenderer.renderBeaconBeam(
                poseStack,
                bufferSource,
                BeaconRenderer.BEAM_LOCATION,
                partials,
                1,
                player.level.getGameTime(),
                0,
                319,
                waypointBeamColor.asFloatArray(),
                0.166F,
                0.33F);
        poseStack.popPose();

        bufferSource.endBatch();
    }
}
