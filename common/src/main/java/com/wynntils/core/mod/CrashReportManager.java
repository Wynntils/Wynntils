/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.mod;

import com.wynntils.core.managers.Manager;
import com.wynntils.core.managers.Managers;
import com.wynntils.mc.utils.McUtils;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.CrashReportCategory;

public final class CrashReportManager extends Manager {
    private final Map<String, Supplier<String>> crashHandlers = new HashMap<>();
    private static CrashReportManager instance = null;

    public CrashReportManager() {
        super(List.of());
        if (instance != null) {
            // Save a local copy since we can't rely on being able to use the
            // Managers class in case of a crash. Note especially that minecraft does a
            // CrashReport.preload() early at game startup
            instance = this;
        }
    }

    public void registerCrashContext(String name, Supplier<String> handler) {
        crashHandlers.put(name, handler);
    }

    public static CrashReportCategory generateDetails() {
        CrashReportCategory wynntilsCategory = new CrashReportCategory("Wynntils");

        if (instance == null) {
            wynntilsCategory.setDetail("No crash handler loaded yet", "");
            return wynntilsCategory;
        }

        Map<String, Supplier<String>> crashHandlers = instance.getCrashHandlers();
        for (String handlerName : crashHandlers.keySet()) {
            String report = crashHandlers.get(handlerName).get();
            if (report != null) {
                wynntilsCategory.setDetail(handlerName, report);
            }
        }

        return wynntilsCategory;
    }

    private Map<String, Supplier<String>> getCrashHandlers() {
        return crashHandlers;
    }
}
