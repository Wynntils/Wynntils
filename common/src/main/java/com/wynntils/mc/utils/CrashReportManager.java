/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.utils;

import java.util.ArrayList;
import java.util.List;

public class CrashReportManager {
    private static final List<ICrashContext> crashContexts = new ArrayList<>();

    public static void registerCrashContext(ICrashContext context) {
        crashContexts.add(context);
    }

    public static String generateInfo() {
        StringBuilder result = new StringBuilder();

        for (ICrashContext crashContext : crashContexts) {
            List<String> infos = crashContext.generate();

            for (String info : infos) {
                if (info != null && !info.isEmpty()) {
                    result.append("\n\t\t").append(info);
                }
            }
        }

        return result.toString();
    }

    public interface ICrashContext {
        List<String> generate();
    }
}
