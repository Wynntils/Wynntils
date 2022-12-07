/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.wynntils.core.managers.CoreManager;
import com.wynntils.core.net.Reference;
import com.wynntils.core.net.downloader.DownloadableResource;
import com.wynntils.core.net.downloader.Downloader;
import com.wynntils.wynn.item.IdentificationOrderer;
import com.wynntils.wynn.objects.profiles.ItemGuessProfile;
import com.wynntils.wynn.objects.profiles.ingredient.IngredientProfile;
import com.wynntils.wynn.objects.profiles.item.ItemProfile;
import com.wynntils.wynn.objects.profiles.item.ItemType;
import com.wynntils.wynn.objects.profiles.item.MajorIdentification;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class ItemProfilesManager extends CoreManager {
    private static final Gson GSON = new Gson();

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

    public static void init() {
        loadCommonObjects();
    }

    private static void loadCommonObjects() {
        tryLoadItemList();
        tryLoadItemGuesses();
        tryLoadIngredientList();
    }

    private static void tryLoadItemGuesses() {
        String url = Reference.URLs.getItemGuesses();
        DownloadableResource dl = Downloader.download(url, "item_guesses.json", "item_guesses");
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
        String url = Reference.URLs.getAthena() + "/cache/get/itemList";
        DownloadableResource dl = Downloader.download(url, "item_list.json", "item_list");
        dl.handleJsonObject(json -> {
            Type hashmapType = new TypeToken<HashMap<String, String>>() {}.getType();
            translatedReferences = GSON.fromJson(json.getAsJsonObject("translatedReferences"), hashmapType);
            internalIdentifications = GSON.fromJson(json.getAsJsonObject("internalIdentifications"), hashmapType);

            Type majorIdsType = new TypeToken<HashMap<String, MajorIdentification>>() {}.getType();
            majorIds = GSON.fromJson(json.getAsJsonObject("majorIdentifications"), majorIdsType);
            Type materialTypesType = new TypeToken<HashMap<ItemType, String[]>>() {}.getType();
            //            materialTypes = gson.fromJson(json.getAsJsonObject("materialTypes"), materialTypesType);

            // FIXME: We should not be doing Singleton housekeeping for IdentificationOrderer!
            IdentificationOrderer.INSTANCE =
                    GSON.fromJson(json.getAsJsonObject("identificationOrder"), IdentificationOrderer.class);

            ItemProfile[] gItems = GSON.fromJson(json.getAsJsonArray("items"), ItemProfile[].class);

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
        String url = Reference.URLs.getAthena() + "/cache/get/ingredientList";

        DownloadableResource dl = Downloader.download(url, "ingredient_list.json", "ingredientList");
        dl.handleJsonObject(json -> {
            Type hashmapType = new TypeToken<HashMap<String, String>>() {}.getType();
            ingredientHeadTextures = GSON.fromJson(json.getAsJsonObject("headTextures"), hashmapType);

            IngredientProfile[] gItems = GSON.fromJson(json.getAsJsonArray("ingredients"), IngredientProfile[].class);
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
