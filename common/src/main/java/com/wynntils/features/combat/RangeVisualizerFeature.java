/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.combat;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.wynntils.core.components.Models;
import com.wynntils.core.components.Services;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.core.persisted.config.ConfigProfile;
import com.wynntils.mc.event.PlayerRenderEvent;
import com.wynntils.mc.event.RenderTileLevelLastEvent;
import com.wynntils.mc.event.TickEvent;
import com.wynntils.mc.extension.EntityRenderStateExtension;
import com.wynntils.models.gambits.type.Gambit;
import com.wynntils.models.gear.type.GearInfo;
import com.wynntils.models.inventory.type.InventoryAccessory;
import com.wynntils.models.inventory.type.InventoryArmor;
import com.wynntils.models.items.WynnItem;
import com.wynntils.models.items.items.game.GearItem;
import com.wynntils.services.hades.HadesUser;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.buffered.CustomRenderType;
import com.wynntils.utils.type.Pair;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.Position;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import org.joml.Matrix4f;

@ConfigCategory(Category.COMBAT)
public class RangeVisualizerFeature extends Feature {
    private static final MultiBufferSource.BufferSource BUFFER_SOURCE =
            MultiBufferSource.immediate(new ByteBufferBuilder(256));

    // number of straight lines to draw when rendering circle, higher = smoother but more expensive
    private static final int SEGMENTS = 128;
    private static final float HEIGHT = 0.1f;
    private static final int TRANSPARENCY = 95;

    private final Map<Player, List<Pair<CustomColor, Float>>> circlesToRender = new HashMap<>();
    private final Set<Player> detectedPlayers = new HashSet<>();

    @Persisted
    private final Config<Boolean> renderInFirstPerson = new Config<>(true);

    @Persisted
    private final Config<Boolean> showGambitCircles = new Config<>(true);

    @Persisted
    private final Config<Boolean> showMajorIDCircles = new Config<>(true);

    public RangeVisualizerFeature() {
        super(new ProfileDefault.Builder()
                .disableFor(ConfigProfile.NEW_PLAYER, ConfigProfile.BLANK_SLATE)
                .build());
    }

    // Handles rendering for other players and ourselves in third person
    @SubscribeEvent
    public void onPlayerRender(PlayerRenderEvent e) {
        Entity entity = ((EntityRenderStateExtension) e.getPlayerRenderState()).getEntity();
        if (!(entity instanceof AbstractClientPlayer player)) return;
        // We render the circle for ourselves in onRenderLevelLast if first person rendering is enabled
        if (player.equals(McUtils.player()) && renderInFirstPerson.get()) return;

        detectedPlayers.add(player);

        List<Pair<CustomColor, Float>> circles = circlesToRender.get(player);
        if (circles == null) return;

        circles.forEach(circleType -> {
            float radius = circleType.b();
            int color = circleType.a().asInt();

            renderCircle(e.getPoseStack(), player.position(), radius, color);
        });
    }

    // Handles first person rendering for ourself
    @SubscribeEvent
    public void onRenderLevelLast(RenderTileLevelLastEvent event) {
        if (!Models.WorldState.onWorld()) return;
        if (!renderInFirstPerson.get()) return;

        Player player = McUtils.player();
        if (player == null) return;

        detectedPlayers.add(player);

        List<Pair<CustomColor, Float>> circles = circlesToRender.get(player);
        if (circles == null || circles.isEmpty()) return;

        PoseStack poseStack = event.getPoseStack();
        float partialTick = event.getDeltaTracker().getGameTimeDeltaPartialTick(true);

        double interpX = player.xo + (player.getX() - player.xo) * partialTick;
        double interpY = player.yo + (player.getY() - player.yo) * partialTick;
        double interpZ = player.zo + (player.getZ() - player.zo) * partialTick;

        poseStack.pushPose();
        poseStack.translate(
                interpX - event.getCamera().getPosition().x,
                interpY - event.getCamera().getPosition().y,
                interpZ - event.getCamera().getPosition().z);

        for (Pair<CustomColor, Float> circle : circles) {
            float radius = circle.b();
            int color = circle.a().asInt();

            renderCircle(poseStack, player.position(), radius, color);
        }

        poseStack.popPose();
    }

    @SubscribeEvent
    public void onTick(TickEvent e) {
        // Once every tick, calculate circles for all players that were detected (i.e. rendered)
        // since the previous tick
        circlesToRender.clear();
        detectedPlayers.forEach(this::checkCircles);
        detectedPlayers.clear();
    }

