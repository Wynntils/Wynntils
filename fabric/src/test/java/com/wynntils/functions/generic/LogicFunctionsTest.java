package com.wynntils.functions.generic;

import com.wynntils.core.WynntilsMod;
import com.wynntils.functions.ActivityFunctions;
import com.wynntils.templates.annotations.TemplateFunction;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Modifier;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class LogicFunctionsTest {
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
    void comparesEquality() {
        assertTrue(LogicFunctions.equalsFunction(4.5, 4.5));
        assertFalse(LogicFunctions.equalsFunction(4.5, 4.6));
        assertTrue(LogicFunctions.notEqualsFunction(4.5, 4.6));
    }

    @Test
    void performsBooleanOperations() {
        assertFalse(LogicFunctions.notFunction(true));
        assertTrue(LogicFunctions.andFunction(true, true));
        assertFalse(LogicFunctions.andFunction(true, false));
        assertTrue(LogicFunctions.orFunction(false, true));
        assertFalse(LogicFunctions.orFunction(false, false));
    }

    @Test
    void performsVarargBooleanOperations() {
        assertTrue(LogicFunctions.andFunction(true, true, true));
        assertFalse(LogicFunctions.andFunction(true, false, true));
        assertTrue(LogicFunctions.andFunction(new boolean[0]));

        assertTrue(LogicFunctions.orFunction(false, true, false));
        assertFalse(LogicFunctions.orFunction(false, false, false));
        assertFalse(LogicFunctions.orFunction(new boolean[0]));
    }

    @Test
    void comparesDifferentNumberTypes() {
        assertTrue(LogicFunctions.lessThanFunction(1, 2.0));
        assertTrue(LogicFunctions.lessThanOrEqualsFunction(2L, 2.0f));
        assertTrue(LogicFunctions.greaterThanFunction(3.0, 2));
        assertTrue(LogicFunctions.greaterThanOrEqualsFunction(3, 3L));
    }
}
