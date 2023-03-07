/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.lootruns;

import com.wynntils.features.LootrunFeature;
import com.wynntils.models.lootruns.type.ColoredPath;
import com.wynntils.models.lootruns.type.ColoredPoint;
import com.wynntils.models.lootruns.type.LootrunNote;
import com.wynntils.models.lootruns.type.LootrunPath;
import com.wynntils.utils.MathUtils;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.util.CubicSpline;
import net.minecraft.util.FastColor;
import net.minecraft.util.ToFloatFunction;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;

public final class LootrunCompiler {
    private static final List<Integer> COLORS = List.of(
            ChatFormatting.RED.getColor(),
            ChatFormatting.GOLD.getColor(),
            ChatFormatting.YELLOW.getColor(),
            ChatFormatting.GREEN.getColor(),
            ChatFormatting.BLUE.getColor(),
            0x3f00ff,
            ChatFormatting.DARK_PURPLE.getColor());

    public static LootrunInstance compile(LootrunUncompiled uncompiled, boolean recording) {
        Long2ObjectMap<List<ColoredPath>> points = generatePointsByChunk(uncompiled.path(), recording);
        Long2ObjectMap<Set<BlockPos>> chests = getChests(uncompiled.chests());
        Long2ObjectMap<List<LootrunNote>> notes = getNotes(uncompiled.notes());

        String lootrunName = getLootrunName(uncompiled, recording);
        return new LootrunInstance(lootrunName, uncompiled.path(), points, chests, notes);
    }

    private static String getLootrunName(LootrunUncompiled uncompiled, boolean recording) {
        if (recording) return "recorded_lootrun";
        if (uncompiled.file() == null) return "lootrun";

        return uncompiled.file().getName().replace(".json", "");
    }

    private static List<LootrunPath> sample(LootrunPath raw, float sampleRate) {
        List<LootrunPath> vec3s = new ArrayList<>();
        LootrunPath currentVec3s = new LootrunPath(new ArrayList<>());
        vec3s.add(currentVec3s);
        for (Vec3 element : raw.points()) {
            if (!currentVec3s.points().isEmpty()
                    && currentVec3s
                                    .points()
                                    .get(currentVec3s.points().size() - 1)
                                    .distanceTo(element)
                            >= 32) {
                currentVec3s = new LootrunPath(new ArrayList<>());
                vec3s.add(currentVec3s);
            }
            currentVec3s.points().add(element);
        }

        List<LootrunPath> result = new ArrayList<>();
        for (LootrunPath current : vec3s) {
            float distance = 0f;
            CubicSpline.Builder<Float, ToFloatFunction<Float>> builderX = CubicSpline.builder(ToFloatFunction.IDENTITY);
            CubicSpline.Builder<Float, ToFloatFunction<Float>> builderY = CubicSpline.builder(ToFloatFunction.IDENTITY);
            CubicSpline.Builder<Float, ToFloatFunction<Float>> builderZ = CubicSpline.builder(ToFloatFunction.IDENTITY);
            for (int i = 0; i < current.points().size(); i++) {
                Vec3 vec3 = current.points().get(i);
                if (i > 0) {
                    distance += current.points().get(i - 1).distanceTo(vec3);
                }

                float slopeX = 0f;
                float slopeY = 0f;
                float slopeZ = 0f;
                if (i < current.points().size() - 1) {
                    Vec3 next = current.points().get(i + 1);
                    slopeX = (float) ((next.x - vec3.x) / vec3.distanceTo(next));
                    slopeY = (float) ((next.y - vec3.y) / vec3.distanceTo(next));
                    slopeZ = (float) ((next.z - vec3.z) / vec3.distanceTo(next));
                }
                builderX.addPoint(distance, (float) vec3.x, slopeX);
                builderY.addPoint(distance, (float) vec3.y, slopeY);
                builderZ.addPoint(distance, (float) vec3.z, slopeZ);
            }
            CubicSpline<Float, ToFloatFunction<Float>> splineX = builderX.build();
            CubicSpline<Float, ToFloatFunction<Float>> splineY = builderY.build();
            CubicSpline<Float, ToFloatFunction<Float>> splineZ = builderZ.build();

            LootrunPath newResult = new LootrunPath(new ArrayList<>());
            for (float i = 0f; i < distance; i += (1f / sampleRate)) {
                newResult.points().add(new Vec3(splineX.apply(i), splineY.apply(i), splineZ.apply(i)));
            }
            result.add(newResult);
        }
        return result;
    }

