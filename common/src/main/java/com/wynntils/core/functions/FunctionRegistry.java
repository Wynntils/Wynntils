/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.functions;

import com.wynntils.functions.TestFunction;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Loads {@link Function}s */
public final class FunctionRegistry {
    private static final List<Function> FUNCTIONS = new ArrayList<>();

    private static void registerFunction(Function function) {
        FUNCTIONS.add(function);
        function.init();
    }

    public static List<Function> getFunctions() {
        return FUNCTIONS;
    }

    public static Optional<Function> forName(String functionName) {
        return FunctionRegistry.getFunctions().stream()
                .filter(function -> function.getName().equals(functionName))
                .findFirst();
    }

    public static void init() {
        // debug
        registerFunction(new TestFunction());
    }
}
