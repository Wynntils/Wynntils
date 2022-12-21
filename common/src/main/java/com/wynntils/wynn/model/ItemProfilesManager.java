/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.managers.Manager;
import com.wynntils.core.managers.Managers;
import com.wynntils.core.net.Download;
import com.wynntils.core.net.NetManager;
import com.wynntils.core.net.UrlId;
import com.wynntils.wynn.item.IdentificationOrderer;
import com.wynntils.wynn.objects.profiles.ItemGuessProfile;
import com.wynntils.wynn.objects.profiles.ingredient.IngredientProfile;
import com.wynntils.wynn.objects.profiles.item.ItemProfile;
import com.wynntils.wynn.objects.profiles.item.ItemType;
import com.wynntils.wynn.objects.profiles.item.MajorIdentification;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ItemProfilesManager extends Manager {
    private static final Gson ITEM_GUESS_GSON = new GsonBuilder()
            .registerTypeHierarchyAdapter(HashMap.class, new ItemGuessProfile.ItemGuessDeserializer())
            .create();

    private Map<String, ItemProfile> items = Map.of();
    private Map<String, ItemGuessProfile> itemGuesses = Map.of();
    private Map<String, String> translatedReferences = Map.of();
    private Map<String, String> internalIdentifications = Map.of();
    private Map<String, MajorIdentification> majorIdsMap = Map.of();
    private Map<ItemType, String[]> materialTypes = Map.of();
    private Map<String, IngredientProfile> ingredients = Map.of();
    private Map<String, String> ingredientHeadTextures = Map.of();

    public ItemProfilesManager(NetManager netManager) {
        super(List.of(netManager));
        loadData();
    }

    private void loadData() {
        tryLoadItemList();
        tryLoadItemGuesses();
        tryLoadIngredientList();
    }

    private void tryLoadItemGuesses() {
        Download dl = Managers.Net.download(UrlId.DATA_STATIC_ITEM_GUESSES);
        dl.handleReader(reader -> {
            Type type = new TypeToken<HashMap<String, ItemGuessProfile>>() {}.getType();
            Map<String, ItemGuessProfile> newItemGuesses = new HashMap<>();
            newItemGuesses.putAll(ITEM_GUESS_GSON.fromJson(reader, type));
            itemGuesses = newItemGuesses;
        });
    }

    private void tryLoadItemList() {
        // dataAthenaItemList is based on
        // https://api.wynncraft.com/public_api.php?action=itemDB&category=all
        // but the data is massaged into another form, and wynnBuilderID is injected from
        // https://wynnbuilder.github.io/compress.json

        Download dl = Managers.Net.download(UrlId.DATA_ATHENA_ITEM_LIST);
        dl.handleJsonObject(json -> {
            Type hashmapType = new TypeToken<HashMap<String, String>>() {}.getType();
            translatedReferences = WynntilsMod.GSON.fromJson(json.getAsJsonObject("translatedReferences"), hashmapType);

            internalIdentifications =
                    WynntilsMod.GSON.fromJson(json.getAsJsonObject("internalIdentifications"), hashmapType);

            Type majorIdsType = new TypeToken<HashMap<String, MajorIdentification>>() {}.getType();
            majorIdsMap = WynntilsMod.GSON.fromJson(json.getAsJsonObject("majorIdentifications"), majorIdsType);

            Type materialTypesType = new TypeToken<HashMap<ItemType, String[]>>() {}.getType();
            materialTypes = WynntilsMod.GSON.fromJson(json.getAsJsonObject("materialTypes"), materialTypesType);

            // FIXME: We should not be doing Singleton housekeeping for IdentificationOrderer!
            IdentificationOrderer.INSTANCE =
                    WynntilsMod.GSON.fromJson(json.getAsJsonObject("identificationOrder"), IdentificationOrderer.class);

            ItemProfile[] jsonItems = WynntilsMod.GSON.fromJson(json.getAsJsonArray("items"), ItemProfile[].class);
            HashMap<String, ItemProfile> newItems = new HashMap<>();
            for (ItemProfile itemProfile : jsonItems) {
                itemProfile.getStatuses().forEach((shortId, idProfile) -> idProfile.calculateMinMax(shortId));
                itemProfile.updateMajorIdsFromStrings(majorIdsMap);
                itemProfile.registerIdTypes();

                newItems.put(itemProfile.getDisplayName(), itemProfile);
            }

            items = newItems;
        });
    }

    private void tryLoadIngredientList() {
        // dataAthenaIngredientList is based on
        // https://api.wynncraft.com/v2/ingredient/search/skills/%5Etailoring,armouring,jeweling,cooking,woodworking,weaponsmithing,alchemism,scribing
        // but the data is massaged into another form, and additional "head textures" are added, which are hard-coded
        // in Athena

        Download dl = Managers.Net.download(UrlId.DATA_ATHENA_INGREDIENT_LIST);
        dl.handleJsonObject(json -> {
            Type hashmapType = new TypeToken<HashMap<String, String>>() {}.getType();
            ingredientHeadTextures = WynntilsMod.GSON.fromJson(json.getAsJsonObject("headTextures"), hashmapType);

            IngredientProfile[] jsonItems =
                    WynntilsMod.GSON.fromJson(json.getAsJsonArray("ingredients"), IngredientProfile[].class);

            Map<String, IngredientProfile> newIngredients = new HashMap<>();
            for (IngredientProfile ingredientProfile : jsonItems) {
                newIngredients.put(ingredientProfile.getDisplayName(), ingredientProfile);
            }

            ingredients = newIngredients;
        });
    }

    public ItemGuessProfile getItemGuess(String levelRange) {
        return itemGuesses.get(levelRange);
    }

    public ItemProfile getItemsProfile(String name) {
        return items.get(name);
    }

    public String getInternalIdentification(String internalId) {
        return internalIdentifications.get(internalId);
    }

    public String getTranslatedReference(String untranslatedName) {
        return translatedReferences.getOrDefault(untranslatedName, untranslatedName);
    }

    public IngredientProfile getIngredient(String name) {
        return ingredients.get(name);
    }

    public String getIngredientHeadTexture(String ingredientName) {
        return ingredientHeadTextures.get(ingredientName);
    }

    public Collection<ItemProfile> getItemsCollection() {
        return items.values();
    }

    public Collection<IngredientProfile> getIngredientsCollection() {
        return ingredients.values();
    }
}
