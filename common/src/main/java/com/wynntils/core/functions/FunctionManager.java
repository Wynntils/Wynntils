/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.functions;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Manager;
import com.wynntils.core.functions.arguments.FunctionArguments;
import com.wynntils.core.functions.arguments.parser.ArgumentParser;
import com.wynntils.core.functions.templates.parser.TemplateParser;
import com.wynntils.functions.CharacterFunctions;
import com.wynntils.functions.CombatXpFunctions;
import com.wynntils.functions.EnvironmentFunctions;
import com.wynntils.functions.HorseFunctions;
import com.wynntils.functions.InventoryFunctions;
import com.wynntils.functions.LootrunFunctions;
import com.wynntils.functions.MinecraftFunctions;
import com.wynntils.functions.ProfessionFunctions;
import com.wynntils.functions.SocialFunctions;
import com.wynntils.functions.WorldFunctions;
import com.wynntils.functions.generic.MathFunctions;
import com.wynntils.models.emeralds.type.EmeraldUnits;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.type.ErrorOr;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

/** Manage all built-in {@link Function}s */
public final class FunctionManager extends Manager {
    private static final Pattern INFO_VARIABLE_PATTERN =
            Pattern.compile("%([a-zA-Z_]+|%)%|\\\\([\\\\n%§EBLMH]|x[\\dA-Fa-f]{2}|u[\\dA-Fa-f]{4}|U[\\dA-Fa-f]{8})");

    private final List<Function<?>> functions = new ArrayList<>();
    private final Set<Function<?>> crashedFunctions = new HashSet<>();

    public FunctionManager() {
        super(List.of());
        registerAllFunctions();
    }

    public List<Function<?>> getFunctions() {
        return functions;
    }

    public void enableFunction(Function<?> function) {
        // try to recover, worst case we disable it again
        crashedFunctions.remove(function);
    }

    private void crashFunction(Function<?> function) {
        crashedFunctions.add(function);
    }

    public boolean isCrashed(Function<?> function) {
        return crashedFunctions.contains(function);
    }

    public Optional<Function<?>> forName(String functionName) {
        for (Function<?> function : getFunctions()) {
            if (hasName(function, functionName)) {
                return Optional.of(function);
            }
        }

        return Optional.empty();
    }

    private boolean hasName(Function<?> function, String name) {
        if (function.getName().equalsIgnoreCase(name)) return true;
        for (String alias : function.getAliases()) {
            if (alias.equalsIgnoreCase(name)) return true;
        }
        return false;
    }

    private Optional<Object> getFunctionValueSafely(Function<?> function, FunctionArguments arguments) {
        if (crashedFunctions.contains(function)) {
            return Optional.empty();
        }

        try {
            Object value = function.getValue(arguments);
            return Optional.ofNullable(value);
        } catch (Throwable throwable) {
            WynntilsMod.warn("Exception when trying to get value of function " + function, throwable);
            McUtils.sendMessageToClient(Component.literal(String.format(
                            "Function '%s' was disabled due to an exception.", function.getTranslatedName()))
                    .withStyle(ChatFormatting.RED));

            crashFunction(function);
        }

        return Optional.empty();
    }

    // region String value calculations

    public Component getSimpleValueString(
            Function<?> function, String rawArguments, ChatFormatting color, boolean includeName) {
        MutableComponent header = includeName
                ? Component.literal(function.getTranslatedName() + ": ").withStyle(ChatFormatting.WHITE)
                : Component.literal("");

        ErrorOr<FunctionArguments> errorOrArguments =
                ArgumentParser.parseArguments(function.getArgumentsBuilder(), rawArguments);

        if (errorOrArguments.hasError()) {
            return header.append(Component.literal(errorOrArguments.getError()).withStyle(ChatFormatting.RED));
        }

        Optional<Object> value = getFunctionValueSafely(function, errorOrArguments.getValue());
        if (value.isEmpty()) {
            return header.append(Component.literal("??"));
        }

        String formattedValue = format(value.get(), false, 2);

        return header.append(Component.literal(formattedValue).withStyle(color));
    }

    public String getStringFunctionValue(
            Function<?> function, FunctionArguments arguments, boolean formatted, int decimals) {
        Optional<Object> value = getFunctionValueSafely(function, arguments);
        if (value.isEmpty()) {
            return "??";
        }

        return format(value.get(), formatted, decimals);
    }

