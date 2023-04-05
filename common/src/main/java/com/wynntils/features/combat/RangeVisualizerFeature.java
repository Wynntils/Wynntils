package com.wynntils.features.combat;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.wynntils.core.components.Models;
import com.wynntils.core.config.Category;
import com.wynntils.core.config.ConfigCategory;
import com.wynntils.core.features.Feature;
import com.wynntils.mc.event.PlayerRenderEvent;
import com.wynntils.models.gear.type.GearType;
import com.wynntils.models.items.items.game.GearItem;
import com.wynntils.utils.mc.ComponentUtils;
import com.wynntils.utils.mc.McUtils;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.util.Optional;

@ConfigCategory(Category.COMBAT)
public class RangeVisualizerFeature extends Feature {

    private static final MultiBufferSource.BufferSource BUFFER_SOURCE = MultiBufferSource.immediate(new BufferBuilder(256));
    private static final int SEGMENTS = 128;

    @SubscribeEvent
    public void onPlayerRender(PlayerRenderEvent e) {
        if (ComponentUtils.getUnformatted(e.getPlayer().getName()).equals("§\u0001")) return; // Weird fake player name
        Optional<GearItem> item = Models.Item.asWynnItem(e.getPlayer().getItemInHand(InteractionHand.MAIN_HAND), GearItem.class);
        if (item.isEmpty()) return;
        GearItem gearItem = item.get();

        // Major IDs that we can visualize:
        // Taunt (12 blocks)
        // Magnet (8 blocks)
        // Saviour's Sacrifice (8 blocks)?
        // Heart of the pack (8 blocks)?
        // Guardian (8 blocks)?
        // Marked with a ? needs additional confirmation

        GearType gearType = item.get().getGearType();
        renderCircleWithRadius(e.getPoseStack(), 8);
    }

