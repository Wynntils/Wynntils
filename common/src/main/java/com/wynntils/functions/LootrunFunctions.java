/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions;

import com.wynntils.core.functions.Function;
import com.wynntils.core.functions.arguments.FunctionArguments;
import com.wynntils.features.wynntils.DataStorageFeature;
import java.util.List;

public class LootrunFunctions {
    public static class DryStreakFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            return DataStorageFeature.INSTANCE.dryCount.get();
        }

        @Override
        public List<String> getAliases() {
            return List.of("dry_s");
        }
    }

    public static class DryBoxesFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            return DataStorageFeature.INSTANCE.dryBoxes.get();
        }

        @Override
        public List<String> getAliases() {
            return List.of("dry_b", "dry_boxes_count");
        }
    }
}
