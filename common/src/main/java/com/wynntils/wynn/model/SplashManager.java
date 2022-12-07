/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model;

import com.wynntils.core.managers.CoreManager;
import com.wynntils.core.webapi.WebManager;
import com.wynntils.utils.Utils;
import java.util.List;

public class SplashManager extends CoreManager {
    private static String currentSplash = "";

    public static String getCurrentSplash() {
        return currentSplash;
    }

    public static void init() {
        updateCurrentSplash();
    }

    private static void updateCurrentSplash() {
        if (WebManager.apiUrls == null || WebManager.apiUrls.getList("Splashes") == null) return;

        List<String> splashes = WebManager.apiUrls.getList("Splashes");
        currentSplash = splashes.get(Utils.getRandom().nextInt(splashes.size()));
    }
}
