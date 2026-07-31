/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.wynn;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.platform.NativeImage;
import com.wynntils.core.WynntilsMod;
import com.wynntils.utils.JsonUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

public final class ResourcepackUtils {
    public static boolean forEachBoatModelOverride(
            ResourceManager resourceManager, BiConsumer<Float, String> consumer) {
        Identifier boatJson = Identifier.withDefaultNamespace("models/item/oak_boat.json");
        Optional<Resource> overrideRes = resourceManager.getResource(boatJson);
        if (overrideRes.isEmpty()) return false;

        try (InputStream stream = overrideRes.get().open();
                InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            JsonObject baseJson = JsonParser.parseReader(reader).getAsJsonObject();
            JsonArray overrides = JsonUtils.getNullableJsonArray(baseJson, "overrides");
            if (overrides.isEmpty()) return false;

            for (JsonElement element : overrides) {
                JsonObject override = element.getAsJsonObject();
                float cmd = override.getAsJsonObject("predicate")
                        .get("custom_model_data")
                        .getAsFloat();

                String modelPath = override.get("model").getAsString();
                consumer.accept(cmd, modelPath);
            }
            return true;
        } catch (IOException e) {
            WynntilsMod.error("ResourcepackUtils: Failed to parse oak_boat.json: " + e.getMessage());
            return false;
        }
    }

    public static ModelData parseModelData(ResourceManager resourceManager, String modelPath) {
        Identifier modelId = Identifier.parse(modelPath).withPath(p -> "models/" + p + ".json");

        Optional<Resource> res = resourceManager.getResource(modelId);
        if (res.isEmpty()) return null;

        try (InputStream stream = res.get().open();
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

            return new ModelData(fingerprint, referencedSlots, textureIds);
        } catch (IOException e) {
            WynntilsMod.error("ResourcepackUtils: Failed to parse model JSON for " + modelPath + ": " + e.getMessage());
            return null;
        }
    }

    public static Map<String, String> buildTexturePathToTextureHashMap(ResourceManager resourceManager) {
        Map<String, String> pathToHash = new HashMap<>();

        Map<Identifier, Resource> resources = resourceManager.listResources(
                "textures/item", id -> id.getPath().endsWith(".png"));
        for (Map.Entry<Identifier, Resource> entry : resources.entrySet()) {
            try (InputStream stream = entry.getValue().open();
                    NativeImage img = NativeImage.read(stream)) {
                String logicalPath =
                        entry.getKey().getPath().replaceFirst("^textures/", "").replaceFirst("\\.png$", "");
                pathToHash.put(logicalPath, sha256(Arrays.toString(getPixels(img))));
            } catch (IOException ignored) {
            }
        }

        return pathToHash;
    }

    public static String computeTextureHash(ResourceManager resourceManager, Identifier textureId) {
        Optional<Resource> res = resourceManager.getResource(textureId);
        if (res.isEmpty()) return "not-found";
        try (InputStream stream = res.get().open();
                NativeImage img = NativeImage.read(stream)) {
            return sha256(Arrays.toString(getPixels(img)));
        } catch (IOException e) {
            WynntilsMod.error("ResourcepackUtils: Failed to hash texture " + textureId + ": " + e.getMessage());
            return "error";
        }
    }

    private static String sha256(String input) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            WynntilsMod.error("ResourcepackUtils: SHA-256 unavailable: " + e.getMessage());
            return "hash-error";
        }
    }

    private static int[] getPixels(NativeImage img) {
        int width = img.getWidth();
        int height = img.getHeight();
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                pixels[y * width + x] = img.getPixel(x, y);
            }
        }
        return pixels;
    }

    private static Set<String> collectReferencedTextureSlots(JsonArray elements) {
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

    public record ModelData(String fingerprint, Set<String> referencedSlots, List<Identifier> textureIds) {}
}
