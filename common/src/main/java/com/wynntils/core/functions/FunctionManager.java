/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.functions;

import com.wynntils.core.WynntilsMod;
import com.wynntils.functions.TestFunction;
import com.wynntils.functions.TestWorldNameFunction;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;

/** Loads {@link Function}s */
public final class FunctionManager {
    private static final List<Function> FUNCTIONS = new ArrayList<>();
    private static final Set<EnableableFunction> ENABLED_FUNCTIONS = new HashSet<>();

    private static void registerFunction(Function function) {
        FUNCTIONS.add(function);
        if (function instanceof EnableableFunction<?> enableableFunction) {
            enableableFunction.init();
        }
    }

    public static List<Function> getFunctions() {
        return FUNCTIONS;
    }

    public static boolean enableFunction(Function function) {
        if (!(function instanceof EnableableFunction<?> enableableFunction)) return true;

        WynntilsMod.getEventBus().register(enableableFunction);

        boolean enableSucceeded = enableableFunction.onEnable();

        if (!enableSucceeded) {
            WynntilsMod.getEventBus().unregister(enableableFunction);
        }
        ENABLED_FUNCTIONS.add(enableableFunction);
        return enableSucceeded;
    }

    public static void disableFunction(Function function) {
        if (!(function instanceof EnableableFunction<?> enableableFunction)) return;

        WynntilsMod.getEventBus().unregister(enableableFunction);
        enableableFunction.onDisable();
        ENABLED_FUNCTIONS.remove(enableableFunction);
    }

    public static boolean isEnabled(Function function) {
        if (!(function instanceof EnableableFunction<?>)) return true;

        return (ENABLED_FUNCTIONS.contains(function));
    }

    public static Optional<Function> forName(String functionName) {
        return FunctionManager.getFunctions().stream()
                .filter(function -> function.getName().equals(functionName))
                .findFirst();
    }

    public static Component getSimpleValueString(
            Function function, String argument, ChatFormatting color, boolean includeName) {
        MutableComponent header = includeName
                ? new TextComponent(function.getName() + ": ").withStyle(ChatFormatting.WHITE)
                : new TextComponent("");

        if (function instanceof EnableableFunction<?> enableableFunction && !isEnabled(enableableFunction)) {
            return header.append(new TextComponent("N/A").withStyle(ChatFormatting.RED));
        }

        Object value = function.getValue(argument);
        if (value == null) {
            return header.append(new TextComponent("N/A").withStyle(ChatFormatting.RED));
        }

        return header.append(new TextComponent(value.toString()).withStyle(color));
    }

    public static void init() {
        // debug
        registerFunction(new TestFunction());
        registerFunction(new TestWorldNameFunction());
    }
}
