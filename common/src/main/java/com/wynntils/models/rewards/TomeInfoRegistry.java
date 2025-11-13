/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.rewards;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Models;
import com.wynntils.core.net.Dependency;
import com.wynntils.core.net.DownloadRegistry;
import com.wynntils.core.net.UrlId;
import com.wynntils.models.gear.type.GearMetaInfo;
import com.wynntils.models.gear.type.GearRestrictions;
import com.wynntils.models.gear.type.GearTier;
import com.wynntils.models.rewards.type.TomeInfo;
import com.wynntils.models.rewards.type.TomeRequirements;
import com.wynntils.models.stats.type.StatPossibleValues;
import com.wynntils.models.stats.type.StatType;
import com.wynntils.models.wynnitem.AbstractItemInfoDeserializer;
import com.wynntils.models.wynnitem.type.ItemMaterial;
import com.wynntils.models.wynnitem.type.ItemObtainInfo;
import com.wynntils.utils.JsonUtils;
import com.wynntils.utils.type.Pair;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public class TomeInfoRegistry {
    private static final Gson GSON = new GsonBuilder()
            .registerTypeHierarchyAdapter(TomeInfo.class, new TomeInfoDeserializer())
            .create();

    private List<TomeInfo> tomeInfoRegistry = List.of();
    private Map<String, TomeInfo> tomeInfoLookup = Map.of();

    public void registerDownloads(DownloadRegistry registry) {
        registry.registerDownload(
                        UrlId.DATA_STATIC_TOMES,
                        Dependency.multi(
                                Models.WynnItem,
                                Set.of(UrlId.DATA_STATIC_ITEM_OBTAIN_V2, UrlId.DATA_STATIC_MATERIAL_CONVERSION)))
                .handleJsonObject(this::loadTomeInfoRegistry);
    }

    public TomeInfo getFromDisplayName(String gearName) {
        return tomeInfoLookup.get(gearName);
    }

    public Stream<TomeInfo> getAllTomeInfos() {
        return tomeInfoRegistry.stream();
    }

    private void loadTomeInfoRegistry(JsonObject json) {
        List<TomeInfo> registry = new ArrayList<>();

        for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
            JsonObject itemObject = entry.getValue().getAsJsonObject();

            // Inject the name into the object
            itemObject.addProperty("name", entry.getKey());

            // Deserialize the item
            TomeInfo tomeInfo = GSON.fromJson(itemObject, TomeInfo.class);

            // Add the item to the registry
            registry.add(tomeInfo);
        }

        // Create fast lookup maps
        Map<String, TomeInfo> lookupMap = registry.stream()
                .collect(HashMap::new, (map, tomeInfo) -> map.put(tomeInfo.name(), tomeInfo), HashMap::putAll);

        // Make the result visible to the world
        tomeInfoRegistry = registry;
        tomeInfoLookup = lookupMap;
    }

    private static final class TomeInfoDeserializer extends AbstractItemInfoDeserializer<TomeInfo> {
        @Override
        public TomeInfo deserialize(JsonElement jsonElement, Type jsonType, JsonDeserializationContext context)
                throws JsonParseException {
            JsonObject json = jsonElement.getAsJsonObject();

            Pair<String, String> names = parseNames(json);
            String displayName = names.key();
            String internalName = names.value();

            TomeType type = parseTomeType(json);
            if (type == null) {
                throw new RuntimeException("Invalid Wynncraft data: tome has no tome type");
            }

            GearTier tier = GearTier.fromString(json.get("rarity").getAsString());
            if (tier == null) {
                throw new RuntimeException("Invalid Wynncraft data: tome has no tier");
            }

            GearMetaInfo metaInfo = parseMetaInfo(json, internalName);
            TomeRequirements requirements = parseTomeRequirements(json);

            JsonObject identifications = JsonUtils.getNullableJsonObject(json, "identifications");

            List<Pair<StatType, StatPossibleValues>> variableStats = parseVariableStats(json, "identifications");

            return new TomeInfo(displayName, type, tier, metaInfo, requirements, variableStats);
        }

        private GearMetaInfo parseMetaInfo(JsonObject json, String apiName) {
            GearRestrictions restrictions = parseRestrictions(json);
            ItemMaterial material = parseMaterial(json, apiName);

            List<ItemObtainInfo> obtainInfo = parseObtainInfo(json);

            return new GearMetaInfo(
                    restrictions, material, obtainInfo, Optional.empty(), Optional.empty(), true, false);
        }

        private ItemMaterial parseMaterial(JsonObject json, String name) {
            ItemMaterial material = parseMaterial(json);

            if (material == null) {
                WynntilsMod.warn("Tome DB is missing material for " + name);
                return ItemMaterial.getDefaultTomeItemMaterial();
            }

            return material;
        }

        private TomeRequirements parseTomeRequirements(JsonObject json) {
            JsonObject requirementsJson = json.getAsJsonObject("requirements");
            if (requirementsJson == null) {
                throw new RuntimeException("Invalid Wynncraft data: tome has no requirements");
            }

            int level = JsonUtils.getNullableJsonInt(requirementsJson, "level");
            boolean tomeSeeking = JsonUtils.getNullableJsonBoolean(requirementsJson, "tomeSeeking");

            return new TomeRequirements(level, tomeSeeking);
        }

        private TomeType parseTomeType(JsonObject json) {
            String tomeType = JsonUtils.getNullableJsonString(json, "tomeType");
            return TomeType.fromString(tomeType);
        }
    }
}
