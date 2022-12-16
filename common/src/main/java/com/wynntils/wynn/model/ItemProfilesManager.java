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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public final class ItemProfilesManager extends Manager {
    private static final Gson ITEM_GUESS_GSON = new GsonBuilder()
            .registerTypeHierarchyAdapter(HashMap.class, new ItemGuessProfile.ItemGuessDeserializer())
            .create();

    private HashMap<String, ItemProfile> items = new HashMap<>();
    private Collection<ItemProfile> directItems = new ArrayList<>();
    private HashMap<String, ItemGuessProfile> itemGuesses = new HashMap<>();
    private HashMap<String, String> translatedReferences = new HashMap<>();
    private HashMap<String, String> internalIdentifications = new HashMap<>();
    private HashMap<String, MajorIdentification> majorIds = new HashMap<>();
    private HashMap<ItemType, String[]> materialTypes = new HashMap<>();
    private HashMap<String, IngredientProfile> ingredients = new HashMap<>();
    private Collection<IngredientProfile> directIngredients = new ArrayList<>();
    private HashMap<String, String> ingredientHeadTextures = new HashMap<>();

    public ItemProfilesManager(NetManager netManager) {
        super(List.of(netManager));
        loadCommonObjects();
    }

    private void loadCommonObjects() {
        tryLoadItemList();
        tryLoadItemGuesses();
        tryLoadIngredientList();
    }

    public void reset() {
        // tryLoadItemGuesses
        itemGuesses = null;

        // tryLoadItemList
        items = null;
        directItems = null;
        translatedReferences = null;
        internalIdentifications = null;
        majorIds = null;
        materialTypes = null;
        loadCommonObjects();
    }

    private void tryLoadItemGuesses() {
        Download dl = Managers.Net.download(UrlId.DATA_STATIC_ITEM_GUESSES);
        dl.handleReader(reader -> {
            Type type = new TypeToken<HashMap<String, ItemGuessProfile>>() {}.getType();
            itemGuesses = new HashMap<>();
            itemGuesses.putAll(ITEM_GUESS_GSON.fromJson(reader, type));
        });

        // Check for success
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
            majorIds = WynntilsMod.GSON.fromJson(json.getAsJsonObject("majorIdentifications"), majorIdsType);
            Type materialTypesType = new TypeToken<HashMap<ItemType, String[]>>() {}.getType();
            materialTypes = WynntilsMod.GSON.fromJson(json.getAsJsonObject("materialTypes"), materialTypesType);

            // FIXME: We should not be doing Singleton housekeeping for IdentificationOrderer!
            IdentificationOrderer.INSTANCE =
                    WynntilsMod.GSON.fromJson(json.getAsJsonObject("identificationOrder"), IdentificationOrderer.class);

            ItemProfile[] gItems = WynntilsMod.GSON.fromJson(json.getAsJsonArray("items"), ItemProfile[].class);

            HashMap<String, ItemProfile> citems = new HashMap<>();
            for (ItemProfile prof : gItems) {
                prof.getStatuses().forEach((n, p) -> p.calculateMinMax(n));
                prof.addMajorIds(majorIds);
                citems.put(prof.getDisplayName(), prof);
            }

            citems.values().forEach(ItemProfile::registerIdTypes);

            directItems = citems.values();
            items = citems;
        });

        // Check for success
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

            IngredientProfile[] gItems =
                    WynntilsMod.GSON.fromJson(json.getAsJsonArray("ingredients"), IngredientProfile[].class);
            HashMap<String, IngredientProfile> cingredients = new HashMap<>();

            for (IngredientProfile prof : gItems) {
                cingredients.put(prof.getDisplayName(), prof);
            }

            ingredients = cingredients;
            directIngredients = cingredients.values();
        });
    }

    public HashMap<String, ItemGuessProfile> getItemGuesses() {
        return itemGuesses;
    }

    public Collection<ItemProfile> getItemsCollection() {
        return directItems;
    }

    public HashMap<String, ItemProfile> getItemsMap() {
        return items;
    }

    public HashMap<ItemType, String[]> getMaterialTypes() {
        return materialTypes;
    }

    public HashMap<String, MajorIdentification> getMajorIds() {
        return majorIds;
    }

    public HashMap<String, String> getInternalIdentifications() {
        return internalIdentifications;
    }

    public HashMap<String, String> getTranslatedReferences() {
        return translatedReferences;
    }

    public Collection<IngredientProfile> getIngredientsCollection() {
        return directIngredients;
    }

    public HashMap<String, IngredientProfile> getIngredients() {
        return ingredients;
    }

    public HashMap<String, String> getIngredientHeadTextures() {
        return ingredientHeadTextures;
    }
}
