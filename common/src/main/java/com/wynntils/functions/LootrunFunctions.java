/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions;

import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.functions.Function;
import com.wynntils.core.consumers.functions.arguments.FunctionArguments;
import com.wynntils.models.beacons.type.BeaconColor;
import com.wynntils.models.containers.type.MythicFind;
import com.wynntils.models.lootrun.type.TaskLocation;
import com.wynntils.utils.EnumUtils;
import com.wynntils.utils.type.CappedValue;
import java.util.Comparator;
import java.util.List;

public class LootrunFunctions {
    public static class DryStreakFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            return Models.LootChest.getDryCount();
        }

        @Override
        protected List<String> getAliases() {
            return List.of("dry_s");
        }
    }

    public static class DryBoxesFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            return Models.LootChest.getDryBoxes();
        }

        @Override
        protected List<String> getAliases() {
            return List.of("dry_b", "dry_boxes_count");
        }
    }

    public static class HighestDryStreakFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            return Models.LootChest.getMythicFinds().stream()
                    .max(Comparator.comparing(MythicFind::dryCount))
                    .map(MythicFind::dryCount)
                    .orElse(0);
        }
    }

    public static class LastDryStreakFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            List<MythicFind> mythicFinds = Models.LootChest.getMythicFinds();

            if (mythicFinds.isEmpty()) return 0;

            return mythicFinds.get(mythicFinds.size() - 1).dryCount();
        }
    }

    public static class LastMythicFunction extends Function<String> {
        @Override
        public String getValue(FunctionArguments arguments) {
            List<MythicFind> mythicFinds = Models.LootChest.getMythicFinds();

            if (mythicFinds.isEmpty()) return "";

            return mythicFinds.get(mythicFinds.size() - 1).itemName();
        }
    }

    public static class ChestOpenedFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            return Models.LootChest.getOpenedChestCount();
        }

        @Override
        protected List<String> getAliases() {
            return List.of("chest_count");
        }
    }

    public static class LootrunStateFunction extends Function<String> {
        @Override
        public String getValue(FunctionArguments arguments) {
            return Models.Lootrun.getState().toString();
        }
    }

    public static class LootrunBeaconCountFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            String color = arguments.getArgument("color").getStringValue();

            BeaconColor beaconColor = BeaconColor.fromName(color);
            if (beaconColor == null) return -1;

            return Models.Lootrun.getBeaconCount(beaconColor);
        }

        @Override
        public FunctionArguments.Builder getArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(
                    List.of(new FunctionArguments.Argument<>("color", String.class, null)));
        }
    }

    public static class LootrunTaskNameFunction extends Function<String> {
        @Override
        public String getValue(FunctionArguments arguments) {
            String color = arguments.getArgument("color").getStringValue();

            BeaconColor beaconColor = BeaconColor.fromName(color);
            if (beaconColor == null) return "";

            TaskLocation taskLocation = Models.Lootrun.getTaskForColor(beaconColor);
            if (taskLocation == null) return "";

            return taskLocation.name();
        }

        @Override
        public FunctionArguments.Builder getArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(
                    List.of(new FunctionArguments.Argument<>("color", String.class, null)));
        }
    }

    public static class LootrunTaskTypeFunction extends Function<String> {
        @Override
        public String getValue(FunctionArguments arguments) {
            String color = arguments.getArgument("color").getStringValue();

            BeaconColor beaconColor = BeaconColor.fromName(color);
            if (beaconColor == null) return "";

            TaskLocation taskLocation = Models.Lootrun.getTaskForColor(beaconColor);
            if (taskLocation == null) return "";

            return EnumUtils.toNiceString(taskLocation.taskType());
        }

        @Override
        public FunctionArguments.Builder getArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(
                    List.of(new FunctionArguments.Argument<>("color", String.class, null)));
        }
    }

    public static class LootrunTimeFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            return Models.Lootrun.getCurrentTime();
        }
    }

    public static class LootrunChallengesFunction extends Function<CappedValue> {
        @Override
        public CappedValue getValue(FunctionArguments arguments) {
            return Models.Lootrun.getChallenges();
        }
    }

    public static class LootrunLastSelectedBeaconColorFunction extends Function<String> {
        @Override
        public String getValue(FunctionArguments arguments) {
            BeaconColor beaconColor = Models.Lootrun.getLastTaskBeaconColor();
            if (beaconColor == null) return "";

            return EnumUtils.toNiceString(beaconColor);
        }
    }

    public static class LootrunRedBeaconChallengeCountFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            return Models.Lootrun.getRedBeaconTaskCount();
        }
    }
}
