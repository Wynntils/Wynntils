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
import com.wynntils.core.net.DownloadRegistry;
import com.wynntils.core.net.UrlId;
import com.wynntils.models.profession.type.CraftedItemType;
import com.wynntils.models.profession.type.ProfessionType;
import com.wynntils.models.profession.type.RecipeInfo;
import com.wynntils.models.wynnitem.AbstractItemInfoDeserializer;
import com.wynntils.utils.JsonUtils;
import com.wynntils.utils.type.RangedValue;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class RecipeInfoRegistry {
    private static final Gson GSON = new GsonBuilder()
            .registerTypeHierarchyAdapter(RecipeInfo.class, new RecipeInfoDeserializer())
            .create();

    private List<RecipeInfo> recipeInfoRegistry = List.of();
    private Map<String, RecipeInfo> recipeInfoLookup = Map.of();

    public void registerDownloads(DownloadRegistry registry) {
        registry.registerDownload(UrlId.DATA_STATIC_RECIPES).handleJsonObject(this::handleRecipes);
    }

    public RecipeInfo getFromRecipeName(String recipeName) {
        return recipeInfoLookup.get(recipeName);
    }

    public Stream<RecipeInfo> getRecipeInfoStream() {
        return recipeInfoRegistry.stream();
    }

    private void handleRecipes(JsonObject json) {
        // Create fast lookup maps
        List<RecipeInfo> registry = new ArrayList<>();

        for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
            JsonObject recipeObject = entry.getValue().getAsJsonObject();

            // Inject the recipeName into the object
            recipeObject.addProperty("recipeName", entry.getKey());

            // Deserialize the item
            RecipeInfo recipeInfo = GSON.fromJson(recipeObject, RecipeInfo.class);

            // Add the item to the registry
            registry.add(recipeInfo);
        }

        Map<String, RecipeInfo> lookupMap = new HashMap<>();
        for (RecipeInfo recipeInfo : registry) {
            lookupMap.put(recipeInfo.recipeName(), recipeInfo);
        }

        // Make the result visible to the world
        recipeInfoRegistry = registry;
        recipeInfoLookup = lookupMap;
    }

    private static final class RecipeInfoDeserializer extends AbstractItemInfoDeserializer<RecipeInfo> {
        @Override
        public RecipeInfo deserialize(JsonElement jsonElement, Type jsonType, JsonDeserializationContext context)
                throws JsonParseException {
            JsonObject json = jsonElement.getAsJsonObject();

            String name = json.get("recipeName").getAsString();

            JsonObject levelObject = json.get("level").getAsJsonObject();
            int levelMin = levelObject.get("minimum").getAsInt();
            int levelMax = levelObject.get("maximum").getAsInt();
            RangedValue level = RangedValue.of(levelMin, levelMax);

            ProfessionType professionType =
                    ProfessionType.fromString(json.get("skill").getAsString());
            CraftedItemType craftedItemType =
                    CraftedItemType.fromString(json.get("type").getAsString());
            int xp = JsonUtils.getNullableJsonInt(json, "xp");

            RangedValue healthOrDamage = RangedValue.NONE;
            // Accessories do not have health or damage
            if (!craftedItemType.isAccessory()) {
                JsonObject healthOrDamageObject = json.get("healthOrDamage").getAsJsonObject();
                int healthOrDamageMin = healthOrDamageObject.get("minimum").getAsInt();
                int healthOrDamageMax = healthOrDamageObject.get("maximum").getAsInt();
                healthOrDamage = RangedValue.of(healthOrDamageMin, healthOrDamageMax);
            }

            RangedValue durabilityOrDuration;
            if (craftedItemType.isGear()) {
                // Durability modifier is multiplied by 1000 in the API, so we divide it here
                // (there is no known reason for this)
                JsonObject durabilityObject = json.get("durability").getAsJsonObject();
                int durabilityMin = durabilityObject.get("minimum").getAsInt() / 1000;
                int durabilityMax = durabilityObject.get("maximum").getAsInt() / 1000;
                durabilityOrDuration = RangedValue.of(durabilityMin, durabilityMax);
            } else {
                JsonObject durabilityObject = json.get("basicDuration").getAsJsonObject();
                int durabilityMin = durabilityObject.get("minimum").getAsInt() / 1000;
                int durabilityMax = durabilityObject.get("maximum").getAsInt() / 1000;
                durabilityOrDuration = RangedValue.of(durabilityMin, durabilityMax);
            }

            Map<String, Integer> materialAmounts = new HashMap<>();
            for (JsonElement materialElement : json.getAsJsonArray("materials")) {
                JsonObject material = materialElement.getAsJsonObject();
                materialAmounts.put(
                        material.get("item").getAsString(),
                        material.get("amount").getAsInt());
            }

            return new RecipeInfo(
                    name,
                    durabilityOrDuration,
                    healthOrDamage,
                    level,
                    materialAmounts,
                    professionType,
                    craftedItemType,
                    xp);
        }
    }
}
