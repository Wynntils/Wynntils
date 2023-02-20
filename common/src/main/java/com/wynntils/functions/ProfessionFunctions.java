/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions;

import com.wynntils.core.components.Models;
import com.wynntils.core.functions.Function;
import com.wynntils.core.functions.arguments.FunctionArguments;
import com.wynntils.models.profession.type.ProfessionType;
import java.util.List;

public class ProfessionFunctions {
    public static class WoodcuttingLevelFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            return Models.Profession.getLevel(ProfessionType.WOODCUTTING);
        }

        @Override
        public List<String> getAliases() {
            return List.of("woodcutting");
        }
    }

    public static class MiningLevelFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            return Models.Profession.getLevel(ProfessionType.MINING);
        }

        @Override
        public List<String> getAliases() {
            return List.of("mining");
        }
    }

    public static class FishingLevelFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            return Models.Profession.getLevel(ProfessionType.FISHING);
        }

        @Override
        public List<String> getAliases() {
            return List.of("fishing");
        }
    }

    public static class FarmingLevelFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            return Models.Profession.getLevel(ProfessionType.FARMING);
        }

        @Override
        public List<String> getAliases() {
            return List.of("farming");
        }
    }

    public static class AlchemismLevelFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            return Models.Profession.getLevel(ProfessionType.ALCHEMISM);
        }

        @Override
        public List<String> getAliases() {
            return List.of("alchemism");
        }
    }

    public static class ArmouringLevelFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            return Models.Profession.getLevel(ProfessionType.ARMOURING);
        }

        @Override
        public List<String> getAliases() {
            return List.of("armouring");
        }
    }

    public static class CookingLevelFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            return Models.Profession.getLevel(ProfessionType.COOKING);
        }

        @Override
        public List<String> getAliases() {
            return List.of("cooking");
        }
    }

    public static class JewelingLevelFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            return Models.Profession.getLevel(ProfessionType.JEWELING);
        }

        @Override
        public List<String> getAliases() {
            return List.of("jeweling");
        }
    }

    public static class ScribingLevelFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            return Models.Profession.getLevel(ProfessionType.SCRIBING);
        }

        @Override
        public List<String> getAliases() {
            return List.of("scribing");
        }
    }

    public static class TailoringLevelFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            return Models.Profession.getLevel(ProfessionType.TAILORING);
        }

        @Override
        public List<String> getAliases() {
            return List.of("tailoring");
        }
    }

    public static class WeaponsmithingLevelFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            return Models.Profession.getLevel(ProfessionType.WEAPONSMITHING);
        }

        @Override
        public List<String> getAliases() {
            return List.of("weaponsmithing");
        }
    }

    public static class WoodworkingLevelFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            return Models.Profession.getLevel(ProfessionType.WOODWORKING);
        }

        @Override
        public List<String> getAliases() {
            return List.of("woodworking");
        }
    }

    public static class WoodcuttingPercentageFunction extends Function<Float> {
        @Override
        public Float getValue(FunctionArguments arguments) {
            return Models.Profession.getProgress(ProfessionType.WOODCUTTING);
        }

        @Override
        public List<String> getAliases() {
            return List.of("woodcutting_pct");
        }
    }

    public static class MiningPercentageFunction extends Function<Float> {
        @Override
        public Float getValue(FunctionArguments arguments) {
            return Models.Profession.getProgress(ProfessionType.MINING);
        }

        @Override
        public List<String> getAliases() {
            return List.of("mining_pct");
        }
    }

    public static class FishingPercentageFunction extends Function<Float> {
        @Override
        public Float getValue(FunctionArguments arguments) {
            return Models.Profession.getProgress(ProfessionType.FISHING);
        }

        @Override
        public List<String> getAliases() {
            return List.of("fishing_pct");
        }
    }

    public static class FarmingPercentageFunction extends Function<Float> {
        @Override
        public Float getValue(FunctionArguments arguments) {
            return Models.Profession.getProgress(ProfessionType.FARMING);
        }

        @Override
        public List<String> getAliases() {
            return List.of("farming_pct");
        }
    }

    public static class AlchemismPercentageFunction extends Function<Float> {
        @Override
        public Float getValue(FunctionArguments arguments) {
            return Models.Profession.getProgress(ProfessionType.ALCHEMISM);
        }

        @Override
        public List<String> getAliases() {
            return List.of("alchemism_pct");
        }
    }

    public static class ArmouringPercentageFunction extends Function<Float> {
        @Override
        public Float getValue(FunctionArguments arguments) {
            return Models.Profession.getProgress(ProfessionType.ARMOURING);
        }

        @Override
        public List<String> getAliases() {
            return List.of("armouring_pct");
        }
    }

    public static class CookingPercentageFunction extends Function<Float> {
        @Override
        public Float getValue(FunctionArguments arguments) {
            return Models.Profession.getProgress(ProfessionType.COOKING);
        }

        @Override
        public List<String> getAliases() {
            return List.of("cooking_pct");
        }
    }

    public static class JewelingPercentageFunction extends Function<Float> {
        @Override
        public Float getValue(FunctionArguments arguments) {
            return Models.Profession.getProgress(ProfessionType.JEWELING);
        }

        @Override
        public List<String> getAliases() {
            return List.of("jeweling_pct");
        }
    }

    public static class ScribingPercentageFunction extends Function<Float> {
        @Override
        public Float getValue(FunctionArguments arguments) {
            return Models.Profession.getProgress(ProfessionType.SCRIBING);
        }

        @Override
        public List<String> getAliases() {
            return List.of("scribing_pct");
        }
    }

    public static class TailoringPercentageFunction extends Function<Float> {
        @Override
        public Float getValue(FunctionArguments arguments) {
            return Models.Profession.getProgress(ProfessionType.TAILORING);
        }

        @Override
        public List<String> getAliases() {
            return List.of("tailoring_pct");
        }
    }

    public static class WeaponsmithingPercentageFunction extends Function<Float> {
        @Override
        public Float getValue(FunctionArguments arguments) {
            return Models.Profession.getProgress(ProfessionType.WEAPONSMITHING);
        }

        @Override
        public List<String> getAliases() {
            return List.of("weaponsmithing_pct");
        }
    }

    public static class WoodworkingPercentageFunction extends Function<Float> {
        @Override
        public Float getValue(FunctionArguments arguments) {
            return Models.Profession.getProgress(ProfessionType.WOODWORKING);
        }

        @Override
        public List<String> getAliases() {
            return List.of("woodworking_pct");
        }
    }
}
