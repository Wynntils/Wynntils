/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions.generic;

import com.wynntils.utils.type.RangedValue;
import com.wynntils.templates.annotations.TemplateFunction;

//Functions are accessed via reflection
@SuppressWarnings("unused")
public class RangedFunctions {

    @TemplateFunction(name = "ranged")
    public static RangedValue rangedFunction(int low, int high) {
        return new RangedValue(low, high);
    }

    @TemplateFunction(name = "range_low", aliases = { "low" })
    public static int rangeLowFunction(RangedValue range) {
        return range.low();
    }

    @TemplateFunction(name = "range_high", aliases = { "high" })
    public static int rangeHighFunction(RangedValue range) {
        return range.high();
    }
}
