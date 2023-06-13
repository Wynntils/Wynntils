/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions;

import com.wynntils.core.components.Models;
import com.wynntils.core.functions.Function;
import com.wynntils.core.functions.arguments.FunctionArguments;
import com.wynntils.models.profession.type.ProfessionType;
import com.wynntils.utils.StringUtils;
import java.util.List;

public class ProfessionFunctions {
    public static class ProfessionLevelFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            ProfessionType professionType = ProfessionType.fromString(
                    arguments.getArgument("profession").getStringValue());
            if (professionType == null) return -1;

            return Models.Profession.getLevel(professionType);
        }

        @Override
        public FunctionArguments.Builder getArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(
                    List.of(new FunctionArguments.Argument<>("profession", String.class, null)));
        }

        @Override
        public List<String> getAliases() {
            return List.of("prof_lvl");
        }
    }

    public static class ProfessionPercentageFunction extends Function<Double> {
        @Override
        public Double getValue(FunctionArguments arguments) {
            ProfessionType professionType = ProfessionType.fromString(
                    arguments.getArgument("profession").getStringValue());
            if (professionType == null) return -1.0;

            return Models.Profession.getProgress(professionType);
        }

        @Override
        public FunctionArguments.Builder getArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(
                    List.of(new FunctionArguments.Argument<>("profession", String.class, null)));
        }

        @Override
        public List<String> getAliases() {
            return List.of("prof_pct");
        }
    }

    public static class ProfessionXpPerMinuteRawFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            ProfessionType professionType = ProfessionType.fromString(
                    arguments.getArgument("profession").getStringValue());
            if (professionType == null) return -1;

            return (int) Models.Profession.getRawXpGainInLastMinute().get(professionType).stream()
                    .mapToDouble(Float::doubleValue)
                    .sum();
        }

        @Override
        public FunctionArguments.Builder getArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(
                    List.of(new FunctionArguments.Argument<>("profession", String.class, null)));
        }

        @Override
        public List<String> getAliases() {
            return List.of("prof_xpm_raw");
        }
    }

    public static class ProfessionXpPerMinuteFunction extends Function<String> {
        @Override
        public String getValue(FunctionArguments arguments) {
            ProfessionType professionType = ProfessionType.fromString(
                    arguments.getArgument("profession").getStringValue());
            if (professionType == null) return "Invalid profession";

            return StringUtils.integerToShortString(
                    (int) Models.Profession.getRawXpGainInLastMinute().get(professionType).stream()
                            .mapToDouble(Float::doubleValue)
                            .sum());
        }

        @Override
        public FunctionArguments.Builder getArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(
                    List.of(new FunctionArguments.Argument<>("profession", String.class, null)));
        }

        @Override
        public List<String> getAliases() {
            return List.of("prof_xpm");
        }
    }
}
