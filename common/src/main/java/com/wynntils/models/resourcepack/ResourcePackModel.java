/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.resourcepack;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.platform.NativeImage;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Model;
import com.wynntils.core.net.DownloadRegistry;
import com.wynntils.mc.event.ServerResourcePackEvent;
import com.wynntils.mc.event.WynncraftResourcePackLoadedEvent;
import com.wynntils.models.resourcepack.type.ModelCache;
import com.wynntils.models.resourcepack.type.ModelGroupCache;
import com.wynntils.models.resourcepack.type.ModelInfo;
import com.wynntils.services.resourcepack.WynntilsResourceProvider;
import com.wynntils.utils.JsonUtils;
import com.wynntils.utils.TaskUtils;
import com.wynntils.utils.mc.McUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.resources.CloseableResourceManager;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.bus.api.SubscribeEvent;

public final class ResourcePackModel extends Model {
    private static final Identifier BOAT_MODEL_ID = Identifier.withDefaultNamespace("models/item/oak_boat.json");
    private static final String TEXTURE_NOT_FOUND_HASH = "not-found";
    private static final String TEXTURE_ERROR_HASH = "error";

    private final ExecutorService worker = TaskUtils.createSingleThreadExecutor("Wynntils-Resource-Pack-Model-%d");
    private final ModelGroupRegistry groupRegistry = new ModelGroupRegistry(this::rebuildModelGroupCache);

    private volatile int generation = 0;

    private volatile ModelCache modelCache = ModelCache.EMPTY;
    private volatile ModelGroupCache modelGroupCache = ModelGroupCache.EMPTY;

    public ResourcePackModel() {
        super(List.of());
    }

    @Override
    public void registerDownloads(DownloadRegistry registry) {
        groupRegistry.registerDownloads(registry);
    }

    @SubscribeEvent
    public void onServerResourcePackChange(ServerResourcePackEvent.Change event) {
        invalidateCaches();
    }

    @SubscribeEvent
    public void onWynncraftResourcePackLoaded(WynncraftResourcePackLoadedEvent event) {
        Optional<CloseableResourceManager> resourceManager = openWynncraftResourceManager();
        if (resourceManager.isEmpty()) return;

        int buildGeneration = invalidateCaches();

        try {
            worker.execute(() -> buildCaches(resourceManager.get(), buildGeneration));
        } catch (RejectedExecutionException e) {
            resourceManager.get().close();
        }
    }

    public Map<Identifier, String> getTextureIdToTextureHash() {
        return modelCache.textureIdToTextureHash();
    }

    public Optional<String> getGroup(float customModelData) {
        return Optional.ofNullable(modelGroupCache.modelIdToGroup().get(customModelData));
    }

    public Map<String, List<Float>> getGroupToModelIds() {
        return modelGroupCache.groupToModelIds();
    }

    public Optional<ModelInfo> getModelInfo(float customModelData) {
        return Optional.ofNullable(modelCache.modelIdToModelInfo().get(customModelData));
    }

    private synchronized int invalidateCaches() {
        generation++;
        modelCache = ModelCache.EMPTY;
        modelGroupCache = ModelGroupCache.EMPTY;
        return generation;
    }

    private void buildCaches(CloseableResourceManager manager, int buildGeneration) {
        try (manager) {
            if (generation != buildGeneration) return;

            long start = System.currentTimeMillis();
            ModelCache builtModelCache = buildModelCache(manager, buildGeneration);
            ModelGroupCache builtModelGroupCache = groupRegistry.buildModelGroupCache(builtModelCache);

            if (generation != buildGeneration) return;

            modelGroupCache = builtModelGroupCache;
            modelCache = builtModelCache;

            WynntilsMod.info("ResourcePackModel: Cached "
                    + builtModelCache.modelIdToModelInfo().size() + " models in " + (System.currentTimeMillis() - start)
                    + " ms");
        } catch (Throwable t) {
            WynntilsMod.error("ResourcePackModel: Failed to read the Wynncraft resource pack", t);
        }
    }

    private void rebuildModelGroupCache() {
        try {
            worker.execute(() -> {
                try {
                    ModelCache currentModelCache = modelCache;
                    if (currentModelCache.modelIdToModelInfo().isEmpty()) return;

                    modelGroupCache = groupRegistry.buildModelGroupCache(currentModelCache);
                } catch (Throwable t) {
                    WynntilsMod.error("ResourcePackModel: Failed to build the model groups", t);
                }
            });
        } catch (RejectedExecutionException ignored) {
        }
    }

