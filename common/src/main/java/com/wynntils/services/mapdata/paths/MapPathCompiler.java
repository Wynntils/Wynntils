/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata.paths;

import com.wynntils.services.mapdata.attributes.resolving.ResolvedMapAttributes;
import com.wynntils.services.mapdata.features.type.MapPath;
import com.wynntils.services.mapdata.paths.CompiledMapPath.Note;
import com.wynntils.utils.MathUtils;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.util.BoundedFloatFunction;
import net.minecraft.util.CubicSpline;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;

public final class MapPathCompiler {
    public static final int SAMPLES_PER_ARROW = 10;

    public static CompiledMapPath compile(MapPath feature, ResolvedMapAttributes attributes) {
        PathPoints raw = new PathPoints(feature.getPath());
        Set<BlockPos> highlights = new HashSet<>();
        feature.getHighlights().forEach(p -> highlights.add(BlockPos.containing(p)));

        Long2ObjectMap<List<ColoredPath>> points = generatePointsByChunk(raw, attributes);
        Long2ObjectMap<Set<BlockPos>> highlightChunks = getHighlights(highlights);
        Long2ObjectMap<List<Note>> notes = getNotes(feature.getNotes());
        LongSet chunks = new LongOpenHashSet(points.keySet());
        chunks.addAll(highlightChunks.keySet());
        chunks.addAll(notes.keySet());

        return new CompiledMapPath(feature, points, highlightChunks, notes, chunks);
    }

    private static List<PathPoints> sample(PathPoints raw, float sampleRate) {
        List<PathPoints> positions = new ArrayList<>();
        PathPoints currentPositions = new PathPoints(new ArrayList<>());
        positions.add(currentPositions);
        for (Vec3 element : raw.points()) {
            if (!currentPositions.points().isEmpty()
                    && currentPositions.points().getLast().distanceTo(element) >= 32) {
                currentPositions = new PathPoints(new ArrayList<>());
                positions.add(currentPositions);
            }

            if (currentPositions.points().isEmpty()
                    || !currentPositions.points().getLast().equals(element)) {
                currentPositions.points().add(element);
            }
        }

        List<PathPoints> result = new ArrayList<>();
        for (PathPoints current : positions) {
            if (current.points().size() < 2) continue;

            float distance = 0f;
            CubicSpline.Builder<Float, BoundedFloatFunction<Float>> builderX =
                    CubicSpline.builder(BoundedFloatFunction.IDENTITY);
            CubicSpline.Builder<Float, BoundedFloatFunction<Float>> builderY =
                    CubicSpline.builder(BoundedFloatFunction.IDENTITY);
            CubicSpline.Builder<Float, BoundedFloatFunction<Float>> builderZ =
                    CubicSpline.builder(BoundedFloatFunction.IDENTITY);
            for (int i = 0; i < current.points().size(); i++) {
                Vec3 position = current.points().get(i);
                if (i > 0) {
                    distance += current.points().get(i - 1).distanceTo(position);
                }

                float slopeX = 0f;
                float slopeY = 0f;
                float slopeZ = 0f;
                if (i < current.points().size() - 1) {
                    Vec3 next = current.points().get(i + 1);
                    slopeX = (float) ((next.x - position.x) / position.distanceTo(next));
                    slopeY = (float) ((next.y - position.y) / position.distanceTo(next));
                    slopeZ = (float) ((next.z - position.z) / position.distanceTo(next));
                }

                builderX.addPoint(distance, (float) position.x, slopeX);
                builderY.addPoint(distance, (float) position.y, slopeY);
                builderZ.addPoint(distance, (float) position.z, slopeZ);
            }

            CubicSpline<Float, BoundedFloatFunction<Float>> splineX = builderX.build();
            CubicSpline<Float, BoundedFloatFunction<Float>> splineY = builderY.build();
            CubicSpline<Float, BoundedFloatFunction<Float>> splineZ = builderZ.build();

            PathPoints newResult = new PathPoints(new ArrayList<>());
            for (float i = 0f; i < distance; i += (1f / sampleRate)) {
                newResult.points().add(new Vec3(splineX.apply(i), splineY.apply(i), splineZ.apply(i)));
            }

            newResult.points().add(current.points().getLast());
            result.add(newResult);
        }

        return result;
    }

    private static Long2ObjectMap<List<ColoredPath>> generatePointsByChunk(
            PathPoints raw, ResolvedMapAttributes attributes) {
        float sampleRate = SAMPLES_PER_ARROW;

        List<List<Vec3>> sampled =
                sample(raw, sampleRate).stream().map(PathPoints::points).toList();
        List<Vec3> positions = sampled.stream().flatMap(List::stream).toList();

        ColoredPath locationsList = new ColoredPath(new ArrayList<>());

        int color = attributes.pathColor().asInt();
        for (int i = 0; i < positions.size(); i++) {
            locationsList.points().add(new ColoredPosition(positions.get(i), color, i));
        }

        ColoredPath lastLocationList = null;
        Long2ObjectMap<List<ColoredPath>> sampleByChunk = new Long2ObjectOpenHashMap<>();
        ChunkPos lastChunkPos = null;
        for (int i = 0; i < locationsList.points().size(); i++) {
            Vec3 position = locationsList.points().get(i).position();
            ChunkPos currentChunkPos =
                    new ChunkPos(MathUtils.floor(position.x()) >> 4, MathUtils.floor(position.z()) >> 4);
            if (!currentChunkPos.equals(lastChunkPos)) {
                if (lastChunkPos != null
                        && position.distanceTo(locationsList.points().get(i - 1).position()) < 32) {
                    lastLocationList.points().add(locationsList.points().get(i));
                }

                lastChunkPos = currentChunkPos;
                sampleByChunk.putIfAbsent(ChunkPos.asLong(currentChunkPos.x, currentChunkPos.z), new ArrayList<>());
                lastLocationList = new ColoredPath(new ArrayList<>());
                sampleByChunk
                        .get(ChunkPos.asLong(currentChunkPos.x, currentChunkPos.z))
                        .add(lastLocationList);
            }

            lastLocationList.points().add(locationsList.points().get(i));
        }

        return sampleByChunk;
    }

    private static Long2ObjectMap<Set<BlockPos>> getHighlights(Set<BlockPos> highlights) {
        Long2ObjectMap<Set<BlockPos>> result = new Long2ObjectOpenHashMap<>();
        for (BlockPos pos : highlights) {
            Set<BlockPos> addTo = result.computeIfAbsent(new ChunkPos(pos).toLong(), (chunk) -> new HashSet<>());
            addTo.add(pos);
        }

        return result;
    }

    private static Long2ObjectMap<List<Note>> getNotes(List<PathNote> notes) {
        Long2ObjectMap<List<Note>> result = new Long2ObjectOpenHashMap<>();
        for (PathNote note : notes) {
            ChunkPos chunk = new ChunkPos(BlockPos.containing(note.position()));
            List<Note> notesChunk = result.computeIfAbsent(chunk.toLong(), (chunkPos) -> new ArrayList<>());
            notesChunk.add(new Note(note.position(), note.component()));
        }

        return result;
    }
}
