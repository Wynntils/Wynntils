/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions;

import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.functions.Function;
import com.wynntils.core.consumers.functions.arguments.Argument;
import com.wynntils.core.consumers.functions.arguments.FunctionArguments;
import com.wynntils.models.combat.label.DebuffType;
import com.wynntils.utils.type.CappedValue;
import com.wynntils.utils.type.Time;
import java.util.List;

public class CombatFunctions {
    public static class AreaDamagePerSecondFunction extends Function<Long> {
        @Override
        public Long getValue(FunctionArguments arguments) {
            return Models.Combat.getAreaDamagePerSecond();
        }

        @Override
        protected List<String> getAliases() {
            return List.of("adps");
        }
    }

    public static class AreaDamageAverageFunction extends Function<Double> {
        @Override
        public Double getValue(FunctionArguments arguments) {
            return Models.Combat.getAverageAreaDamagePerSecond(
                    arguments.getArgument("seconds").getIntegerValue());
        }

        @Override
        public FunctionArguments.Builder getArgumentsBuilder() {
            return new FunctionArguments.OptionalArgumentBuilder(List.of(new Argument<>("seconds", Integer.class, 10)));
        }

        @Override
        protected List<String> getAliases() {
            return List.of("adavg");
        }
    }

    public static class TotalAreaDamageFunction extends Function<Double> {
        @Override
        public Double getValue(FunctionArguments arguments) {
            return Models.Combat.getTotalAreaDamageOverSeconds(
                    arguments.getArgument("seconds").getIntegerValue());
        }

        @Override
        public FunctionArguments.Builder getArgumentsBuilder() {
            return new FunctionArguments.OptionalArgumentBuilder(List.of(new Argument<>("seconds", Integer.class, 10)));
        }

        @Override
        protected List<String> getAliases() {
            return List.of("total_dmg", "tdmg");
        }
    }

    public static class BlocksAboveGroundFunction extends Function<Double> {
        @Override
        public Double getValue(FunctionArguments arguments) {
            return Models.CharacterStats.getBlocksAboveGround();
        }

        @Override
        protected List<String> getAliases() {
            return List.of("agl", "above_ground_level");
        }
    }

