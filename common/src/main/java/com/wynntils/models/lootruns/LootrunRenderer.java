/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.lootruns;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.wynntils.features.statemanaged.LootrunFeature;
import com.wynntils.gui.render.buffered.CustomRenderType;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.models.lootruns.type.BlockValidness;
import com.wynntils.models.lootruns.type.ColoredPath;
import com.wynntils.models.lootruns.type.ColoredPoint;
import com.wynntils.models.lootruns.type.LootrunNote;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.minecraft.client.Camera;
import net.minecraft.client.gui.Font;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.util.FastColor;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public final class LootrunRenderer {
    private static final MultiBufferSource.BufferSource BUFFER_SOURCE =
            MultiBufferSource.immediate(new BufferBuilder(256));

    public static void renderLootrun(PoseStack poseStack, LootrunInstance lootrun, int color) {
        if (lootrun == null) {
            return;
        }

        ClientLevel level = McUtils.mc().level;

        if (level == null) {
            return;
        }

        poseStack.pushPose();

        Camera camera = McUtils.mc().gameRenderer.getMainCamera();

        poseStack.translate(-camera.getPosition().x, -camera.getPosition().y, -camera.getPosition().z);

        var points = lootrun.points();
        int renderDistance = McUtils.options().renderDistance().get();
        BlockPos pos = camera.getBlockPosition();
        ChunkPos origin = new ChunkPos(pos);

        for (int i = 0; i <= renderDistance; i++) {
            for (int j = 0; j <= renderDistance; j++) {
                int x = j + origin.x - (renderDistance / 2);
                int z = i + origin.z - (renderDistance / 2);
                ChunkPos chunk = new ChunkPos(x, z);
                if (!level.hasChunk(chunk.x, chunk.z)) {
                    continue;
                }

                long chunkLong = chunk.toLong();

                if (points.containsKey(chunkLong)) {
                    renderPoints(poseStack, points, chunkLong);
                }

                if (lootrun.chests().containsKey(chunkLong)) {
                    renderChests(poseStack, lootrun, color, chunkLong);
                }

                if (LootrunFeature.INSTANCE.showNotes && lootrun.notes().containsKey(chunkLong)) {
                    renderNotes(poseStack, lootrun, color, chunkLong);
                }
            }
        }

        poseStack.popPose();
    }

    private static void renderNotes(PoseStack poseStack, LootrunInstance lootrun, int color, long chunkLong) {
        List<LootrunNote> notes = lootrun.notes().get(chunkLong);

        Font font = McUtils.mc().font;

        for (LootrunNote note : notes) {
            Vec3 location = note.position();
            poseStack.pushPose();
            poseStack.translate(location.x, location.y + 2, location.z);
            poseStack.mulPose(McUtils.mc().gameRenderer.getMainCamera().rotation());
            poseStack.scale(-0.025f, -0.025f, 0.025f);
            Matrix4f pose = poseStack.last().pose();
            List<FormattedCharSequence> lines = font.split(note.component(), 200);
            int offsetY = -(font.lineHeight * lines.size()) / 2;
            for (FormattedCharSequence line : lines) {
                int offsetX = -font.width(line) / 2;
                font.drawInBatch(
                        line, offsetX, offsetY, color, false, pose, BUFFER_SOURCE, false, 0x80000000, 0xf000f0);
                offsetY += font.lineHeight + 2;
            }
            poseStack.popPose();
        }
    }

    private static void renderChests(PoseStack poseStack, LootrunInstance lootrun, int color, long chunkLong) {
        VertexConsumer consumer = BUFFER_SOURCE.getBuffer(RenderType.lines());
        Set<BlockPos> chests = lootrun.chests().get(chunkLong);

        float red = ((float) FastColor.ARGB32.red(color)) / 255;
        float green = ((float) FastColor.ARGB32.green(color)) / 255;
        float blue = ((float) FastColor.ARGB32.blue(color)) / 255;

        for (BlockPos chest : chests) {
            LevelRenderer.renderLineBox(poseStack, consumer, new AABB(chest), red, green, blue, 1f);
        }

        BUFFER_SOURCE.endBatch();
    }

    private static void renderPoints(PoseStack poseStack, Long2ObjectMap<List<ColoredPath>> points, long chunkLong) {
        List<ColoredPath> locations = points.get(chunkLong);

        Level level = McUtils.mc().level;
        if (level == null) return;

        renderNonTexturedLootrunPoints(poseStack, locations, level, CustomRenderType.LOOTRUN_LINE);
    }

    private static void renderNonTexturedLootrunPoints(
            PoseStack poseStack, List<ColoredPath> locations, Level level, RenderType renderType) {
        for (ColoredPath locationsInRoute : locations) {
            VertexConsumer consumer = BUFFER_SOURCE.getBuffer(renderType);
            Matrix4f lastMatrix = poseStack.last().pose();
            boolean sourceBatchEnded = false;

            ColoredPath toRender = new ColoredPath(new ArrayList<>());

            boolean pauseDraw = false;
            BlockPos lastBlockPos = null;

            for (ColoredPoint point : locationsInRoute.points()) {
                BlockPos blockPos = new BlockPos(point.vec3());

                if (blockPos.equals(lastBlockPos)) { // Do not recalculate block validness
                    if (!toRender.points().isEmpty()) {
                        toRender.points().add(point);
                    }
                } else {
                    BlockValidness blockValidness = BlockValidness.checkBlockValidness(level, point);

                    if (blockValidness == BlockValidness.VALID) {
                        pauseDraw = false;
                        if (sourceBatchEnded) {
                            consumer = BUFFER_SOURCE.getBuffer(renderType);
                            sourceBatchEnded = false;
                        }
                        renderQueuedPoints(consumer, lastMatrix, toRender);
                        toRender.points().clear();
                    } else if (blockValidness == BlockValidness.HAS_BARRIER) {
                        pauseDraw = true;
                        toRender.points().clear();
                    } else {
                        pauseDraw = false;
                        toRender.points().add(point);
                        continue;
                    }
                }

                lastBlockPos = blockPos;

                if (!pauseDraw) {
                    renderPoint(consumer, lastMatrix, point);
                } else if (!sourceBatchEnded) {
                    BUFFER_SOURCE.endBatch();
                    sourceBatchEnded = true;
                }
            }
            if (!sourceBatchEnded) {
                renderQueuedPoints(consumer, lastMatrix, toRender);
                BUFFER_SOURCE.endBatch();
            }
        }
    }

    private static void renderQueuedPoints(VertexConsumer consumer, Matrix4f lastMatrix, ColoredPath toRender) {
        for (ColoredPoint location : toRender.points()) {
            renderPoint(consumer, lastMatrix, location);
        }
    }

    private static void renderPoint(VertexConsumer consumer, Matrix4f lastMatrix, ColoredPoint location) {
        Vec3 rawLocation = location.vec3();
        int pathColor = location.color();
        consumer.vertex(lastMatrix, (float) rawLocation.x, (float) rawLocation.y, (float) rawLocation.z)
                .color(pathColor)
                .normal(0, 0, 1)
                .endVertex();
    }
}
