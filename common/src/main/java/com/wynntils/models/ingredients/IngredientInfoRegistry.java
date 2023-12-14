/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.ingredients;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Models;
import com.wynntils.core.net.Download;
import com.wynntils.core.net.UrlId;
import com.wynntils.core.net.event.NetResultProcessedEvent;
import com.wynntils.models.elements.type.Skill;
import com.wynntils.models.ingredients.type.IngredientInfo;
import com.wynntils.models.ingredients.type.IngredientPosition;
import com.wynntils.models.profession.type.ProfessionType;
import com.wynntils.models.stats.type.StatType;
import com.wynntils.models.wynnitem.AbstractItemInfoDeserializer;
import com.wynntils.models.wynnitem.type.ItemMaterial;
import com.wynntils.utils.JsonUtils;
import com.wynntils.utils.type.Pair;
import com.wynntils.utils.type.RangedValue;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class IngredientInfoRegistry {
    private List<IngredientInfo> ingredientInfoRegistry = List.of();
    private Map<String, IngredientInfo> ingredientInfoLookup = Map.of();
    private Map<String, IngredientInfo> ingredientInfoLookupApiName = Map.of();

    public IngredientInfoRegistry() {
        WynntilsMod.registerEventListener(this);

        loadData();
    }

    public void loadData() {
        // We do not explicitly load the ingredient DB here,
        // but when all of it's dependencies are loaded,
        // the NetResultProcessedEvent will trigger the load.
    }

    public IngredientInfo getFromDisplayName(String ingredientName) {
        return ingredientInfoLookup.get(ingredientName);
    }

    public Stream<IngredientInfo> getIngredientInfoStream() {
        return ingredientInfoRegistry.stream();
    }

    @SubscribeEvent
    public void onDataLoaded(NetResultProcessedEvent.ForUrlId event) {
        UrlId urlId = event.getUrlId();
        if (urlId == UrlId.DATA_STATIC_ITEM_OBTAIN || urlId == UrlId.DATA_STATIC_MATERIAL_CONVERSION) {
            // We need both material conversio  and obtain info to be able to load the ingredient DB
            if (!Models.WynnItem.hasObtainInfo()) return;
            if (!Models.WynnItem.hasMaterialConversionInfo()) return;

            loadIngredients();
            return;
        }
    }

    private void loadIngredients() {
        // Download and parse the ingredient DB
        Download dl = Managers.Net.download(UrlId.DATA_STATIC_INGREDIENTS_ADVANCED);
        dl.handleJsonObject(json -> {
            Gson gson = new GsonBuilder()
                    .registerTypeHierarchyAdapter(IngredientInfo.class, new IngredientInfoDeserializer())
                    .create();

            // Create fast lookup maps
            List<IngredientInfo> registry = new ArrayList<>();

            for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                JsonObject ingredientObject = entry.getValue().getAsJsonObject();

                // Inject the name into the object
                ingredientObject.addProperty("name", entry.getKey());

                // Deserialize the item
                IngredientInfo ingredientInfo = gson.fromJson(ingredientObject, IngredientInfo.class);

                // Add the item to the registry
                registry.add(ingredientInfo);
            }

            Map<String, IngredientInfo> lookupMap = new HashMap<>();
            Map<String, IngredientInfo> altLookupMap = new HashMap<>();
            for (IngredientInfo ingredientInfo : registry) {
                lookupMap.put(ingredientInfo.name(), ingredientInfo);
                if (ingredientInfo.apiName().isPresent()) {
                    altLookupMap.put(ingredientInfo.apiName().get(), ingredientInfo);
                }
            }

            // Make the result visisble to the world
            ingredientInfoRegistry = registry;
            ingredientInfoLookup = lookupMap;
            ingredientInfoLookupApiName = altLookupMap;
        });
    }

    private static final class IngredientInfoDeserializer extends AbstractItemInfoDeserializer<IngredientInfo> {
        @Override
        public IngredientInfo deserialize(JsonElement jsonElement, Type jsonType, JsonDeserializationContext context)
                throws JsonParseException {
            JsonObject json = jsonElement.getAsJsonObject();

            Pair<String, String> names = parseNames(json);
            String displayName = names.key();
            String internalName = names.value();

            Optional<String> internalNameOpt = Optional.ofNullable(internalName);

            int tier = JsonUtils.getNullableJsonInt(json, "tier");

            JsonObject requirements = JsonUtils.getNullableJsonObject(json, "requirements");
            int level = requirements.get("level").getAsInt();

            List<ProfessionType> professions = parseProfessions(requirements);

            ItemMaterial material = parseMaterial(json, displayName);

            // Get consumables-only parts
            JsonObject consumableIdsJson = JsonUtils.getNullableJsonObject(json, "consumableOnlyIDs");
            int duration = JsonUtils.getNullableJsonInt(consumableIdsJson, "duration");
            int charges = JsonUtils.getNullableJsonInt(consumableIdsJson, "charges");

            // Get items-only parts
            JsonObject itemIdsJson = JsonUtils.getNullableJsonObject(json, "itemOnlyIDs");
            // Durability modifier is multiplied by 1000 in the API, so we divide it here
            // (there is no known reason for this)
            int durabilityModifier = JsonUtils.getNullableJsonInt(itemIdsJson, "durabilityModifier") / 1000;
            List<Pair<Skill, Integer>> skillRequirements = getSkillRequirements(itemIdsJson);

            // Get recipe position format modifiers
            Map<IngredientPosition, Integer> positionModifiers = getPositionModifiers(json);

            List<Pair<StatType, RangedValue>> variableStats = parseVariableIngredientStats(json);

            return new IngredientInfo(
                    displayName,
                    tier,
                    level,
                    internalNameOpt,
                    material,
                    professions,
                    skillRequirements,
                    positionModifiers,
                    duration,
                    charges,
                    durabilityModifier,
                    variableStats);
        }

        private List<ProfessionType> parseProfessions(JsonObject json) {
            List<ProfessionType> professions = new ArrayList<>();

            JsonArray professionsJson = json.get("skills").getAsJsonArray();
            for (JsonElement professionJson : professionsJson) {
                String professionName = professionJson.getAsString();
                ProfessionType professionType = ProfessionType.fromString(professionName);
                professions.add(professionType);
            }

            return List.copyOf(professions);
        }

        private ItemMaterial parseMaterial(JsonObject json, String name) {
            String material = JsonUtils.getNullableJsonString(json, "material");
            if (material == null || material.isEmpty()) {
                WynntilsMod.warn("Ingredient DB is missing material for " + name);
                return ItemMaterial.fromItemId("minecraft:air", 0);
            }

            String[] materialParts = material.split(":");

            int id = Integer.parseInt(materialParts[0]);
            int damage = Integer.parseInt(materialParts[1]);

            if (id == 397) {
                // This is a player head. Check if we got a skin for it instead!
                String skinTexture = JsonUtils.getNullableJsonString(json, "skin");
                if (skinTexture != null) {
                    return ItemMaterial.fromPlayerHeadUUID(skinTexture);
                }
            }

            return ItemMaterial.fromItemTypeCode(id, damage);
        }

        private List<Pair<Skill, Integer>> getSkillRequirements(JsonObject itemIdsJson) {
            if (itemIdsJson == null) return List.of();

            List<Pair<Skill, Integer>> skillRequirements = new ArrayList<>();

            for (Skill skill : Skill.values()) {
                // In particular, note that the API spelling "defense" is not used
                String requirementName = skill.getDisplayName().toLowerCase(Locale.ROOT) + "Requirement";
                int requirementValue = JsonUtils.getNullableJsonInt(itemIdsJson, requirementName);
                if (requirementValue != 0) {
                    skillRequirements.add(Pair.of(skill, requirementValue));
                }
            }

            return List.copyOf(skillRequirements);
        }

        private Map<IngredientPosition, Integer> getPositionModifiers(JsonObject json) {
            JsonObject positionsJson = JsonUtils.getNullableJsonObject(json, "ingredientPositionModifiers");
            if (positionsJson.isEmpty()) return Map.of();

            Map<IngredientPosition, Integer> positionModifiers = new HashMap<>();
            for (IngredientPosition position : IngredientPosition.values()) {
                int value = JsonUtils.getNullableJsonInt(positionsJson, position.getApiName());
                if (value == 0) continue;

                positionModifiers.put(position, value);
            }

            return Map.copyOf(positionModifiers);
        }

        private List<Pair<StatType, RangedValue>> parseVariableIngredientStats(JsonObject json) {
            List<Pair<StatType, RangedValue>> list = new ArrayList<>();
            JsonObject identificationsJson = JsonUtils.getNullableJsonObject(json, "identifications");

            for (Map.Entry<String, JsonElement> entry : identificationsJson.entrySet()) {
                StatType statType = Models.Stat.fromApiRollId(entry.getKey());

                if (statType == null) {
                    WynntilsMod.warn("Ingredient DB contains invalid stat type " + entry.getKey());
                    continue;
                }

                JsonObject rangeJson = entry.getValue().getAsJsonObject();
                int low = JsonUtils.getNullableJsonInt(rangeJson, "min");
                int high = JsonUtils.getNullableJsonInt(rangeJson, "max");

                RangedValue range = RangedValue.of(low, high);
                list.add(Pair.of(statType, range));
            }

            // Return an immutable list
            return List.copyOf(list);
        }
    }
}
