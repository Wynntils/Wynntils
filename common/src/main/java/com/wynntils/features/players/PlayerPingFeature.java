/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.players;

import com.wynntils.core.components.Models;
import com.wynntils.core.components.Services;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.core.persisted.config.ConfigProfile;
import com.wynntils.hades.protocol.enums.PlayerPingType;
import com.wynntils.mc.event.SubmitCustomGeometryEvent;
import com.wynntils.services.hades.event.HadesPlayerPingEvent;
import com.wynntils.services.hades.type.PlayerPingData;
import com.wynntils.utils.EnumUtils;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.PlayerPingRenderer;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.WheelButtonStyle;
import com.wynntils.utils.type.TimedSet;
import java.util.List;
import java.util.concurrent.TimeUnit;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;

@ConfigCategory(Category.PLAYERS)
public class PlayerPingFeature extends Feature {
    private static final float BLINDNESS_VISIBILITY_DISTANCE = 5f;
    private static final float DARKNESS_VISIBILITY_DISTANCE = 15f;

    private TimedSet<PlayerPingData> selfPlayerPingDataSet;
    private TimedSet<PlayerPingData> otherPlayerPingDataSet;

    @Persisted
    private final Config<MarkerStyle> markerStyle = new Config<>(MarkerStyle.FLOATING_ICON);

    @Persisted
    private final Config<Boolean> showOwnPings = new Config<>(true);

    @Persisted
    private final Config<Integer> ownPingsDuration = new Config<>(5);

    @Persisted
    private final Config<Integer> otherPingsDuration = new Config<>(5);

    @Persisted
    private final Config<Float> pingVolume = new Config<>(1.0f);

    @Persisted
    private final Config<Boolean> showChatMessage = new Config<>(false);

    @Persisted
    public final Config<WheelButtonStyle> buttonStyle = new Config<>(WheelButtonStyle.BUTTON);

    @Persisted
    public final Config<CustomColor> textColor = new Config<>(CommonColors.WHITE);

    @Persisted
    public final Config<CustomColor> textColorHovered = new Config<>(CommonColors.WHITE);

    @Persisted
    public final Config<TextShadow> textShadow = new Config<>(TextShadow.OUTLINE);

    @Persisted
    public final Config<CustomColor> backgroundColor = new Config<>(CustomColor.fromHexString("#2D2D2DEE"));

    @Persisted
    public final Config<CustomColor> backgroundColorHovered = new Config<>(CustomColor.fromHexString("#4C8D2CEE"));

    public PlayerPingFeature() {
        super(
                new ProfileDefault.Builder()
                        .enabledFor(ConfigProfile.DEFAULT, ConfigProfile.LITE)
                        .build(),
                List.of(
                        ConfigDependency.functionality(Models.Friends.queryFriendsList),
                        ConfigDependency.functionality(Models.Party.queryPartyMembers),
                        ConfigDependency.functionality(Models.Guild.requestGuildMembers),
                        ConfigDependency.functionality(Services.Hades.connectToHades)));
    }

    @SubscribeEvent
    public void onSelfPlayerPing(HadesPlayerPingEvent.Self event) {
        if (!showOwnPings.get()) return;

        selfPlayerPingDataSet.put(event.getPlayerPingData());

        playPingSound(event.getPlayerPingData(), 0.8f);
    }

    @SubscribeEvent
    public void onOtherPlayerPing(HadesPlayerPingEvent.Other event) {
        PlayerPingData pingData = event.getPlayerPingData();

        otherPlayerPingDataSet.put(pingData);

        playPingSound(pingData, 1.0f);

        if (showChatMessage.get()) {
            MutableComponent message = Component.literal(pingData.getUsername() + ": ")
                    .withStyle(ChatFormatting.AQUA)
                    .append(Component.literal(EnumUtils.toNiceString(pingData.getPingType()))
                            .withColor(pingData.getPingType().getColor()))
                    .append(Component.literal(" at ").withStyle(ChatFormatting.GRAY))
                    .append(Component.literal(pingData.getLocation().asChatCoordinates())
                            .withStyle(ChatFormatting.GRAY));

            McUtils.sendWynntilsPrefixMessage(message);
        }
    }

