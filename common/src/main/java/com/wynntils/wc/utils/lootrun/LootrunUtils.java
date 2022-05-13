/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wc.utils.lootrun;

import com.google.gson.*;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.wynntils.core.WynntilsMod;
import com.wynntils.mc.event.ClientTickEvent;
import com.wynntils.mc.event.PlayerInteractEvent;
import com.wynntils.mc.event.RenderLevelLastEvent;
import com.wynntils.mc.event.ScreenOpenedEvent;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.wc.utils.lootrun.objects.*;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectObjectImmutablePair;
import java.io.*;
import java.text.DateFormat;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Camera;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
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
import net.minecraftforge.eventbus.api.SubscribeEvent;

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

    public static LootrunUncompiled getUncompiled() {
        return uncompiled;
    }

    public static void registerToEventBus() {
        WynntilsMod.getEventBus().register(LootrunUtils.class);
    }

    public static LootrunState getState() {
        return state;
    }

    public static LootrunUncompiled getRecording() {
        return recording;
    }

    public static RecordingInformation getRecordingInformation() {
        return recordingInformation;
    }

    public static void unregisterFromEventBus() {
        WynntilsMod.getEventBus().register(LootrunUtils.class);
    }

    @SubscribeEvent
    public static void render(RenderLevelLastEvent event) {
        renderLootrun(event, lootrun, ACTIVE_COLOR);
        renderLootrun(event, recordingCompiled, RECORDING_COLOR);
    }

    private static void renderLootrun(RenderLevelLastEvent event, LootrunInstance lootrun, int color) {
        if (lootrun == null) {
            return;
        }
        event.getPoseStack().pushPose();
        Camera cam = McUtils.mc().gameRenderer.getMainCamera();
        event.getPoseStack().translate(-cam.getPosition().x, -cam.getPosition().y, -cam.getPosition().z);
        MultiBufferSource.BufferSource source = McUtils.mc().renderBuffers().bufferSource();
        var points = lootrun.points();
        int renderDistance = McUtils.mc().options.renderDistance;
        BlockPos pos = McUtils.mc().gameRenderer.getMainCamera().getBlockPosition();
        ChunkPos origin = new ChunkPos(pos);
        for (int i = 0; i < renderDistance * renderDistance; i++) {
            int x = i % renderDistance + origin.x - (renderDistance / 2);
            int z = i / renderDistance + origin.z - (renderDistance / 2);
            ChunkPos chunk = new ChunkPos(x, z);
            if (McUtils.mc().level != null && McUtils.mc().level.hasChunk(chunk.x, chunk.z)) {
                long chunkLong = chunk.toLong();
                if (points.containsKey(chunkLong)) {
                    List<List<Point>> locations = points.get(chunkLong);
                    for (List<Point> locationsInRoute : locations) {
                        VertexConsumer consumer = source.getBuffer(LootrunRenderType.LOOTRUN_LINE);
                        Matrix4f last = event.getPoseStack().last().pose();
                        boolean disabled = false;
                        List<Point> toRender = new ArrayList<>();
                        boolean disable = false;
                        BlockPos lastBlockPos = null;
                        for (Point loc : locationsInRoute) {
                            boolean pauseDraw = false;
                            BlockPos blockPos = new BlockPos(loc.vec3());

                            Level level = McUtils.mc().level;

                            if (!blockPos.equals(lastBlockPos)) {
                                BlockPos minPos =
                                        new BlockPos(loc.vec3().x() - 0.3D, loc.vec3().y - 1D, loc.vec3().z - 0.3D);
                                BlockPos maxPos =
                                        new BlockPos(loc.vec3().x + 0.3D, loc.vec3().y - 1D, loc.vec3().z + 0.3D);
                                Iterable<BlockPos> blocks = BlockPos.betweenClosed(minPos, maxPos);
                                boolean barrier = false;
                                boolean validBlock = false;
                                for (BlockPos blockInArea : blocks) {
                                    BlockState blockStateInArea = level.getBlockState(blockInArea);
                                    if (blockStateInArea.is(Blocks.BARRIER)) {
                                        // System.out.println("barrier");
                                        barrier = true;
                                    } else if (blockStateInArea.getCollisionShape(level, blockInArea) != null) {
                                        validBlock = true;
                                    }
                                }

                                if (validBlock) {
                                    disable = false;
                                    if (disabled) {
                                        consumer = source.getBuffer(RenderType.lineStrip());
                                        disabled = false;
                                    }
                                    for (Point location : toRender) {
                                        Vec3 rawLocation = location.vec3();
                                        int pathColor = location.color();
                                        consumer.vertex(last, (float) rawLocation.x, (float) rawLocation.y, (float)
                                                        rawLocation.z)
                                                .color(pathColor)
                                                .normal(0, 0, 1)
                                                .endVertex();
                                    }
                                    toRender.clear();
                                } else if (barrier) {
                                    disable = true;
                                    pauseDraw = true;
                                    toRender.clear();
                                } else if (disable) {
                                    pauseDraw = true;
                                } else {
                                    toRender.add(loc);
                                    continue;
                                }
                            } else if (disable) {
                                pauseDraw = true;
                            } else if (!toRender.isEmpty()) {
                                toRender.add(loc);
                            }

                            lastBlockPos = blockPos;

                            if (!pauseDraw) {
                                Vec3 rawLocation = loc.vec3();
                                int pathColor = loc.color();
                                consumer.vertex(last, (float) rawLocation.x, (float) rawLocation.y, (float)
                                                rawLocation.z)
                                        .color(pathColor)
                                        .normal(0, 0, 1)
                                        .endVertex();
                                // GLX._renderCrosshair(color, pauseDraw, disabled, disable);
                            } else if (!disabled) {
                                source.endBatch();
                                disabled = true;
                            }
                        }
                        if (!disabled) {
                            for (Point location : toRender) {
                                Vec3 rawLocation = location.vec3();
                                int pathColor = location.color();
                                consumer.vertex(last, (float) rawLocation.x, (float) rawLocation.y, (float)
                                                rawLocation.z)
                                        .color(pathColor)
                                        .normal(0, 0, 1)
                                        .endVertex();
                            }
                            source.endBatch();
                        }
                    }
                }

                if (lootrun.chests().containsKey(chunkLong)) {
                    VertexConsumer consumer = source.getBuffer(RenderType.lines());
                    Set<BlockPos> chests = lootrun.chests().get(chunkLong);
                    float red = ((float) FastColor.ARGB32.red(color)) / 255;
                    float green = ((float) FastColor.ARGB32.green(color)) / 255;
                    float blue = ((float) FastColor.ARGB32.blue(color)) / 255;
                    for (BlockPos chest : chests) {
                        LevelRenderer.renderLineBox(
                                event.getPoseStack(), consumer, new AABB(chest), red, green, blue, 1f);
                    }
                    source.endBatch();
                }

                if (lootrun.notes().containsKey(chunkLong)) {
                    List<Pair<Vec3, Component>> notes = lootrun.notes().get(chunkLong);
                    Font font = McUtils.mc().font;
                    for (Pair<Vec3, Component> note : notes) {
                        Vec3 location = note.first();
                        event.getPoseStack().pushPose();
                        event.getPoseStack().translate(location.x, location.y + 2, location.z);
                        event.getPoseStack()
                                .mulPose(McUtils.mc()
                                        .gameRenderer
                                        .getMainCamera()
                                        .rotation());
                        event.getPoseStack().scale(-0.025f, -0.025f, 0.025f);
                        Matrix4f pose = event.getPoseStack().last().pose();
                        List<FormattedCharSequence> lines = font.split(note.right(), 200);
                        int offsetY = -(font.lineHeight * lines.size()) / 2;
                        for (FormattedCharSequence line : lines) {
                            int offsetX = -font.width(line) / 2;
                            font.drawInBatch(
                                    line, offsetX, offsetY, color, false, pose, source, false, 0x80000000, 0xf000f0);
                            offsetY += font.lineHeight + 2;
                        }
                        event.getPoseStack().popPose();
                    }
                }
            }
        }
        event.getPoseStack().popPose();
    }

    public static int addNote(Component text) {
        Entity root = McUtils.player().getRootVehicle();

        LootrunUncompiled current = getActiveLootrun();

        if (current == null) return 0;

        current.notes().add(new ObjectObjectImmutablePair<>(root.position(), text));
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
                        saveLootrun(uncompiled.file().getName());
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

    public static LootrunSaveResult saveLootrun(String name) {
        try {
            File file = new File(LootrunUtils.LOOTRUNS, name + ".json");
            LootrunUtils.uncompiled = new LootrunUncompiled(LootrunUtils.uncompiled, file);

            boolean result = file.createNewFile();

            if (!result) {
                return LootrunSaveResult.ERROR_ALREADY_EXISTS;
            }

            JsonObject json = new JsonObject();
            JsonArray points = new JsonArray();
            for (Vec3 point : uncompiled.points()) {
                JsonObject pointJson = new JsonObject();
                pointJson.addProperty("x", point.x);
                pointJson.addProperty("y", point.y);
                pointJson.addProperty("z", point.z);
                points.add(pointJson);
            }
            json.add("points", points);

            JsonArray chests = new JsonArray();
            for (BlockPos chest : uncompiled.chests()) {
                JsonObject chestJson = new JsonObject();
                chestJson.addProperty("x", chest.getX());
                chestJson.addProperty("y", chest.getY());
                chestJson.addProperty("z", chest.getZ());
                chests.add(chestJson);
            }
            json.add("chests", chests);

            JsonArray notes = new JsonArray();
            for (Pair<Vec3, Component> note : uncompiled.notes()) {
                JsonObject noteJson = new JsonObject();
                JsonObject locationJson = new JsonObject();

                Vec3 location = note.first();
                locationJson.addProperty("x", location.x);
                locationJson.addProperty("y", location.y);
                locationJson.addProperty("z", location.z);
                noteJson.add("location", locationJson);

                noteJson.add("note", Component.Serializer.toJsonTree(note.second()));
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

    @SubscribeEvent
    public static void recordMovement(ClientTickEvent event) {
        if (event.getTickPhase() == ClientTickEvent.Phase.START) {
            if (recording != null) {
                LocalPlayer player = McUtils.mc().player;
                if (player != null) {
                    Entity root = player.getRootVehicle();
                    Vec3 pos = root.position();
                    if (recordingInformation.getLastLocation() == null
                            || pos.distanceToSqr(recordingInformation.getLastLocation()) >= 4d) {
                        recording.points().add(pos);
                        recordingInformation.setLastLocation(pos);
                        recordingInformation.setDirty(true);
                    }

                    if (recordingInformation.isDirty()) {
                        recordingCompiled = compile(recording);
                        recordingInformation.setDirty(false);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (recordingInformation != null) {
            BlockState block = event.getWorld().getBlockState(event.getPos());
            if (block.is(Blocks.CHEST)) {
                recordingInformation.setLastChest(event.getPos());
            }
        }
    }

    @SubscribeEvent
    public static void onOpen(ScreenOpenedEvent event) {
        if (event.getScreen() instanceof ContainerScreen screen && recordingInformation != null) {
            if (screen.getTitle().getString().contains("Loot Chest ") && recordingInformation.getLastChest() != null) {
                recording.chests().add(recordingInformation.getLastChest());
                recordingInformation.setDirty(true);
            }
            recordingInformation.setLastChest(null);
        }
    }

    public static LootrunInstance compile(LootrunUncompiled uncompiled) {
        Long2ObjectMap<List<List<Point>>> points = generatePointsByChunk(uncompiled.points());
        Long2ObjectMap<Set<BlockPos>> chests = getChests(uncompiled.chests());
        Long2ObjectMap<List<Pair<Vec3, Component>>> notes = getNotes(uncompiled.notes());

        return new LootrunInstance(points, chests, notes);
    }

    private static List<List<Vec3>> sample(List<Vec3> raw, float sampleRate) {
        List<List<Vec3>> vec3s = new ArrayList<>();
        List<Vec3> currentVec3s = new ArrayList<>();
        vec3s.add(currentVec3s);
        for (Vec3 element : raw) {
            if (!currentVec3s.isEmpty()
                    && currentVec3s.get(currentVec3s.size() - 1).distanceTo(element) >= 32) {
                currentVec3s = new ArrayList<>();
                vec3s.add(currentVec3s);
            }
            currentVec3s.add(element);
        }

        List<List<Vec3>> result = new ArrayList<>();
        for (List<Vec3> current : vec3s) {
            float distance = 0f;
            CubicSpline.Builder<Float> builderX = CubicSpline.builder((value) -> value);
            CubicSpline.Builder<Float> builderY = CubicSpline.builder((value) -> value);
            CubicSpline.Builder<Float> builderZ = CubicSpline.builder((value) -> value);
            for (int i = 0; i < current.size(); i++) {
                Vec3 vec3 = current.get(i);
                if (i > 0) {
                    distance += current.get(i - 1).distanceTo(vec3);
                }

                float slopeX = 0f;
                float slopeY = 0f;
                float slopeZ = 0f;
                if (i < current.size() - 1) {
                    Vec3 next = current.get(i + 1);
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

            List<Vec3> newResult = new ArrayList<>();
            for (float i = 0f; i < distance; i += (1f / sampleRate)) {
                newResult.add(new Vec3(splineX.apply(i), splineY.apply(i), splineZ.apply(i)));
            }
            result.add(newResult);
        }
        return result;
    }

    private static Long2ObjectMap<List<List<Point>>> generatePointsByChunk(List<Vec3> raw) {
        float sampleRate = 10f;
        List<Vec3> vec3s =
                sample(raw, sampleRate).stream().flatMap(List::stream).toList();
        ChunkPos lastChunkPos = null;
        List<Point> locationsList = new ArrayList<>();
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

            locationsList.add(new Point(location, usedColor | 0xff000000));
        }
        List<Point> lastLocationList = null;
        Long2ObjectMap<List<List<Point>>> sampleByChunk = new Long2ObjectOpenHashMap<>();
        for (int i = 0; i < locationsList.size(); i++) {
            Vec3 location = locationsList.get(i).vec3();
            ChunkPos currentChunkPos = new ChunkPos(Mth.fastFloor(location.x()) >> 4, Mth.fastFloor(location.z()) >> 4);
            if (!currentChunkPos.equals(lastChunkPos)) {
                if (lastChunkPos != null
                        && location.distanceTo(locationsList.get(i - 1).vec3()) < 32) {
                    lastLocationList.add(locationsList.get(i));
                }

                lastChunkPos = currentChunkPos;
                sampleByChunk.putIfAbsent(ChunkPos.asLong(currentChunkPos.x, currentChunkPos.z), new ArrayList<>());
                lastLocationList = new ArrayList<>();
                sampleByChunk
                        .get(ChunkPos.asLong(currentChunkPos.x, currentChunkPos.z))
                        .add(lastLocationList);
            }
            lastLocationList.add(locationsList.get(i));
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

    private static Long2ObjectMap<List<Pair<Vec3, Component>>> getNotes(List<Pair<Vec3, Component>> notes) {
        Long2ObjectMap<List<Pair<Vec3, Component>>> result = new Long2ObjectOpenHashMap<>();
        for (Pair<Vec3, Component> note : notes) {
            ChunkPos chunk = new ChunkPos(new BlockPos(note.first()));
            List<Pair<Vec3, Component>> notesChunk =
                    result.computeIfAbsent(chunk.toLong(), (chunkPos) -> new ArrayList<>());
            notesChunk.add(note);
        }
        return result;
    }

    public static LootrunUncompiled readJson(File file, JsonObject json) {
        JsonArray points = json.getAsJsonArray("points");
        List<Vec3> pointsList = new ArrayList<>();
        for (JsonElement element : points) {
            JsonObject pointJson = element.getAsJsonObject();
            Vec3 location = new Vec3(
                    pointJson.get("x").getAsDouble(),
                    pointJson.get("y").getAsDouble(),
                    pointJson.get("z").getAsDouble());
            pointsList.add(location);
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
        List<Pair<Vec3, Component>> notes = new ArrayList<>();
        if (notesJson != null) {
            for (JsonElement element : notesJson) {
                JsonObject noteJson = element.getAsJsonObject();
                JsonObject positionJson = noteJson.getAsJsonObject("location");
                Vec3 position = new Vec3(
                        positionJson.get("x").getAsDouble(),
                        positionJson.get("y").getAsDouble(),
                        positionJson.get("z").getAsDouble());
                Component component = Component.Serializer.fromJson(noteJson.get("note"));
                Pair<Vec3, Component> note = new ObjectObjectImmutablePair<>(position, component);
                notes.add(note);
            }
        }
        return new LootrunUncompiled(pointsList, chests, notes, file);
    }

    public static void clearCurrentLootrun() {
        unregisterFromEventBus();
        lootrun = null;
        uncompiled = null;
        recording = null;
        recordingCompiled = null;
        recordingInformation = null;
    }

    public static void stopRecording() {
        state = LootrunState.LOADED;
        lootrun = recordingCompiled;
        uncompiled = recording;
        recording = null;
        recordingCompiled = null;
        recordingInformation = null;
    }

    public static void startRecording() {
        recording = new LootrunUncompiled(new ArrayList<>(), new HashSet<>(), new ArrayList<>(), null);
        recordingInformation = new RecordingInformation();
        registerToEventBus();
    }

    public static boolean tryLoadFile(String fileName) {
        String lootrun = fileName + ".json";
        File lootrunFile = new File(LootrunUtils.LOOTRUNS, lootrun);
        if (lootrunFile.exists()) {
            FileReader file;
            try {
                file = new FileReader(lootrunFile);
                JsonObject json = JsonParser.parseReader(file).getAsJsonObject();
                LootrunUtils.registerToEventBus();
                LootrunUtils.uncompiled = LootrunUtils.readJson(lootrunFile, json);
                LootrunUtils.lootrun = LootrunUtils.compile(LootrunUtils.uncompiled);
                file.close();
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

    static {
        LOOTRUNS.mkdirs();
    }

    public enum LootrunSaveResult {
        SAVED,
        ERROR_SAVING,
        ERROR_ALREADY_EXISTS
    }

    public enum LootrunState {
        RECORDING, // Lootrun is being recorded
        LOADED, // Lootrun is loaded and displayed
        DISABLED // No lootrun paths are rendered or loaded
    }
}
