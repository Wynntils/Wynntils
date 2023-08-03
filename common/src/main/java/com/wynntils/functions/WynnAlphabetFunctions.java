/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions;

import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.functions.Function;
import com.wynntils.core.consumers.functions.arguments.FunctionArguments;
import com.wynntils.models.wynnalphabet.WynnAlphabet;
import java.util.List;
import java.util.Locale;

public class WynnAlphabetFunctions {
    public static class TranscribeGavellianFunction extends Function<String> {
        @Override
        public String getValue(FunctionArguments arguments) {
            String input = arguments.getArgument("gavellian").getStringValue().toLowerCase(Locale.ROOT);

            if (input == null) return "";

            return Models.WynnAlphabet.transcribeMessageToWynnAlphabet(input, WynnAlphabet.GAVELLIAN);
        }

        @Override
        public FunctionArguments.Builder getArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(
                    List.of(new FunctionArguments.Argument<>("gavellian", String.class, null)));
        }

        @Override
        public List<String> getAliases() {
            return List.of("gavellian");
        }
    }

    public static class TranscribeWynnicFunction extends Function<String> {
        @Override
        public String getValue(FunctionArguments arguments) {
            String input = arguments.getArgument("wynnic").getStringValue().toLowerCase(Locale.ROOT);

            if (input == null) return "";

            return Models.WynnAlphabet.transcribeMessageToWynnAlphabet(input, WynnAlphabet.WYNNIC);
        }

        @Override
        public FunctionArguments.Builder getArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(
                    List.of(new FunctionArguments.Argument<>("wynnic", String.class, null)));
        }

        @Override
        public List<String> getAliases() {
            return List.of("wynnic");
        }
    }
}
