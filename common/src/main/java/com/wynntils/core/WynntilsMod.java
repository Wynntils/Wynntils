/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core;

import com.wynntils.core.features.FeatureLoader;
import com.wynntils.core.webapi.WebManager;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.mc.utils.keybinds.KeyManager;
import com.wynntils.wc.ModelLoader;
import java.io.File;
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

    public static IEventBus getEventBus() {
        return EVENT_BUS;
    }

    public static void init(Provider provider) {
        WynntilsMod.provider = provider;

        WebManager.init();
        KeyManager.init();

        ModelLoader.init();
        FeatureLoader.init();

        Reference.LOGGER.info("Wynntils initialized");
        parseVersion(provider.getModVersion());
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

    private static Provider provider;

    public static Provider getProvider() {
        return provider;
    }

    public interface Provider {
        String getModVersion();

        void registerEndTickEvent(Consumer<Minecraft> listener);
    }
}
