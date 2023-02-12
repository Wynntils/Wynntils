/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.ingredients;

import com.google.common.reflect.TypeToken;
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
import com.wynntils.models.elements.type.Skill;
import com.wynntils.models.ingredients.type.IngredientInfo;
import com.wynntils.models.ingredients.type.IngredientPosition;
import com.wynntils.models.profession.type.ProfessionType;
import com.wynntils.models.stats.metadata.ItemMaterial;
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
import java.util.stream.Stream;

public class IngredientInfoRegistry {
    private List<IngredientInfo> ingredientInfoRegistry = List.of();
    private Map<String, IngredientInfo> ingredientInfoLookup = Map.of();
    private Map<String, IngredientInfo> ingredientInfoLookupApiName = Map.of();

    public IngredientInfoRegistry() {
        loadRegistry();
    }

    public void reloadData() {
        loadRegistry();
    }

    public IngredientInfo getFromDisplayName(String ingredientName) {
        return ingredientInfoLookup.get(ingredientName);
    }

    public Stream<IngredientInfo> getIngredientInfoStream() {
        return ingredientInfoRegistry.stream();
    }

    private void loadRegistry() {
        // We must download and parse ingredient player head textures before
        // attempting to parse the ingredient DB
        Download skinsDl = Managers.Net.download(UrlId.DATA_STATIC_INGREDIENT_SKINS);
        skinsDl.handleReader(skinsReader -> {
            Type type = new TypeToken<Map<String, String>>() {}.getType();
            Map<String, String> ingredientSkins = WynntilsMod.GSON.fromJson(skinsReader, type);

            // Now we can do the ingredient DB
            Download dl = Managers.Net.download(UrlId.DATA_STATIC_INGREDIENTS);
            dl.handleReader(reader -> {
                Gson ingredientInfoGson = new GsonBuilder()
                        .registerTypeHierarchyAdapter(
                                IngredientInfo.class, new IngredientInfoDeserializer(ingredientSkins))
                        .create();
                WynncraftIngredientInfoResponse ingredientInfoResponse =
                        ingredientInfoGson.fromJson(reader, WynncraftIngredientInfoResponse.class);

                // Create fast lookup maps
                List<IngredientInfo> registry = ingredientInfoResponse.ingredients;
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
        });
    }

    private static final class IngredientInfoDeserializer implements JsonDeserializer<IngredientInfo> {
        private final Map<String, String> ingredientSkins;

        private IngredientInfoDeserializer(Map<String, String> ingredientSkins) {
            this.ingredientSkins = ingredientSkins;
        }

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

            ItemMaterial material = parseMaterial(json, name);

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

            List<Pair<StatType, RangedValue>> variableStats = parseVariableStats(json);

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

        private List<ProfessionType> parseProfessions(JsonObject json) {
            List<ProfessionType> professions = new ArrayList<>();

            JsonArray professionsJson = json.get("skills").getAsJsonArray();
            for (JsonElement professionJson : professionsJson) {
                String professionName = professionJson.getAsString();
                ProfessionType professionType = ProfessionType.fromString(professionName);
                professions.add(professionType);
            }

            return Collections.unmodifiableList(professions);
        }

        private ItemMaterial parseMaterial(JsonObject json, String name) {
            JsonObject sprite = JsonUtils.getNullableJsonObject(json, "sprite");
            if (sprite.getAsJsonObject().size() == 0) {
                WynntilsMod.warn("Ingredient DB is missing sprite for " + name);
                return ItemMaterial.fromItemId("minecraft:air", 0);
            }

            int id = JsonUtils.getNullableJsonInt(sprite, "id");
            int damage = JsonUtils.getNullableJsonInt(sprite, "damage");

            if (id == 397) {
                // This is a player head. Check if we got a skin for it instead!
                String skinTexture = ingredientSkins.get(name);
                if (skinTexture != null) {
                    return ItemMaterial.fromPlayerHeadTexture(skinTexture);
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

            return Collections.unmodifiableList(skillRequirements);
        }

        private Map<IngredientPosition, Integer> getPositionModifiers(JsonObject json) {
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

        private List<Pair<StatType, RangedValue>> parseVariableStats(JsonObject json) {
            List<Pair<StatType, RangedValue>> list = new ArrayList<>();
            JsonObject identificationsJson = JsonUtils.getNullableJsonObject(json, "identifications");

            for (Map.Entry<String, JsonElement> entry : identificationsJson.entrySet()) {
                StatType statType = Models.Stat.fromInternalRollId(entry.getKey());

                if (statType == null) {
                    /*
                    FIXME
                    We are missing:
                    STRENGTHPOINTS
                    DEFENSEPOINTS
                    AGILITYPOINTS
                    INTELLIGENCEPOINTS
                    DEXTERITYPOINTS
                     */
                    // For now, just use a dummy stat
                    statType = Models.Stat.fromInternalRollId("POISON");
                }
                JsonObject rangeJson = entry.getValue().getAsJsonObject();
                int low = JsonUtils.getNullableJsonInt(rangeJson, "minimum");
                int high = JsonUtils.getNullableJsonInt(rangeJson, "maximum");

                RangedValue range = RangedValue.of(low, high);
                list.add(Pair.of(statType, range));
            }

            // Return an immutable list
            return Collections.unmodifiableList(list);
        }
    }

    protected static class WynncraftIngredientInfoResponse {
        List<IngredientInfo> ingredients;
    }
}
