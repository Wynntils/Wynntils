/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model;

import com.google.common.reflect.TypeToken;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.managers.CoreManager;
import com.wynntils.core.net.Download;
import com.wynntils.core.net.NetManager;
import com.wynntils.core.net.UrlId;
import com.wynntils.utils.Utils;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class SplashManager extends CoreManager {
    // Fallback splash in case loading of splashes fails
    private static final String DEFAULT_SPLASH = "The best Wynncraft mod you'll probably find!";

    private static List<String> allSplashes = new ArrayList<>();
    private static String currentSplash = DEFAULT_SPLASH;

    public static String getCurrentSplash() {
        return currentSplash;
    }

    public static void init() {
        updateCurrentSplash();
    }

    private static void updateCurrentSplash() {
        Download dl = NetManager.download(UrlId.DATA_STATIC_SPLASHES);
        dl.handleReader(reader -> {
            Type type = new TypeToken<List<String>>() {}.getType();
            allSplashes = WynntilsMod.GSON.fromJson(reader, type);
            if (allSplashes.isEmpty()) {
                // Use fallback in case of failure
                currentSplash = DEFAULT_SPLASH;
            } else {
                currentSplash = allSplashes.get(Utils.getRandom().nextInt(allSplashes.size()));
            }
        });
    }
}