    private static Long2ObjectMap<List<ColoredPath>> generatePointsByChunk(LootrunPath raw, boolean recording) {
        float sampleRate = 10f;

        List<List<Vec3>> sampled =
                sample(raw, sampleRate).stream().map(LootrunPath::points).toList();
        List<Vec3> vec3s = sampled.stream().flatMap(List::stream).toList();

        ColoredPath locationsList = new ColoredPath(new ArrayList<>());

        Iterator<Integer> colorIterator = COLORS.iterator();
        Integer nextColor = colorIterator.next();
        Integer currentColor = nextColor;

        float differenceRed = 0;
        float differenceGreen = 0;
        float differenceBlue = 0;

        for (int i = 0; i < vec3s.size(); i++) {
            Vec3 location = vec3s.get(i);

            if (LootrunFeature.INSTANCE.rainbowLootRun && !recording) {
                int cycleDistance = LootrunFeature.INSTANCE.cycleDistance;
                int cycle = 10 * cycleDistance;
                int parts = i % cycle;
                float done = (float) parts / (float) cycle;

                int usedColor;
                if (parts == 0) {
                    currentColor = nextColor;
                    if (!colorIterator.hasNext()) {
                        colorIterator = COLORS.iterator();
                    }
                    nextColor = colorIterator.next();
                    differenceRed = (float) (FastColor.ARGB32.red(nextColor) - FastColor.ARGB32.red(currentColor));
                    differenceGreen =
                            (float) (FastColor.ARGB32.green(nextColor) - FastColor.ARGB32.green(currentColor));
                    differenceBlue = (float) (FastColor.ARGB32.blue(nextColor) - FastColor.ARGB32.blue(currentColor));
                    usedColor = currentColor;
                } else {
                    usedColor = currentColor;
                    usedColor += (0x010000) * (int) (differenceRed * done);
                    usedColor += (0x000100) * (int) (differenceGreen * done);
                    usedColor += (int) (differenceBlue * done);
                }

                locationsList.points().add(new ColoredPoint(location, usedColor | 0xff000000));
            } else {
                locationsList
                        .points()
                        .add(new ColoredPoint(
                                location,
                                recording
                                        ? LootrunFeature.INSTANCE.recordingPathColor.asInt()
                                        : LootrunFeature.INSTANCE.activePathColor.asInt()));
            }
        }

        ColoredPath lastLocationList = null;
        Long2ObjectMap<List<ColoredPath>> sampleByChunk = new Long2ObjectOpenHashMap<>();
        ChunkPos lastChunkPos = null;
        for (int i = 0; i < locationsList.points().size(); i++) {
            Vec3 location = locationsList.points().get(i).vec3();
            ChunkPos currentChunkPos =
                    new ChunkPos(MathUtils.floor(location.x()) >> 4, MathUtils.floor(location.z()) >> 4);
            if (!currentChunkPos.equals(lastChunkPos)) {
                if (lastChunkPos != null
                        && location.distanceTo(locationsList.points().get(i - 1).vec3()) < 32) {
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

    private static Long2ObjectMap<Set<BlockPos>> getChests(Set<BlockPos> chests) {
        Long2ObjectMap<Set<BlockPos>> result = new Long2ObjectOpenHashMap<>();
        for (BlockPos pos : chests) {
            Set<BlockPos> addTo = result.computeIfAbsent(new ChunkPos(pos).toLong(), (chunk) -> new HashSet<>());
            addTo.add(pos);
        }
        return result;
    }

    private static Long2ObjectMap<List<LootrunNote>> getNotes(List<LootrunNote> notes) {
        Long2ObjectMap<List<LootrunNote>> result = new Long2ObjectOpenHashMap<>();
        for (LootrunNote note : notes) {
            ChunkPos chunk = new ChunkPos(new BlockPos(note.position()));
            List<LootrunNote> notesChunk = result.computeIfAbsent(chunk.toLong(), (chunkPos) -> new ArrayList<>());
            notesChunk.add(note);
        }
        return result;
    }
}
