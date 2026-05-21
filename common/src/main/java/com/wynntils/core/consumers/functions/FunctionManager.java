/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.consumers.functions;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Manager;
import com.wynntils.core.text.StyledText;

import com.wynntils.functions.ActivityFunctions;
import com.wynntils.functions.BombFunctions;
import com.wynntils.functions.CharacterFunctions;
import com.wynntils.functions.CombatFunctions;
import com.wynntils.functions.CombatXpFunctions;
import com.wynntils.functions.EnvironmentFunctions;
import com.wynntils.functions.GuildFunctions;
import com.wynntils.functions.HadesPartyFunctions;
import com.wynntils.functions.InventoryFunctions;
import com.wynntils.functions.LootrunFunctions;
import com.wynntils.functions.MinecraftFunctions;
import com.wynntils.functions.MountFunctions;
import com.wynntils.functions.ProfessionFunctions;
import com.wynntils.functions.SocialFunctions;
import com.wynntils.functions.WynnFontFunctions;
import com.wynntils.functions.generic.CappedFunctions;
import com.wynntils.functions.generic.ColorFunctions;
import com.wynntils.functions.generic.ConditionalFunctions;
import com.wynntils.functions.generic.LocationFunctions;
import com.wynntils.functions.generic.LogicFunctions;
import com.wynntils.functions.generic.MathFunctions;
import com.wynntils.functions.generic.NamedFunctions;
import com.wynntils.functions.generic.StringFunctions;
import com.wynntils.functions.generic.StyledTextFunctions;
import com.wynntils.models.emeralds.type.EmeraldUnits;
import com.wynntils.templates.TemplateEngine;
import com.wynntils.templates.compiler.CompilerBackend;
import com.wynntils.templates.functions.FunctionDefinition;
import com.wynntils.templates.language.exception.LanguageException;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

public final class FunctionManager extends Manager {
    private static final Pattern HEX_COLOR_PATTERN = Pattern.compile("&(?<!\\\\)(#[0-9A-Fa-f]{8})");
    private static final Pattern FORMATTING_CODE_PATTERN = Pattern.compile("&(?<!\\\\)([0-9a-fA-Fk-oK-OrR])");
    private static final Pattern NBSP_PATTERN = Pattern.compile("\u00A0");
    private final TemplateEngine templateEngine;

    public FunctionManager() {
        super(List.of());
        this.templateEngine = new TemplateEngine(new CompilerBackend(this.getClass().getClassLoader()));
    }

    public List<FunctionDefinition> getFunctions() {
        return templateEngine.getFunctions();
    }

    public Optional<FunctionDefinition> forName(String functionName) {
        for (FunctionDefinition function : getFunctions()) {
            if (Objects.equals(function.name(), functionName)) {
                return Optional.of(function);
            }
        }

        return Optional.empty();
    }

    // region Template formatting

    private String evaluate(String template) {
        String ret = templateEngine.evaluate(template);

        if(templateEngine.hasError()) {
            WynntilsMod.error("\n" + templateEngine.getError());
        }

        return ret;
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

        String calculatedString = evaluate(escapedTemplate);

        // Turn escaped {}& (`\[\`, `\]\` `\&\`) back into real {}&
        calculatedString = calculatedString.replace("\\[\\", "{");
        calculatedString = calculatedString.replace("\\]\\", "}");
        calculatedString = calculatedString.replace("\\&\\", "&");

        return StyledText.fromString(calculatedString).split("\n");
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
    private void registerAllFunctions() {
        templateEngine.registerFunctions(CappedFunctions.class);
        templateEngine.registerFunctions(ColorFunctions.class);
        templateEngine.registerFunctions(ConditionalFunctions.class);
        templateEngine.registerFunctions(LocationFunctions.class);
        templateEngine.registerFunctions(LogicFunctions.class);
        templateEngine.registerFunctions(MathFunctions.class);
        templateEngine.registerFunctions(NamedFunctions.class);
        templateEngine.registerFunctions(StringFunctions.class);
        templateEngine.registerFunctions(StyledTextFunctions.class);
        templateEngine.registerFunctions(WynnFontFunctions.class);

        templateEngine.registerFunctions(ActivityFunctions.class);
        templateEngine.registerFunctions(BombFunctions.class);
        templateEngine.registerFunctions(CharacterFunctions.class);
        templateEngine.registerFunctions(CombatFunctions.class);
        templateEngine.registerFunctions(CombatXpFunctions.class);
        templateEngine.registerFunctions(EnvironmentFunctions.class);
        templateEngine.registerFunctions(GuildFunctions.class);
        templateEngine.registerFunctions(HadesPartyFunctions.class);
        templateEngine.registerFunctions(InventoryFunctions.class);
        templateEngine.registerFunctions(LootrunFunctions.class);
        templateEngine.registerFunctions(MinecraftFunctions.class);
        templateEngine.registerFunctions(MountFunctions.class);
        templateEngine.registerFunctions(ProfessionFunctions.class);

        templateEngine.registerFunctions(SocialFunctions.class);

    }
}
