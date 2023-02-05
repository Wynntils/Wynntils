/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.gear.ingredients;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Models;
import com.wynntils.core.net.Download;
import com.wynntils.core.net.UrlId;
import com.wynntils.models.concepts.ProfessionType;
import com.wynntils.models.concepts.Skill;
import com.wynntils.models.gear.type.GearMaterial;
import com.wynntils.models.stats.StatCalculator;
import com.wynntils.models.stats.type.StatPossibleValues;
import com.wynntils.models.stats.type.StatType;
import com.wynntils.utils.JsonUtils;
import com.wynntils.utils.type.Pair;
import com.wynntils.utils.type.RangedValue;
import com.wynntils.utils.wynn.WynnUtils;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class IngredientInfoRegistry {
    // This is a list of entries in the API that do not really reflect actual ingredients in Wynncraft
    // FIXME: This should be read from an external json file, and have more entries added to it
    private static final List<String> INVALID_ENTRIES = List.of();

    List<IngredientInfo> ingredientInfoRegistry = List.of();
    Map<String, IngredientInfo> ingredientInfoLookup = Map.of();
    Map<String, IngredientInfo> ingredientInfoLookupApiName = Map.of();

    public IngredientInfoRegistry() {
        loadRegistry();
    }

    public void reloadData() {
        loadRegistry();
    }

    private void loadRegistry() {
        Download dl = Managers.Net.download(UrlId.DATA_STATIC_INGREDIENTS);
        dl.handleReader(reader -> {
            Gson ingredientInfoGson = new GsonBuilder()
                    .registerTypeHierarchyAdapter(IngredientInfo.class, new IngredientInfoDeserializer())
                    .create();
            WynncraftIngredientInfoResponse ingredientInfoResponse =
                    ingredientInfoGson.fromJson(reader, WynncraftIngredientInfoResponse.class);

            // Some entries are test entries etc and should be removed
            // FIXME: Is this really needed for ingredients?
            List<IngredientInfo> registry = ingredientInfoResponse.ingredients.stream()
                    .filter(ingredientInfo -> !INVALID_ENTRIES.contains(ingredientInfo.name()))
                    .toList();

            // Create fast lookup maps
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

    private static final class IngredientInfoDeserializer implements JsonDeserializer<IngredientInfo> {
        @Override
        public IngredientInfo deserialize(JsonElement jsonElement, Type jsonType, JsonDeserializationContext context)
                throws JsonParseException {
            JsonObject json = jsonElement.getAsJsonObject();

            String primaryName = WynnUtils.normalizeBadString(json.get("name").getAsString());
            String secondaryName = WynnUtils.normalizeBadString(JsonUtils.getNullableJsonString(json, "displayName"));

            // After normalization, we can end up with the same name. If so, treat this as not having
            // a secondary name.
            if (primaryName.equals(secondaryName)) {
                secondaryName = "";
            }

            // The real name (display name) is the secondaryName if it exists, otherwise it is
            // the primary name.
            // If the secondary name exists, the primary name is the apiName. If the apiName
            // does not exist, the api name is the same as the displayName.
            String name = secondaryName.isEmpty() ? primaryName : secondaryName;
            String apiName = secondaryName.isEmpty() ? null : primaryName;
            Optional<String> apiNameOpt = Optional.ofNullable(apiName);

            int tier = JsonUtils.getNullableJsonInt(json, "tier");
            int level = json.get("level").getAsInt();

            List<ProfessionType> professions = parseProfessions(json);

            GearMaterial material;
            // FIXME: Materials are missing a lot of values, e.g. 383 (enderman_spawn_egg?)
            material = parseMaterial(json);
            if (material == null) {
                // FIXME: Bad?
                WynntilsMod.warn("Ingredient DB is missing sprite for " + name);
                material = GearMaterial.UNKNOWN;
            }

            // Get consumables-only parts
            JsonObject consumableIdsJson = JsonUtils.getNullableJsonObject(json, "consumableOnlyIDs");
            int duration = JsonUtils.getNullableJsonInt(consumableIdsJson, "duration");
            int charges = JsonUtils.getNullableJsonInt(consumableIdsJson, "charges");

            // Get items-only parts
            JsonObject itemIdsJson = JsonUtils.getNullableJsonObject(json, "itemOnlyIDs");
            int durabilityModifier = JsonUtils.getNullableJsonInt(itemIdsJson, "durabilityModifier");
            List<Pair<Skill, Integer>> skillRequirements = getSkillRequirements(itemIdsJson);

            // Get recipe position format modifiers
            Map<IngredientPosition, Integer> positionModifiers = getPositionModifiers(json);

            /*            json fields:                 "identifications"            */
            List<Pair<StatType, StatPossibleValues>> variableStats = null; // parseVariableStats(json);

            return new IngredientInfo(
                    name,
                    tier,
                    level,
                    apiNameOpt,
                    material,
                    professions,
                    skillRequirements,
                    positionModifiers,
                    duration,
                    charges,
                    durabilityModifier,
                    variableStats);
        }

        private static List<ProfessionType> parseProfessions(JsonObject json) {
            List<ProfessionType> professions = new ArrayList<>();

            JsonArray professionsJson = json.get("skills").getAsJsonArray();
            for (JsonElement professionJson : professionsJson) {
                String professionName = professionJson.getAsString();
                ProfessionType professionType = ProfessionType.fromString(professionName);
                professions.add(professionType);
            }

            return Collections.unmodifiableList(professions);
        }

        private static GearMaterial parseMaterial(JsonObject json) {
            JsonObject sprite = JsonUtils.getNullableJsonObject(json, "sprite");
            if (sprite.getAsJsonObject().size() == 0) return null;

            int id = JsonUtils.getNullableJsonInt(sprite, "id");
            int damage = JsonUtils.getNullableJsonInt(sprite, "damage");

            return GearMaterial.fromItemTypeCode(id, damage);
        }

        private static List<Pair<Skill, Integer>> getSkillRequirements(JsonObject itemIdsJson) {
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

            return Collections.unmodifiableList(skillRequirements);
        }

        private static Map<IngredientPosition, Integer> getPositionModifiers(JsonObject json) {
            JsonObject positionsJson = JsonUtils.getNullableJsonObject(json, "ingredientPositionModifiers");
            if (positionsJson.size() == 0) return Map.of();

            Map<IngredientPosition, Integer> positionModifiers = new HashMap<>();
            for (IngredientPosition position : IngredientPosition.values()) {
                int value = JsonUtils.getNullableJsonInt(positionsJson, position.getApiName());
                if (value == 0) continue;

                positionModifiers.put(position, value);
            }

            return Collections.unmodifiableMap(positionModifiers);
        }

        private List<Pair<StatType, StatPossibleValues>> parseVariableStats(JsonObject json) {
            List<Pair<StatType, StatPossibleValues>> list = new ArrayList<>();
            JsonElement identifiedJson = json.get("identified");
            boolean preIdentified = identifiedJson != null && identifiedJson.getAsBoolean();

            for (StatType statType : Models.Stat.getAllStatTypes()) {
                JsonElement statJson = json.get(statType.getApiName());
                if (statJson == null) continue;

                int baseValue = statJson.getAsInt();
                if (baseValue == 0) continue;

                // "Inverted" stats (i.e. spell costs) will be stored as a positive value,
                // and only converted to negative at display time.
                if (statType.showAsInverted()) {
                    baseValue = -baseValue;
                }
                // Range will always be stored such as "low" means "worst possible value" and
                // "high" means "best possible value".
                RangedValue range = StatCalculator.calculatePossibleValuesRange(baseValue, preIdentified);
                StatPossibleValues possibleValues = new StatPossibleValues(statType, range, baseValue, preIdentified);
                list.add(Pair.of(statType, possibleValues));
            }

            // Return an immutable list
            return List.copyOf(list);
        }
    }

    protected static class WynncraftIngredientInfoResponse {
        List<IngredientInfo> ingredients;
    }
}
