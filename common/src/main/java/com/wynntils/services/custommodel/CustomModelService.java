/*
 * Copyright © Wynntils 2025-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.custommodel;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Service;
import com.wynntils.core.net.DownloadRegistry;
import com.wynntils.core.net.UrlId;
import com.wynntils.utils.type.Pair;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.IoSupplier;
import org.apache.commons.lang3.concurrent.ConcurrentException;
import org.apache.commons.lang3.concurrent.LazyInitializer;
import org.jspecify.annotations.Nullable;

public class CustomModelService extends Service {
    private Map<String, Float> floatData = new ConcurrentHashMap<>();
    private Map<String, Pair<Float, Float>> rangeData = new ConcurrentHashMap<>();

    private final LazyInitializer<PackResources> pack = LazyInitializer.<PackResources>builder()
            .setInitializer(() -> Minecraft.getInstance()
                    .getResourceManager()
                    .listPacks()
                    .filter(packResources ->
                            packResources.location().title().getString().contains("Wynncraft"))
                    .findFirst()
                    .orElseThrow())
            .get();
    private final Map<Float, List<Integer>> textureHashes = new ConcurrentHashMap<>();

    public CustomModelService() {
        super(List.of());
    }

    @Override
    public void registerDownloads(DownloadRegistry registry) {
        registry.registerDownload(UrlId.DATA_STATIC_MODEL_DATA).handleJsonObject(this::handleModelData);
    }

    public List<Integer> getTextureHashes(List<Float> floats) throws IOException {
        List<Integer> hashes = new ArrayList<>();
        for (float aFloat : floats) {
            if (!textureHashes.containsKey(aFloat)) {
                PackResources resources;
                try {
                    resources = pack.get();
                } catch (ConcurrentException e) {
                    throw new IOException(e);
                }
                @Nullable
                IoSupplier<InputStream> supplier =
                        resources.getRootResource("assets/minecraft/models/w" + Math.round(aFloat) + ".json");
                if (supplier == null) return Collections.emptyList();
                JsonElement fileElement = JsonParser.parseReader(
                        new JsonReader(new InputStreamReader(supplier.get(), StandardCharsets.UTF_8)));
                JsonObject texturesNode = fileElement.getAsJsonObject().getAsJsonObject("textures");

                for (Map.Entry<String, JsonElement> entry : texturesNode.entrySet()) {
                    String texturePath = entry.getValue().getAsString();
                    if ("item/empty".equals(texturePath)) continue;
                    int hash1 = Arrays.hashCode(resources
                            .getRootResource("assets/minecraft/textures/"
                                    + entry.getValue().getAsString() + ".png")
                            .get()
                            .readAllBytes());
                    // System.out.println(aFloat + " (#" + entry.getKey() + " -> " + entry.getValue().getAsString() + ")
                    // hash is: " + hash1);
                    hashes.add(hash1);
                }
                textureHashes.put(aFloat, hashes);
            } else {
                hashes.addAll(textureHashes.get(aFloat));
            }
        }

        return Collections.unmodifiableList(hashes);
    }

    public Optional<Float> getFloat(String key) {
        if (key == null) return Optional.empty();

        return Optional.ofNullable(floatData.get(key));
    }

    public Optional<Pair<Float, Float>> getRange(String key) {
        if (key == null) return Optional.empty();

        return Optional.ofNullable(rangeData.get(key));
    }

    private void handleModelData(JsonObject jsonObject) {
        Map<String, Float> newFloatData = new ConcurrentHashMap<>();
        Map<String, Pair<Float, Float>> newRangeData = new ConcurrentHashMap<>();

        if (jsonObject.has("floats")) {
            jsonObject.getAsJsonObject("floats").asMap().forEach((key, element) -> {
                if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber()) {
                    newFloatData.put(key, element.getAsJsonPrimitive().getAsFloat());
                } else {
                    WynntilsMod.error("Invalid float custom model data for key: " + key);
                }
            });
        }

        if (jsonObject.has("ranges")) {
            jsonObject.getAsJsonObject("ranges").asMap().forEach((key, element) -> {
                if (element.isJsonArray() && element.getAsJsonArray().size() == 2) {
                    float min = element.getAsJsonArray().get(0).getAsFloat();
                    float max = element.getAsJsonArray().get(1).getAsFloat();
                    newRangeData.put(key, Pair.of(min, max));
                } else {
                    WynntilsMod.error("Invalid range custom model data for key: " + key);
                }
            });
        }

        floatData = newFloatData;
        rangeData = newRangeData;
    }
}
