/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions;

import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.functions.Function;
import com.wynntils.core.consumers.functions.arguments.FunctionArguments;
import com.wynntils.utils.type.CappedValue;
import java.util.List;

public class WorldEventFunctions {
    public static class AnnihilationSunProgressFunction extends Function<CappedValue> {
        @Override
        public CappedValue getValue(FunctionArguments arguments) {
            return Models.WorldEvent.annihilationSunBar.isActive()
                    ? Models.WorldEvent.annihilationSunBar.getBarProgress().value()
                    : CappedValue.EMPTY;
        }

        @Override
        protected List<String> getAliases() {
            return List.of("sun_progress");
        }
    }
}
