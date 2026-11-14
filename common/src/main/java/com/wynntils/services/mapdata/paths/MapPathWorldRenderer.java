/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.paths;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.wynntils.services.mapdata.paths.CompiledMapPath.Note;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.mc.PosUtils;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import java.util.List;
import java.util.Set;
import net.minecraft.client.Camera;
import net.minecraft.client.gui.Font;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public final class MapPathWorldRenderer {
    public static void renderPath(
            PoseStack poseStack,
            OrderedSubmitNodeCollector submitNodeStorage,
            CompiledMapPath path,
            int color,
            Identifier texture) {
        if (path == null) return;

        ClientLevel level = McUtils.mc().level;
        if (level == null) return;

        poseStack.pushPose();

        Camera camera = McUtils.mc().gameRenderer.getMainCamera();
        poseStack.translate(-camera.position().x, -camera.position().y, -camera.position().z);

        Long2ObjectMap<List<ColoredPath>> points = path.points();
        int renderDistance = McUtils.options().renderDistance().get();
        BlockPos pos = camera.blockPosition();
        ChunkPos origin = new ChunkPos(pos);

        for (long chunk : path.chunks()) {
            int chunkX = ChunkPos.getX(chunk);
            int chunkZ = ChunkPos.getZ(chunk);
            if (Math.abs(chunkX - origin.x) > renderDistance || Math.abs(chunkZ - origin.z) > renderDistance) continue;

            if (!level.hasChunk(chunkX, chunkZ)) continue;

            if (points.containsKey(chunk)) {
                renderPoints(poseStack, submitNodeStorage, points.get(chunk), level, texture);
            }

            if (path.highlights().containsKey(chunk)) {
                renderHighlights(path, color, chunk);
            }

            if (path.notes().containsKey(chunk)) {
                renderNotes(poseStack, submitNodeStorage, path, color, chunk);
            }
        }

        poseStack.popPose();
    }

    private static void renderNotes(
            PoseStack poseStack,
            OrderedSubmitNodeCollector submitNodeStorage,
            CompiledMapPath path,
            int color,
            long chunkLong) {
        List<Note> notes = path.notes().get(chunkLong);

        Font font = McUtils.mc().font;

        for (Note note : notes) {
            Position position = note.position();
            poseStack.pushPose();
            poseStack.translate(position.x(), position.y() + 2, position.z());
            poseStack.mulPose(McUtils.mc().gameRenderer.getMainCamera().rotation());
            poseStack.scale(0.025f, -0.025f, 0.025f);
            List<FormattedCharSequence> lines = font.split(note.component(), 200);
            int offsetY = -(font.lineHeight * lines.size()) / 2;
            for (FormattedCharSequence line : lines) {
                int offsetX = -font.width(line) / 2;
                submitNodeStorage.submitText(
                        poseStack,
                        offsetX,
                        offsetY,
                        line,
                        true,
                        Font.DisplayMode.NORMAL,
                        0xF000F0,
                        color,
                        0x80000000,
                        0);
                offsetY += font.lineHeight + 2;
            }

            poseStack.popPose();
        }
    }

    private static void renderHighlights(CompiledMapPath path, int color, long chunkLong) {
        Set<BlockPos> chests = path.highlights().get(chunkLong);

        for (BlockPos chest : chests) {
            // Wynncraft requested that chest highlights are not rendered on these blocks
            BlockState block = McUtils.mc().level.getBlockState(chest);
            if (block.is(Blocks.BARRIER) || block.is(Blocks.AIR)) continue;

            Gizmos.cuboid(new AABB(chest), GizmoStyle.stroke(color));
        }
    }

    private static void renderPoints(
            PoseStack poseStack,
            OrderedSubmitNodeCollector submitNodeStorage,
            List<ColoredPath> locations,
            Level level,
            Identifier texture) {
        if (texture == null) {
            renderNonTexturedPoints(poseStack, submitNodeStorage, locations, level, RenderTypes.LINES);
        } else {
            renderTexturedPoints(poseStack, submitNodeStorage, locations, level, RenderTypes.text(texture));
        }
    }

    private static void renderNonTexturedPoints(
            PoseStack poseStack,
            OrderedSubmitNodeCollector submitNodeStorage,
            List<ColoredPath> locations,
            Level level,
            RenderType renderType) {
        for (ColoredPath locationsInRoute : locations) {
            submitNodeStorage.submitCustomGeometry(poseStack, renderType, (entry, consumer) -> {
                Matrix4f matrix = entry.pose();
                BlockPos lastBlockPos = null;

                for (int i = 0; i < locationsInRoute.points().size() - 1; i++) {
                    ColoredPosition start = locationsInRoute.points().get(i);
                    ColoredPosition end = locationsInRoute.points().get(i + 1);
                    Vec3 direction = end.position().subtract(start.position());
                    if (direction.lengthSqr() == 0 || direction.lengthSqr() >= 32 * 32) continue;

                    BlockPos blockPos = PosUtils.newBlockPos(start.position());
                    if (!blockPos.equals(lastBlockPos)
                            && PathBlockValidness.checkPathBlockValidness(level, start)
                                    == PathBlockValidness.HAS_BARRIER) continue;
                    lastBlockPos = blockPos;
                    Vec3 normal = direction.normalize();
                    for (ColoredPosition point : List.of(start, end)) {
                        consumer.addVertex(
                                        matrix,
                                        (float) point.position().x(),
                                        (float) point.position().y(),
                                        (float) point.position().z())
                                .setColor(point.color())
                                .setNormal((float) normal.x(), (float) normal.y(), (float) normal.z())
                                .setLineWidth(3);
                    }
                }
            });
        }
    }

    private static void renderTexturedPoints(
            PoseStack poseStack,
            OrderedSubmitNodeCollector submitNodeStorage,
            List<ColoredPath> locations,
            Level level,
            RenderType renderType) {
        Camera camera = McUtils.mc().gameRenderer.getMainCamera();

        poseStack.pushPose();
        poseStack.translate(camera.position().x, camera.position().y, camera.position().z);

        for (ColoredPath locationsInRoute : locations) {
            submitNodeStorage.submitCustomGeometry(poseStack, renderType, (entry, consumer) -> {
                BlockPos lastBlockPos = null;
                PathBlockValidness lastValidness = PathBlockValidness.INVALID;
                for (int i = 0; i < locationsInRoute.points().size() - 1; i++) {
                    ColoredPosition point = locationsInRoute.points().get(i);
                    // Keep arrow spacing and palette progression continuous across chunk boundaries.
                    if (point.sampleIndex() % MapPathCompiler.SAMPLES_PER_ARROW != 0) continue;

                    ColoredPosition end = locationsInRoute.points().get(i + 1);
                    BlockPos blockPos = PosUtils.newBlockPos(point.position());
                    if (!blockPos.equals(lastBlockPos)) {
                        lastValidness = PathBlockValidness.checkPathBlockValidness(level, point);
                        lastBlockPos = blockPos;
                    }

                    if (lastValidness == PathBlockValidness.HAS_BARRIER) continue;

                    renderTexturedPoint(point, end, entry.pose(), consumer);
                }
            });
        }

        poseStack.popPose();
    }

    private static void renderTexturedPoint(
            ColoredPosition start, ColoredPosition end, Matrix4f matrix, VertexConsumer vertexConsumer) {
        int color = start.color();
        Vector3f camPos = McUtils.mc().gameRenderer.getMainCamera().position().toVector3f();
        Vector3f startVec = start.position().toVector3f();
        Vector3f endVec = end.position().toVector3f();
        if (startVec.equals(endVec)) return;

        // vertex position delta to starting point
        Vector3f pos1 = new Vector3f(-0.5f, 0.24f, -0.5f);
        Vector3f pos2 = new Vector3f(0.5f, 0.24f, -0.5f);
        Vector3f pos3 = new Vector3f(0.5f, 0.24f, 0.5f);
        Vector3f pos4 = new Vector3f(-0.5f, 0.24f, 0.5f);

        Vector3f direction =
                new Vector3f(endVec.x, endVec.y, endVec.z).sub(startVec).normalize();

        // rotation angle to point surface normal to end position
        // rotate the angle so the arrow point to the end position instead of surface normal
        float xAngle = (float) ((float) Math.acos(direction.y / direction.length()) - Math.PI / 2);
        float yAngle = (float) Math.atan2(direction.x, direction.z);

        Quaternionf yRot = new Quaternionf().rotateY(yAngle);
        Vector3f xRotAxis = new Vector3f(1, 0, 0).rotate(yRot);
        Quaternionf xRot = new Quaternionf().rotateAxis(xAngle, xRotAxis);

        // apply vertex rotation
        pos1.rotate(yRot).rotate(xRot);
        pos2.rotate(yRot).rotate(xRot);
        pos3.rotate(yRot).rotate(xRot);
        pos4.rotate(yRot).rotate(xRot);

        // transform position back to world space and then to position camera delta
        pos1 = pos1.add(startVec).sub(camPos);
        pos2 = pos2.add(startVec).sub(camPos);
        pos3 = pos3.add(startVec).sub(camPos);
        pos4 = pos4.add(startVec).sub(camPos);

        vertexConsumer
                .addVertex(matrix, pos1.x, pos1.y, pos1.z)
                .setUv(0, 1)
                .setColor(color)
                .setLight(LightTexture.FULL_BRIGHT);
        vertexConsumer
                .addVertex(matrix, pos2.x, pos2.y, pos2.z)
                .setUv(0, 0)
                .setColor(color)
                .setLight(LightTexture.FULL_BRIGHT);
        vertexConsumer
                .addVertex(matrix, pos3.x, pos3.y, pos3.z)
                .setUv(1, 0)
                .setColor(color)
                .setLight(LightTexture.FULL_BRIGHT);
        vertexConsumer
                .addVertex(matrix, pos4.x, pos4.y, pos4.z)
                .setUv(1, 1)
                .setColor(color)
                .setLight(LightTexture.FULL_BRIGHT);

        // The text pipeline culls back faces, so add the other side too
        vertexConsumer
                .addVertex(matrix, pos4.x, pos4.y, pos4.z)
                .setUv(1, 1)
                .setColor(color)
                .setLight(LightTexture.FULL_BRIGHT);
        vertexConsumer
                .addVertex(matrix, pos3.x, pos3.y, pos3.z)
                .setUv(1, 0)
                .setColor(color)
                .setLight(LightTexture.FULL_BRIGHT);
        vertexConsumer
                .addVertex(matrix, pos2.x, pos2.y, pos2.z)
                .setUv(0, 0)
                .setColor(color)
                .setLight(LightTexture.FULL_BRIGHT);
        vertexConsumer
                .addVertex(matrix, pos1.x, pos1.y, pos1.z)
                .setUv(0, 1)
                .setColor(color)
                .setLight(LightTexture.FULL_BRIGHT);
    }
}