    @SubscribeEvent
    public void onSubmitCustomGeometry(SubmitCustomGeometryEvent event) {
        for (PlayerPingData pingData : otherPlayerPingDataSet) {
            submitMarker(
                    event,
                    pingData.getLocation().toVec3(),
                    pingData.getDirection(),
                    pingData.getPingTarget(),
                    pingData.getPingType(),
                    pingData.getPingType().getColor());
        }

        for (PlayerPingData pingData : selfPlayerPingDataSet) {
            submitMarker(
                    event,
                    pingData.getLocation().toVec3(),
                    pingData.getDirection(),
                    pingData.getPingTarget(),
                    pingData.getPingType(),
                    tintSelfColor(pingData.getPingType().getColor()));
        }
    }

    @Override
    protected void onConfigUpdate(Config<?> config) {
        switch (config.getFieldName()) {
            case "ownPingsDuration" -> {
                selfPlayerPingDataSet = new TimedSet<>(Math.max(0, ownPingsDuration.get()), TimeUnit.SECONDS, true);
            }
            case "otherPingsDuration" -> {
                otherPlayerPingDataSet = new TimedSet<>(Math.max(0, otherPingsDuration.get()), TimeUnit.SECONDS, true);
            }
        }
    }

    private void submitMarker(
            SubmitCustomGeometryEvent event,
            Vec3 worldPosition,
            Direction direction,
            String pingTarget,
            PlayerPingType pingType,
            int color) {
        ChunkPos chunk = new ChunkPos(BlockPos.containing(worldPosition));
        if (McUtils.mc().level == null || !McUtils.mc().level.hasChunk(chunk.x, chunk.z)) return;
        if (isObscuredByMobEffect(worldPosition, event.getCameraRenderState().pos)) return;

        if (markerStyle.get() == MarkerStyle.FLOATING_ICON) {
            PlayerPingRenderer.submitMarker(
                    event.getSubmitNodeCollector(),
                    event.getCameraRenderState().pos,
                    event.getPoseStack(),
                    worldPosition,
                    direction,
                    pingTarget,
                    pingType,
                    event.getLevelRenderState().gameTime,
                    color);
        } else {
            Gizmos.cuboid(new AABB(BlockPos.containing(worldPosition)), GizmoStyle.stroke(color));
            PlayerPingRenderer.submitTargetText(
                    event.getSubmitNodeCollector(),
                    event.getCameraRenderState().pos,
                    event.getPoseStack(),
                    worldPosition,
                    direction,
                    pingTarget,
                    color);
        }
    }

    private boolean isObscuredByMobEffect(Vec3 worldPosition, Vec3 cameraPosition) {
        double distance = worldPosition.distanceTo(cameraPosition);
        if (McUtils.player().hasEffect(MobEffects.BLINDNESS)) {
            return distance > BLINDNESS_VISIBILITY_DISTANCE;
        }

        return McUtils.player().hasEffect(MobEffects.DARKNESS) && distance > DARKNESS_VISIBILITY_DISTANCE;
    }

    private void playPingSound(PlayerPingData pingData, float pitch) {
        double distance =
                McUtils.player().position().distanceTo(pingData.getLocation().toVec3());
        float attenuation = (float) Math.max(0d, 1d - Math.ceil(Math.max(0d, distance - 100d) / 50d) * 0.1d);
        McUtils.playSoundAmbient(SoundEvents.EXPERIENCE_ORB_PICKUP, pingVolume.get() * attenuation, pitch);
    }

    private int tintSelfColor(int color) {
        return CustomColor.fromARGBInt(color).brightnessShift(0.1f).asInt();
    }

    private enum MarkerStyle {
        FLOATING_ICON,
        CUBE_OUTLINE
    }
}
