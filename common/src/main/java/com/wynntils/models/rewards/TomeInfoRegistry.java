/*
 * Copyright © Wynntils 2023.
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
import com.wynntils.core.components.Managers;
import com.wynntils.core.net.Download;
import com.wynntils.core.net.UrlId;
import com.wynntils.models.elements.type.Skill;
import com.wynntils.models.gear.type.GearDropRestrictions;
import com.wynntils.models.gear.type.GearMetaInfo;
import com.wynntils.models.gear.type.GearRestrictions;
import com.wynntils.models.gear.type.GearTier;
import com.wynntils.models.rewards.type.TomeInfo;
import com.wynntils.models.rewards.type.TomeRequirements;
import com.wynntils.models.rewards.type.TomeVariant;
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

public class TomeInfoRegistry {
    private List<TomeInfo> tomeInfoRegistry = List.of();
    private Map<String, TomeInfo> tomeInfoLookup = Map.of();

    public TomeInfoRegistry() {
        WynntilsMod.registerEventListener(this);

        reloadData();
    }

    public void reloadData() {
        loadTomeInfoRegistry();
    }

    public TomeInfo getFromDisplayName(String gearName) {
        return tomeInfoLookup.get(gearName);
    }

    private void loadTomeInfoRegistry() {
        Download dl = Managers.Net.download(UrlId.DATA_STATIC_TOMES);
        dl.handleJsonObject(json -> {
            Gson gson = new GsonBuilder()
                    .registerTypeHierarchyAdapter(TomeInfo.class, new TomeInfoDeserizalier())
                    .create();

            List<TomeInfo> registry = new ArrayList<>();

            for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                JsonObject itemObject = entry.getValue().getAsJsonObject();

                // Inject the name into the object
                itemObject.addProperty("name", entry.getKey());

                // Deserialize the item
                TomeInfo tomeInfo = gson.fromJson(itemObject, TomeInfo.class);

                // Add the item to the registry
                registry.add(tomeInfo);
            }

            // Create fast lookup maps
            Map<String, TomeInfo> lookupMap = registry.stream()
                    .collect(HashMap::new, (map, tomeInfo) -> map.put(tomeInfo.name(), tomeInfo), HashMap::putAll);

            // Make the result visisble to the world
            tomeInfoRegistry = registry;
            tomeInfoLookup = lookupMap;
        });
    }

    private static final class TomeInfoDeserizalier extends AbstractItemInfoDeserializer<TomeInfo> {
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

            GearTier tier = GearTier.fromString(json.get("tier").getAsString());
            if (tier == null) {
                throw new RuntimeException("Invalid Wynncraft data: tome has no tier");
            }

            TomeVariant variant = TomeVariant.fromString(json.get("tomeVariant").getAsString());
            if (variant == null) {
                throw new RuntimeException("Invalid Wynncraft data: tome has no tome variant");
            }

            GearMetaInfo metaInfo = parseMetaInfo(json, displayName, internalName, type);
            TomeRequirements requirements = parseTomeRequirements(json);

            JsonObject identifications = JsonUtils.getNullableJsonObject(json, "identifications");
            List<Pair<Skill, Integer>> skillBonuses = parseSkillBonuses(identifications);

            List<Pair<StatType, StatPossibleValues>> variableStats = parseVariableStats(json);

            return new TomeInfo(displayName, type, variant, tier, metaInfo, requirements, skillBonuses, variableStats);
        }

        private GearMetaInfo parseMetaInfo(JsonObject json, String name, String apiName, TomeType type) {
            GearDropRestrictions dropRestrictions = parseDropRestrictions(json);
            GearRestrictions restrictions = parseRestrictions(json);
            ItemMaterial material = parseOtherMaterial(json, type);

            List<ItemObtainInfo> obtainInfo = parseObtainInfo(json, name);

            return new GearMetaInfo(
                    dropRestrictions,
                    restrictions,
                    material,
                    obtainInfo,
                    Optional.empty(),
                    Optional.empty(),
                    true,
                    false);
        }

        private ItemMaterial parseOtherMaterial(JsonObject json, TomeType tomeType) {
            String material = JsonUtils.getNullableJsonString(json, "material");
            if (material == null) {
                // We're screwed.
                // The best we can do is to give a generic default representation
                return ItemMaterial.getDefaultTomeItemMaterial();
            }

            String[] materialArray = material.split(":");
            int itemTypeCode = Integer.parseInt(materialArray[0]);
            int damageCode = materialArray.length > 1 ? Integer.parseInt(materialArray[1]) : 0;
            return ItemMaterial.fromItemTypeCode(itemTypeCode, damageCode);
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
