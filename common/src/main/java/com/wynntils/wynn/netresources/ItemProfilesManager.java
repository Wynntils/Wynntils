/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.netresources;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.wynntils.core.net.downloader.DownloadableResource;
import com.wynntils.core.net.downloader.Downloader;
import com.wynntils.core.webapi.WebManager;
import com.wynntils.wynn.item.IdentificationOrderer;
import com.wynntils.wynn.netresources.profiles.ItemGuessProfile;
import com.wynntils.wynn.netresources.profiles.ingredient.IngredientProfile;
import com.wynntils.wynn.netresources.profiles.item.ItemProfile;
import com.wynntils.wynn.netresources.profiles.item.ItemType;
import com.wynntils.wynn.netresources.profiles.item.MajorIdentification;
import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class ItemProfilesManager {
    private static HashMap<String, ItemGuessProfile> itemGuesses = new HashMap<>();
    private static HashMap<String, MajorIdentification> majorIds = new HashMap<>();
    //    private static HashMap<ItemType, String[]> materialTypes = new HashMap<>();

    private static HashMap<String, ItemProfile> items = new HashMap<>();
    private static Collection<ItemProfile> directItems = new ArrayList<>();
    private static HashMap<String, String> translatedReferences = new HashMap<>();
    private static HashMap<String, String> internalIdentifications = new HashMap<>();
    private static HashMap<String, IngredientProfile> ingredients = new HashMap<>();
    private static Collection<IngredientProfile> directIngredients = new ArrayList<>();
    private static HashMap<String, String> ingredientHeadTextures = new HashMap<>();

    public static void loadCommonObjects() {
        tryLoadItemList();
        tryLoadItemGuesses();
        tryLoadIngredientList();
    }

    private static void tryLoadItemGuesses() {
        if (WebManager.apiUrls == null || !WebManager.apiUrls.hasKey("ItemGuesses")) return;
        String url = WebManager.apiUrls.get("ItemGuesses");
        DownloadableResource dl =
                Downloader.download(url, new File(WebManager.API_CACHE_ROOT, "item_guesses.json"), "item_guesses");
        dl.handleJsonObject(json -> {
            Type type = new TypeToken<HashMap<String, ItemGuessProfile>>() {}.getType();

            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.registerTypeHierarchyAdapter(HashMap.class, new ItemGuessProfile.ItemGuessDeserializer());
            Gson gson = gsonBuilder.create();

            itemGuesses = new HashMap<>();
            itemGuesses.putAll(gson.fromJson(json, type));

            return true;
        });

        // Check for success
    }

    private static void tryLoadItemList() {
        if (WebManager.apiUrls == null || !WebManager.apiUrls.hasKey("Athena")) return;
        String url = WebManager.apiUrls.get("Athena") + "/cache/get/itemList";
        DownloadableResource dl =
                Downloader.download(url, new File(WebManager.API_CACHE_ROOT, "item_list.json"), "item_list");
        dl.handleJsonObject(json -> {
            Type hashmapType = new TypeToken<HashMap<String, String>>() {}.getType();
            translatedReferences = WebManager.gson.fromJson(json.getAsJsonObject("translatedReferences"), hashmapType);
            internalIdentifications =
                    WebManager.gson.fromJson(json.getAsJsonObject("internalIdentifications"), hashmapType);

            Type majorIdsType = new TypeToken<HashMap<String, MajorIdentification>>() {}.getType();
            majorIds = WebManager.gson.fromJson(json.getAsJsonObject("majorIdentifications"), majorIdsType);
            Type materialTypesType = new TypeToken<HashMap<ItemType, String[]>>() {}.getType();
            //            materialTypes = gson.fromJson(json.getAsJsonObject("materialTypes"), materialTypesType);

            // FIXME: We should not be doing Singleton housekeeping for IdentificationOrderer!
            IdentificationOrderer.INSTANCE =
                    WebManager.gson.fromJson(json.getAsJsonObject("identificationOrder"), IdentificationOrderer.class);

            ItemProfile[] gItems = WebManager.gson.fromJson(json.getAsJsonArray("items"), ItemProfile[].class);

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
        });

        // Check for success
    }

    private static void tryLoadIngredientList() {
        if (WebManager.apiUrls == null || !WebManager.apiUrls.hasKey("Athena")) return;
        String url = WebManager.apiUrls.get("Athena") + "/cache/get/ingredientList";

        DownloadableResource dl =
                Downloader.download(url, new File(WebManager.API_CACHE_ROOT, "ingredient_list.json"), "ingredientList");
        dl.handleJsonObject(json -> {
            Type hashmapType = new TypeToken<HashMap<String, String>>() {}.getType();
            ingredientHeadTextures = WebManager.gson.fromJson(json.getAsJsonObject("headTextures"), hashmapType);

            IngredientProfile[] gItems =
                    WebManager.gson.fromJson(json.getAsJsonArray("ingredients"), IngredientProfile[].class);
            HashMap<String, IngredientProfile> cingredients = new HashMap<>();

            for (IngredientProfile prof : gItems) {
                cingredients.put(prof.getDisplayName(), prof);
            }

            ingredients = cingredients;
            directIngredients = cingredients.values();

            return true;
        });
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

    public static void reset() {
        // tryLoadItemGuesses
        itemGuesses = null;

        // tryLoadItemList
        items = null;
        directItems = null;
        translatedReferences = null;
        internalIdentifications = null;
        majorIds = null;
    }
}
