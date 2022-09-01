/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.functions;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.managers.CoreManager;
import com.wynntils.core.managers.ManagerRegistry;
import com.wynntils.core.managers.Model;
import com.wynntils.functions.CharacterFunctions;
import com.wynntils.functions.EnvironmentFunctions;
import com.wynntils.functions.HorseFunctions;
import com.wynntils.functions.MinecraftFunctions;
import com.wynntils.functions.WorldFunction;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;

/** Manage all built-in {@link Function}s */
public final class FunctionManager extends CoreManager {
    private static final List<Function<?>> FUNCTIONS = new ArrayList<>();
    private static final Set<ActiveFunction<?>> ENABLED_FUNCTIONS = new HashSet<>();

    private static void registerFunction(Function<?> function) {
        FUNCTIONS.add(function);
        if (function instanceof ActiveFunction<?> activeFunction) {
            activeFunction.init();
        }
        // FIXME: This is sort of hacky. We should have these as ActiveFunctions instead,
        //        and register/unregister the model dependency when enabling/disabling
        if (function instanceof DependantFunction<?> dependantFunction) {
            for (Class<? extends Model> dependency : dependantFunction.getModelDependencies()) {
                ManagerRegistry.addFunctionDependency(dependantFunction, dependency);
            }
        }
    }

    public static List<Function<?>> getFunctions() {
        return FUNCTIONS;
    }

    public static boolean enableFunction(Function<?> function) {
        if (!(function instanceof ActiveFunction<?> activeFunction)) return true;

        WynntilsMod.getEventBus().register(activeFunction);

        boolean enableSucceeded = activeFunction.onEnable();

        if (!enableSucceeded) {
            WynntilsMod.getEventBus().unregister(activeFunction);
        }
        ENABLED_FUNCTIONS.add(activeFunction);
        return enableSucceeded;
    }

    public static void disableFunction(Function<?> function) {
        if (!(function instanceof ActiveFunction<?> activeFunction)) return;

        WynntilsMod.getEventBus().unregister(activeFunction);
        activeFunction.onDisable();
        ENABLED_FUNCTIONS.remove(activeFunction);
    }

    public static boolean isEnabled(Function<?> function) {
        if (!(function instanceof ActiveFunction<?>)) return true;

        return (ENABLED_FUNCTIONS.contains(function));
    }

    public static Optional<Function<?>> forName(String functionName) {
        return FunctionManager.getFunctions().stream()
                .filter(function -> hasName(function, functionName))
                .findFirst();
    }

    private static boolean hasName(Function<?> function, String name) {
        if (function.getName().equalsIgnoreCase(name)) return true;
        for (String alias : function.getAliases()) {
            if (alias.equalsIgnoreCase(name)) return true;
        }
        return false;
    }

    public static Component getSimpleValueString(
            Function<?> function, String argument, ChatFormatting color, boolean includeName) {
        MutableComponent header = includeName
                ? new TextComponent(function.getTranslatedName() + ": ").withStyle(ChatFormatting.WHITE)
                : new TextComponent("");

        Object value = function.getValue(argument);
        if (value == null) {
            return header.append(new TextComponent("??"));
        }

        String formattedValue = format(value);

        return header.append(new TextComponent(formattedValue).withStyle(color));
    }

    private static String format(Object value) {
        if (value instanceof Number number) {
            return NumberFormat.getInstance().format(number);
        }
        return value.toString();
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
    public static List<Function<?>> getFunctionsInTemplate(String template) {
        // FIXME: implement template parser
        return List.of();
    }

    public static <T> void doFormat(
            String format,
            Consumer<T> consumer,
            java.util.function.Function<String, T> mapper,
            Map<String, T> infoVariableMap) {
        Set<String> infoVariables = infoVariableMap.keySet();

        int index = 0;
        // TODO: Can we get away with less calculations since we now have asymmetric delimiters?
        while (index < format.length()) {
            int indexStartOfNextVariable = format.indexOf('{', index);
            if (indexStartOfNextVariable == -1) {
                break;
            }

            int indexEndOfNextVariable = format.indexOf('}', indexStartOfNextVariable + 1);
            if (indexEndOfNextVariable == -1) {
                break;
            }

            if (index != indexStartOfNextVariable) { // update none done too
                consumer.accept(mapper.apply(format.substring(index, indexStartOfNextVariable)));
            }

            String toMatch = format.substring(indexStartOfNextVariable + 1, indexEndOfNextVariable);

            for (String infoVariable : infoVariables) {
                if (!toMatch.equals(infoVariable)) {
                    continue;
                }

                index = indexEndOfNextVariable + 1; // skip ending }
                consumer.accept(infoVariableMap.get(infoVariable));
                break;
            }
        }

        consumer.accept(mapper.apply(format.substring(index)));
    }

    public static void init() {
        registerFunction(new WorldFunction());

        registerFunction(new CharacterFunctions.SoulpointFunction());
        registerFunction(new CharacterFunctions.SoulpointMaxFunction());
        registerFunction(new CharacterFunctions.SoulpointTimerFunction());
        registerFunction(new CharacterFunctions.SoulpointTimerMFunction());
        registerFunction(new CharacterFunctions.SoulpointTimerSFunction());
        registerFunction(new CharacterFunctions.ClassFunction());
        registerFunction(new CharacterFunctions.ManaFunction());
        registerFunction(new CharacterFunctions.ManaMaxFunction());
        registerFunction(new CharacterFunctions.HealthFunction());
        registerFunction(new CharacterFunctions.HealthMaxFunction());
        registerFunction(new CharacterFunctions.HealthPctFunction());
        registerFunction(new CharacterFunctions.LevelFunction());
        registerFunction(new CharacterFunctions.XpFunction());
        registerFunction(new CharacterFunctions.XpRawFunction());
        registerFunction(new CharacterFunctions.XpReqFunction());
        registerFunction(new CharacterFunctions.XpReqRawFunction());
        registerFunction(new CharacterFunctions.XpPctFunction());

        registerFunction(new EnvironmentFunctions.ClockFunction());
        registerFunction(new EnvironmentFunctions.ClockmFunction());
        registerFunction(new EnvironmentFunctions.MemMaxFunction());
        registerFunction(new EnvironmentFunctions.MemUsedFunction());
        registerFunction(new EnvironmentFunctions.MemPctFunction());

        registerFunction(new HorseFunctions.HorseLevelFunction());
        registerFunction(new HorseFunctions.HorseLevelMaxFunction());
        registerFunction(new HorseFunctions.HorseXpFunction());
        registerFunction(new HorseFunctions.HorseTierFunction());
        registerFunction(new HorseFunctions.HorseNameFunction());

        registerFunction(new MinecraftFunctions.XFunction());
        registerFunction(new MinecraftFunctions.YFunction());
        registerFunction(new MinecraftFunctions.ZFunction());
        registerFunction(new MinecraftFunctions.DirFunction());
        registerFunction(new MinecraftFunctions.FpsFunction());
    }
}
