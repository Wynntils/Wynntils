/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils;

import com.wynntils.features.SoulPointTimerFeature;
import com.wynntils.features.WynncraftButtonFeature;
import com.wynntils.framework.feature.Feature;
import java.util.Arrays;
import net.minecraftforge.eventbus.api.BusBuilder;
import net.minecraftforge.eventbus.api.IEventBus;

public class WynntilsMod {
    public static final String MOD_ID = "wynntils";

    public static final IEventBus EVENT_BUS = BusBuilder.builder().build();

    public static final Feature[] FEATURES =
            new Feature[] {
                new WynncraftButtonFeature() ,
                new SoulPointTimerFeature()
    };

    public static void init() {
        System.out.println("Wynntils initialized");

        Arrays.stream(FEATURES).forEach(Feature::onEnable);
    }
}