    private void checkCircles(Player player) {
        // Don't render for ghost/npc
        if (!Models.Player.isLocalPlayer(player)) return;

        List<Pair<CustomColor, Float>> circles = new ArrayList<>();
        if (showMajorIDCircles.get()) {
            List<GearInfo> validGear;

            if (player == McUtils.player()) {
                // This is ourselves, rendered from outside

                // Don't render for preview in inventory or character selection screen
                if (McUtils.screen() != null && !(McUtils.screen() instanceof ChatScreen)) return;

                validGear = Models.CharacterStats.getWornGear();
            } else {
                // Wynncraft no longer sends the worn gear by other players so we rely on players opting in to share
                // with Hades
                Optional<HadesUser> hadesUserOpt = Services.Hades.getHadesUser(player.getUUID());
                if (hadesUserOpt.isEmpty()) return;

                validGear = new ArrayList<>();
                // Check held item
                GearInfo heldItemGearInfo =
                        getOtherPlayerGearInfo(hadesUserOpt.get().getHeldItem());
                if (heldItemGearInfo != null) {
                    if (heldItemGearInfo.type().isWeapon()) {
                        // We cannot verify class or level :(
                        validGear.add(heldItemGearInfo);
                    }
                }

                // Check armor
                for (InventoryArmor armor : hadesUserOpt.get().getArmor().keySet()) {
                    GearInfo armorGearInfo =
                            getOtherPlayerGearInfo(hadesUserOpt.get().getArmor().get(armor));
                    if (armorGearInfo != null) {
                        if (armorGearInfo.type().isArmor()) {
                            validGear.add(armorGearInfo);
                        }
                    }
                }
                // Check accessories
                for (InventoryAccessory accessory :
                        hadesUserOpt.get().getAccessories().keySet()) {
                    GearInfo accessoryGearInfo = getOtherPlayerGearInfo(
                            hadesUserOpt.get().getAccessories().get(accessory));
                    if (accessoryGearInfo != null) {
                        if (accessoryGearInfo.type().isAccessory()) {
                            validGear.add(accessoryGearInfo);
                        }
                    }
                }
            }

            // For each valid gear, check all its major IDs, and store them as color/radius pairs
            // Offset the radius slightly so multiple circles can be shown for each player
            // Only a few major IDs can actually be applied at the same time, but we make this general
            validGear.stream()
                    .flatMap(gearInfo -> gearInfo.fixedStats().majorIds().stream()
                            .map(majorId -> getCircleFromMajorId(majorId.name())))
                    .filter(Objects::nonNull)
                    .forEach(circles::add);
        }

        // add circles gained from raid major id buffs and gambits
        if (Models.Raid.getCurrentRaid() != null) {
            // only show our own gambit circles
            if (player == McUtils.player() && showGambitCircles.get()) {
                Models.Gambit.getActiveGambits().stream()
                        .map(this::getCircleFromGambit)
                        .filter(Objects::nonNull)
                        .forEach(circles::add);
            }

            if (showMajorIDCircles.get()) {
                Models.Raid.getRaidMajorIds(player.getName().getString()).stream()
                        .map(this::getCircleFromMajorId)
                        .filter(Objects::nonNull)
                        .forEach(circles::add);
            }
        }

        if (!circles.isEmpty()) {
            circlesToRender.put(player, circles);
        }
    }

    private Pair<CustomColor, Float> getCircleFromMajorId(String majorIdName) {
        return switch (majorIdName) {
            case "Taunt" -> Pair.of(CommonColors.ORANGE.withAlpha(TRANSPARENCY), 12f);
            case "Saviour's Sacrifice" -> Pair.of(CommonColors.WHITE.withAlpha(TRANSPARENCY), 8f);
            case "Altruism" -> Pair.of(CommonColors.PINK.withAlpha(TRANSPARENCY), 16f);
            case "Guardian" -> Pair.of(CommonColors.RED.withAlpha(TRANSPARENCY), 12f);
            default -> null;
        };
    }

    private Pair<CustomColor, Float> getCircleFromGambit(Gambit gambit) {
        return switch (gambit) {
            case FARSIGHTED -> Pair.of(CommonColors.RED.withAlpha(TRANSPARENCY), 3f);
            case MYOPIC -> Pair.of(CommonColors.RED.withAlpha(TRANSPARENCY), 12f);
            default -> null;
        };
    }

    private GearInfo getOtherPlayerGearInfo(WynnItem wynnItem) {
        if (wynnItem instanceof GearItem gearItem) {
            return gearItem.getItemInfo();
        }

        return null;
    }

    /**
     * Renders a circle with the given radius. Some notes for future reference:<p>
     * - The circle is rendered at the player's feet, from the ground to HEIGHT blocks above the ground.<p>
     * - .color() takes floats from 0-1, but ints from 0-255<p>
     * - Increase SEGMENTS to make the circle smoother, but it will also increase the amount of vertices (and thus the amount of memory used and the amount of time it takes to render)<p>
     * - The order of the consumer.vertex() calls matter. Here, we draw a quad, so we do bottom left corner, top left corner, top right corner, bottom right corner. This is filled in with the color we set.<p>
     *
     * @param poseStack The pose stack to render with. This is supposed to be the pose stack from the event.
     *                  We do the translation here, so no need to do it before passing it in.
     * @param radius
     * @param color
     */
    private void renderCircle(PoseStack poseStack, Position position, float radius, int color) {
        // Circle must be rendered on both sides, otherwise it will be invisible when looking at
        // it from the outside
        RenderSystem.disableCull();

        poseStack.pushPose();
        poseStack.translate(-position.x(), -position.y(), -position.z());
        VertexConsumer consumer = BUFFER_SOURCE.getBuffer(CustomRenderType.POSITION_COLOR_QUAD);

        Matrix4f matrix4f = poseStack.last().pose();
        double angleStep = 2 * Math.PI / SEGMENTS;
        double startingAngle = -(System.currentTimeMillis() % 40000) * 2 * Math.PI / 40000.0;
        double angle = startingAngle;
        for (int i = 0; i < SEGMENTS; i++) {
            if (i % 4 > 2) {
                angle += angleStep;
                continue;
            }
            float x = (float) (position.x() + Math.sin(angle) * radius);
            float z = (float) (position.z() + Math.cos(angle) * radius);
            consumer.addVertex(matrix4f, x, (float) position.y(), z).setColor(color);
            consumer.addVertex(matrix4f, x, (float) position.y() + HEIGHT, z).setColor(color);
            angle += angleStep;
            float x2 = (float) (position.x() + Math.sin(angle) * radius);
            float z2 = (float) (position.z() + Math.cos(angle) * radius);
            consumer.addVertex(matrix4f, x2, (float) position.y() + HEIGHT, z2).setColor(color);
            consumer.addVertex(matrix4f, x2, (float) position.y(), z2).setColor(color);
        }

        BUFFER_SOURCE.endBatch();
        poseStack.popPose();
        RenderSystem.enableCull();
    }
}
