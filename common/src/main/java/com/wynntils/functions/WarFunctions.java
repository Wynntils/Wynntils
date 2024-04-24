/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions;

import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.functions.Function;
import com.wynntils.core.consumers.functions.arguments.FunctionArguments;
import com.wynntils.models.war.type.WarBattleInfo;
import com.wynntils.utils.type.CappedValue;
import com.wynntils.utils.type.RangedValue;
import java.util.Optional;

public class WarFunctions {
    public static class AuraTimerFunction extends Function<Double> {
        @Override
        public Double getValue(FunctionArguments arguments) {
            return Models.GuildWarTower.getRemainingTimeUntilAura() / 1000d;
        }
    }

    public static class VolleyTimerFunction extends Function<Double> {
        @Override
        public Double getValue(FunctionArguments arguments) {
            return Models.GuildWarTower.getRemainingTimeUntilVolley() / 1000d;
        }
    }

    // These are static, and only change per war

    public static class TowerOwnerFunction extends Function<String> {
        @Override
        public String getValue(FunctionArguments arguments) {
            Optional<WarBattleInfo> warBattleInfoOpt = Models.GuildWarTower.getWarBattleInfo();

            if (warBattleInfoOpt.isEmpty()) return "-";

            return warBattleInfoOpt.get().getOwnerGuild();
        }
    }

    public static class TowerTerritoryFunction extends Function<String> {
        @Override
        public String getValue(FunctionArguments arguments) {
            Optional<WarBattleInfo> warBattleInfoOpt = Models.GuildWarTower.getWarBattleInfo();

            if (warBattleInfoOpt.isEmpty()) return "-";

            return warBattleInfoOpt.get().getTerritory();
        }
    }

    // Initial tower state functions

    public static class InitialTowerHealthFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            // Technically this should be a long, but functions don't support longs
            Optional<WarBattleInfo> warBattleInfoOpt = Models.GuildWarTower.getWarBattleInfo();

            if (warBattleInfoOpt.isEmpty()) return -1;

            return (int) warBattleInfoOpt.get().getInitialState().health();
        }
    }

    public static class InitialTowerDefenseFunction extends Function<Double> {
        @Override
        public Double getValue(FunctionArguments arguments) {
            Optional<WarBattleInfo> warBattleInfoOpt = Models.GuildWarTower.getWarBattleInfo();

            if (warBattleInfoOpt.isEmpty()) return -1d;

            return warBattleInfoOpt.get().getInitialState().defense();
        }
    }

    public static class InitialTowerDamageFunction extends Function<CappedValue> {
        @Override
        public CappedValue getValue(FunctionArguments arguments) {
            // Technically this is a RangedValue, but functions don't support RangedValue
            Optional<WarBattleInfo> warBattleInfoOpt = Models.GuildWarTower.getWarBattleInfo();

            if (warBattleInfoOpt.isEmpty()) return CappedValue.EMPTY;

            RangedValue damage = warBattleInfoOpt.get().getInitialState().damage();
            return new CappedValue(damage.low(), damage.high());
        }
    }

    public static class InitialTowerAttackSpeedFunction extends Function<Double> {
        @Override
        public Double getValue(FunctionArguments arguments) {
            Optional<WarBattleInfo> warBattleInfoOpt = Models.GuildWarTower.getWarBattleInfo();

            if (warBattleInfoOpt.isEmpty()) return -1d;

            return warBattleInfoOpt.get().getInitialState().attackSpeed();
        }
    }

    // Current tower state functions

    public static class CurrentTowerHealthFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            // Technically this should be a long, but functions don't support longs
            Optional<WarBattleInfo> warBattleInfoOpt = Models.GuildWarTower.getWarBattleInfo();

            if (warBattleInfoOpt.isEmpty()) return -1;

            return (int) warBattleInfoOpt.get().getCurrentState().health();
        }
    }

    public static class CurrentTowerDefenseFunction extends Function<Double> {
        @Override
        public Double getValue(FunctionArguments arguments) {
            Optional<WarBattleInfo> warBattleInfoOpt = Models.GuildWarTower.getWarBattleInfo();

            if (warBattleInfoOpt.isEmpty()) return -1d;

            return warBattleInfoOpt.get().getCurrentState().defense();
        }
    }

    public static class CurrentTowerDamageFunction extends Function<CappedValue> {
        @Override
        public CappedValue getValue(FunctionArguments arguments) {
            // Technically this is a RangedValue, but functions don't support RangedValue
            Optional<WarBattleInfo> warBattleInfoOpt = Models.GuildWarTower.getWarBattleInfo();

            if (warBattleInfoOpt.isEmpty()) return CappedValue.EMPTY;

            RangedValue damage = warBattleInfoOpt.get().getCurrentState().damage();
            return new CappedValue(damage.low(), damage.high());
        }
    }

    public static class CurrentTowerAttackSpeedFunction extends Function<Double> {
        @Override
        public Double getValue(FunctionArguments arguments) {
            Optional<WarBattleInfo> warBattleInfoOpt = Models.GuildWarTower.getWarBattleInfo();

            if (warBattleInfoOpt.isEmpty()) return -1d;

            return warBattleInfoOpt.get().getCurrentState().attackSpeed();
        }
    }
}
