/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions;

import com.wynntils.core.components.Models;
import com.wynntils.core.functions.Function;
import com.wynntils.core.functions.arguments.FunctionArguments;
import java.util.List;

public class CombatFunctions {
    public static class AreaDamagePerSecondFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            return Models.Damage.getAreaDamagePerSecond();
        }

        @Override
        public List<String> getAliases() {
            return List.of("adps");
        }
    }
}
