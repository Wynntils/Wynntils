/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.profession;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Services;
import com.wynntils.core.net.Dependency;
import com.wynntils.core.net.DownloadRegistry;
import com.wynntils.core.net.UrlId;
import com.wynntils.models.profession.type.MaterialInfo;
import com.wynntils.models.profession.type.MaterialType;
import com.wynntils.models.profession.type.ProfessionType;
import com.wynntils.models.profession.type.ResourceType;
import com.wynntils.models.profession.type.SourceMaterial;
import com.wynntils.models.wynnitem.AbstractItemInfoDeserializer;
import com.wynntils.models.wynnitem.type.ItemMaterial;
import com.wynntils.utils.JsonUtils;
import com.wynntils.utils.type.Pair;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.ChatFormatting;

public class MaterialInfoRegistry {
    private static final Gson GSON = new GsonBuilder()
            .registerTypeHierarchyAdapter(MaterialInfo.class, new MaterialInfoDeserializer())
            .create();

    private List<MaterialInfo> materialInfoRegistry = List.of();
    private Map<String, MaterialInfo> materialInfoLookup = Map.of();
    private Map<String, MaterialInfo> materialInfoLookupApiName = Map.of();

    public void registerDownloads(DownloadRegistry registry) {
        registry.registerDownload(
                        UrlId.DATA_STATIC_MATERIALS,
                        Dependency.simple(Services.CustomModel, UrlId.DATA_STATIC_MODEL_DATA))
                .handleJsonObject(this::handleMaterials);
    }

    public MaterialInfo getFromDisplayName(String materialName) {
        return materialInfoLookup.get(materialName);
    }

    public MaterialInfo getFromApiName(String materialName) {
        return materialInfoLookupApiName.get(materialName);
    }

    public Stream<MaterialInfo> getMaterialInfoStream() {
        return materialInfoRegistry.stream();
    }

    public MaterialInfo getFromSourceAndResource(String sourceMaterialName, String resourceTypeName) {
        ResourceType resourceType = ResourceType.fromString(resourceTypeName);
        if (resourceType == null) return null;

        return getFromDisplayName(sourceMaterialName + " " + capitalized(resourceType.name()));
    }

    public Optional<Pair<MaterialType, SourceMaterial>> findBySourceMaterialName(
            String name, ChatFormatting labelColor) {
        return materialInfoRegistry.stream()
                .filter(materialInfo -> materialInfo.professionType() != null)
                .filter(materialInfo -> getMaterialType(materialInfo.professionType()) != null)
                .filter(materialInfo ->
                        getMaterialType(materialInfo.professionType()).getLabelColor() == labelColor)
                .map(this::toSourceMaterialPair)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(pair -> pair.value().name().equals(name))
                .findFirst();
    }

    public Optional<Pair<MaterialType, SourceMaterial>> findBySourceMaterialName(String name) {
        return materialInfoRegistry.stream()
                .map(this::toSourceMaterialPair)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(pair -> pair.value().name().equals(name))
                .findFirst();
    }

    private Optional<Pair<MaterialType, SourceMaterial>> toSourceMaterialPair(MaterialInfo materialInfo) {
        ResourceType resourceType = getResourceType(materialInfo);
        if (resourceType == null) return Optional.empty();

        String suffix = " " + capitalized(resourceType.name());
        if (!materialInfo.name().endsWith(suffix)) return Optional.empty();

        String sourceMaterialName =
                materialInfo.name().substring(0, materialInfo.name().length() - suffix.length());
        return Optional.of(new Pair<>(
                resourceType.getMaterialType(), new SourceMaterial(sourceMaterialName, materialInfo.level())));
    }

    private ResourceType getResourceType(MaterialInfo materialInfo) {
        for (ResourceType resourceType : ResourceType.values()) {
            if (materialInfo.name().endsWith(" " + capitalized(resourceType.name()))) {
                return resourceType;
            }
        }

        return null;
    }

    public MaterialType getMaterialType(ProfessionType professionType) {
        return switch (professionType) {
            case MINING -> MaterialType.ORE;
            case WOODCUTTING -> MaterialType.LOG;
            case FARMING -> MaterialType.CROP;
            case FISHING -> MaterialType.FISH;
            default -> null;
        };
    }

    private static String capitalized(String str) {
        return str.charAt(0) + str.substring(1).toLowerCase();
    }

    private void handleMaterials(JsonObject json) {
        // Create fast lookup maps
        List<MaterialInfo> registry = new ArrayList<>();

        for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
            JsonObject materialObject = entry.getValue().getAsJsonObject();

            // Inject the name into the object
            materialObject.addProperty("name", entry.getKey());

            // Deserialize the item
            MaterialInfo materialInfo = GSON.fromJson(materialObject, MaterialInfo.class);

            // Add the item to the registry
            registry.add(materialInfo);
        }

        Map<String, MaterialInfo> lookupMap = new HashMap<>();
        Map<String, MaterialInfo> altLookupMap = new HashMap<>();
        for (MaterialInfo materialInfo : registry) {
            lookupMap.put(materialInfo.name(), materialInfo);
            altLookupMap.put(materialInfo.apiName(), materialInfo);
        }

        // Make the result visible to the world
        materialInfoRegistry = registry;
        materialInfoLookup = lookupMap;
        materialInfoLookupApiName = altLookupMap;
    }

    private static final class MaterialInfoDeserializer extends AbstractItemInfoDeserializer<MaterialInfo> {
        @Override
        public MaterialInfo deserialize(JsonElement jsonElement, Type jsonType, JsonDeserializationContext context)
                throws JsonParseException {
            JsonObject json = jsonElement.getAsJsonObject();

            Pair<String, String> names = parseNames(json);
            String displayName = names.key();
            String internalName = names.value();

            JsonObject requirements = JsonUtils.getNullableJsonObject(json, "requirements");
            int level = requirements.get("level").getAsInt();

            Map<Integer, Integer> chances = parseChances(JsonUtils.getNullableJsonObject(json, "chances"));

            ItemMaterial material = parseMaterial(json, displayName);

            ProfessionType professionType = ProfessionType.fromString(JsonUtils.getNullableJsonString(json, "subType"));

            String emblem = JsonUtils.getNullableJsonString(json, "emblem");

            return new MaterialInfo(displayName, level, internalName, chances, material, professionType, emblem);
        }

        private ItemMaterial parseMaterial(JsonObject json, String name) {
            ItemMaterial material = parseMaterial(json);

            if (material == null) {
                WynntilsMod.warn("Material DB is missing material for " + name);
                return ItemMaterial.fromItemId("minecraft:air", 0);
            }

            return material;
        }

        private Map<Integer, Integer> parseChances(JsonObject jsonObject) {
            Map<Integer, Integer> chances = new HashMap<>();

            if (jsonObject == null || jsonObject.isJsonNull()) return chances;

            for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                int tier =
                        switch (entry.getKey()) {
                            case "TIER_1" -> 1;
                            case "TIER_2" -> 2;
                            case "TIER_3" -> 3;
                            default -> 0;
                        };

                chances.put(tier, entry.getValue().getAsInt());
            }

            return chances;
        }
    }
}
