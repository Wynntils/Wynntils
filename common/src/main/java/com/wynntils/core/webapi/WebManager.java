/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.webapi;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.webapi.profiles.ItemGuessProfile;
import com.wynntils.core.webapi.request.RequestBuilder;
import com.wynntils.core.webapi.request.RequestHandler;
import com.wynntils.wc.objects.items.IdentificationContainer;
import com.wynntils.wc.objects.items.ItemProfile;
import com.wynntils.wc.objects.items.ItemType;
import com.wynntils.wc.objects.items.MajorIdentification;
import com.wynntils.wc.utils.IdentificationOrderer;
import java.io.File;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Supplier;
import org.jetbrains.annotations.Nullable;

/** Provides and loads web content on demand */
public class WebManager {
    public static final File API_CACHE_ROOT = new File(WynntilsMod.MOD_STORAGE_ROOT, "apicache");

    private static final Set<Supplier<Boolean>> routesMarkedForUse = new HashSet<>();

    private static boolean setup = false;
    private static final RequestHandler handler = new RequestHandler();

    public static @Nullable WebReader apiUrls = null;

    private static final Gson gson = new Gson();

    private static @Nullable HashMap<String, ItemProfile> items = null;
    private static @Nullable Collection<ItemProfile> directItems = null;
    private static @Nullable HashMap<String, ItemGuessProfile> itemGuesses = null;
    private static @Nullable HashMap<String, String> translatedReferences = null;
    private static @Nullable HashMap<String, String> internalIdentifications = null;
    private static @Nullable HashMap<String, MajorIdentification> majorIds = null;
    private static @Nullable HashMap<ItemType, String[]> materialTypes = null;

    public static void init() {
        tryReloadApiUrls(false);
    }

    public static void reset() {
        // tryReloadApiUrls
        apiUrls = null;

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

    public static boolean reloadUsedRoutes() {
        boolean success = tryReloadApiUrls(false);

        for (Supplier<Boolean> entry :
                WebManager.getRoutesMarkedForUse()) {
            success &= entry.get();
        }

        return success;
    }

    public static boolean tryLoadItemGuesses() {
        routesMarkedForUse.add(WebManager::tryLoadItemGuesses);
        if (!isItemGuessesLoaded()) {
            handler.addRequest(
                    new RequestBuilder(apiUrls.get("ItemGuesses"), "item_guesses")
                            .cacheTo(new File(API_CACHE_ROOT, "item_guesses.json"))
                            .handleJsonObject(
                                    json -> {
                                        Type type =
                                                new TypeToken<
                                                        HashMap<
                                                                String,
                                                                ItemGuessProfile>>() {}.getType();

                                        GsonBuilder gsonBuilder = new GsonBuilder();
                                        gsonBuilder.registerTypeHierarchyAdapter(
                                                HashMap.class,
                                                new ItemGuessProfile.ItemGuessDeserializer());
                                        Gson gson = gsonBuilder.create();

                                        itemGuesses = new HashMap<>();
                                        itemGuesses.putAll(gson.fromJson(json, type));

                                        return true;
                                    })
                            .useCacheAsBackup()
                            .build());

            handler.dispatch(false);

            // Check for success
            return isItemGuessesLoaded();
        }

        return true;
    }

    public static boolean tryLoadItemList() {
        routesMarkedForUse.add(WebManager::tryLoadItemList);
        if (!isItemListLoaded()) {
            handler.addRequest(
                    new RequestBuilder(apiUrls.get("Athena") + "/cache/get/itemList", "item_list")
                            .cacheTo(new File(API_CACHE_ROOT, "item_list.json"))
                            .handleJsonObject(
                                    json -> {
                                        translatedReferences =
                                                gson.fromJson(
                                                        json.getAsJsonObject(
                                                                "translatedReferences"),
                                                        HashMap.class);
                                        internalIdentifications =
                                                gson.fromJson(
                                                        json.getAsJsonObject(
                                                                "internalIdentifications"),
                                                        HashMap.class);

                                        Type majorIdsType =
                                                new TypeToken<
                                                        HashMap<
                                                                String,
                                                                MajorIdentification>>() {}.getType();
                                        majorIds =
                                                gson.fromJson(
                                                        json.getAsJsonObject(
                                                                "majorIdentifications"),
                                                        majorIdsType);
                                        Type materialTypesType =
                                                new TypeToken<
                                                        HashMap<ItemType, String[]>>() {}.getType();
                                        materialTypes =
                                                gson.fromJson(
                                                        json.getAsJsonObject("materialTypes"),
                                                        materialTypesType);

                                        IdentificationOrderer.INSTANCE =
                                                gson.fromJson(
                                                        json.getAsJsonObject("identificationOrder"),
                                                        IdentificationOrderer.class);

                                        ItemProfile[] gItems =
                                                gson.fromJson(
                                                        json.getAsJsonArray("items"),
                                                        ItemProfile[].class);

                                        HashMap<String, ItemProfile> citems = new HashMap<>();
                                        for (ItemProfile prof : gItems) {
                                            prof.getStatuses()
                                                    .values()
                                                    .forEach(
                                                            IdentificationContainer
                                                                    ::calculateMinMax);
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

            handler.dispatch(false);

            // Check for success
            return isItemListLoaded();
        }

        return true;
    }

    public static boolean tryReloadApiUrls(boolean async) {
        if (apiUrls == null) {
            handler.addRequest(
                    new RequestBuilder("https://api.wynntils.com/webapi", "webapi")
                            .cacheTo(new File(API_CACHE_ROOT, "webapi.txt"))
                            .handleWebReader(
                                    reader -> {
                                        apiUrls = reader;
                                        if (!setup) {
                                            setup = true;
                                        }
                                        return true;
                                    })
                            .build());

            handler.dispatch(async);
            return setup;
        }

        return true;
    }

    public static boolean isItemGuessesLoaded() {
        return itemGuesses != null;
    }

    public static boolean isItemListLoaded() {
        return items != null
                && directItems != null
                && translatedReferences != null
                && internalIdentifications != null
                && majorIds != null
                && materialTypes != null;
    }

    public static @Nullable HashMap<String, ItemGuessProfile> getItemGuesses() {
        return itemGuesses;
    }

    public static @Nullable Collection<ItemProfile> getItemsCollection() {
        return directItems;
    }

    public static @Nullable HashMap<String, ItemProfile> getItemsMap() {
        return items;
    }

    public static @Nullable HashMap<ItemType, String[]> getMaterialTypes() {
        return materialTypes;
    }

    public static @Nullable HashMap<String, MajorIdentification> getMajorIds() {
        return majorIds;
    }

    public static @Nullable HashMap<String, String> getInternalIdentifications() {
        return internalIdentifications;
    }

    public static @Nullable HashMap<String, String> getTranslatedReferences() {
        return translatedReferences;
    }

    public static Set<Supplier<Boolean>> getRoutesMarkedForUse() {
        return routesMarkedForUse;
    }

    public static boolean isSetup() {
        return setup;
    }

    public static @Nullable WebReader getApiUrls() {
        return apiUrls;
    }
}
