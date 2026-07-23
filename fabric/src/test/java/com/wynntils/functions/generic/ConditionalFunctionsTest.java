package com.wynntils.functions.generic;

import com.wynntils.core.WynntilsMod;
import com.wynntils.functions.ActivityFunctions;
import com.wynntils.templates.annotations.TemplateFunction;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Modifier;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class ConditionalFunctionsTest {
    @BeforeAll
    public static void setup() {
        WynntilsMod.setupTestEnv();
    }

    @Test
    void allPublicStaticMethodsAreTemplateFunctions() {
        Arrays.stream(ActivityFunctions.class.getDeclaredMethods())
                .filter(method -> Modifier.isPublic(method.getModifiers()))
                .filter(method -> Modifier.isStatic(method.getModifiers()))
                .forEach(method -> assertNotNull(
                        method.getAnnotation(TemplateFunction.class),
                        () -> method.getName() + " is missing @TemplateFunction"));
    }

    @Test
    void ifReturnsSelectedBranch() {
        Object left = new Object();
        Object right = new Object();

        assertSame(left, ConditionalFunctions.ifFunction(true, left, right));
        assertSame(right, ConditionalFunctions.ifFunction(false, left, right));
    }

    @Test
    void switchReturnsMatchingCase() {
        assertEquals("two", ConditionalFunctions.switchCaseFunction(
                2, "default", 1, "one", 2, "two", 3, "three"));
    }

    @Test
    void switchReturnsDefaultWhenNothingMatches() {
        assertEquals("default", ConditionalFunctions.switchCaseFunction(
                4, "default", 1, "one", 2, "two"));
    }

    @Test
    void switchReturnsDefaultForOddCaseCount() {
        assertEquals("default", ConditionalFunctions.switchCaseFunction(
                1, "default", 1, "one", 2));
    }

    @Test
    void switchReturnsDefaultForMismatchedTypes() {
        assertEquals("default", ConditionalFunctions.switchCaseFunction(
                1, "default", "1", "one"));
    }
}
