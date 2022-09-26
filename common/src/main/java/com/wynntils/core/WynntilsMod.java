/*
 * Copyright © Wynntils 2021-2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core;

import com.wynntils.core.events.EventBusWrapper;
import com.wynntils.core.features.Feature;
import com.wynntils.core.features.FeatureRegistry;
import com.wynntils.core.features.UserFeature;
import com.wynntils.core.managers.CrashReportManager;
import com.wynntils.core.managers.ManagerRegistry;
import com.wynntils.mc.utils.McUtils;
import java.io.File;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.IEventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** The common implementation of Wynntils */
public final class WynntilsMod {
    public static final String MOD_ID = "wynntils";

    private static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static final File MOD_STORAGE_ROOT = new File(McUtils.mc().gameDirectory, MOD_ID);

    private static ModLoader modLoader;
    private static String version = "";
    private static int buildNumber = -1;
    private static boolean developmentEnvironment;
    private static boolean featuresInited = false;
    private static IEventBus eventBus;

    public static ModLoader getModLoader() {
        return modLoader;
    }

    public static void unregisterEventListener(Object object) {
        eventBus.unregister(object);
    }

    public static void registerEventListener(Object object) {
        eventBus.register(object);
    }

    public static boolean postEvent(Event event) {
        try {
            return eventBus.post(event);
        } catch (Throwable t) {
            handleExceptionInEventListener(t);
            return false;
        }
    }

    private static void handleExceptionInEventListener(Throwable t) {
        StackTraceElement[] stackTrace = t.getStackTrace();
        String crashingFeatureName = null;
        for (StackTraceElement line : stackTrace) {
            if (line.getClassName().startsWith("com.wynntils.features.")) {
                crashingFeatureName = line.getClassName();
                break;
            }
        }

        if (crashingFeatureName == null) {
            WynntilsMod.error("Exception in event listener not belonging to a feature", t);
            return;
        }

        String featureClassName = crashingFeatureName.substring(crashingFeatureName.lastIndexOf('.') + 1);
        Optional<Feature> featureOptional = FeatureRegistry.getFeatureFromString(featureClassName);
        if (featureOptional.isEmpty()) {
            WynntilsMod.error(
                    "Exception in event listener in feature that cannot be located: " + crashingFeatureName, t);
            return;
        }

        if (!(featureOptional.get() instanceof UserFeature feature)) {
            WynntilsMod.error("Exception in event listener in non-user feature: " + crashingFeatureName, t);
            return;
        }

        WynntilsMod.error("Exception in feature " + feature.getTranslatedName(), t);
        WynntilsMod.warn("This feature will be disabled");
        McUtils.sendMessageToClient(new TextComponent("Wynntils error: Feature '" + feature.getTranslatedName()
                        + "' has crashed and will be disabled")
                .withStyle(ChatFormatting.RED));

        feature.setUserEnabled(false);
        feature.tryUserToggle();
    }

    public static String getVersion() {
        return version;
    }

    public static int getBuildNumber() {
        return buildNumber;
    }

    public static boolean isDevelopmentEnvironment() {
        return developmentEnvironment;
    }

    public static File getModStorageDir(String dirName) {
        return new File(MOD_STORAGE_ROOT, dirName);
    }

    public static Logger getLogger() {
        return LOGGER;
    }

    public static void error(String msg) {
        LOGGER.error(msg);
    }

    public static void error(String msg, Throwable t) {
        LOGGER.error(msg, t);
    }

    public static void warn(String msg) {
        LOGGER.warn(msg);
    }

    public static void warn(String msg, Throwable t) {
        LOGGER.warn(msg, t);
    }

    public static void info(String msg) {
        LOGGER.info(msg);
    }

    // Ran when resources (including I18n) are available
    public static void onResourcesFinishedLoading() {
        if (featuresInited) return;

        try {
            initFeatures();
        } catch (Throwable t) {
            LOGGER.error("Failed to initialize Wynntils features", t);
            return;
        }
        featuresInited = true;
    }

    public static void init(ModLoader loader, String modVersion, boolean isDevelopmentEnvironment) {
        // Note that at this point, no resources (including I18n) are available, so we postpone features until then

        // Setup mod core properties
        modLoader = loader;
        developmentEnvironment = isDevelopmentEnvironment;
        parseVersion(modVersion);
        addCrashCallbacks();

        // MC will sometimes think it's running headless and refuse to set clipboard contents
        // making sure this is set to false will fix that
        System.setProperty("java.awt.headless", "false");
        WynntilsMod.eventBus = EventBusWrapper.createEventBus();

        ManagerRegistry.init();
    }

    private static void initFeatures() {
        // Init all features. Now resources (i.e I18n) are available.
        FeatureRegistry.init();
        LOGGER.info(
                "Wynntils: {} features are now loaded and ready",
                FeatureRegistry.getFeatures().size());
    }

    private static void parseVersion(String versionString) {
        LOGGER.info(
                "Wynntils: Starting version {} (using {} on Minecraft {})",
                versionString,
                modLoader,
                Minecraft.getInstance().getLaunchedVersion());

        Matcher result = Pattern.compile("^(\\d+\\.\\d+\\.\\d+)\\+(DEV|\\d+).+").matcher(versionString);

        if (!result.find()) {
            LOGGER.warn("Unable to parse mod version");
        }

        version = result.group(1);

        try {
            buildNumber = Integer.parseInt(result.group(2));
        } catch (NumberFormatException ignored) {
        }
    }

    private static void addCrashCallbacks() {
        CrashReportManager.registerCrashContext(new CrashReportManager.ICrashContext("In Development") {
            @Override
            public Object generate() {
                return isDevelopmentEnvironment() ? "Yes" : "No";
            }
        });
    }

    public enum ModLoader {
        FORGE,
        FABRIC,
        QUILT
    }
}
