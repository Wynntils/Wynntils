package com.wynntils.templates.functions;

import com.wynntils.templates.annotations.TemplateFunction;

@SuppressWarnings("unused") // shut up intellij we do use these
public final class TestFunctions {

    @TemplateFunction(name = "add", aliases = {"sum", "plus"})
    public static double addFunction(double[] values) {
        double sum = 0;
        for (double value : values) {
            sum += value;
        }

        return sum;
    }
}