    public static class KillsPerMinuteFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            return Models.Combat.getKillsPerMinute(
                    arguments.getArgument("includeShared").getBooleanValue());
        }

        @Override
        public FunctionArguments.Builder getArgumentsBuilder() {
            return new FunctionArguments.OptionalArgumentBuilder(
                    List.of(new Argument<>("includeShared", Boolean.class, true)));
        }

        @Override
        protected List<String> getAliases() {
            return List.of("kpm");
        }
    }

    public static class LastSpellNameFunction extends Function<String> {
        @Override
        public String getValue(FunctionArguments arguments) {
            return arguments.getArgument("burst").getBooleanValue()
                    ? Models.Spell.getLastBurstSpellName()
                    : Models.Spell.getLastSpellName();
        }

        @Override
        public FunctionArguments.Builder getArgumentsBuilder() {
            return new FunctionArguments.OptionalArgumentBuilder(
                    List.of(new Argument<>("burst", Boolean.class, false)));
        }

        @Override
        protected List<String> getAliases() {
            return List.of("recast_name");
        }
    }

    public static class LastSpellRepeatCountFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            return arguments.getArgument("burst").getBooleanValue()
                    ? Models.Spell.getRepeatedBurstSpellCount()
                    : Models.Spell.getRepeatedSpellCount();
        }

        @Override
        public FunctionArguments.Builder getArgumentsBuilder() {
            return new FunctionArguments.OptionalArgumentBuilder(
                    List.of(new Argument<>("burst", Boolean.class, false)));
        }

        @Override
        protected List<String> getAliases() {
            return List.of("recast_count");
        }
    }

    public static class TicksSinceLastSpellFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            return arguments.getArgument("burst").getBooleanValue()
                    ? Models.Spell.getTicksSinceCastBurst()
                    : Models.Spell.getTicksSinceCast();
        }

        @Override
        public FunctionArguments.Builder getArgumentsBuilder() {
            return new FunctionArguments.OptionalArgumentBuilder(
                    List.of(new Argument<>("burst", Boolean.class, false)));
        }

        @Override
        protected List<String> getAliases() {
            return List.of("recast_ticks");
        }
    }

    public static class FocusedMobNameFunction extends Function<String> {
        @Override
        public String getValue(final FunctionArguments arguments) {
            return Models.Combat.getFocusedMobName();
        }

        @Override
        protected List<String> getAliases() {
            return List.of("foc_mob_name");
        }
    }

    public static class FocusedMobHealthFunction extends Function<Long> {
        @Override
        public Long getValue(final FunctionArguments arguments) {
            return Models.Combat.getFocusedMobHealth();
        }

        @Override
        protected List<String> getAliases() {
            return List.of("foc_mob_hp");
        }
    }

    public static class FocusedMobHealthPercentFunction extends Function<CappedValue> {
        @Override
        public CappedValue getValue(FunctionArguments arguments) {
            return Models.Combat.getFocusedMobHealthPercent();
        }

        @Override
        protected List<String> getAliases() {
            return List.of("foc_mob_hp_pct");
        }
    }

    public static class LastDamageDealtFunction extends Function<Time> {
        @Override
        public Time getValue(FunctionArguments arguments) {
            return Time.of(Models.Combat.getLastDamageDealtTimestamp());
        }

        @Override
        protected List<String> getAliases() {
            return List.of("last_dam");
        }
    }

    public static class TimeSinceLastDamageDealtFunction extends Function<Long> {
        @Override
        public Long getValue(FunctionArguments arguments) {
            return System.currentTimeMillis() - Models.Combat.getLastDamageDealtTimestamp();
        }

        @Override
        protected List<String> getAliases() {
            return List.of("last_dam_ms");
        }
    }

    public static class LastKillFunction extends Function<Time> {
        @Override
        public Time getValue(FunctionArguments arguments) {
            return Time.of(Models.Combat.getLastKillTimestamp(
                    arguments.getArgument("includeShared").getBooleanValue()));
        }

        @Override
        public FunctionArguments.Builder getArgumentsBuilder() {
            return new FunctionArguments.OptionalArgumentBuilder(
                    List.of(new Argument<>("includeShared", Boolean.class, false)));
        }
    }

    public static class TimeSinceLastKillFunction extends Function<Long> {
        @Override
        public Long getValue(FunctionArguments arguments) {
            return System.currentTimeMillis()
                    - Models.Combat.getLastKillTimestamp(
                            arguments.getArgument("includeShared").getBooleanValue());
        }

        @Override
        public FunctionArguments.Builder getArgumentsBuilder() {
            return new FunctionArguments.OptionalArgumentBuilder(
                    List.of(new Argument<>("includeShared", Boolean.class, false)));
        }

        @Override
        protected List<String> getAliases() {
            return List.of("last_kill_ms");
        }
    }

    public static class TargetedMobDebuffValueFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            DebuffType debuffType =
                    DebuffType.fromName(arguments.getArgument("debuffName").getStringValue());
            if (debuffType == null) return 0;
            double range = arguments.getArgument("range").getDoubleValue();
            double horizontalFovDegrees =
                    arguments.getArgument("horizontalDegrees").getDoubleValue();
            double verticalFovDegrees = arguments.getArgument("verticalDegrees").getDoubleValue();

            return Models.Combat.getTargetedDebuffCount(range, horizontalFovDegrees, verticalFovDegrees, debuffType);
        }

        @Override
        public FunctionArguments.Builder getArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(
                    new Argument<>("range", Double.class, null),
                    new Argument<>("horizontalDegrees", Double.class, null),
                    new Argument<>("verticalDegrees", Double.class, null),
                    new Argument<>("debuffName", String.class, null)));
        }
    }

    public static class DebuffsInRadiusValueFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            DebuffType debuffType =
                    DebuffType.fromName(arguments.getArgument("debuffName").getStringValue());
            if (debuffType == null) return 0;
            double radius = arguments.getArgument("radius").getDoubleValue();

            return Models.Combat.getDebuffCountInRadius(radius, debuffType);
        }

        @Override
        public FunctionArguments.Builder getArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(
                    new Argument<>("radius", Double.class, null), new Argument<>("debuffName", String.class, null)));
        }
    }
}
