/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Service;
import com.wynntils.core.components.Services;
import com.wynntils.core.mod.event.WynntilsInitEvent;
import com.wynntils.mc.event.SubmitCustomGeometryEvent;
import com.wynntils.mc.event.TickEvent;
import com.wynntils.models.containers.event.LootChestOpenedEvent;
import com.wynntils.services.mapdata.attributes.MapAttributesBuilder;
import com.wynntils.services.mapdata.attributes.impl.MapPathAttributesImpl;
import com.wynntils.services.mapdata.attributes.resolving.ResolvedMapAttributes;
import com.wynntils.services.mapdata.features.impl.MapPathImpl;
import com.wynntils.services.mapdata.features.type.MapFeature;
import com.wynntils.services.mapdata.features.type.MapPath;
import com.wynntils.services.mapdata.paths.CompiledMapPath;
import com.wynntils.services.mapdata.paths.LootrunPathMigration;
import com.wynntils.services.mapdata.paths.MapPathCompiler;
import com.wynntils.services.mapdata.paths.MapPathFiles;
import com.wynntils.services.mapdata.paths.MapPathWorldRenderer;
import com.wynntils.services.mapdata.paths.PathCategoriesProvider;
import com.wynntils.services.mapdata.paths.PathNote;
import com.wynntils.services.mapdata.paths.RecordingPathProvider;
import com.wynntils.services.mapdata.providers.json.JsonProviderInfo;
import com.wynntils.services.mapdata.type.MapIcon;
import com.wynntils.utils.mc.McUtils;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;

public final class MapPathsService extends Service {
    private static final PathCategoriesProvider PATH_CATEGORIES_PROVIDER = new PathCategoriesProvider();

    private final RecordingPathProvider recordingProvider = new RecordingPathProvider();

    private final Map<MapPath, CachedPath> compiled = new HashMap<>();
    private final List<Vec3> points = new ArrayList<>();
    private final List<Vec3> highlights = new ArrayList<>();
    private final List<PathNote> notes = new ArrayList<>();
    private String category = PathCategoriesProvider.PATH_CATEGORY;
    private String label = "";
    private MapPathAttributesImpl attributes =
            new MapAttributesBuilder().asPathAttributes().build();
    private boolean recording;
    private boolean draft;
    private boolean recordHighlights = true;
    private StoredPath editing;
    private String featureId;

    public MapPathsService() {
        super(List.of());
    }

    @SubscribeEvent
    public void onModInitFinished(WynntilsInitEvent.ModInitFinished event) {
        Services.MapData.registerBuiltInProvider(PATH_CATEGORIES_PROVIDER, this, false);
        Services.MapData.registerBuiltInProvider(recordingProvider, this, false);
        List<Path> migrated = LootrunPathMigration.migrate(
                WynntilsMod.getModStorageDir("lootruns").toPath(), MapDataService.LOCAL_PROVIDERS.toPath());
        for (Path file : migrated) {
            String id = file.getFileName().toString().replaceFirst("\\.json$", "");
            JsonProviderInfo providerInfo =
                    JsonProviderInfo.createLocal(id, file.toAbsolutePath().toString());
            Services.MapData.addJsonProvider(providerInfo);
            if (Services.MapData.isProviderEnabled(providerInfo.completeId())) {
                Services.MapData.toggleProvider(providerInfo.completeId());
            }
        }
    }

    @SubscribeEvent
    public void recordMovement(TickEvent event) {
        if (!recording || McUtils.player() == null) return;

        recordPosition(McUtils.player().getRootVehicle().position());
    }

    @SubscribeEvent
    public void onLootChestOpened(LootChestOpenedEvent event) {
        if (recording && recordHighlights) {
            addHighlight(Vec3.atLowerCornerOf(event.getBlockPos()));
        }
    }

    @SubscribeEvent
    public void renderPaths(SubmitCustomGeometryEvent event) {
        Set<MapPath> active = new HashSet<>();
        Services.MapData.getFeatures()
                .filter(MapPath.class::isInstance)
                .map(MapPath.class::cast)
                .forEach(path -> {
                    active.add(path);
                    ResolvedMapAttributes attrs = Services.MapData.resolveMapAttributes(path);
                    if (!attrs.hasPath()) return;

                    int color = attrs.pathColor().asInt();
                    CachedPath cached = compiled.get(path);
                    if (cached == null || cached.color() != color) {
                        cached = new CachedPath(color, MapPathCompiler.compile(path, attrs));
                        compiled.put(path, cached);
                    }

                    Identifier texture = attrs.pathIconId().equals(MapIcon.NO_ICON_ID)
                            ? null
                            : Services.MapData.getIconOrFallback(attrs.pathIconId())
                                    .getIdentifier();
                    MapPathWorldRenderer.renderPath(
                            event.getPoseStack(), event.getSubmitNodeCollector(), cached.compiled(), color, texture);
                });
        compiled.keySet().retainAll(active);
    }

