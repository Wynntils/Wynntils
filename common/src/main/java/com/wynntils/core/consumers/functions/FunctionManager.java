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
import com.wynntils.templates.backends.compiler.CompilerBackend;
import com.wynntils.templates.functions.FunctionDefinition;
import com.wynntils.templates.language.Error;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

public final class FunctionManager extends Manager {
    private final TemplateEngine templateEngine;
    private final HashMap<String, String> crashedTemplates = new HashMap<>();

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
        if (crashedTemplates.containsKey(template)) {
            return crashedTemplates.get(template);
        }

        String ret = templateEngine.evaluate(template);

        if (templateEngine.hasError()) {
            crashedTemplates.put(template, formatError(templateEngine.getError().get()));
            WynntilsMod.error("\n" + templateEngine.getError());
        }

        return ret;
    }

    private String formatError(Error error) {
        if (error.row() > 0 && error.column() > 0) {
            return ("§c[Template Error at " + (error.row() + 1) + ":" + (error.column() + 1) + "]§r " + "§7" + error.message() + "§r");
        } else {
            return "§c[Template Error]§r " + "§7" + error.message() + "§r";
        }
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

    private String parseColorCodes(String input) {
        StringBuilder out = new StringBuilder(input.length());

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);

            // escape: \&
            if (c == '\\' && i + 1 < input.length()) {
                char next = input.charAt(i + 1);
                if (next == '&') {
                    out.append('\\').append('&');
                    i++;
                    continue;
                }
            }

            // hex color: &#AARRGGBB
            if (c == '&' && i + 9 < input.length() && input.charAt(i + 1) == '#') {
                String hex = input.substring(i + 2, i + 10);
                if (isHex(hex)) {
                    out.append('§').append('#').append(hex);
                    i += 9;
                    continue;
                }
            }

            // normal color code: &a
            if (c == '&' && i + 1 < input.length()) {
                char code = input.charAt(i + 1);
                if (isColorCode(code)) {
                    out.append('§').append(code);
                    i++;
                    continue;
                }
            }

            out.append(c);
        }

        return out.toString();
    }

    private boolean isColorCode(char c) {
        return (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F') || c == 'r' || c == 'k' || c == 'l' || c == 'm' || c == 'n' || c == 'o';
    }

    private boolean isHex(String s) {
        if (s.length() != 8) return false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            boolean hex = (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
            if (!hex) return false;
        }
        return true;
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