    private String format(Object value, boolean formatted, int decimals) {
        if (value instanceof Number number) {
            if (formatted) {
                // French locale has NBSP
                // https://stackoverflow.com/questions/34156585/java-decimal-format-parsing-issue
                NumberFormat instance = NumberFormat.getInstance();
                instance.setMinimumFractionDigits(decimals);
                instance.setMaximumFractionDigits(decimals);

                return instance.format(number).replaceAll("\u00A0", " ");
            } else {
                if (decimals == 0) {
                    return String.valueOf(number.intValue());
                }

                DecimalFormat decimalFormat = new DecimalFormat("#." + "0".repeat(decimals));
                return decimalFormat.format(number);
            }
        }

        return value.toString();
    }

    // endregion

    // region Raw value calculations
    // These are needed for getting a function value without converting its type to a string

    public ErrorOr<Object> getRawFunctionValue(Function<?> function, FunctionArguments arguments) {
        Optional<Object> value = getFunctionValueSafely(function, arguments);
        return value.map(ErrorOr::of)
                .orElseGet(() -> ErrorOr.error("Failed to get value of function: " + function.getName()));
    }

    // endregion

    // region Template formatting

    public String doFormat(String templateString) {
        return TemplateParser.doFormat(templateString);
    }

    // endregion

    // region Legacy formatting

    public List<Function<?>> getDependenciesFromStringLegacy(String renderableText) {
        List<Function<?>> dependencies = new ArrayList<>();

        Matcher m = INFO_VARIABLE_PATTERN.matcher(renderableText);
        while (m.find()) {
            if (m.group(1) != null && forName(m.group(1)).isPresent()) {
                // %variable%
                Function<?> function = forName(m.group(1)).get();
                dependencies.add(function);
            }
        }

        return dependencies;
    }

