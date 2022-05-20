/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.functionalities;

import com.wynntils.mc.utils.CrashReportManager;
import java.util.ArrayList;
import java.util.List;

public class FunctionalityRegistry {
    private static final List<Functionality> FUNCTIONALITIES = new ArrayList<>();

    public static void init() {
        addCrashCallbacks();
    }

    private static void registerFunctionality(Functionality functionality) {
        FUNCTIONALITIES.add(functionality);
    }

    private static void addCrashCallbacks() {
        CrashReportManager.registerCrashContext(new CrashReportManager.ICrashContext() {
            @Override
            public String name() {
                return "Loaded Functionalities";
            }

            @Override
            public Object generate() {
                StringBuilder result = new StringBuilder();

                for (Functionality functionality : FUNCTIONALITIES) {
                    if (functionality.isInstantiated()) {
                        result.append("\n\t\t").append(functionality.getName());
                    }
                }

                return result.toString();
            }
        });
    }
}
