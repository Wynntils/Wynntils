/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.managers.CoreManager;
import com.wynntils.core.webapi.WebManager;
import com.wynntils.core.webapi.request.RequestBuilder;
import com.wynntils.wynn.item.IdentificationOrderer;
import com.wynntils.wynn.objects.profiles.ItemGuessProfile;
import com.wynntils.wynn.objects.profiles.ingredient.IngredientProfile;
import com.wynntils.wynn.objects.profiles.item.ItemProfile;
import com.wynntils.wynn.objects.profiles.item.ItemType;
import com.wynntils.wynn.objects.profiles.item.MajorIdentification;
import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class ItemProfilesManager extends CoreManager {
    private static final Gson ITEM_GUESS_GSON = new GsonBuilder()
            .registerTypeHierarchyAdapter(HashMap.class, new ItemGuessProfile.ItemGuessDeserializer())
            .create();

    private static HashMap<String, ItemProfile> items = new HashMap<>();
    private static Collection<ItemProfile> directItems = new ArrayList<>();
    private static HashMap<String, ItemGuessProfile> itemGuesses = new HashMap<>();
    private static HashMap<String, String> translatedReferences = new HashMap<>();
    private static HashMap<String, String> internalIdentifications = new HashMap<>();
    private static HashMap<String, MajorIdentification> majorIds = new HashMap<>();
    private static HashMap<ItemType, String[]> materialTypes = new HashMap<>();
    private static HashMap<String, IngredientProfile> ingredients = new HashMap<>();
    private static Collection<IngredientProfile> directIngredients = new ArrayList<>();
    private static HashMap<String, String> ingredientHeadTextures = new HashMap<>();

    public static void init() {
        loadCommonObjects();
    }

    private static void loadCommonObjects() {
        tryLoadItemList();
        tryLoadItemGuesses();
        tryLoadIngredientList();
    }

    public static void reset() {
        // tryLoadItemGuesses
        itemGuesses = null;

        // tryLoadItemList
        items = null;
        directItems = null;
        translatedReferences = null;
        internalIdentifications = null;
        majorIds = null;
        materialTypes = null;
    }

    private static void tryLoadItemGuesses() {
        if (WebManager.apiUrls == null || !WebManager.apiUrls.hasKey("ItemGuesses")) return;
        WebManager.getHandler()
                .addAndDispatch(new RequestBuilder(WebManager.apiUrls.get("ItemGuesses"), "item_guesses")
                        .cacheTo(new File(WebManager.API_CACHE_ROOT, "item_guesses.json"))
                        .handleJsonObject(json -> {
                            Type type = new TypeToken<HashMap<String, ItemGuessProfile>>() {}.getType();
                            itemGuesses = new HashMap<>();
                            itemGuesses.putAll(ITEM_GUESS_GSON.fromJson(json, type));

                            return true;
                        })
                        .useCacheAsBackup()
                        .build());

        // Check for success
    }

    private static void tryLoadItemList() {
        if (WebManager.apiUrls == null || !WebManager.apiUrls.hasKey("Athena")) return;
        WebManager.getHandler()
                .addAndDispatch(new RequestBuilder(
                                WebManager.apiUrls.get("Athena") + "/cache/get/itemList", "item_list")
                        .cacheTo(new File(WebManager.API_CACHE_ROOT, "item_list.json"))
                        .handleJsonObject(json -> {
                            Type hashmapType = new TypeToken<HashMap<String, String>>() {}.getType();
                            translatedReferences = WynntilsMod.GSON.fromJson(
                                    json.getAsJsonObject("translatedReferences"), hashmapType);
                            internalIdentifications = WynntilsMod.GSON.fromJson(
                                    json.getAsJsonObject("internalIdentifications"), hashmapType);

                            Type majorIdsType = new TypeToken<HashMap<String, MajorIdentification>>() {}.getType();
                            majorIds = WynntilsMod.GSON.fromJson(
                                    json.getAsJsonObject("majorIdentifications"), majorIdsType);
                            Type materialTypesType = new TypeToken<HashMap<ItemType, String[]>>() {}.getType();
                            materialTypes =
                                    WynntilsMod.GSON.fromJson(json.getAsJsonObject("materialTypes"), materialTypesType);

                            // FIXME: We should not be doing Singleton housekeeping for IdentificationOrderer!
                            IdentificationOrderer.INSTANCE = WynntilsMod.GSON.fromJson(
                                    json.getAsJsonObject("identificationOrder"), IdentificationOrderer.class);

                            ItemProfile[] gItems =
                                    WynntilsMod.GSON.fromJson(json.getAsJsonArray("items"), ItemProfile[].class);

                            HashMap<String, ItemProfile> citems = new HashMap<>();
                            for (ItemProfile prof : gItems) {
                                prof.getStatuses().forEach((n, p) -> p.calculateMinMax(n));
                                prof.addMajorIds(majorIds);
                                citems.put(prof.getDisplayName(), prof);
                            }

                            citems.values().forEach(ItemProfile::registerIdTypes);

                            directItems = citems.values();
                            items = citems;

                            return true;
                        })
                        .useCacheAsBackup()
                        .build());

        // Check for success
    }

    public static void tryLoadIngredientList() {
        if (WebManager.apiUrls == null || !WebManager.apiUrls.hasKey("Athena")) return;

        WebManager.getHandler()
                .addRequest(new RequestBuilder(
                                WebManager.apiUrls.get("Athena") + "/cache/get/ingredientList", "ingredientList")
                        .cacheTo(new File(WebManager.API_CACHE_ROOT, "ingredient_list.json"))
                        .useCacheAsBackup()
                        .handleJsonObject(j -> {
                            Type hashmapType = new TypeToken<HashMap<String, String>>() {}.getType();
                            ingredientHeadTextures =
                                    WynntilsMod.GSON.fromJson(j.getAsJsonObject("headTextures"), hashmapType);

                            IngredientProfile[] gItems = WynntilsMod.GSON.fromJson(
                                    j.getAsJsonArray("ingredients"), IngredientProfile[].class);
                            HashMap<String, IngredientProfile> cingredients = new HashMap<>();

                            for (IngredientProfile prof : gItems) {
                                cingredients.put(prof.getDisplayName(), prof);
                            }

                            ingredients = cingredients;
                            directIngredients = cingredients.values();

                            return true;
                        })
                        .build());
    }

    public static HashMap<String, ItemGuessProfile> getItemGuesses() {
        return itemGuesses;
    }

    public static Collection<ItemProfile> getItemsCollection() {
        return directItems;
    }

    public static HashMap<String, ItemProfile> getItemsMap() {
        return items;
    }

    public static HashMap<ItemType, String[]> getMaterialTypes() {
        return materialTypes;
    }

    public static HashMap<String, MajorIdentification> getMajorIds() {
        return majorIds;
    }

    public static HashMap<String, String> getInternalIdentifications() {
        return internalIdentifications;
    }

    public static HashMap<String, String> getTranslatedReferences() {
        return translatedReferences;
    }

    public static Collection<IngredientProfile> getIngredientsCollection() {
        return directIngredients;
    }

    public static HashMap<String, IngredientProfile> getIngredients() {
        return ingredients;
    }

    public static HashMap<String, String> getIngredientHeadTextures() {
        return ingredientHeadTextures;
    }
}