    public String[] getLinesFromLegacyTemplate(String renderableText) {
        StringBuilder builder = new StringBuilder(renderableText.length() + 10);
        Matcher m = INFO_VARIABLE_PATTERN.matcher(renderableText);
        while (m.find()) {
            String replacement = null;
            if (m.group(1) != null && forName(m.group(1)).isPresent()) {
                // %variable%
                Function<?> function = forName(m.group(1)).get();

                FunctionArguments.Builder arguments = function.getArgumentsBuilder();

                if (arguments instanceof FunctionArguments.OptionalArgumentBuilder optionalArgumentBuilder) {
                    replacement =
                            getStringFunctionValue(function, optionalArgumentBuilder.buildWithDefaults(), false, 2);
                } else {
                    // This is to be removed with legacy formatting all together
                    replacement = "Function needs arguments.";
                }

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

        return parseColorCodes(builder.toString()).split("\n");
    }

    private String parseColorCodes(String toProcess) {
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

    private String doEscapeFormat(String escaped) {
        return switch (escaped) {
            case "\\" -> "\\\\";
            case "n" -> "\n";
            case "%" -> "%";
            case "§" -> "&";
            case "E" -> EmeraldUnits.EMERALD.getSymbol();
            case "B" -> EmeraldUnits.EMERALD_BLOCK.getSymbol();
            case "L" -> EmeraldUnits.LIQUID_EMERALD.getSymbol();
            case "M" -> "✺";
            case "H" -> "❤";
            default -> null;
        };
    }
    // endregion

    private void registerFunction(Function<?> function) {
        functions.add(function);
    }

    private void registerAllFunctions() {
        // Generic Functions

        registerFunction(new MathFunctions.AddFunction());
        registerFunction(new MathFunctions.DivideFunction());
        registerFunction(new MathFunctions.ModuloFunction());
        registerFunction(new MathFunctions.MultiplyFunction());
        registerFunction(new MathFunctions.PowerFunction());
        registerFunction(new MathFunctions.RoundFunction());
        registerFunction(new MathFunctions.SquareRootFunction());
        registerFunction(new MathFunctions.SubtractFunction());

        // Regular Functions
        registerFunction(new WorldFunctions.CurrentWorldFunction());
        registerFunction(new WorldFunctions.CurrentWorldUptimeFunction());

        registerFunction(new CharacterFunctions.BpsFunction());
        registerFunction(new CharacterFunctions.BpsXzFunction());
        registerFunction(new CharacterFunctions.ClassFunction());
        registerFunction(new CharacterFunctions.HealthFunction());
        registerFunction(new CharacterFunctions.HealthMaxFunction());
        registerFunction(new CharacterFunctions.HealthPctFunction());
        registerFunction(new CharacterFunctions.ManaFunction());
        registerFunction(new CharacterFunctions.ManaMaxFunction());
        registerFunction(new CharacterFunctions.ManaPctFunction());
        registerFunction(new CharacterFunctions.SoulpointFunction());
        registerFunction(new CharacterFunctions.SoulpointMaxFunction());
        registerFunction(new CharacterFunctions.SoulpointTimerFunction());
        registerFunction(new CharacterFunctions.SoulpointTimerMFunction());
        registerFunction(new CharacterFunctions.SoulpointTimerSFunction());

        registerFunction(new CombatXpFunctions.LevelFunction());
        registerFunction(new CombatXpFunctions.XpFunction());
        registerFunction(new CombatXpFunctions.XpPctFunction());
        registerFunction(new CombatXpFunctions.XpPerMinuteFunction());
        registerFunction(new CombatXpFunctions.XpPerMinuteRawFunction());
        registerFunction(new CombatXpFunctions.XpPercentagePerMinuteFunction());
        registerFunction(new CombatXpFunctions.XpRawFunction());
        registerFunction(new CombatXpFunctions.XpReqFunction());
        registerFunction(new CombatXpFunctions.XpReqRawFunction());

        registerFunction(new EnvironmentFunctions.ClockFunction());
        registerFunction(new EnvironmentFunctions.ClockmFunction());
        registerFunction(new EnvironmentFunctions.MemMaxFunction());
        registerFunction(new EnvironmentFunctions.MemPctFunction());
        registerFunction(new EnvironmentFunctions.MemUsedFunction());

        registerFunction(new InventoryFunctions.EmeraldBlockFunction());
        registerFunction(new InventoryFunctions.EmeraldStringFunction());
        registerFunction(new InventoryFunctions.EmeraldsFunction());
        registerFunction(new InventoryFunctions.HeldItemCurrentDurabilityFunction());
        registerFunction(new InventoryFunctions.HeldItemMaxDurabilityFunction());
        registerFunction(new InventoryFunctions.IngredientPouchOpenSlotsFunction());
        registerFunction(new InventoryFunctions.IngredientPouchUsedSlotsFunction());
        registerFunction(new InventoryFunctions.InventoryFreeFunction());
        registerFunction(new InventoryFunctions.InventoryUsedFunction());
        registerFunction(new InventoryFunctions.LiquidEmeraldFunction());
        registerFunction(new InventoryFunctions.MoneyFunction());

        registerFunction(new HorseFunctions.HorseLevelFunction());
        registerFunction(new HorseFunctions.HorseLevelMaxFunction());
        registerFunction(new HorseFunctions.HorseNameFunction());
        registerFunction(new HorseFunctions.HorseTierFunction());
        registerFunction(new HorseFunctions.HorseXpFunction());

        registerFunction(new LootrunFunctions.DryBoxesFunction());
        registerFunction(new LootrunFunctions.DryStreakFunction());

        registerFunction(new MinecraftFunctions.DirFunction());
        registerFunction(new MinecraftFunctions.FpsFunction());
        registerFunction(new MinecraftFunctions.XFunction());
        registerFunction(new MinecraftFunctions.YFunction());
        registerFunction(new MinecraftFunctions.ZFunction());

        registerFunction(new ProfessionFunctions.AlchemismLevelFunction());
        registerFunction(new ProfessionFunctions.AlchemismPercentageFunction());
        registerFunction(new ProfessionFunctions.ArmouringLevelFunction());
        registerFunction(new ProfessionFunctions.ArmouringPercentageFunction());
        registerFunction(new ProfessionFunctions.CookingLevelFunction());
        registerFunction(new ProfessionFunctions.CookingPercentageFunction());
        registerFunction(new ProfessionFunctions.FarmingLevelFunction());
        registerFunction(new ProfessionFunctions.FarmingPercentageFunction());
        registerFunction(new ProfessionFunctions.FishingLevelFunction());
        registerFunction(new ProfessionFunctions.FishingPercentageFunction());
        registerFunction(new ProfessionFunctions.JewelingLevelFunction());
        registerFunction(new ProfessionFunctions.JewelingPercentageFunction());
        registerFunction(new ProfessionFunctions.MiningLevelFunction());
        registerFunction(new ProfessionFunctions.MiningPercentageFunction());
        registerFunction(new ProfessionFunctions.ScribingLevelFunction());
        registerFunction(new ProfessionFunctions.ScribingPercentageFunction());
        registerFunction(new ProfessionFunctions.TailoringLevelFunction());
        registerFunction(new ProfessionFunctions.TailoringPercentageFunction());
        registerFunction(new ProfessionFunctions.WeaponsmithingLevelFunction());
        registerFunction(new ProfessionFunctions.WeaponsmithingPercentageFunction());
        registerFunction(new ProfessionFunctions.WoodcuttingLevelFunction());
        registerFunction(new ProfessionFunctions.WoodcuttingPercentageFunction());
        registerFunction(new ProfessionFunctions.WoodworkingLevelFunction());
        registerFunction(new ProfessionFunctions.WoodworkingPercentageFunction());

        registerFunction(new SocialFunctions.OnlineFriendsFunction());
        registerFunction(new SocialFunctions.OnlinePartyMembersFunction());
    }
}
