/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions.generic;

import com.wynntils.templates.annotations.TemplateFunction;

import java.util.Objects;

@SuppressWarnings("unused") // Functions are accessed via reflection
public class LogicFunctions {

    @TemplateFunction(name = "equals", aliases = "eq", isPure = true)
    public static boolean equalsFunction(double a, double b) {
        return Objects.equals(a, b);
    }

    @TemplateFunction(name = "not_equals", aliases = "neq", isPure = true)
    public static boolean notEqualsFunction(double a, double b) {
        return !Objects.equals(a, b);
    }


    @TemplateFunction(name = "not", isPure = true)
    public static boolean notFunction(boolean value) {
        return !value;
    }


    @TemplateFunction(name = "and", isPure = true)
    public static boolean andFunction(boolean a, boolean b) {
        return a && b;
    }

    @TemplateFunction(name = "and", isPure = true)
    public static boolean andFunction(boolean... values) {
        for (boolean value : values) {
            if (!value) return false;
        }
        return true;
    }


    @TemplateFunction(name = "or", isPure = true)
    public static boolean orFunction(boolean a, boolean b) {
        return a || b;
    }

    @TemplateFunction(name = "or", isPure = true)
    public static boolean orFunction(boolean... values) {
        for (boolean value : values) {
            if (value) return true;
        }
        return false;
    }

    @TemplateFunction(name = "less_than", aliases = "lt", isPure = true)
    public static boolean lessThanFunction(Number a, Number b) {
        return a.doubleValue() < b.doubleValue();
    }

    @TemplateFunction(name = "less_than_or_equals", aliases = {"lte", "less_than_equals", "leq"}, isPure = true)
    public static boolean lessThanOrEqualsFunction(Number a, Number b) {
        return a.doubleValue() <= b.doubleValue();
    }

    @TemplateFunction(name = "greater_than", aliases = {"mt", "more_than", "gt"}, isPure = true)
    public static boolean greaterThanFunction(Number a, Number b) {
        return a.doubleValue() > b.doubleValue();
    }

    @TemplateFunction(name = "greater_than_or_equals", aliases = {"mte", "more_than_equals", "greater_than_equals", "gte", "geq"}, isPure = true)
    public static boolean greaterThanOrEqualsFunction(Number a, Number b) {
        return a.doubleValue() >= b.doubleValue();
    }
}
