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
import com.wynntils.gui.render.TextRenderSetting;
import com.wynntils.gui.render.TextRenderTask;
import com.wynntils.wynn.objects.EmeraldSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
            ManagerRegistry.addAllDependencies(dependantFunction);
        }
    }

    public static List<Function<?>> getFunctions() {
        return FUNCTIONS;
    }

    public static boolean enableFunction(Function<?> function) {
        if (!(function instanceof ActiveFunction<?> activeFunction)) return true;

        WynntilsMod.registerEventListener(activeFunction);

        boolean enableSucceeded = activeFunction.onEnable();

        if (!enableSucceeded) {
            WynntilsMod.unregisterEventListener(activeFunction);
        }
        ENABLED_FUNCTIONS.add(activeFunction);
        return enableSucceeded;
    }

    public static void disableFunction(Function<?> function) {
        if (!(function instanceof ActiveFunction<?> activeFunction)) return;

        WynntilsMod.unregisterEventListener(activeFunction);
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

    public static String getRawValueString(Function<?> function, String argument) {
        Object value = function.getValue(argument);
        if (value == null) {
            return "??";
        }

        return format(value);
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

    // region Legacy formatting

    private static final Pattern INFO_VARIABLE_PATTERN =
            Pattern.compile("%([a-zA-Z_]+|%)%|\\\\([\\\\n%§EBLMH]|x[\\dA-Fa-f]{2}|u[\\dA-Fa-f]{4}|U[\\dA-Fa-f]{8})");

    public static List<Function<?>> getDependenciesFromStringLegacy(String renderableText) {
        List<Function<?>> dependencies = new ArrayList<>();

        Matcher m = INFO_VARIABLE_PATTERN.matcher(renderableText);
        while (m.find()) {
            if (m.group(1) != null && FunctionManager.forName(m.group(1)).isPresent()) {
                // %variable%
                Function<?> function = FunctionManager.forName(m.group(1)).get();
                dependencies.add(function);
            }
        }

        return dependencies;
    }

    public static TextRenderTask getStringFromLegacyTemplate(String renderableText) {
        StringBuilder builder = new StringBuilder(renderableText.length() + 10);
        Matcher m = INFO_VARIABLE_PATTERN.matcher(renderableText);
        while (m.find()) {
            String replacement = null;
            if (m.group(1) != null && FunctionManager.forName(m.group(1)).isPresent()) {
                // %variable%
                Function<?> function = FunctionManager.forName(m.group(1)).get();

                replacement = FunctionManager.getRawValueString(function, "");
            } else if (m.group(2) != null) {
                // \escape
                replacement = doEscapeFormat(m.group(2));
            }
            if (replacement == null) {
                replacement = m.group(0);
            }

            m.appendReplacement(builder, replacement);
        }
        m.appendTail(builder);

        return new TextRenderTask(parseColorCodes(builder.toString()), TextRenderSetting.DEFAULT);
    }

    private static String parseColorCodes(String toProcess) {
        // For every & symbol, check if the next symbol is a color code and if so, replace it with §
        // But don't do it if a \ precedes the &
        String validColors = "0123456789abcdefklmnor";
        StringBuilder sb = new StringBuilder(toProcess);
        for (int i = 0; i < sb.length(); i++) {
            if (sb.charAt(i) == '&') { // char == &
                if (i + 1 < sb.length()
                        && validColors.contains(String.valueOf(sb.charAt(i + 1)))) { // char after is valid color
                    if (i - 1 < 0 || sb.charAt(i - 1) != '\\') { // & is first char || char before is not \
                        sb.setCharAt(i, '§');
                    } else if (sb.charAt(i - 1) == '\\') { // & is preceded by \, just remove the \
                        sb.deleteCharAt(i - 1);
                    }
                }
            }
        }
        return sb.toString();
    }

    private static String doEscapeFormat(String escaped) {
        return switch (escaped) {
            case "\\" -> "\\\\";
            case "n" -> "\n";
            case "%" -> "%";
            case "§" -> "&";
            case "E" -> EmeraldSymbols.E_STRING;
            case "B" -> EmeraldSymbols.B_STRING;
            case "L" -> EmeraldSymbols.L_STRING;
            case "M" -> "✺";
            case "H" -> "❤";
            default -> null;
        };
    }
    // endregion

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