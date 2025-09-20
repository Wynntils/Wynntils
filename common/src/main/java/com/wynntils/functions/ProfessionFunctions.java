/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions;

import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.functions.Function;
import com.wynntils.core.consumers.functions.arguments.Argument;
import com.wynntils.core.consumers.functions.arguments.FunctionArguments;
import com.wynntils.models.profession.type.HarvestInfo;
import com.wynntils.models.profession.type.ProfessionType;
import com.wynntils.utils.StringUtils;
import com.wynntils.utils.type.CappedValue;
import java.util.List;
import java.util.Optional;

public class ProfessionFunctions {
    public static class ProfessionXpFunction extends Function<CappedValue> {
        @Override
        public CappedValue getValue(FunctionArguments arguments) {
            ProfessionType professionType = ProfessionType.fromString(
                    arguments.getArgument("profession").getStringValue());
            if (professionType == null) return CappedValue.EMPTY;
            return Models.Profession.getXP(professionType);
        }

        @Override
        public FunctionArguments.Builder getArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(
                    List.of(new Argument<>("profession", String.class, null)));
        }

        @Override
        protected List<String> getAliases() {
            return List.of("prof_xp");
        }
    }

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
                    List.of(new Argument<>("profession", String.class, null)));
        }

        @Override
        protected List<String> getAliases() {
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
                    List.of(new Argument<>("profession", String.class, null)));
        }

        @Override
        protected List<String> getAliases() {
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
                    List.of(new Argument<>("profession", String.class, null)));
        }

        @Override
        protected List<String> getAliases() {
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
                    List.of(new Argument<>("profession", String.class, null)));
        }

        @Override
        protected List<String> getAliases() {
            return List.of("prof_xpm");
        }
    }

    public static class LastHarvestResourceTypeFunction extends Function<String> {
        @Override
        public String getValue(FunctionArguments arguments) {
            Optional<HarvestInfo> lastHarvest = Models.Profession.getLastHarvest();

            if (lastHarvest.isEmpty()) return "";

            return lastHarvest.get().materialProfile().getResourceType().name();
        }
    }

    public static class LastHarvestMaterialTypeFunction extends Function<String> {
        @Override
        public String getValue(FunctionArguments arguments) {
            Optional<HarvestInfo> lastHarvest = Models.Profession.getLastHarvest();

            if (lastHarvest.isEmpty()) return "";

            return lastHarvest
                    .get()
                    .materialProfile()
                    .getResourceType()
                    .getMaterialType()
                    .name();
        }
    }

    public static class LastHarvestMaterialNameFunction extends Function<String> {
        @Override
        public String getValue(FunctionArguments arguments) {
            Optional<HarvestInfo> lastHarvest = Models.Profession.getLastHarvest();

            if (lastHarvest.isEmpty()) return "";

            return lastHarvest.get().materialProfile().getSourceMaterial().name();
        }
    }

    public static class LastHarvestMaterialLevelFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            Optional<HarvestInfo> lastHarvest = Models.Profession.getLastHarvest();

            if (lastHarvest.isEmpty()) return -1;

            return lastHarvest.get().materialProfile().getSourceMaterial().level();
        }
    }

    public static class LastHarvestMaterialTierFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            Optional<HarvestInfo> lastHarvest = Models.Profession.getLastHarvest();

            if (lastHarvest.isEmpty()) return -1;

            return lastHarvest.get().materialProfile().getTier();
        }
    }

    public static class LastHarvestXpGainFunction extends Function<Float> {
        @Override
        public Float getValue(FunctionArguments arguments) {
            Optional<HarvestInfo> lastHarvest = Models.Profession.getLastHarvest();

            if (lastHarvest.isEmpty()) return -1f;

            return lastHarvest.get().xpGain();
        }
    }

    public static class MaterialDryStreak extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            return Models.Profession.getProfessionDryStreak();
        }

        @Override
        protected List<String> getAliases() {
            return List.of("mat_dry");
        }
    }

    public static class LastProfessionXpGainFunction extends Function<String> {
        @Override
        public String getValue(FunctionArguments arguments) {
            Optional<ProfessionType> profession = Models.Profession.getLastProfessionXpGain();
            if (profession.isEmpty()) return "";
            return profession.get().getDisplayName();
        }
    }
}
