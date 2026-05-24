/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions.generic;

import com.wynntils.templates.annotations.TemplateFunction;
import com.wynntils.utils.type.CappedValue;

@SuppressWarnings("unused") // Functions are accessed via reflection
public final class CappedFunctions {
    @TemplateFunction(name = "current", aliases = "curr")
    public static int currentFunction(CappedValue cappedValue) {
        return cappedValue.current();
    }

    @TemplateFunction(name = "cap")
    public static int capFunction(CappedValue cappedValue) {
        return cappedValue.max();
    }

    @TemplateFunction(name = "remaining", aliases = "rem")
    public static int remainingFunction(CappedValue cappedValue) {
        return cappedValue.getRemaining();
    }

    @TemplateFunction(name = "percentage", aliases = "pct")
    public static double percentageFunction(CappedValue cappedValue) {
        return cappedValue.getPercentage();
    }

    @TemplateFunction(name = "at_cap")
    public static boolean atCapFunction(CappedValue cappedValue) {
        return cappedValue.isAtCap();
    }

    @TemplateFunction(name = "capped", isPure = true)
    public static CappedValue cappedFunction(int current, int cap) {
        return new CappedValue(current, cap);
    }
}
