/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.splashes;

import com.google.common.reflect.TypeToken;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Service;
import com.wynntils.core.net.Download;
import com.wynntils.core.net.UrlId;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class SplashService extends Service {
    // Fallback splash in case loading of splashes fails
    private static final String DEFAULT_SPLASH = "The best Wynncraft mod you'll probably find!";
    private static final Random RANDOM = new Random();

    private List<String> allSplashes = new ArrayList<>();
    private String currentSplash = DEFAULT_SPLASH;

    public SplashService() {
        super(List.of());
        updateCurrentSplash();
    }

    @Override
    public void reloadData() {
        updateCurrentSplash();
    }

    public String getCurrentSplash() {
        return currentSplash;
    }

    private void updateCurrentSplash() {
        Download dl = Managers.Net.download(UrlId.DATA_STATIC_SPLASHES);
        dl.handleReader(reader -> {
            Type type = new TypeToken<List<String>>() {}.getType();
            allSplashes = WynntilsMod.GSON.fromJson(reader, type);
            if (allSplashes.isEmpty()) {
                // Use fallback in case of failure
                currentSplash = DEFAULT_SPLASH;
            } else {
                currentSplash = allSplashes.get(RANDOM.nextInt(allSplashes.size()));
            }
        });
    }
}
