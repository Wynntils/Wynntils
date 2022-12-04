/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.netresources;

import com.wynntils.core.managers.CoreManager;
import com.wynntils.utils.Utils;
import java.util.List;

public class SplashManager extends CoreManager {
    private static String currentSplash = "";
    private static final List<String> SPLASHES = List.of("Move splashes to separate file!", "json FTW!");

    public static List<String> getSplashes() {
        return SPLASHES;
    }

    public static void init() {
        updateCurrentSplash();
    }

    private static void updateCurrentSplash() {
        // FIXME: load splashes from new json

        List<String> splashes = getSplashes();
        currentSplash = splashes.get(Utils.getRandom().nextInt(splashes.size()));
    }

    public static String getCurrentSplash() {
        return currentSplash;
    }
}
