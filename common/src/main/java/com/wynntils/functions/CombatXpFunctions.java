/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions;

import com.wynntils.core.components.Models;
import com.wynntils.core.functions.Function;
import com.wynntils.utils.StringUtils;
import java.util.List;

public class CombatXpFunctions {
    public static class XpPerMinuteRawFunction extends Function<Integer> {
        @Override
        public Integer getValue(String argument) {
            return (int) (Models.CombatXp.getRawXpGainInLastMinute().stream()
                    .mapToDouble(Float::doubleValue)
                    .sum());
        }

        @Override
        public List<String> getAliases() {
            return List.of("xpm_raw");
        }
    }

    public static class XpPerMinuteFunction extends Function<String> {
        @Override
        public String getValue(String argument) {
            return StringUtils.integerToShortString((int) (Models.CombatXp.getRawXpGainInLastMinute().stream()
                    .mapToDouble(Float::doubleValue)
                    .sum()));
        }

        @Override
        public List<String> getAliases() {
            return List.of("xpm");
        }
    }

    public static class XpPercentagePerMinuteFunction extends Function<Double> {
        @Override
        public Double getValue(String argument) {
            // Round to 2 decimal places
            return Math.round(Models.CombatXp.getPercentageXpGainInLastMinute().stream()
                                    .mapToDouble(Float::doubleValue)
                                    .sum()
                            * 100.0)
                    / 100.0;
        }

        @Override
        public List<String> getAliases() {
            return List.of("xppm");
        }
    }

    public static class LevelFunction extends Function<Integer> {
        @Override
        public Integer getValue(String argument) {
            return Models.CombatXp.getXpLevel();
        }

        @Override
        public List<String> getAliases() {
            return List.of("lvl");
        }
    }

    public static class XpFunction extends Function<String> {
        @Override
        public String getValue(String argument) {
            return StringUtils.integerToShortString((int) Models.CombatXp.getCurrentXp());
        }
    }

    public static class XpRawFunction extends Function<Float> {
        @Override
        public Float getValue(String argument) {
            return Models.CombatXp.getCurrentXp();
        }
    }

    public static class XpReqFunction extends Function<String> {
        @Override
        public String getValue(String argument) {
            return StringUtils.integerToShortString(Models.CombatXp.getXpPointsNeededToLevelUp());
        }
    }

    public static class XpReqRawFunction extends Function<Integer> {
        @Override
        public Integer getValue(String argument) {
            return Models.CombatXp.getXpPointsNeededToLevelUp();
        }
    }

    public static class XpPctFunction extends Function<Float> {
        @Override
        public Float getValue(String argument) {
            return Models.CombatXp.getXpProgress() * 100.0f;
        }
    }
}
