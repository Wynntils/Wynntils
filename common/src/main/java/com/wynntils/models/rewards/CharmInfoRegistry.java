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
import com.wynntils.models.rewards.type.CharmInfo;
import com.wynntils.models.rewards.type.CharmRequirements;
import com.wynntils.models.stats.type.StatPossibleValues;
import com.wynntils.models.stats.type.StatType;
import com.wynntils.models.wynnitem.AbstractItemInfoDeserializer;
import com.wynntils.models.wynnitem.type.ItemMaterial;
import com.wynntils.models.wynnitem.type.ItemObtainInfo;
import com.wynntils.utils.JsonUtils;
import com.wynntils.utils.type.Pair;
import com.wynntils.utils.type.RangedValue;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public class CharmInfoRegistry {
    private static final Gson GSON = new GsonBuilder()
            .registerTypeHierarchyAdapter(CharmInfo.class, new CharmInfoDeserializer())
            .create();

    private List<CharmInfo> charmInfoRegistry = List.of();
    private Map<String, CharmInfo> charmInfoLookup = Map.of();

    public void registerDownloads(DownloadRegistry registry) {
        registry.registerDownload(
                        UrlId.DATA_STATIC_CHARMS,
                        Dependency.multi(
                                Models.WynnItem,
                                Set.of(UrlId.DATA_STATIC_ITEM_OBTAIN_V2, UrlId.DATA_STATIC_MATERIAL_CONVERSION)))
                .handleJsonObject(this::handleCharmInfoRegistry);
    }

    public CharmInfo getFromDisplayName(String gearName) {
        return charmInfoLookup.get(gearName);
    }

    public Stream<CharmInfo> getAllCharmInfos() {
        return charmInfoRegistry.stream();
    }

    private void handleCharmInfoRegistry(JsonObject json) {
        List<CharmInfo> registry = new ArrayList<>();

        for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
            JsonObject itemObject = entry.getValue().getAsJsonObject();

            // Inject the name into the object
            itemObject.addProperty("name", entry.getKey());

            // Deserialize the item
            CharmInfo charmInfo = GSON.fromJson(itemObject, CharmInfo.class);

            // Add the item to the registry
            registry.add(charmInfo);
        }

        // Create fast lookup maps
        Map<String, CharmInfo> lookupMap = registry.stream()
                .collect(HashMap::new, (map, charmInfo) -> map.put(charmInfo.name(), charmInfo), HashMap::putAll);

        // Make the result visible to the world
        charmInfoRegistry = registry;
        charmInfoLookup = lookupMap;
    }

    private static final class CharmInfoDeserializer extends AbstractItemInfoDeserializer<CharmInfo> {
        @Override
        public CharmInfo deserialize(JsonElement jsonElement, Type jsonType, JsonDeserializationContext context)
                throws JsonParseException {
            JsonObject json = jsonElement.getAsJsonObject();

            Pair<String, String> names = parseNames(json);
            String displayName = names.key();
            String internalName = names.value();

            GearTier tier = GearTier.fromString(json.get("rarity").getAsString());
            if (tier == null) {
                throw new RuntimeException("Invalid Wynncraft data: charm has no tier");
            }

            GearMetaInfo metaInfo = parseMetaInfo(json, internalName);
            CharmRequirements requirements = parseCharmRequirements(json);

            // Base stats are parsed the same way as variable stats
            List<Pair<StatType, StatPossibleValues>> baseStats = parseVariableStats(json, "base");
            List<Pair<StatType, StatPossibleValues>> variableStats = parseVariableStats(json, "identifications");

            // For now, concat base and variable stats, they are the same from our perspective
            return new CharmInfo(
                    displayName,
                    tier,
                    metaInfo,
                    requirements,
                    Stream.concat(baseStats.stream(), variableStats.stream()).toList());
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
                WynntilsMod.warn("Charm DB is missing material for " + name);
                return ItemMaterial.getDefaultCharmItemMaterial();
            }

            return material;
        }

        private CharmRequirements parseCharmRequirements(JsonObject json) {
            JsonObject requirementsJson = json.getAsJsonObject("requirements");
            if (requirementsJson == null) {
                throw new RuntimeException("Invalid Wynncraft data: charm has no requirements");
            }

            int level = JsonUtils.getNullableJsonInt(requirementsJson, "level");
            JsonObject levelRangeJson = JsonUtils.getNullableJsonObject(requirementsJson, "levelRange");
            int min = levelRangeJson.get("min").getAsInt();
            int max = levelRangeJson.get("max").getAsInt();

            return new CharmRequirements(level, RangedValue.of(min, max));
        }

        private TomeType parseTomeType(JsonObject json) {
            String tomeType = JsonUtils.getNullableJsonString(json, "tomeType");
            return TomeType.fromString(tomeType);
        }
    }
}
