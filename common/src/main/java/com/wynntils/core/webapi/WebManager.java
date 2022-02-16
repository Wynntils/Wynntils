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
import com.wynntils.mc.EventFactory;
import java.io.File;
import java.lang.reflect.Type;
import java.util.HashMap;
import org.jetbrains.annotations.Nullable;

/** Provides and loads web content on demand */
public class WebManager {
    public static final File API_CACHE_ROOT = new File(WynntilsMod.MOD_STORAGE_ROOT, "apicache");

    private static boolean setup = false;
    private static final RequestHandler handler = new RequestHandler();

    public static @Nullable WebReader apiUrls = null;

    private static final HashMap<String, ItemGuessProfile> itemGuesses = new HashMap<>();

    public static void init() {
        tryReloadApiUrls(false);
    }

    public static boolean tryLoadItemGuesses() {
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
                                                                ItemGuessProfile>>() {
                                                }.getType();

                                        GsonBuilder gsonBuilder = new GsonBuilder();
                                        gsonBuilder.registerTypeHierarchyAdapter(
                                                HashMap.class,
                                                new ItemGuessProfile.ItemGuessDeserializer());
                                        Gson gson = gsonBuilder.create();

                                        itemGuesses.putAll(gson.fromJson(json, type));

                                        markFlag(0);
                                        return true;
                                    })
                            .useCacheAsBackup()
                            .build());

            handler.dispatch(false);

            //Check for success
            return isItemGuessesLoaded();
        }

        return true;
    }

    private static boolean tryReloadApiUrls(boolean async) {
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
        return hasFlag(0);
    }

    public static HashMap<String, ItemGuessProfile> getItemGuesses() {
        return itemGuesses;
    }

    public static boolean isSetup() {
        return setup;
    }

    private static long flag = 0L;

    private static boolean hasFlag(int i) {
        return (flag & (1L << i)) == 1;
    }

    private static void markFlag(int i) {
        flag |= (1L << i);
    }
}
