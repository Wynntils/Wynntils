/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions.generic;

import com.wynntils.templates.annotations.TemplateFunction;
import com.wynntils.utils.type.NamedValue;
import java.util.List;

@SuppressWarnings("unused") // Functions are accessed via reflection
public final class NamedFunctions {


    @TemplateFunction(name = "name")
    public static String nameFunction(NamedValue namedValue) {
        return namedValue.name();
    }


    @TemplateFunction(name = "value", aliases = "val")
    public static double valueFunction(NamedValue namedValue) {
        return namedValue.value();
    }

    @TemplateFunction(name = "named_value", aliases = "named", isPure = true)
    public static NamedValue namedValueFunction(String name, double value) {
        return new NamedValue(name, value);
    }
}
