package com.wynntils.features.combat;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.wynntils.core.components.Models;
import com.wynntils.core.config.Category;
import com.wynntils.core.config.ConfigCategory;
import com.wynntils.core.features.Feature;
import com.wynntils.mc.event.ItemInHandRenderEvent;
import com.wynntils.mc.event.PlayerRenderEvent;
import com.wynntils.models.gear.type.GearInfo;
import com.wynntils.models.gear.type.GearMajorId;
import com.wynntils.utils.mc.ComponentUtils;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.buffered.CustomRenderType;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.util.List;

@ConfigCategory(Category.COMBAT)
public class RangeVisualizerFeature extends Feature {

    private static final MultiBufferSource.BufferSource BUFFER_SOURCE = MultiBufferSource.immediate(new BufferBuilder(256));
    private static final int SEGMENTS = 128;
    private static final float HEIGHT = 0.1f;

    /**
     * Deals with rendering the circle for other party members only.
     */
    @SubscribeEvent
    public void onPlayerRender(PlayerRenderEvent e) {
        if (!Models.Player.isLocalPlayer(e.getPlayer())) return; // Don't render for ghost/npc
        String playerName = ComponentUtils.getUnformatted(e.getPlayer().getName());
        if (ComponentUtils.getUnformatted(McUtils.player().getName()).equals(playerName)) {
            if (McUtils.mc().screen instanceof InventoryScreen) return; // Don't render for preview in inventory
        }
        if (Models.Party.isInParty() && !Models.Party.getPartyMembers().contains(playerName)) return; // Other players must be in party

        // We are getting the item info the same way as GearViewerScreen since we care about other people's items
        String gearName = ComponentUtils.getUnformatted(e.getPlayer().getMainHandItem().getHoverName());
        GearInfo gearInfo = Models.Gear.getGearInfoFromApiName(gearName);
        if (gearInfo == null) return;
        List<GearMajorId> majorIds = gearInfo.fixedStats().majorIds();
        if (majorIds.isEmpty()) return;

        // Major IDs that we can visualize:
        // Taunt (12 blocks)
        // Saviour's Sacrifice (8 blocks)?
        // Heart of the Pack (8 blocks)?
        // Guardian (8 blocks)?
        // Marked with a ? needs additional confirmation

        for (GearMajorId majorId : majorIds) {
            if (majorId.name().equals("Taunt")) {
                renderCircleWithRadius(e.getPoseStack(), 12, e.getPlayer().position());
            } else if (majorId.name().equals("Saviour's Sacrifice") ||
                    majorId.name().equals("Heart of the Pack") ||
                    majorId.name().equals("Guardian")) {
                renderCircleWithRadius(e.getPoseStack(), 8, e.getPlayer().position());
            }
        }
    }

    @SubscribeEvent
    public void onItemInHandRender(ItemInHandRenderEvent e) {
        renderFirstPersonCircleWithRadius(e.getPoseStack(), 8);
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
    private void renderCircleWithRadius(PoseStack poseStack, int radius, Position position) {
        RenderSystem.disableCull(); // Circle must be rendered on both sides, otherwise it will be invisible when looking at it from the outside
        poseStack.pushPose();
        poseStack.translate(-position.x(), -position.y(), -position.z());
        VertexConsumer consumer = BUFFER_SOURCE.getBuffer(CustomRenderType.POSITION_COLOR_QUAD);

        Matrix4f matrix4f = poseStack.last().pose();
        Matrix3f matrix3f = poseStack.last().normal();
        double angleStep = 2 * Math.PI / SEGMENTS;
        double angle = 0;
        for (int i = 0; i < SEGMENTS; i++) {
            float x = (float) (position.x() + Math.sin(angle) * radius);
            float z = (float) (position.z() + Math.cos(angle) * radius);
            consumer.vertex(matrix4f, x, (float) position.y(), z).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
            consumer.vertex(matrix4f, x, (float) position.y() + HEIGHT, z).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
            angle += angleStep;
            float x2 = (float) (position.x() + Math.sin(angle) * radius);
            float z2 = (float) (position.z() + Math.cos(angle) * radius);
            consumer.vertex(matrix4f, x2, (float) position.y() + HEIGHT, z2).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
            consumer.vertex(matrix4f, x2, (float) position.y(), z2).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
        }

        BUFFER_SOURCE.endBatch();
        poseStack.popPose();
        RenderSystem.enableCull();
    }

    private void renderFirstPersonCircleWithRadius(PoseStack poseStack, int radius) {
        RenderSystem.disableCull();
        poseStack.pushPose();
        poseStack.translate(-McUtils.player().getX(), -McUtils.player().getY(), -McUtils.player().getZ());

        VertexConsumer consumer = BUFFER_SOURCE.getBuffer(CustomRenderType.POSITION_COLOR_QUAD);
        Matrix4f matrix4f = poseStack.last().pose();
        Matrix3f matrix3f = poseStack.last().normal();
        double angleStep = 2 * Math.PI / SEGMENTS;
        double angle = 0;

        for (int i = 0; i < SEGMENTS; i++) {
            float x = (float) (Math.sin(angle) * radius);
            float z = (float) (Math.cos(angle) * radius);
            consumer.vertex(matrix4f, x, (float) McUtils.player().getY(), z).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
            consumer.vertex(matrix4f, x, (float) McUtils.player().getY() + HEIGHT, z).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
            angle += angleStep;
            float x2 = (float) (Math.sin(angle) * radius);
            float z2 = (float) (Math.cos(angle) * radius);
            consumer.vertex(matrix4f, x2, (float) McUtils.player().getY() + HEIGHT, z2).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
            consumer.vertex(matrix4f, x2, (float) McUtils.player().getY(), z2).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
        }

        BUFFER_SOURCE.endBatch();
        poseStack.popPose();
        RenderSystem.enableCull();
    }
}
