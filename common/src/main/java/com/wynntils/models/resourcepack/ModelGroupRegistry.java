/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.resourcepack;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.net.DownloadRegistry;
import com.wynntils.core.net.UrlId;
import com.wynntils.models.resourcepack.type.GroupInfo;
import com.wynntils.models.resourcepack.type.ModelCache;
import com.wynntils.models.resourcepack.type.ModelGroupCache;
import com.wynntils.models.resourcepack.type.ModelInfo;
import com.wynntils.models.resourcepack.type.VariantInfo;
import com.wynntils.utils.JsonUtils;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.resources.Identifier;

public final class ModelGroupRegistry {
    private static final Gson GSON = new GsonBuilder()
            .registerTypeHierarchyAdapter(GroupInfo.class, new GroupInfoDeserializer())
            .create();

    private volatile List<GroupInfo> groupInfos = List.of();

    private final Runnable onEntriesChanged;

    public ModelGroupRegistry(Runnable onEntriesChanged) {
        this.onEntriesChanged = onEntriesChanged;
    }

    public void registerDownloads(DownloadRegistry registry) {
        registry.registerDownload(UrlId.DATA_STATIC_TEXTURE_AND_FINGERPRINT_HASHES)
                .handleJsonObject(this::handleJson);
    }

    public ModelGroupCache buildModelGroupCache(ModelCache modelCache) {
        return buildModelGroupCache(modelCache, groupInfos);
    }

    private void handleJson(JsonObject jsonObject) {
        List<GroupInfo> infos = new ArrayList<>();

        for (Map.Entry<String, JsonElement> groupEntry : jsonObject.entrySet()) {
            JsonObject groupObject = groupEntry.getValue().getAsJsonObject();

            groupObject.addProperty("name", groupEntry.getKey());

            GroupInfo groupInfo = GSON.fromJson(groupObject, GroupInfo.class);
            if (groupInfo == null) continue;

            infos.add(groupInfo);
        }

        groupInfos = List.copyOf(infos);
        onEntriesChanged.run();
    }

    private ModelGroupCache buildModelGroupCache(ModelCache modelCache, List<GroupInfo> infos) {
        Map<String, List<Float>> groupToModelIds = new LinkedHashMap<>();
        for (GroupInfo groupInfo : infos) {
            groupToModelIds.put(groupInfo.name(), new ArrayList<>());
        }

        Map<Float, String> modelIdToGroup = new HashMap<>();
        for (Map.Entry<Float, ModelInfo> model : modelCache.modelIdToModelInfo().entrySet()) {
            ModelInfo data = model.getValue();

            for (GroupInfo groupInfo : infos) {
                for (Identifier textureId : data.textureIds()) {
                    String textureHash = modelCache.textureIdToTextureHash().get(textureId);

                    if (groupInfo
                            .matchingVariant(textureHash, data.fingerprint())
                            .isPresent()) {
                        groupToModelIds.get(groupInfo.name()).add(model.getKey());
                        modelIdToGroup.put(model.getKey(), groupInfo.name());
                        break;
                    }
                }
            }
        }

        // Make the lists unmodifiable
        groupToModelIds.replaceAll((name, ids) -> List.copyOf(ids));

        logUnmatchedEntries(modelCache, infos);

        return new ModelGroupCache(
                Collections.unmodifiableMap(groupToModelIds), Collections.unmodifiableMap(modelIdToGroup));
    }

    // TODO: maybe make it so that we do not have to match all models twice
    private void logUnmatchedEntries(ModelCache modelCache, List<GroupInfo> infos) {
        if (modelCache.modelIdToModelInfo().isEmpty()) return;

        for (GroupInfo groupInfo : infos) {
            for (Map.Entry<String, VariantInfo> variantEntry :
                    groupInfo.variants().entrySet()) {
                VariantInfo variant = variantEntry.getValue();

                Set<String> unmatchedHashes = new LinkedHashSet<>(variant.textureHashes());
                Set<String> unmatchedFingerprints = new LinkedHashSet<>(variant.fingerprints());

                for (ModelInfo model : modelCache.modelIdToModelInfo().values()) {
                    for (Identifier textureId : model.textureIds()) {
                        String textureHash = modelCache.textureIdToTextureHash().get(textureId);
                        if (!variant.matches(textureHash, model.fingerprint())) continue;

                        unmatchedHashes.remove(textureHash);
                        unmatchedFingerprints.remove(model.fingerprint());
                    }
                }

                if (!unmatchedHashes.isEmpty()) {
                    WynntilsMod.error("ModelGroupRegistry: Group '" + groupInfo.name() + "' variant '"
                            + variantEntry.getKey() + "' has texture hashes that never matched any model: "
                            + unmatchedHashes);
                }

                if (!unmatchedFingerprints.isEmpty()) {
                    WynntilsMod.error("ModelGroupRegistry: Group '" + groupInfo.name() + "' variant '"
                            + variantEntry.getKey() + "' has fingerprints that never matched any model: "
                            + unmatchedFingerprints);
                }
            }
        }
    }

    private static final class GroupInfoDeserializer implements JsonDeserializer<GroupInfo> {
        @Override
        public GroupInfo deserialize(JsonElement jsonElement, Type jsonType, JsonDeserializationContext context)
                throws JsonParseException {
            JsonObject json = jsonElement.getAsJsonObject();

            String name = json.get("name").getAsString();

            Map<String, VariantInfo> variants = new LinkedHashMap<>();
            for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                if (!entry.getValue().isJsonObject()) continue;

                JsonObject variantObject = entry.getValue().getAsJsonObject();
                variants.put(
                        entry.getKey(),
                        new VariantInfo(
                                readStringSet(variantObject, "textures"),
                                readStringSet(variantObject, "fingerprints")));
            }

            if (variants.isEmpty()) {
                WynntilsMod.warn("Model group has no variants: " + name);
                return null;
            }

            return new GroupInfo(name, Collections.unmodifiableMap(variants));
        }

        private Set<String> readStringSet(JsonObject json, String key) {
            Set<String> result = new LinkedHashSet<>();
            for (JsonElement element : JsonUtils.getNullableJsonArray(json, key)) {
                result.add(element.getAsString());
            }

            return Set.copyOf(result);
        }
    }
}
