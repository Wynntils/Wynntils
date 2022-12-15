/*
 * Copyright © Wynntils 2021-2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.wynntils.core.events.EventBusWrapper;
import com.wynntils.core.features.Feature;
import com.wynntils.core.features.FeatureRegistry;
import com.wynntils.core.features.UserFeature;
import com.wynntils.core.managers.CrashReportManager;
import com.wynntils.core.managers.ModelRegistry;
import com.wynntils.mc.event.ClientsideMessageEvent;
import com.wynntils.mc.utils.McUtils;
import java.io.File;
import java.io.InputStream;
import java.util.Optional;
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
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static final File MOD_STORAGE_ROOT = new File(McUtils.mc().gameDirectory, MOD_ID);

    private static ModLoader modLoader;
    private static String version = "";
    private static boolean developmentBuild = false;
    private static boolean developmentEnvironment;
    private static IEventBus eventBus;
    private static File modJar;

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
            handleExceptionInEventListener(t, event);
            return false;
        }
    }

    private static void handleExceptionInEventListener(Throwable t, Event event) {
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

        feature.setUserEnabled(false);
        feature.tryUserToggle();

        WynntilsMod.error("Exception in feature " + feature.getTranslatedName(), t);
        WynntilsMod.warn("This feature will be disabled");

        // TODO: This is a temporary fix for a crash that occurs when an error happens in a client-side message
        //       event, and we send a new message about disabling X feature,
        //       causing a new exception in client-side message event.
        if (!(event instanceof ClientsideMessageEvent)) {
            McUtils.sendMessageToClient(new TextComponent("Wynntils error: Feature '" + feature.getTranslatedName()
                            + "' has crashed and will be disabled")
                    .withStyle(ChatFormatting.RED));
        }
    }

    public static File getModJar() {
        return modJar;
    }

    public static String getVersion() {
        return version;
    }

    public static boolean isDevelopmentBuild() {
        return developmentBuild;
    }

    public static boolean isDevelopmentEnvironment() {
        return developmentEnvironment;
    }

    public static File getModStorageDir(String dirName) {
        return new File(MOD_STORAGE_ROOT, dirName);
    }

    public static InputStream getModResourceAsStream(String resourceName) {
        return WynntilsMod.class.getClassLoader().getResourceAsStream("assets/" + MOD_ID + "/" + resourceName);
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
        if (FeatureRegistry.isInitCompleted()) return;

        try {
            initFeatures();
        } catch (Throwable t) {
            LOGGER.error("Failed to initialize Wynntils features", t);
        }
    }

    public static void init(ModLoader loader, String modVersion, boolean isDevelopmentEnvironment, File modFile) {
        modJar = modFile;

        // Note that at this point, no resources (including I18n) are available, so we postpone features until then

        // Setup mod core properties
        modLoader = loader;
        developmentEnvironment = isDevelopmentEnvironment;

        parseVersion(modVersion);

        LOGGER.info(
                "Wynntils: Starting version {} (using {} on Minecraft {})",
                version,
                modLoader,
                Minecraft.getInstance().getLaunchedVersion());

        addCrashCallbacks();

        WynntilsMod.eventBus = EventBusWrapper.createEventBus();

        ModelRegistry.init();
    }

    private static void parseVersion(String modVersion) {
        if (modVersion.equals("DEV")) {
            version = modVersion;
            developmentBuild = true;
        } else {
            version = "v" + modVersion;
            developmentBuild = false;
        }
    }

    private static void initFeatures() {
        // Init all features. Now resources (i.e I18n) are available.
        FeatureRegistry.init();
        LOGGER.info(
                "Wynntils: {} features are now loaded and ready",
                FeatureRegistry.getFeatures().size());
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
