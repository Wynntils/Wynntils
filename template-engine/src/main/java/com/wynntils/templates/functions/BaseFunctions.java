/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.templates.functions;

import com.wynntils.templates.annotations.TemplateFunction;

@SuppressWarnings("unused") // shut up intellij we do use these
public final class BaseFunctions {
    @TemplateFunction(name = "add", aliases = {"sum", "plus"}, isPure = true)
    public static double addFunction(double a, double b) {
        return a + b;
    }

    @TemplateFunction(name = "add", aliases = {"sum", "plus"}, isPure = true)
    public static double addFunction(double... values) {
        double sum = 0;
        for (double value : values) {
            sum += value;
        }

        return sum;
    }

    @TemplateFunction(name = "concat", isPure = true)
    public static String concat(String a, String b) {
        return a + b;
    }
}
