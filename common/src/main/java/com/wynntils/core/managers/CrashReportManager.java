/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.managers;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.CrashReportCategory;

public final class CrashReportManager {
    private static final List<ICrashContext> crashContexts = new ArrayList<>();

    public static void registerCrashContext(ICrashContext context) {
        crashContexts.add(context);
    }

    public static CrashReportCategory generateDetails() {
        CrashReportCategory wynntilsCategory = new CrashReportCategory("Wynntils");

        for (ICrashContext crashContext : crashContexts) {

            Object infos = crashContext.generate();

            if (infos != null) {
                wynntilsCategory.setDetail(crashContext.name(), crashContext.generate());
            }
        }

        return wynntilsCategory;
    }

    public interface ICrashContext {
        String name();

        /** Return null to not add */
        Object generate();
    }
}
