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
import com.wynntils.models.mount.type.MountChoice;
import com.wynntils.models.mount.type.MountStat;
import com.wynntils.utils.type.CappedValue;
import java.util.List;
import java.util.Optional;

public class MountFunctions {
    public static class CappedMountStatFunction extends Function<CappedValue> {
        @Override
        public CappedValue getValue(FunctionArguments arguments) {
            Optional<MountItem> mountItemOpt = getMount(arguments);
            if (mountItemOpt.isEmpty()) return CappedValue.EMPTY;

            Optional<MountStat> mountStatOpt =
                    MountStat.fromKey(arguments.getArgument("stat").getStringValue());
            if (mountStatOpt.isEmpty()) return CappedValue.EMPTY;

            if (mountItemOpt.get().getMountInfo().stats().containsKey(mountStatOpt.get())) {
                return mountItemOpt.get().getMountInfo().stats().get(mountStatOpt.get());
            }

            return CappedValue.EMPTY;
        }

        @Override
        public FunctionArguments.Builder getArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(
                    new Argument<>("mountType", String.class, null), new Argument<>("stat", String.class, null)));
        }

        @Override
        protected List<String> getAliases() {
            return List.of("cap_mnt_stat");
        }
    }

    public static class MountPotentialFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            Optional<MountItem> mountItemOpt = getMount(arguments);

            return mountItemOpt
                    .map(mountItem -> mountItem.getMountInfo().potential())
                    .orElse(-1);
        }

        @Override
        protected List<String> getAliases() {
            return List.of("mnt_potential");
        }

        @Override
        public FunctionArguments.Builder getArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(
                    List.of(new Argument<>("mountType", String.class, null)));
        }
    }

    public static class MountNameFunction extends Function<String> {
        @Override
        public String getValue(FunctionArguments arguments) {
            Optional<MountItem> mountItemOpt = getMount(arguments);

            return mountItemOpt.map(MountItem::getName).orElse("");
        }

        @Override
        protected List<String> getAliases() {
            return List.of("mnt_name");
        }

        @Override
        public FunctionArguments.Builder getArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(
                    List.of(new Argument<>("mountType", String.class, null)));
        }
    }

    public static class CurrentMountEnergyFunction extends Function<CappedValue> {
        @Override
        public CappedValue getValue(FunctionArguments arguments) {
            return Models.Mount.getCurrentMountEnergy().orElse(CappedValue.EMPTY);
        }
    }

    private static Optional<MountItem> getMount(FunctionArguments arguments) {
        String mountChoiceArg = arguments.getArgument("mountType").getStringValue();
        MountChoice mountChoice = MountChoice.fromName(mountChoiceArg);

        if (mountChoice == null) return Optional.empty();

        return Models.Mount.getMount(mountChoice);
    }
}