    private ModelCache buildModelCache(ResourceManager resourceManager, int buildGeneration) {
        Optional<Resource> boatResource = resourceManager.getResource(BOAT_MODEL_ID);
        if (boatResource.isEmpty()) {
            WynntilsMod.error(
                    "ResourcePackModel: Could not find " + BOAT_MODEL_ID.toShortString() + " in the Wynncraft pack.");
            return ModelCache.EMPTY;
        }

        Map<Float, ModelInfo> modelIdToModelInfo = new LinkedHashMap<>();
        Map<Identifier, String> textureIdToTextureHash = new HashMap<>();

        try (InputStream stream = boatResource.get().open();
                InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            JsonObject baseJson = JsonParser.parseReader(reader).getAsJsonObject();
            JsonArray overrides = JsonUtils.getNullableJsonArray(baseJson, "overrides");

            if (overrides.isEmpty()) {
                WynntilsMod.error("ResourcePackModel: " + BOAT_MODEL_ID.toShortString()
                        + " in the Wynncraft pack has no overrides.");
                return ModelCache.EMPTY;
            }

            for (JsonElement element : overrides) {
                if (generation != buildGeneration) return ModelCache.EMPTY;

                JsonObject override = element.getAsJsonObject();
                float customModelData = override.getAsJsonObject("predicate")
                        .get("custom_model_data")
                        .getAsFloat();
                String modelPath = override.get("model").getAsString();

                ModelInfo modelInfo = parseModel(resourceManager, modelPath);
                if (modelInfo == null) continue;

                modelIdToModelInfo.put(customModelData, modelInfo);

                for (Identifier textureId : modelInfo.textureIds()) {
                    textureIdToTextureHash.computeIfAbsent(textureId, id -> computeTextureHash(resourceManager, id));
                }
            }
        } catch (IOException e) {
            WynntilsMod.error(
                    "ResourcePackModel: Failed to parse " + BOAT_MODEL_ID.toShortString() + " " + e.getMessage());
        }

        return new ModelCache(
                Collections.unmodifiableMap(modelIdToModelInfo), Collections.unmodifiableMap(textureIdToTextureHash));
    }

    private ModelInfo parseModel(ResourceManager resourceManager, String modelPath) {
        Identifier modelId = Identifier.parse(modelPath).withPath(p -> "models/" + p + ".json");

        Optional<Resource> resource = resourceManager.getResource(modelId);
        if (resource.isEmpty()) return null;

        try (InputStream stream = resource.get().open();
                InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();

            JsonArray elements = JsonUtils.getNullableJsonArray(json, "elements");
            if (elements.isEmpty()) return null;

            String fingerprint = sha256(elements.toString());
            Set<String> referencedSlots = collectReferencedTextureSlots(elements);

            List<Identifier> textureIds = new ArrayList<>();
            JsonObject textures = JsonUtils.getNullableJsonObject(json, "textures");
            for (String slot : referencedSlots) {
                String texturePath = JsonUtils.getNullableJsonString(textures, slot);
                if (texturePath == null) continue;
                textureIds.add(Identifier.parse(texturePath).withPath(p -> "textures/" + p + ".png"));
            }

            return new ModelInfo(fingerprint, referencedSlots, List.copyOf(textureIds));
        } catch (IOException e) {
            WynntilsMod.error("ResourcePackModel: Failed to parse model JSON for " + modelPath + ": " + e.getMessage());
            return null;
        }
    }

    private String computeTextureHash(ResourceManager resourceManager, Identifier textureId) {
        Optional<Resource> resource = resourceManager.getResource(textureId);
        if (resource.isEmpty()) return TEXTURE_NOT_FOUND_HASH;

        try (InputStream stream = resource.get().open();
                NativeImage image = NativeImage.read(stream)) {
            return sha256(Arrays.toString(getPixels(image)));
        } catch (IOException e) {
            WynntilsMod.error("ResourcePackModel: Failed to hash texture " + textureId + ": " + e.getMessage());
            return TEXTURE_ERROR_HASH;
        }
    }

    private Set<String> collectReferencedTextureSlots(JsonArray elements) {
        Set<String> slots = new LinkedHashSet<>();

        for (JsonElement element : elements) {
            JsonObject el = element.getAsJsonObject();
            JsonObject faces = JsonUtils.getNullableJsonObject(el, "faces");

            for (String faceKey : faces.keySet()) {
                String ref = JsonUtils.getNullableJsonString(faces.getAsJsonObject(faceKey), "texture");
                if (ref != null && ref.startsWith("#")) {
                    slots.add(ref.substring(1));
                }
            }
        }

        return slots;
    }

    private Optional<Pack> getWynncraftPack() {
        PackRepository packRepository = McUtils.mc().getResourcePackRepository();

        Pack found = null;
        for (Pack pack : packRepository.getSelectedPacks()) {
            if (WynntilsResourceProvider.isWynncraftPack(pack)) {
                found = pack;
            }
        }

        return Optional.ofNullable(found);
    }

    private Optional<CloseableResourceManager> openWynncraftResourceManager() {
        Optional<Pack> wynncraftPack = getWynncraftPack();
        if (wynncraftPack.isEmpty()) {
            WynntilsMod.warn("ResourcePackModel: Wynncraft resource pack is not selected, cannot open it.");
            return Optional.empty();
        }

        return Optional.of(new MultiPackResourceManager(
                PackType.CLIENT_RESOURCES, List.of(wynncraftPack.get().open())));
    }

    private int[] getPixels(NativeImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                pixels[y * width + x] = image.getPixel(x, y);
            }
        }
        return pixels;
    }

    private String sha256(String input) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            WynntilsMod.error("ResourcePackModel: SHA-256 unavailable: " + e.getMessage());
            return "hash-error";
        }
    }
}
