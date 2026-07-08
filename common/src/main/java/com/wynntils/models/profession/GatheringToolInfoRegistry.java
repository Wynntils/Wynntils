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
import com.wynntils.core.net.DownloadRegistry;
import com.wynntils.core.net.UrlId;
import com.wynntils.models.gear.type.GearTier;
import com.wynntils.models.profession.type.GatheringToolInfo;
import com.wynntils.models.profession.type.ProfessionType;
import com.wynntils.models.wynnitem.AbstractItemInfoDeserializer;
import com.wynntils.models.wynnitem.type.ItemMaterial;
import com.wynntils.utils.JsonUtils;
import com.wynntils.utils.type.Pair;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class GatheringToolInfoRegistry {
    private static final Gson GSON = new GsonBuilder()
            .registerTypeHierarchyAdapter(GatheringToolInfo.class, new GatheringToolInfoDeserializer())
            .create();

    private List<GatheringToolInfo> gatheringToolInfoRegistry = List.of();
    private Map<String, GatheringToolInfo> gatheringToolInfoLookup = Map.of();
    private Map<String, GatheringToolInfo> gatheringToolInfoLookupApiName = Map.of();

    public void registerDownloads(DownloadRegistry registry) {
        registry.registerDownload(UrlId.DATA_STATIC_TOOLS).handleJsonObject(this::handleGatheringTools);
    }

    public GatheringToolInfo getFromDisplayName(String toolName) {
        return gatheringToolInfoLookup.get(toolName);
    }

    public GatheringToolInfo getFromApiName(String toolName) {
        return gatheringToolInfoLookupApiName.get(toolName);
    }

    public Stream<GatheringToolInfo> getGatheringToolInfoStream() {
        return gatheringToolInfoRegistry.stream();
    }

    private void handleGatheringTools(JsonObject json) {
        // Create fast lookup maps
        List<GatheringToolInfo> registry = new ArrayList<>();

        for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
            JsonObject materialObject = entry.getValue().getAsJsonObject();

            // Inject the name into the object
            materialObject.addProperty("name", entry.getKey());

            // Deserialize the item
            GatheringToolInfo gatheringToolInfo = GSON.fromJson(materialObject, GatheringToolInfo.class);

            // Add the item to the registry
            registry.add(gatheringToolInfo);
        }

        Map<String, GatheringToolInfo> lookupMap = new HashMap<>();
        Map<String, GatheringToolInfo> altLookupMap = new HashMap<>();
        for (GatheringToolInfo gatheringToolInfo : registry) {
            lookupMap.put(gatheringToolInfo.name(), gatheringToolInfo);
            altLookupMap.put(gatheringToolInfo.apiName(), gatheringToolInfo);
        }

        // Make the result visible to the world
        gatheringToolInfoRegistry = registry;
        gatheringToolInfoLookup = lookupMap;
        gatheringToolInfoLookupApiName = altLookupMap;
    }

    private static final class GatheringToolInfoDeserializer extends AbstractItemInfoDeserializer<GatheringToolInfo> {
        @Override
        public GatheringToolInfo deserialize(JsonElement jsonElement, Type jsonType, JsonDeserializationContext context)
                throws JsonParseException {
            JsonObject json = jsonElement.getAsJsonObject();

            Pair<String, String> names = parseNames(json);
            String displayName = names.key();
            String internalName = names.value();

            JsonObject requirements = JsonUtils.getNullableJsonObject(json, "requirements");
            int level = requirements.get("level").getAsInt();

            ItemMaterial material = parseMaterial(json, displayName);

            ProfessionType professionType = ProfessionType.fromString(JsonUtils.getNullableJsonString(json, "subType"));

            int gatheringSpeed = JsonUtils.getNullableJsonInt(json, "gatheringSpeed");
            int durability = JsonUtils.getNullableJsonInt(json, "durability");

            GearTier gearTier = GearTier.fromString(JsonUtils.getNullableJsonString(json, "tier"));

            String emblem = JsonUtils.getNullableJsonString(json, "emblem");

            return new GatheringToolInfo(
                    displayName,
                    level,
                    internalName,
                    material,
                    professionType,
                    gatheringSpeed,
                    durability,
                    gearTier,
                    emblem);
        }

        private ItemMaterial parseMaterial(JsonObject json, String name) {
            ItemMaterial material = parseMaterial(json);

            if (material == null) {
                WynntilsMod.warn("Gathering Tool DB is missing material for " + name);
                return ItemMaterial.fromItemId("minecraft:air", 0);
            }

            return material;
        }
    }
}
