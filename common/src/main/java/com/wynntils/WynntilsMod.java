/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils;

import com.wynntils.features.SoulPointTimerFeature;
import com.wynntils.features.WynncraftButtonFeature;
import com.wynntils.framework.feature.FeatureHandler;
import com.wynntils.model.Model;
import net.minecraftforge.eventbus.api.BusBuilder;
import net.minecraftforge.eventbus.api.IEventBus;

public class WynntilsMod {
    public static final String MOD_ID = "wynntils";

    public static final IEventBus EVENT_BUS = BusBuilder.builder().build();

    public static void init() {
        System.out.println("Wynntils initialized");
        Model.init();

        FeatureHandler.registerFeature(new WynncraftButtonFeature());
        FeatureHandler.registerFeature(new SoulPointTimerFeature());
        FeatureHandler.initalizeFeatures();
    }

    public static void logUnknown(String msg, String obj) {
        System.out.println("Found unhandled input from Wynncraft: " + msg);
        System.out.println(obj);
    }
}
