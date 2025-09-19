/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions;

import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.functions.Function;
import com.wynntils.core.consumers.functions.arguments.Argument;
import com.wynntils.core.consumers.functions.arguments.FunctionArguments;
import com.wynntils.models.war.type.WarBattleInfo;
import com.wynntils.utils.type.RangedValue;
import com.wynntils.utils.type.Time;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

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

    public static class InitialTowerHealthFunction extends Function<Long> {
        @Override
        public Long getValue(FunctionArguments arguments) {
            Optional<WarBattleInfo> warBattleInfoOpt = Models.GuildWarTower.getWarBattleInfo();

            if (warBattleInfoOpt.isEmpty()) return -1L;

            return warBattleInfoOpt.get().getInitialState().health();
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

    public static class InitialTowerDamageFunction extends Function<RangedValue> {
        @Override
        public RangedValue getValue(FunctionArguments arguments) {
            Optional<WarBattleInfo> warBattleInfoOpt = Models.GuildWarTower.getWarBattleInfo();

            if (warBattleInfoOpt.isEmpty()) return RangedValue.NONE;

            return warBattleInfoOpt.get().getInitialState().damage();
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

    public static class CurrentTowerHealthFunction extends Function<Long> {
        @Override
        public Long getValue(FunctionArguments arguments) {
            Optional<WarBattleInfo> warBattleInfoOpt = Models.GuildWarTower.getWarBattleInfo();

            if (warBattleInfoOpt.isEmpty()) return -1L;

            return warBattleInfoOpt.get().getCurrentState().health();
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

    public static class CurrentTowerDamageFunction extends Function<RangedValue> {
        @Override
        public RangedValue getValue(FunctionArguments arguments) {
            Optional<WarBattleInfo> warBattleInfoOpt = Models.GuildWarTower.getWarBattleInfo();

            if (warBattleInfoOpt.isEmpty()) return RangedValue.NONE;

            return warBattleInfoOpt.get().getCurrentState().damage();
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

    public static class WarStartFunction extends Function<Time> {
        @Override
        public Time getValue(FunctionArguments arguments) {
            Optional<WarBattleInfo> warBattleInfoOpt = Models.GuildWarTower.getWarBattleInfo();
            if (warBattleInfoOpt.isEmpty()) return Time.NONE;

            return Time.of(warBattleInfoOpt.get().getInitialState().timestamp());
        }
    }

    public static class TimeInWarFunction extends Function<Long> {
        @Override
        public Long getValue(FunctionArguments arguments) {
            Optional<WarBattleInfo> warBattleInfoOpt = Models.GuildWarTower.getWarBattleInfo();

            if (warBattleInfoOpt.isEmpty()) return -1L;

            return warBattleInfoOpt.get().getTotalLengthSeconds();
        }
    }

    public static class TowerEffectiveHpFunction extends Function<Long> {
        @Override
        public Long getValue(FunctionArguments arguments) {
            Optional<WarBattleInfo> warBattleInfoOpt = Models.GuildWarTower.getWarBattleInfo();

            if (warBattleInfoOpt.isEmpty()) return -1L;

            return warBattleInfoOpt.get().getTowerEffectiveHp();
        }
    }

    public static class TowerDpsFunction extends Function<RangedValue> {
        @Override
        public RangedValue getValue(FunctionArguments arguments) {
            Optional<WarBattleInfo> warBattleInfoOpt = Models.GuildWarTower.getWarBattleInfo();

            if (warBattleInfoOpt.isEmpty()) return RangedValue.NONE;

            return warBattleInfoOpt.get().getTowerDps();
        }
    }

    public static class TeamDpsFunction extends Function<Long> {
        @Override
        public Long getValue(FunctionArguments arguments) {
            Optional<WarBattleInfo> warBattleInfoOpt = Models.GuildWarTower.getWarBattleInfo();

            if (warBattleInfoOpt.isEmpty()) return -1L;

            return warBattleInfoOpt
                    .get()
                    .getDps(arguments.getArgument("seconds").getLongValue());
        }

        @Override
        public FunctionArguments.Builder getArgumentsBuilder() {
            return new FunctionArguments.OptionalArgumentBuilder(
                    List.of(new Argument<>("seconds", Long.class, Long.MAX_VALUE)));
        }
    }

    public static class EstimatedWarEndFunction extends Function<Time> {
        @Override
        public Time getValue(FunctionArguments arguments) {
            Optional<WarBattleInfo> warBattleInfoOpt = Models.GuildWarTower.getWarBattleInfo();
            if (warBattleInfoOpt.isEmpty()) return Time.NONE;

            int timeRemaining = (int) warBattleInfoOpt.get().getEstimatedTimeRemaining();
            return Time.now().offset(timeRemaining);
        }
    }

    public static class EstimatedTimeToFinishWarFunction extends Function<Long> {
        @Override
        public Long getValue(FunctionArguments arguments) {
            Optional<WarBattleInfo> warBattleInfoOpt = Models.GuildWarTower.getWarBattleInfo();

            if (warBattleInfoOpt.isEmpty()) return -1L;

            return warBattleInfoOpt.get().getEstimatedTimeRemaining();
        }
    }

    public static class IsTerritoryQueuedFunction extends Function<Boolean> {
        @Override
        public Boolean getValue(FunctionArguments arguments) {
            String territoryName = arguments.getArgument("territoryName").getStringValue();

            return Models.GuildAttackTimer.getAttackTimerForTerritory(territoryName)
                    .isPresent();
        }

        @Override
        protected List<String> getAliases() {
            return List.of("is_queued");
        }

        @Override
        public FunctionArguments.Builder getArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(
                    List.of(new Argument<>("territoryName", String.class, null)));
        }
    }

    public static class WarsSinceFunction extends Function<Long> {
        @Override
        public Long getValue(FunctionArguments arguments) {
            int sinceDays = arguments.getArgument("sinceDays").getIntegerValue();

            return Models.War.historicWars.get().stream()
                    .filter(historicWarInfo -> historicWarInfo.endedTimestamp()
                            >= System.currentTimeMillis() - TimeUnit.DAYS.toMillis(sinceDays))
                    .count();
        }

        @Override
        public FunctionArguments.Builder getArgumentsBuilder() {
            return new FunctionArguments.OptionalArgumentBuilder(
                    List.of(new Argument<>("sinceDays", Integer.class, 7)));
        }
    }
}
