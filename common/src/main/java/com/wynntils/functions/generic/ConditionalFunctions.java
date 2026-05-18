/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions.generic;


import com.wynntils.templates.annotations.TemplateFunction;

@SuppressWarnings("unused") // Functions are accessed via reflection
public class ConditionalFunctions {

    @TemplateFunction(name = "if", aliases = {"if_string", "if_str", "if_number", "if_num", "if_capped_value", "if_capped", "if_cap", "if_custom_color", "if_color", "if_customcolor"}, isPure = true)
    public static Object ifFunction(boolean condition, Object left, Object right) {
        return condition ? left : right;
    }
}
