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
            return getRequestedStat(arguments).orElse(CappedValue.EMPTY);
        }

        @Override
        protected List<String> getAliases() {
            return List.of("cap_mnt_stat");
        }
    }

    public static class MountStatFunction extends MountStatFunctionBase<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            return getRequestedStat(arguments).map(CappedValue::current).orElse(-1);
        }

        @Override
        protected List<String> getAliases() {
            return List.of("mnt_stat");
        }
    }

    public static class MountStatMaxFunction extends MountStatFunctionBase<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            return getRequestedStat(arguments).map(CappedValue::max).orElse(-1);
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

        protected Optional<CappedValue> getRequestedStat(FunctionArguments arguments) {
            Optional<MountItem> mount = getMount();
            if (mount.isEmpty()) return Optional.empty();

            String statArg = arguments.getArgument("stat").getStringValue();
            Optional<MountStat> stat = MountStat.fromKey(statArg);
            if (stat.isEmpty()) return Optional.empty();

            return Optional.of(getStatValue(mount.get(), stat.get()));
        }
    }

    private static Optional<MountItem> getMount() {
        return Models.Mount.getMount();
    }

    private static CappedValue getStatValue(MountItem mount, MountStat stat) {
        return switch (stat) {
            case ACCELERATION -> mount.getAcceleration();
            case ALTITUDE -> mount.getAltitude();
            case ENERGY -> mount.getEnergy();
            case HANDLING -> mount.getHandling();
            case POWERUP -> mount.getPowerup();
            case SPEED -> mount.getSpeed();
            case TOUGHNESS -> mount.getToughness();
            case TRAINING -> mount.getTraining();
        };
    }
}
