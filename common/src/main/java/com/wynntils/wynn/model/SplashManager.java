/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.wynntils.core.managers.CoreManager;
import com.wynntils.core.webapi.WebManager;
import com.wynntils.core.webapi.request.RequestBuilder;
import com.wynntils.core.webapi.request.RequestHandler;
import com.wynntils.utils.Utils;
import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class SplashManager extends CoreManager {
    private static String currentSplash = "";
    private static final List<String> SPLASHES = List.of("Move splashes to separate file!", "json FTW!");
    private static final String SPLASHES_URL =
            "https://raw.githubusercontent.com/Wynntils/WynntilsWebsite-API/master/splashes.json";
    // Fallback splash in case loading of splashes fails
    private static final String DEFAULT_SPLASH = "The best Wynncraft mod you'll probably find!";
    private static final Gson GSON = new GsonBuilder().create();

    private static List<String> allSplashes = new ArrayList<>();
    private static String currentSplash = DEFAULT_SPLASH;

    public static List<String> getSplashes() {
        return SPLASHES;
    }

    public static void init() {
        updateCurrentSplash();
    }

    private static void updateCurrentSplash() {
        RequestHandler handler = WebManager.getHandler();
        handler.addAndDispatch(new RequestBuilder(SPLASHES_URL, "splashes")
                .cacheTo(new File(WebManager.API_CACHE_ROOT, "splashes.json"))
                .useCacheAsBackup()
                .handleJsonArray(json -> {
                    Type type = new TypeToken<List<String>>() {}.getType();
                    allSplashes = GSON.fromJson(json, type);
                    if (allSplashes.isEmpty()) {
                        // Use fallback in case of failure
                        currentSplash = DEFAULT_SPLASH;
                    } else {
                        currentSplash = allSplashes.get(Utils.getRandom().nextInt(allSplashes.size()));
                    }
                    return true;
                })
                .build());
    }

    public static String getCurrentSplash() {
        return currentSplash;
    }
}
