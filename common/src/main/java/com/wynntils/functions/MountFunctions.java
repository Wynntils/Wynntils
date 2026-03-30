/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions;

import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.functions.Function;
import com.wynntils.core.consumers.functions.arguments.Argument;
import com.wynntils.core.consumers.functions.arguments.FunctionArguments;
import com.wynntils.models.items.items.game.MountItem;
import com.wynntils.models.mount.type.MountStat;
import com.wynntils.utils.type.CappedValue;
import java.util.List;
import java.util.Optional;

public class MountFunctions {
    public static class CappedMountStatFunction extends MountStatFunctionBase<CappedValue> {
        @Override
        public CappedValue getValue(FunctionArguments arguments) {
            return getRequestedCappedStat(arguments).orElse(CappedValue.EMPTY);
        }

        @Override
        protected List<String> getAliases() {
            return List.of("cap_mnt_stat");
        }
    }

    public static class MountStatFunction extends MountStatFunctionBase<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            return getRequestedStatCurrent(arguments).orElse(-1);
        }

        @Override
        protected List<String> getAliases() {
            return List.of("mnt_stat");
        }
    }

    public static class MountStatMaxFunction extends MountStatFunctionBase<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            return getRequestedStatMax(arguments).orElse(-1);
        }

        @Override
        protected List<String> getAliases() {
            return List.of("mnt_stat_max");
        }
    }

    public static class MountNameFunction extends Function<String> {
        @Override
        public String getValue(FunctionArguments arguments) {
            return getMount().flatMap(MountItem::getName).orElse("");
        }

        @Override
        protected List<String> getAliases() {
            return List.of("mnt_name");
        }
    }

    private abstract static class MountStatFunctionBase<T> extends Function<T> {
        @Override
        public FunctionArguments.Builder getArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(new Argument<>("stat", String.class, null)));
        }

        protected Optional<CappedValue> getRequestedCappedStat(FunctionArguments arguments) {
            Optional<MountItem> mount = getMount();
            if (mount.isEmpty()) return Optional.empty();

            Optional<MountStat> stat = getRequestedStat(arguments);
            if (stat.isEmpty() || !stat.get().isCapped()) return Optional.empty();

            return Optional.of(getCappedStatValue(mount.get(), stat.get()));
        }

        protected Optional<Integer> getRequestedStatCurrent(FunctionArguments arguments) {
            Optional<MountItem> mount = getMount();
            if (mount.isEmpty()) return Optional.empty();

            return getRequestedStat(arguments).map(stat -> getStatCurrentValue(mount.get(), stat));
        }

        protected Optional<Integer> getRequestedStatMax(FunctionArguments arguments) {
            Optional<MountItem> mount = getMount();
            if (mount.isEmpty()) return Optional.empty();

            Optional<MountStat> stat = getRequestedStat(arguments);
            if (stat.isEmpty() || !stat.get().isCapped()) return Optional.empty();

            return Optional.of(getCappedStatValue(mount.get(), stat.get()).max());
        }

        private Optional<MountStat> getRequestedStat(FunctionArguments arguments) {
            String statArg = arguments.getArgument("stat").getStringValue();
            return MountStat.fromKey(statArg);
        }
    }

    private static Optional<MountItem> getMount() {
        return Models.Mount.getMount();
    }

    private static int getStatCurrentValue(MountItem mount, MountStat stat) {
        return switch (stat) {
            case ACCELERATION -> mount.getAcceleration().current();
            case ALTITUDE -> mount.getAltitude().current();
            case ENERGY -> mount.getEnergy().current();
            case HANDLING -> mount.getHandling().current();
            case POTENTIAL -> mount.getPotential();
            case POWERUP -> mount.getPowerup().current();
            case SPEED -> mount.getSpeed().current();
            case TOUGHNESS -> mount.getToughness().current();
            case TRAINING -> mount.getTraining().current();
        };
    }

    private static CappedValue getCappedStatValue(MountItem mount, MountStat stat) {
        return switch (stat) {
            case ACCELERATION -> mount.getAcceleration();
            case ALTITUDE -> mount.getAltitude();
            case ENERGY -> mount.getEnergy();
            case HANDLING -> mount.getHandling();
            case POWERUP -> mount.getPowerup();
            case SPEED -> mount.getSpeed();
            case TOUGHNESS -> mount.getToughness();
            case TRAINING -> mount.getTraining();
            case POTENTIAL -> CappedValue.EMPTY;
        };
    }
}
