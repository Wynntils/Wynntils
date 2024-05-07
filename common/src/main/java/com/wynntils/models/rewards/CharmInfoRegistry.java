/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.rewards;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Models;
import com.wynntils.core.net.Download;
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
import java.util.stream.Stream;

public class CharmInfoRegistry {
    private List<CharmInfo> charmInfoRegistry = List.of();
    private Map<String, CharmInfo> charmInfoLookup = Map.of();

    public CharmInfoRegistry() {
        reloadData();
    }

    public void reloadData() {
        loadCharmInfoRegistry();
    }

    public CharmInfo getFromDisplayName(String gearName) {
        return charmInfoLookup.get(gearName);
    }

    public Stream<CharmInfo> getAllCharmInfos() {
        return charmInfoRegistry.stream();
    }

    private void loadCharmInfoRegistry() {
        if (!Models.WynnItem.hasObtainInfo()) return;
        if (!Models.WynnItem.hasMaterialConversionInfo()) return;

        Download dl = Managers.Net.download(UrlId.DATA_STATIC_CHARMS);
        dl.handleJsonObject(json -> {
            Gson gson = new GsonBuilder()
                    .registerTypeHierarchyAdapter(CharmInfo.class, new CharmInfoDeserizalier())
                    .create();

            List<CharmInfo> registry = new ArrayList<>();

            for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                JsonObject itemObject = entry.getValue().getAsJsonObject();

                // Inject the name into the object
                itemObject.addProperty("name", entry.getKey());

                // Deserialize the item
                CharmInfo charmInfo = gson.fromJson(itemObject, CharmInfo.class);

                // Add the item to the registry
                registry.add(charmInfo);
            }

            // Create fast lookup maps
            Map<String, CharmInfo> lookupMap = registry.stream()
                    .collect(HashMap::new, (map, charmInfo) -> map.put(charmInfo.name(), charmInfo), HashMap::putAll);

            // Make the result visisble to the world
            charmInfoRegistry = registry;
            charmInfoLookup = lookupMap;
        });
    }

    private static final class CharmInfoDeserizalier extends AbstractItemInfoDeserializer<CharmInfo> {
        @Override
        public CharmInfo deserialize(JsonElement jsonElement, Type jsonType, JsonDeserializationContext context)
                throws JsonParseException {
            JsonObject json = jsonElement.getAsJsonObject();

            Pair<String, String> names = parseNames(json);
            String displayName = names.key();
            String internalName = names.value();

            GearTier tier = GearTier.fromString(json.get("tier").getAsString());
            if (tier == null) {
                throw new RuntimeException("Invalid Wynncraft data: charm has no tier");
            }

            GearMetaInfo metaInfo = parseMetaInfo(json, internalName);
            CharmRequirements requirements = parseCharmRequirements(json);

            // Base stats are parsed the same way as variable stats
            List<Pair<StatType, StatPossibleValues>> variableStats = parseVariableStats(json, "identifications");

            return new CharmInfo(displayName, tier, metaInfo, requirements, variableStats);
        }

        private GearMetaInfo parseMetaInfo(JsonObject json, String apiName) {
            GearRestrictions restrictions = parseRestrictions(json);
            ItemMaterial material = parseOtherMaterial(json);

            List<ItemObtainInfo> obtainInfo = parseObtainInfo(json);

            return new GearMetaInfo(
                    restrictions, material, obtainInfo, Optional.empty(), Optional.empty(), true, false);
        }

        private ItemMaterial parseOtherMaterial(JsonObject json) {
            String material = JsonUtils.getNullableJsonString(json, "material");
            if (material == null) {
                // We're screwed.
                // The best we can do is to give a generic default representation
                return ItemMaterial.getDefaultCharmItemMaterial();
            }

            String[] materialArray = material.split(":");
            int itemTypeCode = Integer.parseInt(materialArray[0]);
            int damageCode = materialArray.length > 1 ? Integer.parseInt(materialArray[1]) : 0;
            return ItemMaterial.fromItemTypeCode(itemTypeCode, damageCode);
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
