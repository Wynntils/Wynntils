/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.functions;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.managers.CoreManager;
import com.wynntils.functions.TestFunction;
import com.wynntils.functions.WorldNameFunction;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;

/** Manage all built-in {@link Function}s */
public final class FunctionManager extends CoreManager {
    private static final List<Function> FUNCTIONS = new ArrayList<>();
    private static final Set<ActiveFunction> ENABLED_FUNCTIONS = new HashSet<>();

    private static void registerFunction(Function function) {
        FUNCTIONS.add(function);
        if (function instanceof ActiveFunction<?> activeFunction) {
            activeFunction.init();
        }
    }

    public static List<Function> getFunctions() {
        return FUNCTIONS;
    }

    public static boolean enableFunction(Function function) {
        if (!(function instanceof ActiveFunction<?> activeFunction)) return true;

        WynntilsMod.getEventBus().register(activeFunction);

        boolean enableSucceeded = activeFunction.onEnable();

        if (!enableSucceeded) {
            WynntilsMod.getEventBus().unregister(activeFunction);
        }
        ENABLED_FUNCTIONS.add(activeFunction);
        return enableSucceeded;
    }

    public static void disableFunction(Function function) {
        if (!(function instanceof ActiveFunction<?> activeFunction)) return;

        WynntilsMod.getEventBus().unregister(activeFunction);
        activeFunction.onDisable();
        ENABLED_FUNCTIONS.remove(activeFunction);
    }

    public static boolean isEnabled(Function function) {
        if (!(function instanceof ActiveFunction<?>)) return true;

        return (ENABLED_FUNCTIONS.contains(function));
    }

    public static Optional<Function> forName(String functionName) {
        return FunctionManager.getFunctions().stream()
                .filter(function -> function.getName().equalsIgnoreCase(functionName))
                .findFirst();
    }

    public static Component getSimpleValueString(
            Function function, String argument, ChatFormatting color, boolean includeName) {
        MutableComponent header = includeName
                ? new TextComponent(function.getTranslatedName() + ": ").withStyle(ChatFormatting.WHITE)
                : new TextComponent("");

        if (function instanceof ActiveFunction<?> activeFunction && !isEnabled(activeFunction)) {
            return header.append(new TextComponent("N/A").withStyle(ChatFormatting.RED));
        }

        Object value = function.getValue(argument);
        if (value == null) {
            return header.append(new TextComponent("N/A").withStyle(ChatFormatting.RED));
        }

        return header.append(new TextComponent(value.toString()).withStyle(color));
    }

    /**
     * Return a string, based on the template, with values filled in from the referenced
     * functions.
     */
    public static Component getStringFromTemplate(String template) {
        // FIXME: implement template parser
        return new TextComponent(template);
    }

    /**
     * Return a list of all functions referenced in a template string
     */
    public static List<Function> getFunctionsInTemplate(String template) {
        // FIXME: implement template parser
        return List.of();
    }

    public static void init() {
        // debug
        registerFunction(new TestFunction());
        registerFunction(new WorldNameFunction());
    }
}