    public boolean isRecording() {
        return recording;
    }

    public boolean hasDraft() {
        return draft;
    }

    public boolean isRecordingHighlights() {
        return recordHighlights;
    }

    public String getCategory() {
        return category;
    }

    public String getLabel() {
        return label;
    }

    public MapPathAttributesImpl getAttributes() {
        return attributes;
    }

    public List<PathNote> getNotes() {
        return List.copyOf(notes);
    }

    public int getNoteCount() {
        return notes.size();
    }

    public boolean canEditNotes() {
        return recording || editing != null;
    }

    public int getPointCount() {
        return points.size();
    }

    public int getHighlightCount() {
        return highlights.size();
    }

    public StoredPath getEditing() {
        return editing;
    }

    public void configure(String category, String label, MapPathAttributesImpl attributes, boolean recordHighlights) {
        this.category = category;
        this.label = label;
        this.attributes = attributes;
        this.recordHighlights = recordHighlights;
        if (draft) {
            publish();
        }
    }

    public boolean startRecording() {
        if (editing != null) {
            McUtils.sendErrorToClient(Component.translatable("command.wynntils.map.path.editingInProgress")
                    .getString());

            return false;
        }

        if (recording) {
            McUtils.sendErrorToClient(Component.translatable("command.wynntils.map.path.alreadyRecording")
                    .getString());

            return false;
        }

        if (!draft) {
            points.clear();
            highlights.clear();
            notes.clear();
            editing = null;
            featureId = "user:path:" + UUID.randomUUID();
            draft = true;
        }

        recording = true;
        publish();

        return true;
    }

    public void cancel() {
        recording = false;
        draft = false;
        editing = null;
        category = PathCategoriesProvider.PATH_CATEGORY;
        label = "";
        attributes = new MapAttributesBuilder().asPathAttributes().build();
        recordHighlights = true;
        featureId = null;
        points.clear();
        highlights.clear();
        notes.clear();
        recordingProvider.update(null);
        compiled.clear();
    }

    public boolean addHighlight(Vec3 position) {
        if (!recording || highlights.contains(position)) return false;

        highlights.add(position);
        publish();

        return true;
    }

    public boolean removeHighlight(BlockPos position) {
        if (!recording) return false;

        boolean removed = highlights.removeIf(p -> BlockPos.containing(p).equals(position));
        if (removed) {
            publish();
        }

        return removed;
    }

    public boolean addNote(Component text) {
        if (!canEditNotes() || McUtils.player() == null) return false;

        return addNote(McUtils.player().getRootVehicle().position(), text);
    }

    public boolean addNote(Vec3 position, Component text) {
        if (!canEditNotes()
                || !Double.isFinite(position.x())
                || !Double.isFinite(position.y())
                || !Double.isFinite(position.z())) return false;

        PathNote note = PathNote.of(position, text);
        if (note == null) return false;

        notes.add(note);
        publish();

        return true;
    }

    public PathNote updateNote(PathNote note, Vec3 position, Component text) {
        if (!canEditNotes()
                || !Double.isFinite(position.x())
                || !Double.isFinite(position.y())
                || !Double.isFinite(position.z())) return null;

        int index = notes.indexOf(note);
        if (index < 0) return null;

        PathNote updated = PathNote.of(position, text);
        if (updated == null) return null;

        notes.set(index, updated);
        publish();

        return updated;
    }

    public boolean deleteNote(PathNote note) {
        if (!canEditNotes()) return false;

        boolean removed = notes.remove(note);
        if (removed) {
            publish();
        }

        return removed;
    }

    public boolean deleteNote(BlockPos position) {
        if (!canEditNotes()) return false;

        boolean removed =
                notes.removeIf(note -> BlockPos.containing(note.position()).equals(position));
        if (removed) {
            publish();
        }

        return removed;
    }

