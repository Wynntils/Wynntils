/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions;

import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.functions.Function;
import com.wynntils.core.consumers.functions.arguments.FunctionArguments;
import com.wynntils.models.containers.type.MythicFind;
import java.util.Comparator;
import java.util.List;

public class LootrunFunctions {
    public static class DryStreakFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            return Models.LootChest.getDryCount();
        }

        @Override
        public List<String> getAliases() {
            return List.of("dry_s");
        }
    }

    public static class DryBoxesFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            return Models.LootChest.getDryBoxes();
        }

        @Override
        public List<String> getAliases() {
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
        public List<String> getAliases() {
            return List.of("chest_count");
        }
    }
}
