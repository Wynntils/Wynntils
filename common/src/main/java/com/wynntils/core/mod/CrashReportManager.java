/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.mod;

import com.wynntils.core.components.Manager;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.CrashReportCategory;

public final class CrashReportManager extends Manager {
    private static final Map<String, Supplier<String>> CRASH_HANDLERS = new HashMap<>();

    public CrashReportManager() {
        super(List.of());
    }

    public void registerCrashContext(String name, Supplier<String> handler) {
        CRASH_HANDLERS.put(name, handler);
    }

    //  Note: this is called directly from a mixin!
    public static CrashReportCategory generateDetails() {
        CrashReportCategory wynntilsCategory = new CrashReportCategory("Wynntils");

        for (Map.Entry<String, Supplier<String>> entry : CRASH_HANDLERS.entrySet()) {
            String report = entry.getValue().get();
            if (report != null) {
                wynntilsCategory.setDetail(entry.getKey(), report);
            }
        }

        return wynntilsCategory;
    }
}
