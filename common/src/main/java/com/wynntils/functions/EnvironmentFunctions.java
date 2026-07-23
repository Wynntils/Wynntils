/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Services;
import com.wynntils.services.athena.type.WynncraftVersion;
import com.wynntils.templates.annotations.TemplateFunction;
import com.wynntils.utils.SystemUtils;
import com.wynntils.utils.type.CappedValue;
import com.wynntils.utils.type.Time;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;
import net.minecraft.SharedConstants;

@SuppressWarnings("unused") // Functions are accessed via reflection
public class EnvironmentFunctions {
    @TemplateFunction(name = "capped_memory", aliases = "capped_mem")
    public static CappedValue cappedMemoryFunction() {
        return new CappedValue(SystemUtils.getMemUsed(), SystemUtils.getMemMax());
    }

    @TemplateFunction(name = "now")
    public static Time nowFunction() {
        return Time.now();
    }

    @TemplateFunction(name = "clock")
    public static String clockFunction() {
        LocalDateTime date = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT);
        return date.format(formatter);
    }

    @TemplateFunction(name = "clock_m")
    public static String clockMFunction() {
        LocalDateTime date = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ROOT);
        return date.format(formatter);
    }

    @TemplateFunction(name = "stopwatch_zero", aliases = "stopwatch_is_zero")
    public static boolean stopwatchZeroFunction() {
        return Services.Stopwatch.isZero();
    }

    @TemplateFunction(name = "stopwatch_running")
    public static boolean stopwatchRunningFunction() {
        return Services.Stopwatch.isRunning();
    }

    @TemplateFunction(name = "stopwatch_hours")
    public static int stopwatchHoursFunction() {
        return Services.Stopwatch.getHours();
    }

    @TemplateFunction(name = "stopwatch_minutes")
    public static int stopwatchMinutesFunction() {
        return Services.Stopwatch.getMinutes();
    }

    @TemplateFunction(name = "stopwatch_seconds")
    public static int stopwatchSecondsFunction() {
        return Services.Stopwatch.getSeconds();
    }

    @TemplateFunction(name = "stopwatch_milliseconds")
    public static int stopwatchMillisecondsFunction() {
        return Services.Stopwatch.getMilliseconds();
    }

    @TemplateFunction(name = "memory_max", aliases = "memmax")
    public static int memoryMaxFunction() {
        return SystemUtils.getMemMax();
    }

    @TemplateFunction(name = "memory_used", aliases = "memused")
    public static int memoryUsedFunction() {
        return SystemUtils.getMemUsed();
    }

    @TemplateFunction(
            name = "memory_percent",
            aliases = {"memorypct", "mempct"})
    public static int memoryPercentFunction() {
        return (int) (((float) SystemUtils.getMemUsed() / SystemUtils.getMemMax()) * 100f);
    }

    @TemplateFunction(name = "wynntils_version")
    public static String wynntilsVersionFunction() {
        return WynntilsMod.getVersion();
    }

    @TemplateFunction(name = "minecraft_version")
    public static String minecraftVersionFunction() {
        return SharedConstants.getCurrentVersion().name();
    }

    @TemplateFunction(name = "wynncraft_version")
    public static String wynncraftVersionFunction() {
        WynncraftVersion version = Services.Compatibility.getWynncraftVersion();
        return version != null ? version.toString() : "";
    }
}