    public SaveResult save(String name) {
        if (!draft || points.size() < 2) {
            WynntilsMod.warn("Record at least two path points before saving");

            return SaveResult.TOO_FEW_POINTS;
        }

        if (name.isBlank()) {
            WynntilsMod.warn("A path label is required");

            return SaveResult.MISSING_LABEL;
        }

        label = name;
        MapPathImpl path = snapshot(category, attributes);
        Path file;
        String providerId;
        if (editing != null) {
            file = editing.file();
            providerId = editing.providerId();
            if (!MapPathFiles.update(file, path)) return SaveResult.WRITE_FAILED;
        } else {
            file = MapDataService.LOCAL_PROVIDERS.toPath().resolve(MapPathFiles.filename(name));
            providerId = file.getFileName().toString().replaceFirst("\\.json$", "");
            if (Services.MapData.getJsonProviderInfos().containsKey(providerId) || Files.exists(file)) {
                WynntilsMod.warn("A provider with that name already exists: " + providerId);

                return SaveResult.NAME_EXISTS;
            }

            if (!MapPathFiles.write(file, MapPathFiles.provider(path), false)) return SaveResult.WRITE_FAILED;
        }

        Services.MapData.addJsonProvider(
                JsonProviderInfo.createLocal(providerId, file.toAbsolutePath().toString()));
        cancel();

        return SaveResult.SAVED;
    }

    public List<StoredPath> getStoredPaths() {
        List<StoredPath> paths = new ArrayList<>();
        for (JsonProviderInfo info : Services.MapData.getJsonProviderInfos().values()) {
            if (info.providerType() != JsonProviderInfo.JsonProviderType.LOCAL) continue;

            Services.MapData.getProviderFromId(info.completeId()).ifPresent(provider -> {
                for (MapFeature<?> feature : provider.getFeatures().toList()) {
                    if (feature instanceof MapPathImpl path) {
                        paths.add(new StoredPath(info.providerId(), Path.of(info.providerFilePath()), path));
                    }
                }
            });
        }

        return List.copyOf(paths);
    }

    public Optional<StoredPath> findPathByName(String name) {
        List<StoredPath> paths = getStoredPaths();
        List<StoredPath> matches = paths.stream()
                .filter(path -> path.path().getFeatureId().equals(name))
                .toList();
        if (matches.isEmpty()) {
            matches = paths.stream()
                    .filter(path -> path.providerId().equals(name))
                    .toList();
        }

        if (matches.isEmpty()) {
            matches = paths.stream().filter(path -> path.label().equals(name)).toList();
        }

        if (matches.isEmpty()) {
            WynntilsMod.warn("Map path not found: " + name);

            return Optional.empty();
        }

        if (matches.size() > 1) {
            WynntilsMod.warn("Multiple paths match '" + name + "'; use the feature ID instead");

            return Optional.empty();
        }

        return Optional.of(matches.getFirst());
    }

    public boolean edit(StoredPath stored) {
        if (draft) return false;

        editing = stored;
        draft = true;
        featureId = stored.path().getFeatureId();
        String categoryId = stored.path().getCategoryId();
        String rootCategory = PathCategoriesProvider.PATH_CATEGORY;
        category = categoryId.equals(rootCategory) || categoryId.startsWith(rootCategory + ":")
                ? categoryId
                : rootCategory;
        attributes = new MapAttributesBuilder()
                .from(stored.path().getAttributes().orElse(null))
                .asPathAttributes()
                .build();
        label = stored.label();
        points.addAll(stored.path().getPath());
        highlights.addAll(stored.path().getHighlights());
        notes.addAll(stored.path().getNotes());

        return true;
    }

    private MapPathImpl snapshot(String categoryId, MapPathAttributesImpl style) {
        return new MapPathImpl(
                featureId,
                categoryId,
                new MapAttributesBuilder()
                        .from(style)
                        .setLabel(label)
                        .setDescription(attributes.getDescription().orElse(null))
                        .asPathAttributes()
                        .build(),
                points,
                highlights,
                notes);
    }

    private void publish() {
        recordingProvider.update(
                recording && points.size() >= 2 ? snapshot(PathCategoriesProvider.RECORDING_CATEGORY, null) : null);
    }

    private void recordPosition(Vec3 position) {
        if (!recording
                || !Double.isFinite(position.x())
                || !Double.isFinite(position.y())
                || !Double.isFinite(position.z())) return;

        if (points.isEmpty() || points.getLast().distanceToSqr(position) >= 4) {
            points.add(position);
            publish();
        }
    }

    public enum SaveResult {
        SAVED("success"),
        TOO_FEW_POINTS("saveTooFewPoints"),
        MISSING_LABEL("saveMissingLabel"),
        NAME_EXISTS("saveNameExists"),
        WRITE_FAILED("saveFailed");

        private final String messageKey;

        SaveResult(String messageKey) {
            this.messageKey = "command.wynntils.map.path." + messageKey;
        }

        public MutableComponent getMessage() {
            return Component.translatable(messageKey);
        }
    }

    public record StoredPath(String providerId, Path file, MapPathImpl path) {
        public String label() {
            return path.getAttributes().flatMap(a -> a.getLabel()).orElse(path.getFeatureId());
        }
    }

    private record CachedPath(int color, CompiledMapPath compiled) {}
}
