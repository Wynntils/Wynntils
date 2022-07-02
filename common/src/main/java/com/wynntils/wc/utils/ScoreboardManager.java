/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wc.utils;

import com.wynntils.core.WynntilsMod;

public class ScoreboardManager {
    public static void init() {
        WynntilsMod.getEventBus().register(ScoreboardManager.class);
    }
}
