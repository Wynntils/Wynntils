/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.consumers.functions;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Manager;
import com.wynntils.core.consumers.functions.arguments.Argument;
import com.wynntils.core.consumers.functions.arguments.FunctionArguments;
import com.wynntils.core.consumers.functions.arguments.parser.ArgumentParser;
import com.wynntils.core.consumers.functions.expressions.Expression;
import com.wynntils.core.consumers.functions.expressions.parser.ExpressionParser;
import com.wynntils.core.consumers.functions.templates.Template;
import com.wynntils.core.consumers.functions.templates.parser.TemplateParser;
import com.wynntils.core.mod.type.CrashType;
import com.wynntils.core.text.StyledText;
import com.wynntils.functions.ActivityFunctions;
import com.wynntils.functions.BombFunctions;
import com.wynntils.functions.CharacterFunctions;
import com.wynntils.functions.CombatFunctions;
import com.wynntils.functions.CombatXpFunctions;
import com.wynntils.functions.EnvironmentFunctions;
import com.wynntils.functions.GuildFunctions;
import com.wynntils.functions.HadesPartyFunctions;
import com.wynntils.functions.HorseFunctions;
import com.wynntils.functions.InventoryFunctions;
import com.wynntils.functions.LootrunFunctions;
import com.wynntils.functions.MinecraftFunctions;
import com.wynntils.functions.ProfessionFunctions;
import com.wynntils.functions.RaidFunctions;
import com.wynntils.functions.SocialFunctions;
import com.wynntils.functions.SpellFunctions;
import com.wynntils.functions.StatisticFunctions;
import com.wynntils.functions.StatusEffectFunctions;
import com.wynntils.functions.WarFunctions;
import com.wynntils.functions.WorldEventFunctions;
import com.wynntils.functions.WorldFunctions;
import com.wynntils.functions.WynnAlphabetFunctions;
import com.wynntils.functions.WynnFontFunctions;
import com.wynntils.functions.generic.CappedFunctions;
import com.wynntils.functions.generic.ColorFunctions;
import com.wynntils.functions.generic.ConditionalFunctions;
import com.wynntils.functions.generic.LocationFunctions;
import com.wynntils.functions.generic.LogicFunctions;
import com.wynntils.functions.generic.MathFunctions;
import com.wynntils.functions.generic.NamedFunctions;
import com.wynntils.functions.generic.RangedFunctions;
import com.wynntils.functions.generic.StringFunctions;
import com.wynntils.functions.generic.TimeFunctions;
import com.wynntils.models.emeralds.type.EmeraldUnits;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.type.ErrorOr;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

/** Manage all built-in {@link Function}s */
public final class FunctionManager extends Manager {
    private static final Pattern HEX_COLOR_PATTERN = Pattern.compile("&(?<!\\\\)(#[0-9A-Fa-f]{8})");
    private static final Pattern FORMATTING_CODE_PATTERN = Pattern.compile("&(?<!\\\\)([0-9a-fA-Fk-oK-OrR])");
    private static final Pattern NBSP_PATTERN = Pattern.compile("\u00A0");
    private final List<Function<?>> functions = new ArrayList<>();
    private final Set<Function<?>> crashedFunctions = new HashSet<>();

    // We do not clear this cache, as it is not expected to grow too large
    private final Map<String, Template> calculatedTemplateCache = new HashMap<>();

    public FunctionManager() {
        super(List.of());
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
        for (String alias : function.getAliasList()) {
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
            crashFunction(function);

            WynntilsMod.reportCrash(
                    CrashType.FUNCTION,
                    function.getTranslatedName(),
                    function.getClass().getName(),
                    "calculation",
                    throwable);
        }

        return Optional.empty();
    }

    // region String value calculations

