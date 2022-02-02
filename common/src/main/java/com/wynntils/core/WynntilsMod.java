/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core;

import com.wynntils.core.features.FeatureRegistry;
import com.wynntils.core.webapi.WebManager;
import com.wynntils.mc.utils.CrashReportManager;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.mc.utils.keybinds.KeyManager;
import com.wynntils.wc.ModelLoader;
import java.io.File;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.client.Minecraft;
import net.minecraftforge.eventbus.api.BusBuilder;
import net.minecraftforge.eventbus.api.IEventBus;

/** The common implementation of Wynntils */
public class WynntilsMod {
    public static final String MOD_ID = "wynntils";
    public static String VERSION = "";
    public static int BUILD_NUMBER = -1;
    public static final File MOD_STORAGE_ROOT = new File(McUtils.mc().gameDirectory, MOD_ID);

    private static final IEventBus EVENT_BUS = BusBuilder.builder().build();

    public static boolean developmentEnvironment = true;

    public static IEventBus getEventBus() {
        return EVENT_BUS;
    }

    public static void init(String modVersion) {
        // TODO find out if dev environ
        // Reference.developmentEnvironment = ((boolean)
        // Launch.blackboard.get("fml.deobfuscatedEnvironment"))
        // || (System.getProperty("wynntils.development") != null &&
        // System.getProperty("wynntils.development").equals("true"));

        WebManager.init();
        KeyManager.init();

        addCrashCallbacks();

        ModelLoader.init();
        FeatureRegistry.init();

        Reference.LOGGER.info("Wynntils initialized");
        parseVersion(modVersion);
    }

    public static void parseVersion(String versionString) {
        if (developmentEnvironment)
            Reference.LOGGER.info("Wynntils running on version " + versionString);

        Matcher result =
                Pattern.compile("^(\\d+\\.\\d+\\.\\d+)\\+(DEV|\\d+).+").matcher(versionString);

        if (!result.find()) {
            Reference.LOGGER.warn("Unable to parse mod version");
        }

        VERSION = result.group(1);

        try {
            BUILD_NUMBER = Integer.parseInt(result.group(2));
        } catch (NumberFormatException ignored) {
        }
    }

    private static void addCrashCallbacks() {
        CrashReportManager.registerCrashContext(
                () -> List.of("In Development: " + (developmentEnvironment ? "Yes" : "No")));
    }
}
