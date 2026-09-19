/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils;

import com.wynntils.core.WynntilsMod;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;
import net.minecraft.SharedConstants;

public final class BugReportUtils {
    private static Supplier<String> installedMods = () -> "Unavailable";
    private static Supplier<String> loaderVersion = () -> "Unknown";

    private BugReportUtils() {}

    public static void registerPlatform(Supplier<String> mods, Supplier<String> loader) {
        installedMods = mods;
        loaderVersion = loader;
    }

    public static String collectSystemInformation() {
        return "Wynntils: " + WynntilsMod.getVersion()
                + "\nMinecraft: " + SharedConstants.getCurrentVersion().name()
                + "\nMod loader: " + loaderName() + " " + loaderVersion.get()
                + "\nOperating system: " + operatingSystem()
                + "\nJava: " + System.getProperty("java.version")
                + "\n\nInstalled mods, including dependencies:\n" + installedMods.get();
    }

    public static URI reportUrl() {
        // Keep the potentially large mod list out of the URL. Users paste it into the form.
        return URI.create("https://github.com/Hezaerd/Wynntils/issues/new?template=bug_report.yml"
                + parameter("wynntils-version", WynntilsMod.getVersion())
                + parameter(
                        "minecraft-version", SharedConstants.getCurrentVersion().name())
                + parameter("mod-loader", loaderName())
                + parameter("loader-version", loaderVersion.get())
                + parameter("operating-system", operatingSystem()));
    }

    private static String loaderName() {
        return switch (WynntilsMod.getModLoader()) {
            case FABRIC -> "Fabric";
            case NEOFORGE -> "NeoForge";
        };
    }

    private static String operatingSystem() {
        return System.getProperty("os.name") + " " + System.getProperty("os.version") + " "
                + System.getProperty("os.arch");
    }

    private static String parameter(String key, String value) {
        return "&" + key + "=" + URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