    public Component getSimpleValueString(
            Function<?> function, String rawArguments, ChatFormatting color, boolean includeName) {
        MutableComponent header = includeName
                ? Component.literal(function.getTranslatedName() + ": ").withStyle(ChatFormatting.WHITE)
                : Component.literal("");

        ErrorOr<List<Expression>> errorOrArgumentExpressions =
                ArgumentParser.parseArguments(function.getArgumentsBuilder(), rawArguments);

        if (errorOrArgumentExpressions.hasError()) {
            return header.append(
                    Component.literal(errorOrArgumentExpressions.getError()).withStyle(ChatFormatting.RED));
        }

        List<ErrorOr<Object>> errorsOrArgumentObjects = errorOrArgumentExpressions.getValue().stream()
                .map(Expression::calculate)
                .toList();

        Optional<ErrorOr<Object>> argumentError =
                errorsOrArgumentObjects.stream().filter(ErrorOr::hasError).findFirst();
        if (argumentError.isPresent()) {
            return header.append(
                    Component.literal(argumentError.get().getError()).withStyle(ChatFormatting.RED));
        }

        ErrorOr<FunctionArguments> errorOrArgument = function.getArgumentsBuilder()
                .buildWithValues(
                        errorsOrArgumentObjects.stream().map(ErrorOr::getValue).toList());
        if (errorOrArgument.hasError()) {
            return header.append(Component.literal(errorOrArgument.getError()).withStyle(ChatFormatting.RED));
        }

        Optional<Object> value = getFunctionValueSafely(function, errorOrArgument.getValue());
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
        if (value instanceof Integer integer && !formatted) {
            return String.valueOf(integer);
        } else if (value instanceof Number number) {
            if (formatted) {
                // French locale has NBSP
                // https://stackoverflow.com/questions/34156585/java-decimal-format-parsing-issue
                NumberFormat instance = NumberFormat.getInstance();
                instance.setMinimumFractionDigits(decimals);
                instance.setMaximumFractionDigits(decimals);

                return NBSP_PATTERN.matcher(instance.format(number)).replaceAll(" ");
            } else {
                if (decimals == 0) {
                    return String.valueOf(number.intValue());
                }

                DecimalFormat decimalFormat = new DecimalFormat("0." + "0".repeat(decimals));
                return decimalFormat.format(number);
            }
        } else if (value instanceof CustomColor color) {
            return "§" + color.toHexString();
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

    public <T> ErrorOr<T> tryGetRawValueOfType(String valueTemplate, Class<T> clazz) {
        ErrorOr<Expression> valueExpression = ExpressionParser.tryParse(valueTemplate);

        if (valueExpression.hasError()) {
            return ErrorOr.error(valueExpression.getError());
        }

        Expression expression = valueExpression.getValue();

        ErrorOr<Object> calculatedExpression = expression.calculate();

        if (calculatedExpression.hasError()) {
            return ErrorOr.error(calculatedExpression.getError());
        }

        try {
            return ErrorOr.of(clazz.cast(calculatedExpression.getValue()));
        } catch (Exception e) {
            return ErrorOr.error("The provided expression does not return a " + clazz.getSimpleName());
        }
    }

    // endregion

    // region Template formatting

    private String doFormat(String templateString) {
        calculatedTemplateCache.computeIfAbsent(templateString, TemplateParser::getTemplateFromString);
        return calculatedTemplateCache.get(templateString).getString();
    }

    public StyledText[] doFormatLines(String templateString) {
        StringBuilder resultBuilder = new StringBuilder();

        // Iterate though the string and escape characters
        // that are prefixed with `\`, remove the `\` and add it to the result

        for (int i = 0; i < templateString.length(); i++) {
            char c = templateString.charAt(i);
            if (c == '\\') {
                if (i + 1 < templateString.length()) {
                    char nextChar = templateString.charAt(i + 1);

                    resultBuilder.append(doEscapeFormat(nextChar));
                    i++;

                    continue;
                }
            }

            resultBuilder.append(c);
        }

        // Parse color codes before calculating the templates
        String escapedTemplate = parseColorCodes(resultBuilder.toString());

        String calculatedString = doFormat(escapedTemplate);

        // Turn escaped {}& (`\[\`, `\]\` `\&\`) back into real {}&
        calculatedString = calculatedString.replace("\\[\\", "{");
        calculatedString = calculatedString.replace("\\]\\", "}");
        calculatedString = calculatedString.replace("\\&\\", "&");

        return Arrays.stream(calculatedString.split("\n"))
                .map(StyledText::fromString)
                .toArray(StyledText[]::new);
    }

    private String parseColorCodes(String toProcess) {
        // Replace &<code> with §<code> if not escaped (e.g., &a → §a, but \&\a stays unchanged)
        // doEscapeFormat preprocesses the string and replaces \& with \&\ so that it doesn't get replaced
        String processed = FORMATTING_CODE_PATTERN.matcher(toProcess).replaceAll("§$1");

        // Replace &#AARRGGBB with §#AARRGGBB for hex colors
        processed = HEX_COLOR_PATTERN.matcher(processed).replaceAll("§$1");

        return processed;
    }

    private String doEscapeFormat(char escaped) {
        return switch (escaped) {
            case '\\' -> "\\\\";
            case 'n' -> "\n";
            case '{' -> "\\[\\";
            case '}' -> "\\]\\";
            case 'E' -> EmeraldUnits.EMERALD.getSymbol();
            case 'B' -> EmeraldUnits.EMERALD_BLOCK.getSymbol();
            case 'L' -> EmeraldUnits.LIQUID_EMERALD.getSymbol();
            case 'M' -> "✺";
            case 'H' -> "❤";
            case '&' -> "\\&\\";
            default -> '\\' + String.valueOf(escaped);
        };
    }

    // endregion

    public void init() {
        try {
            registerAllFunctions();
        } catch (AssertionError ae) {
            WynntilsMod.error("Fix i18n for functions", ae);
            if (WynntilsMod.isDevelopmentEnvironment()) {
                System.exit(1);
            }
        }
    }

    private void registerFunction(Function<?> function) {
        functions.add(function);

        assert !function.getTranslatedName().startsWith("function.wynntils.")
                : "Fix i18n name for " + function.getTranslatedName();
        assert !function.getDescription().startsWith("function.wynntils.")
                : "Fix i18n description for " + function.getDescription();
        for (Argument<?> argument : function.getArgumentsBuilder().getArguments()) {
            assert !function.getArgumentDescription(argument.getName()).startsWith("function.wynntils.")
                    : "Fix i18n argument description for " + function.getArgumentDescription(argument.getName());
        }
    }

    private void registerAllFunctions() {
        // Generic Functions
        registerFunction(new CappedFunctions.AtCapFunction());
        registerFunction(new CappedFunctions.CapFunction());
        registerFunction(new CappedFunctions.CappedFunction());
        registerFunction(new CappedFunctions.CurrentFunction());
        registerFunction(new CappedFunctions.PercentageFunction());
        registerFunction(new CappedFunctions.RemainingFunction());

        registerFunction(new ColorFunctions.BlinkShaderFunction());
        registerFunction(new ColorFunctions.BrightnessShiftFunction());
        registerFunction(new ColorFunctions.FadeShaderFunction());
        registerFunction(new ColorFunctions.FromHexFunction());
        registerFunction(new ColorFunctions.FromRgbFunction());
        registerFunction(new ColorFunctions.FromRgbPercentFunction());
        registerFunction(new ColorFunctions.GradientShaderFunction());
        registerFunction(new ColorFunctions.HueShiftFunction());
        registerFunction(new ColorFunctions.RainbowShaderFunction());
        registerFunction(new ColorFunctions.SaturationShiftFunction());
        registerFunction(new ColorFunctions.ShineShaderFunction());
        registerFunction(new ColorFunctions.ToHexStringFunction());

        registerFunction(new ConditionalFunctions.IfCappedValueFunction());
        registerFunction(new ConditionalFunctions.IfCustomColorFunction());
        registerFunction(new ConditionalFunctions.IfFunction());
        registerFunction(new ConditionalFunctions.IfNumberFunction());
        registerFunction(new ConditionalFunctions.IfStringFunction());

        registerFunction(new LocationFunctions.DistanceFunction());
        registerFunction(new LocationFunctions.LocationFunction());
        registerFunction(new LocationFunctions.XFunction());
        registerFunction(new LocationFunctions.YFunction());
        registerFunction(new LocationFunctions.ZFunction());

        registerFunction(new LogicFunctions.AndFunction());
        registerFunction(new LogicFunctions.EqualsFunction());
        registerFunction(new LogicFunctions.GreaterThanFunction());
        registerFunction(new LogicFunctions.GreaterThanOrEqualsFunction());
        registerFunction(new LogicFunctions.LessThanFunction());
        registerFunction(new LogicFunctions.LessThanOrEqualsFunction());
        registerFunction(new LogicFunctions.NotEqualsFunction());
        registerFunction(new LogicFunctions.NotFunction());
        registerFunction(new LogicFunctions.OrFunction());

        registerFunction(new MathFunctions.AddFunction());
        registerFunction(new MathFunctions.DivideFunction());
        registerFunction(new MathFunctions.IntegerFunction());
        registerFunction(new MathFunctions.LongFunction());
        registerFunction(new MathFunctions.MaxFunction());
        registerFunction(new MathFunctions.MinFunction());
        registerFunction(new MathFunctions.ModuloFunction());
        registerFunction(new MathFunctions.MultiplyFunction());
        registerFunction(new MathFunctions.PowerFunction());
        registerFunction(new MathFunctions.RandomFunction());
        registerFunction(new MathFunctions.RoundFunction());
        registerFunction(new MathFunctions.SquareRootFunction());
        registerFunction(new MathFunctions.SubtractFunction());

        registerFunction(new NamedFunctions.NameFunction());
        registerFunction(new NamedFunctions.NamedValueFunction());
        registerFunction(new NamedFunctions.ValueFunction());

        registerFunction(new StringFunctions.CappedStringFunction());
        registerFunction(new StringFunctions.ConcatFunction());
        registerFunction(new StringFunctions.FormatCappedFunction());
        registerFunction(new StringFunctions.FormatDateFunction());
        registerFunction(new StringFunctions.FormatDurationFunction());
        registerFunction(new StringFunctions.FormatFunction());
        registerFunction(new StringFunctions.FormatRangedFunction());
        registerFunction(new StringFunctions.LeadingZerosFunction());
        registerFunction(new StringFunctions.ParseDoubleFunction());
        registerFunction(new StringFunctions.ParseIntegerFunction());
        registerFunction(new StringFunctions.RegexFindFunction());
        registerFunction(new StringFunctions.RegexMatchFunction());
        registerFunction(new StringFunctions.RegexReplaceFunction());
        registerFunction(new StringFunctions.RepeatFunction());
        registerFunction(new StringFunctions.StringContainsFunction());
        registerFunction(new StringFunctions.StringEqualsFunction());
        registerFunction(new StringFunctions.StringFunction());
        registerFunction(new StringFunctions.ToRomanNumeralsFunction());

        registerFunction(new TimeFunctions.AbsoluteTimeFunction());
        registerFunction(new TimeFunctions.FormatTimeAdvancedFunction());
        registerFunction(new TimeFunctions.SecondsBetweenFunction());
        registerFunction(new TimeFunctions.SecondsSinceFunction());
        registerFunction(new TimeFunctions.TimeFunction());
        registerFunction(new TimeFunctions.TimeOffsetFunction());
        registerFunction(new TimeFunctions.TimeStringFunction());
        registerFunction(new TimeFunctions.TimestampFunction());

        // Regular Functions
        registerFunction(new ActivityFunctions.ActivityColorFunction());
        registerFunction(new ActivityFunctions.ActivityNameFunction());
        registerFunction(new ActivityFunctions.ActivityTaskFunction());
        registerFunction(new ActivityFunctions.ActivityTypeFunction());
        registerFunction(new ActivityFunctions.IsTrackingActivityFunction());

        registerFunction(new BombFunctions.BombEndTimeFunction());
        registerFunction(new BombFunctions.BombFormattedStringFunction());
        registerFunction(new BombFunctions.BombLengthFunction());
        registerFunction(new BombFunctions.BombOwnerFunction());
        registerFunction(new BombFunctions.BombRemainingTimeFunction());
        registerFunction(new BombFunctions.BombStartTimeFunction());
        registerFunction(new BombFunctions.BombTypeFunction());
        registerFunction(new BombFunctions.BombWorldFunction());

        registerFunction(new CharacterFunctions.AspectTierFunction());
        registerFunction(new CharacterFunctions.BpsFunction());
        registerFunction(new CharacterFunctions.BpsXzFunction());
        registerFunction(new CharacterFunctions.CappedAwakenedProgressFunction());
        registerFunction(new CharacterFunctions.CappedBloodPoolFunction());
        registerFunction(new CharacterFunctions.CappedCorruptedFunction());
        registerFunction(new CharacterFunctions.CappedFocusFunction());
        registerFunction(new CharacterFunctions.CappedHealthFunction());
        registerFunction(new CharacterFunctions.CappedHolyPowerFunction());
        registerFunction(new CharacterFunctions.CappedManaBankFunction());
        registerFunction(new CharacterFunctions.CappedManaFunction());
        registerFunction(new CharacterFunctions.CappedOphanimFunction());
        registerFunction(new CharacterFunctions.ClassFunction());
        registerFunction(new CharacterFunctions.CommanderActivatedFunction());
        registerFunction(new CharacterFunctions.CommanderDurationFunction());
        registerFunction(new CharacterFunctions.EquippedAspectFunction());
        registerFunction(new CharacterFunctions.GuildObjectiveGoalFunction());
        registerFunction(new CharacterFunctions.GuildObjectiveEventBonusFunction());
        registerFunction(new CharacterFunctions.GuildObjectiveScoreFunction());
        registerFunction(new CharacterFunctions.HasNoGuiFunction());
        registerFunction(new CharacterFunctions.HealthFunction());
        registerFunction(new CharacterFunctions.HealthMaxFunction());
        registerFunction(new CharacterFunctions.HealthPctFunction());
        registerFunction(new CharacterFunctions.HummingbirdsStateFunction());
        registerFunction(new CharacterFunctions.IdFunction());
        registerFunction(new CharacterFunctions.IsAspectEquippedFunction());
        registerFunction(new CharacterFunctions.IsRidingHorseFunction());
        registerFunction(new CharacterFunctions.LeaderboardPositionFunction());
        registerFunction(new CharacterFunctions.ManaFunction());
        registerFunction(new CharacterFunctions.ManaMaxFunction());
        registerFunction(new CharacterFunctions.ManaPctFunction());
        registerFunction(new CharacterFunctions.MomentumFunction());
        registerFunction(new CharacterFunctions.MomentumPercentFunction());
        registerFunction(new CharacterFunctions.OphanimActive());
        registerFunction(new CharacterFunctions.OphanimHealingPercentFunction());
        registerFunction(new CharacterFunctions.OphanimOrb());
        registerFunction(new CharacterFunctions.PersonalObjectiveGoalFunction());
        registerFunction(new CharacterFunctions.PersonalObjectiveEventBonusFunction());
        registerFunction(new CharacterFunctions.PersonalObjectiveScoreFunction());
        registerFunction(new CharacterFunctions.SprintFunction());

        registerFunction(new CombatFunctions.AreaDamageAverageFunction());
        registerFunction(new CombatFunctions.AreaDamagePerSecondFunction());
        registerFunction(new CombatFunctions.BlocksAboveGroundFunction());
        registerFunction(new CombatFunctions.FocusedMobHealthFunction());
        registerFunction(new CombatFunctions.FocusedMobHealthPercentFunction());
        registerFunction(new CombatFunctions.FocusedMobNameFunction());
        registerFunction(new CombatFunctions.KillsPerMinuteFunction());
        registerFunction(new CombatFunctions.LastDamageDealtFunction());
        registerFunction(new CombatFunctions.LastKillFunction());
        registerFunction(new CombatFunctions.LastSpellNameFunction());
        registerFunction(new CombatFunctions.LastSpellRepeatCountFunction());
        registerFunction(new CombatFunctions.TicksSinceLastSpellFunction());
        registerFunction(new CombatFunctions.TimeSinceLastDamageDealtFunction());
        registerFunction(new CombatFunctions.TimeSinceLastKillFunction());
        registerFunction(new CombatFunctions.TotalAreaDamageFunction());

        registerFunction(new CombatXpFunctions.CappedLevelFunction());
        registerFunction(new CombatXpFunctions.CappedXpFunction());
        registerFunction(new CombatXpFunctions.LevelFunction());
        registerFunction(new CombatXpFunctions.XpFunction());
        registerFunction(new CombatXpFunctions.XpOverflowFunction());
        registerFunction(new CombatXpFunctions.XpPctFunction());
        registerFunction(new CombatXpFunctions.XpPerMinuteFunction());
        registerFunction(new CombatXpFunctions.XpPerMinuteRawFunction());
        registerFunction(new CombatXpFunctions.XpPercentagePerMinuteFunction());
        registerFunction(new CombatXpFunctions.XpRawFunction());
        registerFunction(new CombatXpFunctions.XpReqFunction());
        registerFunction(new CombatXpFunctions.XpReqRawFunction());

        registerFunction(new EnvironmentFunctions.CappedMemFunction());
        registerFunction(new EnvironmentFunctions.ClockFunction());
        registerFunction(new EnvironmentFunctions.ClockmFunction());
        registerFunction(new EnvironmentFunctions.MemMaxFunction());
        registerFunction(new EnvironmentFunctions.MemPctFunction());
        registerFunction(new EnvironmentFunctions.MemUsedFunction());
        registerFunction(new EnvironmentFunctions.MinecraftVersionFunction());
        registerFunction(new EnvironmentFunctions.NowFunction());
        registerFunction(new EnvironmentFunctions.StopwatchHoursFunction());
        registerFunction(new EnvironmentFunctions.StopwatchMillisecondsFunction());
        registerFunction(new EnvironmentFunctions.StopwatchMinutesFunction());
        registerFunction(new EnvironmentFunctions.StopwatchRunningFunction());
        registerFunction(new EnvironmentFunctions.StopwatchSecondsFunction());
        registerFunction(new EnvironmentFunctions.StopwatchZero());
        registerFunction(new EnvironmentFunctions.WynncraftVersionFunction());
        registerFunction(new EnvironmentFunctions.WynntilsVersionFunction());

        registerFunction(new GuildFunctions.CappedGuildLevelProgressFunction());
        registerFunction(new GuildFunctions.CappedGuildObjectivesProgressFunction());
        registerFunction(new GuildFunctions.ContributedGuildXpFunction());
        registerFunction(new GuildFunctions.ContributionRankFunction());
        registerFunction(new GuildFunctions.GuildLevelFunction());
        registerFunction(new GuildFunctions.GuildNameFunction());
        registerFunction(new GuildFunctions.GuildRankFunction());
        registerFunction(new GuildFunctions.IsAlliedGuildFunction());
        registerFunction(new GuildFunctions.IsGuildMemberFunction());
        registerFunction(new GuildFunctions.ObjectiveStreakFunction());

        registerFunction(new HadesPartyFunctions.HadesPartyMemberHealthFunction());
        registerFunction(new HadesPartyFunctions.HadesPartyMemberLocationFunction());
        registerFunction(new HadesPartyFunctions.HadesPartyMemberManaFunction());
        registerFunction(new HadesPartyFunctions.HadesPartyMemberNameFunction());

        registerFunction(new HorseFunctions.CappedHorseLevelFunction());
        registerFunction(new HorseFunctions.CappedHorseTotalLevelTimeFunction());
        registerFunction(new HorseFunctions.CappedHorseXpFunction());
        registerFunction(new HorseFunctions.HorseLevelFunction());
        registerFunction(new HorseFunctions.HorseLevelMaxFunction());
        registerFunction(new HorseFunctions.HorseLevelTimeFunction());
        registerFunction(new HorseFunctions.HorseNameFunction());
        registerFunction(new HorseFunctions.HorseTierFunction());
        registerFunction(new HorseFunctions.HorseXpFunction());

        registerFunction(new InventoryFunctions.AccessoryDurabilityFunction());
        registerFunction(new InventoryFunctions.AllShinyStatsFunction());
        registerFunction(new InventoryFunctions.ArmorDurabilityFunction());
        registerFunction(new InventoryFunctions.CappedHeldItemDurabilityFunction());
        registerFunction(new InventoryFunctions.CappedIngredientPouchSlotsFunction());
        registerFunction(new InventoryFunctions.CappedInventorySlotsFunction());
        registerFunction(new InventoryFunctions.EmeraldBlockFunction());
        registerFunction(new InventoryFunctions.EmeraldStringFunction());
        registerFunction(new InventoryFunctions.EmeraldsFunction());
        registerFunction(new InventoryFunctions.EquippedArmorNameFunction());
        registerFunction(new InventoryFunctions.HeldItemCooldownFunction());
        registerFunction(new InventoryFunctions.HeldItemCurrentDurabilityFunction());
        registerFunction(new InventoryFunctions.HeldItemMaxDurabilityFunction());
        registerFunction(new InventoryFunctions.HeldItemNameFunction());
        registerFunction(new InventoryFunctions.HeldItemShinyStatFunction());
        registerFunction(new InventoryFunctions.HeldItemTypeFunction());
        registerFunction(new InventoryFunctions.IngredientPouchIngredientsFunction());
        registerFunction(new InventoryFunctions.IngredientPouchOpenSlotsFunction());
        registerFunction(new InventoryFunctions.IngredientPouchUsedSlotsFunction());
        registerFunction(new InventoryFunctions.InventoryFreeFunction());
        registerFunction(new InventoryFunctions.InventoryIngredientsFunction());
        registerFunction(new InventoryFunctions.InventoryUsedFunction());
        registerFunction(new InventoryFunctions.ItemCountFunction());
        registerFunction(new InventoryFunctions.LiquidEmeraldFunction());
        registerFunction(new InventoryFunctions.MaterialCountFunction());
        registerFunction(new InventoryFunctions.MoneyFunction());

        registerFunction(new LootrunFunctions.ChestOpenedFunction());
        registerFunction(new LootrunFunctions.ChestsOpenedThisSessionFunction());
        registerFunction(new LootrunFunctions.DryBoxesFunction());
        registerFunction(new LootrunFunctions.DryPullsFunction());
        registerFunction(new LootrunFunctions.DryStreakFunction());
        registerFunction(new LootrunFunctions.HighestDryStreakFunction());
        registerFunction(new LootrunFunctions.LastDryStreakFunction());
        registerFunction(new LootrunFunctions.LastMythicFunction());
        registerFunction(new LootrunFunctions.LootrunBeaconCountFunction());
        registerFunction(new LootrunFunctions.LootrunBeaconVibrantFunction());
        registerFunction(new LootrunFunctions.LootrunChallengesFunction());
        registerFunction(new LootrunFunctions.LootrunLastSelectedBeaconColorFunction());
        registerFunction(new LootrunFunctions.LootrunLastSelectedBeaconVibrantFunction());
        registerFunction(new LootrunFunctions.LootrunMissionFunction());
        registerFunction(new LootrunFunctions.LootrunNextOrangeExpireFunction());
        registerFunction(new LootrunFunctions.LootrunOrangeBeaconCountFunction());
        registerFunction(new LootrunFunctions.LootrunRainbowBeaconCountFunction());
        registerFunction(new LootrunFunctions.LootrunRedBeaconChallengeCountFunction());
        registerFunction(new LootrunFunctions.LootrunRerollsFunction());
        registerFunction(new LootrunFunctions.LootrunSacrificesFunction());
        registerFunction(new LootrunFunctions.LootrunStateFunction());
        registerFunction(new LootrunFunctions.LootrunTaskLocationFunction());
        registerFunction(new LootrunFunctions.LootrunTaskNameFunction());
        registerFunction(new LootrunFunctions.LootrunTaskTypeFunction());
        registerFunction(new LootrunFunctions.LootrunTimeFunction());
        registerFunction(new LootrunFunctions.LootrunTrialFunction());

        registerFunction(new MinecraftFunctions.DirFunction());
        registerFunction(new MinecraftFunctions.FpsFunction());
        registerFunction(new MinecraftFunctions.KeyPressedFunction());
        registerFunction(new MinecraftFunctions.MinecraftEffectDurationFunction());
        registerFunction(new MinecraftFunctions.MyLocationFunction());
        registerFunction(new MinecraftFunctions.TicksFunction());

        registerFunction(new ProfessionFunctions.LastHarvestMaterialLevelFunction());
        registerFunction(new ProfessionFunctions.LastHarvestMaterialNameFunction());
        registerFunction(new ProfessionFunctions.LastHarvestMaterialTierFunction());
        registerFunction(new ProfessionFunctions.LastHarvestMaterialTypeFunction());
        registerFunction(new ProfessionFunctions.LastHarvestResourceTypeFunction());
        registerFunction(new ProfessionFunctions.LastHarvestXpGainFunction());
        registerFunction(new ProfessionFunctions.LastProfessionXpGainFunction());
        registerFunction(new ProfessionFunctions.MaterialDryStreak());
        registerFunction(new ProfessionFunctions.ProfessionLevelFunction());
        registerFunction(new ProfessionFunctions.ProfessionPercentageFunction());
        registerFunction(new ProfessionFunctions.ProfessionXpFunction());
        registerFunction(new ProfessionFunctions.ProfessionXpPerMinuteFunction());
        registerFunction(new ProfessionFunctions.ProfessionXpPerMinuteRawFunction());

        registerFunction(new RaidFunctions.CurrentRaidBossCountFunction());
        registerFunction(new RaidFunctions.CurrentRaidChallengeCountFunction());
        registerFunction(new RaidFunctions.CurrentRaidDamageFunction());
        registerFunction(new RaidFunctions.CurrentRaidFunction());
        registerFunction(new RaidFunctions.CurrentRaidRoomDamageFunction());
        registerFunction(new RaidFunctions.CurrentRaidRoomNameFunction());
        registerFunction(new RaidFunctions.CurrentRaidRoomStartFunction());
        registerFunction(new RaidFunctions.CurrentRaidRoomTimeFunction());
        registerFunction(new RaidFunctions.CurrentRaidStartFunction());
        registerFunction(new RaidFunctions.CurrentRaidTimeFunction());
        registerFunction(new RaidFunctions.DryAspectsFunction());
        registerFunction(new RaidFunctions.DryRaidRewardPullsFunction());
        registerFunction(new RaidFunctions.DryRaidsAspectsFunction());
        registerFunction(new RaidFunctions.DryRaidsTomesFunction());
        registerFunction(new RaidFunctions.RaidChallengesFunction());
        registerFunction(new RaidFunctions.RaidHasRoomFunction());
        registerFunction(new RaidFunctions.RaidIntermissionTimeFunction());
        registerFunction(new RaidFunctions.RaidIsBossRoomFunction());
        registerFunction(new RaidFunctions.RaidPersonalBestTimeFunction());
        registerFunction(new RaidFunctions.RaidRoomDamageFunction());
        registerFunction(new RaidFunctions.RaidRoomNameFunction());
        registerFunction(new RaidFunctions.RaidRoomStartFunction());
        registerFunction(new RaidFunctions.RaidRoomTimeFunction());
        registerFunction(new RaidFunctions.RaidTimeRemainingFunction());
        registerFunction(new RaidFunctions.RaidsRunsSinceFunction());
        registerFunction(new RaidFunctions.SpecificRaidRunsSinceFunction());

        registerFunction(new RangedFunctions.RangeHighFunction());
        registerFunction(new RangedFunctions.RangeLowFunction());
        registerFunction(new RangedFunctions.RangedFunction());

        registerFunction(new SocialFunctions.FriendsFunction());
        registerFunction(new SocialFunctions.IsFriendFunction());
        registerFunction(new SocialFunctions.IsPartyMemberFunction());
        registerFunction(new SocialFunctions.PartyLeaderFunction());
        registerFunction(new SocialFunctions.PartyMembersFunction());
        registerFunction(new SocialFunctions.PlayerNameFunction());
        registerFunction(new SocialFunctions.WynntilsRoleFunction());

        registerFunction(new SpellFunctions.ArrowShieldCountFunction());
        registerFunction(new SpellFunctions.GuardianAngelsCountFunction());
        registerFunction(new SpellFunctions.MantleShieldCountFunction());
        registerFunction(new SpellFunctions.ShamanMaskFunction());
        registerFunction(new SpellFunctions.ShamanTotemDistanceFunction());
        registerFunction(new SpellFunctions.ShamanTotemLocationFunction());
        registerFunction(new SpellFunctions.ShamanTotemStateFunction());
        registerFunction(new SpellFunctions.ShamanTotemTimeLeftFunction());
        registerFunction(new SpellFunctions.ShieldTypeNameFunction());

        registerFunction(new StatisticFunctions.StatisticsAverageFunction());
        registerFunction(new StatisticFunctions.StatisticsCountFunction());
        registerFunction(new StatisticFunctions.StatisticsFirstModifiedFunction());
        registerFunction(new StatisticFunctions.StatisticsFirstModifiedTimeFunction());
        registerFunction(new StatisticFunctions.StatisticsFormattedFunction());
        registerFunction(new StatisticFunctions.StatisticsLastModifiedFunction());
        registerFunction(new StatisticFunctions.StatisticsLastModifiedTimeFunction());
        registerFunction(new StatisticFunctions.StatisticsMaxFunction());
        registerFunction(new StatisticFunctions.StatisticsMinFunction());
        registerFunction(new StatisticFunctions.StatisticsTotalFunction());

        registerFunction(new StatusEffectFunctions.StatusEffectActiveFunction());
        registerFunction(new StatusEffectFunctions.StatusEffectDurationFunction());
        registerFunction(new StatusEffectFunctions.StatusEffectModifierFunction());
        registerFunction(new StatusEffectFunctions.StatusEffectPrefixFunction());
        registerFunction(new StatusEffectFunctions.StatusEffectsFunction());

        registerFunction(new WarFunctions.AuraTimerFunction());
        registerFunction(new WarFunctions.CurrentTowerAttackSpeedFunction());
        registerFunction(new WarFunctions.CurrentTowerDamageFunction());
        registerFunction(new WarFunctions.CurrentTowerDefenseFunction());
        registerFunction(new WarFunctions.CurrentTowerHealthFunction());
        registerFunction(new WarFunctions.EstimatedTimeToFinishWarFunction());
        registerFunction(new WarFunctions.EstimatedWarEndFunction());
        registerFunction(new WarFunctions.InitialTowerAttackSpeedFunction());
        registerFunction(new WarFunctions.InitialTowerDamageFunction());
        registerFunction(new WarFunctions.InitialTowerDefenseFunction());
        registerFunction(new WarFunctions.InitialTowerHealthFunction());
        registerFunction(new WarFunctions.IsTerritoryQueuedFunction());
        registerFunction(new WarFunctions.TeamDpsFunction());
        registerFunction(new WarFunctions.TimeInWarFunction());
        registerFunction(new WarFunctions.TowerDpsFunction());
        registerFunction(new WarFunctions.TowerEffectiveHpFunction());
        registerFunction(new WarFunctions.TowerOwnerFunction());
        registerFunction(new WarFunctions.TowerTerritoryFunction());
        registerFunction(new WarFunctions.VolleyTimerFunction());
        registerFunction(new WarFunctions.WarStartFunction());
        registerFunction(new WarFunctions.WarsSinceFunction());

        registerFunction(new WorldEventFunctions.AnnihilationDryCount());
        registerFunction(new WorldEventFunctions.AnnihilationSunProgressFunction());
        registerFunction(new WorldEventFunctions.CurrentWorldEventFunction());
        registerFunction(new WorldEventFunctions.CurrentWorldEventStartTimeFunction());
        registerFunction(new WorldEventFunctions.WorldEventStartTimeFunction());

        registerFunction(new WorldFunctions.CurrentTerritoryFunction());
        registerFunction(new WorldFunctions.CurrentTerritoryOwnerFunction());
        registerFunction(new WorldFunctions.CurrentWorldFunction());
        registerFunction(new WorldFunctions.GatheringTotemCountFunction());
        registerFunction(new WorldFunctions.GatheringTotemDistanceFunction());
        registerFunction(new WorldFunctions.GatheringTotemFunction());
        registerFunction(new WorldFunctions.GatheringTotemOwnerFunction());
        registerFunction(new WorldFunctions.GatheringTotemTimeLeftFunction());
        registerFunction(new WorldFunctions.InMappedAreaFunction());
        registerFunction(new WorldFunctions.InStreamFunction());
        registerFunction(new WorldFunctions.MobTotemCountFunction());
        registerFunction(new WorldFunctions.MobTotemDistanceFunction());
        registerFunction(new WorldFunctions.MobTotemFunction());
        registerFunction(new WorldFunctions.MobTotemOwnerFunction());
        registerFunction(new WorldFunctions.MobTotemTimeLeftFunction());
        registerFunction(new WorldFunctions.NewestWorldFunction());
        registerFunction(new WorldFunctions.PingFunction());
        registerFunction(new WorldFunctions.TokenGatekeeperCountFunction());
        registerFunction(new WorldFunctions.TokenGatekeeperDepositedFunction());
        registerFunction(new WorldFunctions.TokenGatekeeperFunction());
        registerFunction(new WorldFunctions.TokenGatekeeperTypeFunction());
        registerFunction(new WorldFunctions.WorldStateFunction());
        registerFunction(new WorldFunctions.WorldUptimeFunction());

        registerFunction(new WynnAlphabetFunctions.TranscribeGavellianFunction());
        registerFunction(new WynnAlphabetFunctions.TranscribeWynnicFunction());

        registerFunction(new WynnFontFunctions.ToBackgroundTextFunction());
        registerFunction(new WynnFontFunctions.ToFancyTextFunction());
    }
}