    // renders circle around player with radius
    private void renderCircleWithRadius(PoseStack poseStack, int radius) {
        poseStack.pushPose();
        poseStack.translate(-McUtils.player().getX(), -McUtils.player().getY(), -McUtils.player().getZ());
        VertexConsumer consumer = BUFFER_SOURCE.getBuffer(RenderType.LINE_STRIP);


        double angleStep = 2 * Math.PI / SEGMENTS;
        double angle = 0;
        for (int i = 0; i < SEGMENTS; i++) {
            Matrix4f matrix4f = poseStack.last().pose();
            Matrix3f matrix3f = poseStack.last().normal();
            float x = (float) (McUtils.player().getX() + Math.sin(angle) * radius);
            float z = (float) (McUtils.player().getZ() + Math.cos(angle) * radius);
            consumer.vertex(matrix4f, x, (float) McUtils.player().getY(), z).color(1, 1, 1, 1).normal(matrix3f, 0, -1, 0).endVertex();
            angle += angleStep;
            float x2 = (float) (McUtils.player().getX() + Math.sin(angle) * radius);
            float z2 = (float) (McUtils.player().getZ() + Math.cos(angle) * radius);
            consumer.vertex(matrix4f, x2, (float) McUtils.player().getY(), z2).color(0, 1, 0, 1).normal(matrix3f, 0, -1, 0).endVertex();

            // debug
            //LevelRenderer.renderLineBox(poseStack, consumer, x, McUtils.player().getY(), z, x2, McUtils.player().getY() + 5, z2, 1, 1, 1, 1, 1, 1, 1);
            // why doesn't this part render???? it's the EXACT same as the single line above?????
            matrix4f = poseStack.last().pose();
            matrix3f = poseStack.last().normal();
            float minX = x;
            float minY = (float) McUtils.player().getY();
            float minZ = z;
            float maxX = x2;
            float maxY = (float) (McUtils.player().getY() + 5);
            float maxZ = z2;
            consumer.vertex(matrix4f, minX, minY, minZ).color(1, 1, 1, 1).normal(matrix3f, 1.0F, 0.0F, 0.0F).endVertex();
            consumer.vertex(matrix4f, maxX, minY, minZ).color(1, 1, 1, 1).normal(matrix3f, 1.0F, 0.0F, 0.0F).endVertex();
            consumer.vertex(matrix4f, minX, minY, minZ).color(1, 1, 1, 1).normal(matrix3f, 0.0F, 1.0F, 0.0F).endVertex();
            consumer.vertex(matrix4f, minX, maxY, minZ).color(1, 1, 1, 1).normal(matrix3f, 0.0F, 1.0F, 0.0F).endVertex();
            consumer.vertex(matrix4f, minX, minY, minZ).color(1, 1, 1, 1).normal(matrix3f, 0.0F, 0.0F, 1.0F).endVertex();
            consumer.vertex(matrix4f, minX, minY, maxZ).color(1, 1, 1, 1).normal(matrix3f, 0.0F, 0.0F, 1.0F).endVertex();
            consumer.vertex(matrix4f, maxX, minY, minZ).color(1, 1, 1, 1).normal(matrix3f, 0.0F, 1.0F, 0.0F).endVertex();
            consumer.vertex(matrix4f, maxX, maxY, minZ).color(1, 1, 1, 1).normal(matrix3f, 0.0F, 1.0F, 0.0F).endVertex();
            consumer.vertex(matrix4f, maxX, maxY, minZ).color(1, 1, 1, 1).normal(matrix3f, -1.0F, 0.0F, 0.0F).endVertex();
            consumer.vertex(matrix4f, minX, maxY, minZ).color(1, 1, 1, 1).normal(matrix3f, -1.0F, 0.0F, 0.0F).endVertex();
            consumer.vertex(matrix4f, minX, maxY, minZ).color(1, 1, 1, 1).normal(matrix3f, 0.0F, 0.0F, 1.0F).endVertex();
            consumer.vertex(matrix4f, minX, maxY, maxZ).color(1, 1, 1, 1).normal(matrix3f, 0.0F, 0.0F, 1.0F).endVertex();
            consumer.vertex(matrix4f, minX, maxY, maxZ).color(1, 1, 1, 1).normal(matrix3f, 0.0F, -1.0F, 0.0F).endVertex();
            consumer.vertex(matrix4f, minX, minY, maxZ).color(1, 1, 1, 1).normal(matrix3f, 0.0F, -1.0F, 0.0F).endVertex();
            consumer.vertex(matrix4f, minX, minY, maxZ).color(1, 1, 1, 1).normal(matrix3f, 1.0F, 0.0F, 0.0F).endVertex();
            consumer.vertex(matrix4f, maxX, minY, maxZ).color(1, 1, 1, 1).normal(matrix3f, 1.0F, 0.0F, 0.0F).endVertex();
            consumer.vertex(matrix4f, maxX, minY, maxZ).color(1, 1, 1, 1).normal(matrix3f, 0.0F, 0.0F, -1.0F).endVertex();
            consumer.vertex(matrix4f, maxX, minY, minZ).color(1, 1, 1, 1).normal(matrix3f, 0.0F, 0.0F, -1.0F).endVertex();
            consumer.vertex(matrix4f, minX, maxY, maxZ).color(1, 1, 1, 1).normal(matrix3f, 1.0F, 0.0F, 0.0F).endVertex();
            consumer.vertex(matrix4f, maxX, maxY, maxZ).color(1, 1, 1, 1).normal(matrix3f, 1.0F, 0.0F, 0.0F).endVertex();
            consumer.vertex(matrix4f, maxX, minY, maxZ).color(1, 1, 1, 1).normal(matrix3f, 0.0F, 1.0F, 0.0F).endVertex();
            consumer.vertex(matrix4f, maxX, maxY, maxZ).color(1, 1, 1, 1).normal(matrix3f, 0.0F, 1.0F, 0.0F).endVertex();
            consumer.vertex(matrix4f, maxX, maxY, minZ).color(1, 1, 1, 1).normal(matrix3f, 0.0F, 0.0F, 1.0F).endVertex();
            consumer.vertex(matrix4f, maxX, maxY, maxZ).color(1, 1, 1, 1).normal(matrix3f, 0.0F, 0.0F, 1.0F).endVertex();
        }

        BUFFER_SOURCE.endBatch();
        poseStack.popPose();
    }
}
