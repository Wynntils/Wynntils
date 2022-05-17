/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wc.utils.lootrun;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.features.FeatureRegistry;
import com.wynntils.features.LootrunFeature;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.wc.utils.lootrun.objects.LootrunRenderType;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Camera;
import net.minecraft.client.gui.Font;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CubicSpline;
import net.minecraft.util.FastColor;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class LootrunUtils {

    public static final File LOOTRUNS = new File(WynntilsMod.MOD_STORAGE_ROOT, "lootruns");

    private static final List<Integer> COLORS = List.of(
            ChatFormatting.RED.getColor(),
            ChatFormatting.GOLD.getColor(),
            ChatFormatting.YELLOW.getColor(),
            ChatFormatting.GREEN.getColor(),
            ChatFormatting.BLUE.getColor(),
            0x3f00ff,
            ChatFormatting.DARK_PURPLE.getColor());

    private static final int ACTIVE_COLOR = ChatFormatting.AQUA.getColor();
    private static final int RECORDING_COLOR = 0xff0000;

    private static LootrunState state = LootrunState.DISABLED;

    private static LootrunInstance lootrun = null;
    private static LootrunUncompiled uncompiled = null;
    private static LootrunInstance recordingCompiled = null;
    private static LootrunUncompiled recording = null;

    private static RecordingInformation recordingInformation = null;

    public static LootrunState getState() {
        return state;
    }

    public static void enableFeature() {
        FeatureRegistry.getFeatures().get(LootrunFeature.class).tryEnable();
    }

    public static void disableFeature() {
        FeatureRegistry.getFeatures().get(LootrunFeature.class).tryDisable();
    }

    public static void render(PoseStack poseStack) {
        renderLootrun(poseStack, lootrun, ACTIVE_COLOR);
        renderLootrun(poseStack, recordingCompiled, RECORDING_COLOR);
    }

    private static void renderLootrun(PoseStack poseStack, LootrunInstance lootrun, int color) {
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

        MultiBufferSource.BufferSource source = McUtils.mc().renderBuffers().bufferSource();
        var points = lootrun.points();
        int renderDistance = McUtils.mc().options.renderDistance;
        BlockPos pos = McUtils.mc().gameRenderer.getMainCamera().getBlockPosition();
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
                    renderPoints(poseStack, source, points, chunkLong);
                }

                if (lootrun.chests().containsKey(chunkLong)) {
                    renderChests(poseStack, lootrun, color, source, chunkLong);
                }

                if (lootrun.notes().containsKey(chunkLong)) {
                    renderNotes(poseStack, lootrun, color, source, chunkLong);
                }
            }
        }

        poseStack.popPose();
    }

    private static void renderNotes(
            PoseStack poseStack,
            LootrunInstance lootrun,
            int color,
            MultiBufferSource.BufferSource source,
            long chunkLong) {
        List<Note> notes = lootrun.notes().get(chunkLong);

        Font font = McUtils.mc().font;

        for (Note note : notes) {
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
                font.drawInBatch(line, offsetX, offsetY, color, false, pose, source, false, 0x80000000, 0xf000f0);
                offsetY += font.lineHeight + 2;
            }
            poseStack.popPose();
        }
    }

    private static void renderChests(
            PoseStack poseStack,
            LootrunInstance lootrun,
            int color,
            MultiBufferSource.BufferSource source,
            long chunkLong) {
        VertexConsumer consumer = source.getBuffer(RenderType.lines());
        Set<BlockPos> chests = lootrun.chests().get(chunkLong);

        float red = ((float) FastColor.ARGB32.red(color)) / 255;
        float green = ((float) FastColor.ARGB32.green(color)) / 255;
        float blue = ((float) FastColor.ARGB32.blue(color)) / 255;

        for (BlockPos chest : chests) {
            LevelRenderer.renderLineBox(poseStack, consumer, new AABB(chest), red, green, blue, 1f);
        }

        source.endBatch();
    }

    private static void renderPoints(
            PoseStack poseStack,
            MultiBufferSource.BufferSource source,
            Long2ObjectMap<List<ColoredPath>> points,
            long chunkLong) {
        List<ColoredPath> locations = points.get(chunkLong);

        Level level = McUtils.mc().level;
        if (level == null) return;

        for (ColoredPath locationsInRoute : locations) {
            VertexConsumer consumer = source.getBuffer(LootrunRenderType.LOOTRUN_LINE);
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
                    BlockValidness blockValidness = checkBlockValidness(level, point);

                    if (blockValidness == BlockValidness.VALID) {
                        pauseDraw = false;
                        if (sourceBatchEnded) {
                            consumer = source.getBuffer(LootrunRenderType.LOOTRUN_LINE);
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
                    source.endBatch();
                    sourceBatchEnded = true;
                }
            }
            if (!sourceBatchEnded) {
                renderQueuedPoints(consumer, lastMatrix, toRender);
                source.endBatch();
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

    private static BlockValidness checkBlockValidness(Level level, ColoredPoint point) {
        BlockValidness state = BlockValidness.INVALID;
        Iterable<BlockPos> blocks = getBlocksForPoint(point);

        for (BlockPos blockInArea : blocks) {
            BlockState blockStateInArea = level.getBlockState(blockInArea);
            if (blockStateInArea.is(Blocks.BARRIER)) {
                state = BlockValidness.HAS_BARRIER;
            } else if (blockStateInArea.getCollisionShape(level, blockInArea) != null) {
                state = BlockValidness.VALID;
                return state;
            }
        }

        return state;
    }

    private static Iterable<BlockPos> getBlocksForPoint(ColoredPoint loc) {
        BlockPos minPos = new BlockPos(loc.vec3().x - 0.3D, loc.vec3().y - 1D, loc.vec3().z - 0.3D);
        BlockPos maxPos = new BlockPos(loc.vec3().x + 0.3D, loc.vec3().y - 1D, loc.vec3().z + 0.3D);

        return BlockPos.betweenClosed(minPos, maxPos);
    }

    public static int addNote(Component text) {
        Entity root = McUtils.player().getRootVehicle();

        LootrunUncompiled current = getActiveLootrun();

        if (current == null) return 0;

        current.notes().add(new Note(root.position(), text));
        return recompileLootrun(true);
    }

    @Nullable
    public static LootrunUncompiled getActiveLootrun() {
        LootrunUncompiled instance = null;
        if (recording != null) instance = recording;
        else if (uncompiled != null) instance = uncompiled;

        return instance;
    }

    public static int recompileLootrun(boolean saveToFile) {
        if (recording != null) {
            recordingInformation.setDirty(true);
        } else if (uncompiled != null) {
            lootrun = compile(uncompiled);
            if (saveToFile && uncompiled.file() != null) {
                LootrunSaveResult lootrunSaveResult =
                        trySaveCurrentLootrun(uncompiled.file().getName());

                if (lootrunSaveResult == null) {
                    return 0;
                }

                switch (lootrunSaveResult) {
                    case SAVED -> {
                        return 1;
                    }
                    case ERROR_SAVING, ERROR_ALREADY_EXISTS -> {
                        return 0;
                    }
                }
            }
        }
        return 1;
    }

    public static LootrunInstance compile(LootrunUncompiled uncompiled) {
        Long2ObjectMap<List<ColoredPath>> points = generatePointsByChunk(uncompiled.path());
        Long2ObjectMap<Set<BlockPos>> chests = getChests(uncompiled.chests());
        Long2ObjectMap<List<Note>> notes = getNotes(uncompiled.notes());

        return new LootrunInstance(points, chests, notes);
    }

    private static List<Path> sample(Path raw, float sampleRate) {
        List<Path> vec3s = new ArrayList<>();
        Path currentVec3s = new Path(new ArrayList<>());
        vec3s.add(currentVec3s);
        for (Vec3 element : raw.points()) {
            if (!currentVec3s.points().isEmpty()
                    && currentVec3s
                                    .points()
                                    .get(currentVec3s.points().size() - 1)
                                    .distanceTo(element)
                            >= 32) {
                currentVec3s = new Path(new ArrayList<>());
                vec3s.add(currentVec3s);
            }
            currentVec3s.points().add(element);
        }

        List<Path> result = new ArrayList<>();
        for (Path current : vec3s) {
            float distance = 0f;
            CubicSpline.Builder<Float> builderX = CubicSpline.builder((value) -> value);
            CubicSpline.Builder<Float> builderY = CubicSpline.builder((value) -> value);
            CubicSpline.Builder<Float> builderZ = CubicSpline.builder((value) -> value);
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
            CubicSpline<Float> splineX = builderX.build();
            CubicSpline<Float> splineY = builderY.build();
            CubicSpline<Float> splineZ = builderZ.build();

            Path newResult = new Path(new ArrayList<>());
            for (float i = 0f; i < distance; i += (1f / sampleRate)) {
                newResult.points().add(new Vec3(splineX.apply(i), splineY.apply(i), splineZ.apply(i)));
            }
            result.add(newResult);
        }
        return result;
    }

    private static Long2ObjectMap<List<ColoredPath>> generatePointsByChunk(Path raw) {
        float sampleRate = 10f;
        List<List<Vec3>> sampled =
                sample(raw, sampleRate).stream().map(Path::points).toList();
        List<Vec3> vec3s = sampled.stream().flatMap(List::stream).toList();
        ChunkPos lastChunkPos = null;
        ColoredPath locationsList = new ColoredPath(new ArrayList<>());
        Iterator<Integer> colorIterator = COLORS.iterator();
        Integer currentColor = null;
        Integer nextColor = colorIterator.next();
        float differenceRed = 0;
        float differenceGreen = 0;
        float differenceBlue = 0;
        for (int i = 0; i < vec3s.size(); i++) {
            Vec3 location = vec3s.get(i);
            int cycleDistance = 20;
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
                differenceGreen = (float) (FastColor.ARGB32.green(nextColor) - FastColor.ARGB32.green(currentColor));
                differenceBlue = (float) (FastColor.ARGB32.blue(nextColor) - FastColor.ARGB32.blue(currentColor));
                usedColor = currentColor;
            } else {
                usedColor = currentColor;
                usedColor += (0x010000) * (int) (differenceRed * done);
                usedColor += (0x000100) * (int) (differenceGreen * done);
                usedColor += (int) (differenceBlue * done);
            }

            locationsList.points().add(new ColoredPoint(location, usedColor | 0xff000000));
        }
        ColoredPath lastLocationList = null;
        Long2ObjectMap<List<ColoredPath>> sampleByChunk = new Long2ObjectOpenHashMap<>();
        for (int i = 0; i < locationsList.points().size(); i++) {
            Vec3 location = locationsList.points().get(i).vec3();
            ChunkPos currentChunkPos = new ChunkPos(Mth.fastFloor(location.x()) >> 4, Mth.fastFloor(location.z()) >> 4);
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

    private static Long2ObjectMap<List<Note>> getNotes(List<Note> notes) {
        Long2ObjectMap<List<Note>> result = new Long2ObjectOpenHashMap<>();
        for (Note note : notes) {
            ChunkPos chunk = new ChunkPos(new BlockPos(note.position()));
            List<Note> notesChunk = result.computeIfAbsent(chunk.toLong(), (chunkPos) -> new ArrayList<>());
            notesChunk.add(note);
        }
        return result;
    }

    public static LootrunUncompiled readJson(File file, JsonObject json) {
        JsonArray points = json.getAsJsonArray("points");
        Path pointsList = new Path(new ArrayList<>());
        for (JsonElement element : points) {
            JsonObject pointJson = element.getAsJsonObject();
            Vec3 location = new Vec3(
                    pointJson.get("x").getAsDouble(),
                    pointJson.get("y").getAsDouble(),
                    pointJson.get("z").getAsDouble());
            pointsList.points().add(location);
        }
        JsonArray chestsJson = json.getAsJsonArray("chests");
        Set<BlockPos> chests = new HashSet<>();
        if (chestsJson != null) {
            for (JsonElement element : chestsJson) {
                JsonObject chestJson = element.getAsJsonObject();
                BlockPos pos = new BlockPos(
                        chestJson.get("x").getAsInt(),
                        chestJson.get("y").getAsInt(),
                        chestJson.get("z").getAsInt());
                chests.add(pos);
            }
        }
        JsonArray notesJson = json.getAsJsonArray("notes");
        List<Note> notes = new ArrayList<>();
        if (notesJson != null) {
            for (JsonElement element : notesJson) {
                JsonObject noteJson = element.getAsJsonObject();
                JsonObject positionJson = noteJson.getAsJsonObject("location");
                Vec3 position = new Vec3(
                        positionJson.get("x").getAsDouble(),
                        positionJson.get("y").getAsDouble(),
                        positionJson.get("z").getAsDouble());
                Component component = Component.Serializer.fromJson(noteJson.get("note"));
                Note note = new Note(position, component);
                notes.add(note);
            }
        }
        return new LootrunUncompiled(pointsList, chests, notes, file);
    }

    public static void clearCurrentLootrun() {
        disableFeature();
        state = LootrunState.DISABLED;
        lootrun = null;
        uncompiled = null;
        recording = null;
        recordingCompiled = null;
        recordingInformation = null;
    }

    public static void stopRecording() {
        // At this point, we already have LootrunFeature registed to the event bus
        state = LootrunState.LOADED;
        lootrun = recordingCompiled;
        uncompiled = recording;
        recording = null;
        recordingCompiled = null;
        recordingInformation = null;
    }

    public static void startRecording() {
        state = LootrunState.RECORDING;
        recording = new LootrunUncompiled(new Path(new ArrayList<>()), new HashSet<>(), new ArrayList<>(), null);
        recordingInformation = new RecordingInformation();
        enableFeature();
    }

    public static boolean tryLoadFile(String fileName) {
        String lootrun = fileName + ".json";
        File lootrunFile = new File(LOOTRUNS, lootrun);
        if (lootrunFile.exists()) {
            FileReader file;
            try {
                file = new FileReader(lootrunFile);
                JsonObject json = JsonParser.parseReader(file).getAsJsonObject();
                uncompiled = readJson(lootrunFile, json);
                LootrunUtils.lootrun = compile(uncompiled);
                state = LootrunState.LOADED;
                enableFeature();
                file.close();
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

    public static LootrunUndoResult tryUndo() {
        Vec3 position = McUtils.player().position();
        Path points = recording.path();
        Path removed = new Path(new ArrayList<>());
        boolean left = false;
        for (int i = points.points().size() - 1; i >= 0; i--) {
            if (i == 0) {
                if (left) {
                    return LootrunUndoResult.ERROR_STAND_NEAR_POINT;
                } else {
                    return LootrunUndoResult.ERROR_NOT_FAR_ENOUGH;
                }
            }

            if (points.points().get(i).distanceToSqr(position) < 4) {
                if (left) {
                    break;
                }
            } else {
                left = true;
            }

            removed.points().add(points.points().get(i));
        }

        points.points().removeAll(removed.points());
        recordingInformation.setDirty(true);
        return LootrunUndoResult.SUCCESSFUL;
    }

    public static boolean addChest(BlockPos pos) {
        LootrunUncompiled current = LootrunUtils.getActiveLootrun();
        if (current == null) return false;
        return current.chests().add(pos);
    }

    public static boolean removeChest(BlockPos pos) {
        LootrunUncompiled current = LootrunUtils.getActiveLootrun();

        if (current == null) return false;

        return current.chests().remove(pos);
    }

    public static Note deleteNoteAt(BlockPos pos) {
        LootrunUncompiled current = LootrunUtils.getActiveLootrun();

        if (current == null) return null;

        List<Note> notes = current.notes();
        for (int i = 0; i < notes.size(); i++) {
            Note note = notes.get(i);
            if (pos.equals(new BlockPos(note.position()))) {
                return notes.remove(i);
            }
        }
        return null;
    }

    public static List<Note> getCurrentNotes() {
        LootrunUncompiled activeLootrun = getActiveLootrun();

        if (activeLootrun != null) return activeLootrun.notes();

        return new ArrayList<>();
    }

    public static void setLastChestIfRecording(BlockPos pos) {
        if (state != LootrunState.RECORDING) {
            return;
        }

        recordingInformation.setLastChest(pos);
    }

    public static void addChestIfRecording() {
        if (state != LootrunState.RECORDING) {
            return;
        }

        if (recordingInformation.lastChest == null) {
            return;
        }

        recording.chests().add(recordingInformation.getLastChest());
        recordingInformation.setDirty(true);
        recordingInformation.setLastChest(null);
    }

    public static void recordMovementIfRecording() {
        if (state != LootrunState.RECORDING) {
            return;
        }

        LocalPlayer player = McUtils.mc().player;
        if (player != null) {
            Entity root = player.getRootVehicle();
            Vec3 pos = root.position();
            if (recordingInformation.getLastLocation() == null
                    || pos.distanceToSqr(recordingInformation.getLastLocation()) >= 4d) {
                recording.path().points().add(pos);
                recordingInformation.setLastLocation(pos);
                recordingInformation.setDirty(true);
            }

            if (recordingInformation.isDirty()) {
                recordingCompiled = compile(recording);
                recordingInformation.setDirty(false);
            }
        }
    }

    public static Vec3 getStartingPoint() {
        LootrunUncompiled activeLootrun = getActiveLootrun();

        if (activeLootrun == null) {
            return null;
        }

        if (activeLootrun.path == null || activeLootrun.path.points().size() == 0) return null;

        return activeLootrun.path.points().get(0);
    }

    public static LootrunSaveResult trySaveCurrentLootrun(String name) {
        LootrunUncompiled activeLootrun = getActiveLootrun();

        if (activeLootrun == null) {
            return null;
        }

        return activeLootrun.saveLootrun(name);
    }

    public enum LootrunSaveResult {
        SAVED,
        ERROR_SAVING,
        ERROR_ALREADY_EXISTS
    }

    public enum LootrunUndoResult {
        SUCCESSFUL,
        ERROR_STAND_NEAR_POINT,
        ERROR_NOT_FAR_ENOUGH
    }

    public enum LootrunState {
        RECORDING, // Lootrun is being recorded
        LOADED, // Lootrun is loaded and displayed
        DISABLED // No lootrun paths are rendered or loaded
    }

    public enum BlockValidness {
        VALID,
        HAS_BARRIER,
        INVALID
    }

    public record LootrunInstance(
            Long2ObjectMap<List<ColoredPath>> points,
            Long2ObjectMap<Set<BlockPos>> chests,
            Long2ObjectMap<List<Note>> notes) {}

    public record ColoredPoint(Vec3 vec3, int color) {}

    public static class RecordingInformation {
        private Vec3 lastLocation;
        private BlockPos lastChest;
        private boolean dirty;

        public Vec3 getLastLocation() {
            return lastLocation;
        }

        public void setLastLocation(Vec3 lastLocation) {
            this.lastLocation = lastLocation;
        }

        public BlockPos getLastChest() {
            return lastChest;
        }

        public void setLastChest(BlockPos lastChest) {
            this.lastChest = lastChest;
        }

        public boolean isDirty() {
            return dirty;
        }

        public void setDirty(boolean dirty) {
            this.dirty = dirty;
        }
    }

    public record LootrunUncompiled(Path path, Set<BlockPos> chests, List<Note> notes, File file) {

        public LootrunUncompiled(LootrunUncompiled old, File file) {
            this(old.path, old.chests, old.notes, file);
        }

        public LootrunSaveResult saveLootrun(String name) {
            try {
                File file = new File(LootrunUtils.LOOTRUNS, name + ".json");
                LootrunUtils.uncompiled = new LootrunUncompiled(this, file);

                boolean result = file.createNewFile();

                if (!result) {
                    return LootrunSaveResult.ERROR_ALREADY_EXISTS;
                }

                JsonObject json = new JsonObject();
                JsonArray points = new JsonArray();
                for (Vec3 point : this.path().points()) {
                    JsonObject pointJson = new JsonObject();
                    pointJson.addProperty("x", point.x);
                    pointJson.addProperty("y", point.y);
                    pointJson.addProperty("z", point.z);
                    points.add(pointJson);
                }
                json.add("points", points);

                JsonArray chests = new JsonArray();
                for (BlockPos chest : this.chests()) {
                    JsonObject chestJson = new JsonObject();
                    chestJson.addProperty("x", chest.getX());
                    chestJson.addProperty("y", chest.getY());
                    chestJson.addProperty("z", chest.getZ());
                    chests.add(chestJson);
                }
                json.add("chests", chests);

                JsonArray notes = new JsonArray();
                for (Note note : this.notes()) {
                    JsonObject noteJson = new JsonObject();
                    JsonObject locationJson = new JsonObject();

                    Vec3 location = note.position();
                    locationJson.addProperty("x", location.x);
                    locationJson.addProperty("y", location.y);
                    locationJson.addProperty("z", location.z);
                    noteJson.add("position", locationJson);

                    noteJson.add("note", Component.Serializer.toJsonTree(note.component()));
                    notes.add(noteJson);
                }
                json.add("notes", notes);

                json.addProperty(
                        "date",
                        DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.US)
                                .format(new Date()));
                FileWriter writer = new FileWriter(file);
                new GsonBuilder().setPrettyPrinting().create().toJson(json, writer);
                writer.close();
                return LootrunSaveResult.SAVED;
            } catch (IOException ex) {
                return LootrunSaveResult.ERROR_SAVING;
            }
        }
    }

    public record Note(Vec3 position, Component component) {}

    public record ColoredPath(List<ColoredPoint> points) {}

    public record Path(List<Vec3> points) {}
}
