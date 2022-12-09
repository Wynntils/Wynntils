/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.wynntils.core.managers.CoreManager;
import com.wynntils.core.net.UrlManager;
import com.wynntils.core.net.downloader.DownloadableResource;
import com.wynntils.core.net.downloader.Downloader;
import com.wynntils.utils.Utils;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class SplashManager extends CoreManager {
    // Fallback splash in case loading of splashes fails
    private static final String DEFAULT_SPLASH = "The best Wynncraft mod you'll probably find!";
    private static final Gson GSON = new GsonBuilder().create();

    private static List<String> allSplashes = new ArrayList<>();
    private static String currentSplash = DEFAULT_SPLASH;

    public static String getCurrentSplash() {
        return currentSplash;
    }

    public static void init() {
        updateCurrentSplash();
    }

    private static void updateCurrentSplash() {
        DownloadableResource dl =
                Downloader.download(UrlManager.getUrl(UrlManager.DATA_STATIC_SPLASHES), "splashes.json", "splashes");
        dl.handleJsonObject(json -> {
            Type type = new TypeToken<List<String>>() {}.getType();
            allSplashes = GSON.fromJson(json, type);
            if (allSplashes.isEmpty()) {
                // Use fallback in case of failure
                currentSplash = DEFAULT_SPLASH;
            } else {
                currentSplash = allSplashes.get(Utils.getRandom().nextInt(allSplashes.size()));
            }
            return true;
        });
    }
}
