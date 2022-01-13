/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core;

import com.wynntils.core.webapi.WebManager;
import com.wynntils.mc.utils.MinecraftUtils;
import com.wynntils.wc.Models;
import java.io.File;
import net.minecraftforge.eventbus.api.BusBuilder;
import net.minecraftforge.eventbus.api.IEventBus;

public class WynntilsMod {
    public static final String MOD_ID = "wynntils";
    public static String VERSION = "";
    public static int BUILD_NUMBER = -1;
    public static final File MOD_STORAGE_ROOT = new File(MinecraftUtils.mc().gameDirectory, MOD_ID);

    private static final IEventBus EVENT_BUS = BusBuilder.builder().build();

    public static IEventBus getEventBus() {
        return EVENT_BUS;
    }

    public static void init() {
        Models.init();
        WebManager.init();
        FeatureHandler.init();

        System.out.println("Wynntils initialized");
    }
}
