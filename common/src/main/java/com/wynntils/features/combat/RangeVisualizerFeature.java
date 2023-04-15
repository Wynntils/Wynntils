/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.combat;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.wynntils.core.components.Models;
import com.wynntils.core.config.Category;
import com.wynntils.core.config.ConfigCategory;
import com.wynntils.core.features.Feature;
import com.wynntils.mc.event.PlayerRenderEvent;
import com.wynntils.mc.event.TickEvent;
import com.wynntils.models.character.type.ClassType;
import com.wynntils.models.gear.type.GearInfo;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.ComponentUtils;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.buffered.CustomRenderType;
import com.wynntils.utils.type.Pair;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.Position;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.joml.Matrix4f;

@ConfigCategory(Category.COMBAT)
public class RangeVisualizerFeature extends Feature {
    private static final MultiBufferSource.BufferSource BUFFER_SOURCE =
            MultiBufferSource.immediate(new BufferBuilder(256));

    // number of straight lines to draw when rendering circle, higher = smoother but more expensive
    private static final int SEGMENTS = 128;
    private static final float HEIGHT = 0.1f;

    private final Map<Player, Pair<CustomColor, Integer>> circlesToRender = new HashMap<>();
    private final Set<Player> detectedPlayers = new HashSet<>();

    @SubscribeEvent
    public void onPlayerRender(PlayerRenderEvent e) {
        AbstractClientPlayer player = e.getPlayer();
        detectedPlayers.add(player);

        Pair<CustomColor, Integer> circleType = circlesToRender.get(player);
        if (circleType == null) return;

        renderCircle(e.getPoseStack(), player.position(), circleType);
    }

    @SubscribeEvent
    public void onTick(TickEvent e) {
        // Once every tick, calculate circles for all players that were detected (i.e. rendered)
        // since the previous tick
        circlesToRender.clear();
        detectedPlayers.forEach((player) -> {
            Pair<CustomColor, Integer> circleType = calculateCircleType(player);
            if (circleType == null) return;

            circlesToRender.put(player, circleType);
        });
        detectedPlayers.clear();
    }

    private Pair<CustomColor, Integer> calculateCircleType(Player player) {
        if (!Models.Player.isLocalPlayer(player)) return null; // Don't render for ghost/npc

        String playerName = ComponentUtils.getUnformatted(player.getName());
        boolean isSelf =
                ComponentUtils.getUnformatted(McUtils.player().getName()).equals(playerName);
        if (isSelf && McUtils.mc().screen instanceof InventoryScreen)
            return null; // Don't render for preview in inventory

        if (!Models.Party.getPartyMembers().contains(playerName) && !isSelf)
            return null; // Other players must be in party

        // We are getting the item info the same way as GearViewerScreen since we care about other people's items
        String gearName = ComponentUtils.getUnformatted(player.getMainHandItem().getHoverName());
        GearInfo gearInfo = Models.Gear.getGearInfoFromApiName(gearName);
        if (gearInfo == null) return null;

        if (isSelf) { // Do not render if the item is not for the player's class
            if (gearInfo.requirements().classType().isEmpty()) return null;

            ClassType classType = gearInfo.requirements().classType().get();
            if (classType != Models.Character.getClassType()) return null;
        }

        return gearInfo.fixedStats().majorIds().stream()
                .map(majorId -> switch (majorId.name()) {
                    case "TAUNT" -> Pair.of(CommonColors.ORANGE, 12);
                    case "HERO" -> Pair.of(CommonColors.WHITE, 8);
                    case "ALTRUISM" -> Pair.of(CommonColors.PINK, 8);
                    case "GUARDIAN" -> Pair.of(CommonColors.RED, 8);
                    default -> null;
                })
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    /**
     * Renders a circle with the given radius. Some notes for future reference:<p>
     * - The circle is rendered at the player's feet, from the ground to HEIGHT blocks above the ground.<p>
     * - .color() takes floats from 0-1, but ints from 0-255<p>
     * - Increase SEGMENTS to make the circle smoother, but it will also increase the amount of vertices (and thus the amount of memory used and the amount of time it takes to render)<p>
     * - The order of the consumer.vertex() calls matter. Here, we draw a quad, so we do bottom left corner, top left corner, top right corner, bottom right corner. This is filled in with the color we set.<p>
     * @param poseStack The pose stack to render with. This is supposed to be the pose stack from the event.
     *                  We do the translation here, so no need to do it before passing it in.
     */
    private void renderCircle(PoseStack poseStack, Position position, Pair<CustomColor, Integer> circleType) {
        int radius = circleType.b();
        int color = circleType.a().asInt();

        // Circle must be rendered on both sides, otherwise it will be invisible when looking at
        // it from the outside
        RenderSystem.disableCull();

        poseStack.pushPose();
        poseStack.translate(-position.x(), -position.y(), -position.z());
        VertexConsumer consumer = BUFFER_SOURCE.getBuffer(CustomRenderType.POSITION_COLOR_QUAD);

        Matrix4f matrix4f = poseStack.last().pose();
        double angleStep = 2 * Math.PI / SEGMENTS;
        double angle = 0;
        for (int i = 0; i < SEGMENTS; i++) {
            float x = (float) (position.x() + Math.sin(angle) * radius);
            float z = (float) (position.z() + Math.cos(angle) * radius);
            consumer.vertex(matrix4f, x, (float) position.y(), z).color(color).endVertex();
            consumer.vertex(matrix4f, x, (float) position.y() + HEIGHT, z)
                    .color(color)
                    .endVertex();
            angle += angleStep;
            float x2 = (float) (position.x() + Math.sin(angle) * radius);
            float z2 = (float) (position.z() + Math.cos(angle) * radius);
            consumer.vertex(matrix4f, x2, (float) position.y() + HEIGHT, z2)
                    .color(color)
                    .endVertex();
            consumer.vertex(matrix4f, x2, (float) position.y(), z2).color(color).endVertex();
        }

        BUFFER_SOURCE.endBatch();
        poseStack.popPose();
        RenderSystem.enableCull();
    }
}
