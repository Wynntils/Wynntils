/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core;

import com.wynntils.wc.Models;
import net.minecraftforge.eventbus.api.BusBuilder;
import net.minecraftforge.eventbus.api.IEventBus;

public class WynntilsMod {
    public static final String MOD_ID = "wynntils";

    private static final IEventBus EVENT_BUS = BusBuilder.builder().build();

    public static IEventBus getEventBus() {
        return EVENT_BUS;
    }

    public static void init() {
        Models.init();
        FeatureHandler.init();

        System.out.println("Wynntils initialized");
    }
}
