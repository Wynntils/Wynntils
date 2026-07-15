/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions.generic;

import com.wynntils.templates.annotations.TemplateFunction;

import java.util.List;
import java.util.Objects;

public class ConditionalFunctions {
    @TemplateFunction(
            name = "if",
            aliases = {
                    "if_string",
                    "if_str",
                    "if_number",
                    "if_num",
                    "if_capped_value",
                    "if_capped",
                    "if_cap",
                    "if_custom_color",
                    "if_color",
                    "if_customcolor"
            },
            isPure = true)
    public static Object ifFunction(boolean condition, Object left, Object right) {
        return condition ? left : right;
    }


    @TemplateFunction(name = "switch_case", aliases = {"switch"})
    public static Object switchCaseFunction(Object toTest, Object defaultVal, Object... cases) {
        if (cases.length % 2 != 0) {
            return defaultVal; // error not enough arguments
        }
        for (int i = 0; i < cases.length; i += 2) {
            if (toTest.getClass() != cases[i].getClass()) {
                return defaultVal; // error not comparing the same items
            } else if (Objects.equals(toTest, cases[i])) {
                return cases[i + 1];
            }
        }

        return defaultVal;
    }
}
