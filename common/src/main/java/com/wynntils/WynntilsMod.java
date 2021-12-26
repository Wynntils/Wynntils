/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils;

import com.wynntils.features.SoulPointTimerFeature;
import com.wynntils.features.WynncraftButtonFeature;
import com.wynntils.framework.events.EventBus;
import com.wynntils.framework.feature.Feature;
import java.util.Arrays;

public class WynntilsMod {
    public static final String MOD_ID = "wynntils";

    public static EventBus eventBus = new EventBus();

    public static Feature[] features =
            new Feature[] {new WynncraftButtonFeature(), new SoulPointTimerFeature()};

    public static void init() {
        System.out.println("Wynntils initialized");

        Arrays.stream(features).forEach(Feature::onEnable);
    }
}
