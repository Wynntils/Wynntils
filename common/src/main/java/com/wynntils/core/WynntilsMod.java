/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core;

import com.wynntils.core.webapi.WebManager;
import com.wynntils.mc.utils.MinecraftUtils;
import com.wynntils.wc.Models;
import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraftforge.eventbus.api.BusBuilder;
import net.minecraftforge.eventbus.api.IEventBus;

/** The common implementation of Wynntils */
public class WynntilsMod {
    public static final String MOD_ID = "wynntils";
    public static String VERSION = "";
    public static int BUILD_NUMBER = -1;
    public static final File MOD_STORAGE_ROOT = new File(MinecraftUtils.mc().gameDirectory, MOD_ID);

    private static final IEventBus EVENT_BUS = BusBuilder.builder().build();

    public static IEventBus getEventBus() {
        return EVENT_BUS;
    }

    public static void init(String versionString) {
        Models.init();
        WebManager.init();
        FeatureLoader.init();

        Reference.LOGGER.info("Wynntils initialized");
        parseVersion(versionString);
    }

    public static void parseVersion(String versionString) {
        Matcher result = Pattern.compile("^(\\d+\\.\\d+\\.\\d+)_(DEV|\\d+)").matcher(versionString);
        result.find();

        VERSION = result.group(1);

        try {
            BUILD_NUMBER = Integer.parseInt(result.group(2));
        } catch (NumberFormatException ignored) {
        }

        System.out.println(VERSION + ":" + BUILD_NUMBER);
    }
}
