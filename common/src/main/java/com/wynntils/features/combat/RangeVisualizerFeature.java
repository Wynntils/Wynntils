package com.wynntils.features.combat;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.wynntils.core.components.Models;
import com.wynntils.core.config.Category;
import com.wynntils.core.config.ConfigCategory;
import com.wynntils.core.features.Feature;
import com.wynntils.mc.event.PlayerRenderEvent;
import com.wynntils.models.gear.type.GearMajorId;
import com.wynntils.models.items.items.game.GearItem;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.buffered.CustomRenderType;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.util.List;
import java.util.Optional;

@ConfigCategory(Category.COMBAT)
public class RangeVisualizerFeature extends Feature {

    private static final MultiBufferSource.BufferSource BUFFER_SOURCE = MultiBufferSource.immediate(new BufferBuilder(256));
    private static final int SEGMENTS = 128;
    private static final float HEIGHT = 0.1f;

    @SubscribeEvent
    public void onPlayerRender(PlayerRenderEvent e) {
        if (!Models.Player.isLocalPlayer(e.getPlayer())) return;
        Optional<GearItem> item = Models.Item.asWynnItem(e.getPlayer().getItemInHand(InteractionHand.MAIN_HAND), GearItem.class);
        if (item.isEmpty()) return;
        renderCircleWithRadius(e.getPoseStack(), 4); // TODO: remove debug
        List<GearMajorId> majorIds = item.get().getGearInfo().fixedStats().majorIds();
        if (majorIds.isEmpty()) return;

        // Major IDs that we can visualize:
        // Taunt (12 blocks)
        // Saviour's Sacrifice (8 blocks)?
        // Heart of the Pack (8 blocks)?
        // Guardian (8 blocks)?
        // Marked with a ? needs additional confirmation

        for (GearMajorId majorId : majorIds) {
            if (majorId.name().equals("Taunt")) {
                renderCircleWithRadius(e.getPoseStack(), 12);
            } else if (majorId.name().equals("Saviour's Sacrifice") ||
                    majorId.name().equals("Heart of the Pack") ||
                    majorId.name().equals("Guardian")) {
                renderCircleWithRadius(e.getPoseStack(), 8);
            }
        }
    }

    /**
     * Renders a circle with the given radius. Some notes for future reference:<p>
     * - The circle is rendered at the player's feet, from the ground to HEIGHT blocks above the ground.<p>
     * - .color() takes floats from 0-1, but ints from 0-255<p>
     * - Increase SEGMENTS to make the circle smoother, but it will also increase the amount of vertices (and thus the amount of memory used and the amount of time it takes to render)<p>
     * - The order of the consumer.vertex() calls matter. Here, we draw a quad, so we do bottom left corner, top left corner, top right corner, bottom right corner. This is filled in with the color we set.<p>
     * @param poseStack The pose stack to render with. This is supposed to be the pose stack from the event.
     *                  We do the translation here, so no need to do it before passing it in.
     * @param radius Pretty self explanatory, radius in blocks.
     */
    private void renderCircleWithRadius(PoseStack poseStack, int radius) {
        poseStack.pushPose();
        poseStack.translate(-McUtils.player().getX(), -McUtils.player().getY(), -McUtils.player().getZ());
        VertexConsumer consumer = BUFFER_SOURCE.getBuffer(CustomRenderType.POSITION_COLOR_QUAD);

        Matrix4f matrix4f = poseStack.last().pose();
        Matrix3f matrix3f = poseStack.last().normal();
        double angleStep = 2 * Math.PI / SEGMENTS;
        double angle = 0;
        for (int i = 0; i < SEGMENTS; i++) {
            float x = (float) (McUtils.player().getX() + Math.sin(angle) * radius);
            float z = (float) (McUtils.player().getZ() + Math.cos(angle) * radius);
            consumer.vertex(matrix4f, x, (float) McUtils.player().getY(), z).color(1.0F, 1.0F, 1.0F, 1.0F).normal(matrix3f, 1.0F, 1.0F, 1.0F).endVertex();
            consumer.vertex(matrix4f, x, (float) McUtils.player().getY() + HEIGHT, z).color(1.0F, 1.0F, 1.0F, 1.0F).normal(matrix3f, 1.0F, 1.0F, 1.0F).endVertex();
            angle += angleStep;
            float x2 = (float) (McUtils.player().getX() + Math.sin(angle) * radius);
            float z2 = (float) (McUtils.player().getZ() + Math.cos(angle) * radius);
            consumer.vertex(matrix4f, x2, (float) McUtils.player().getY() + HEIGHT, z2).color(1.0F, 1.0F, 1.0F, 1.0F).normal(matrix3f, 1.0F, 1.0F, 1.0F).endVertex();
            consumer.vertex(matrix4f, x2, (float) McUtils.player().getY(), z2).color(1.0F, 1.0F, 1.0F, 1.0F).normal(matrix3f, 1.0F, 1.0F, 1.0F).endVertex();
        }

        BUFFER_SOURCE.endBatch();
        poseStack.popPose();
    }
}
