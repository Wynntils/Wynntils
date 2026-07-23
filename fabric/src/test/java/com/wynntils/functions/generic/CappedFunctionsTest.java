package com.wynntils.functions.generic;

import com.wynntils.core.WynntilsMod;
import com.wynntils.functions.ActivityFunctions;
import com.wynntils.templates.annotations.TemplateFunction;
import com.wynntils.utils.type.CappedValue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Modifier;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class CappedFunctionsTest {
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
    void createsAndReadsCappedValue() {
        CappedValue value = CappedFunctions.cappedFunction(25, 100);

        assertEquals(25, CappedFunctions.currentFunction(value));
        assertEquals(100, CappedFunctions.capFunction(value));
        assertEquals(75, CappedFunctions.remainingFunction(value));
        assertEquals(25, CappedFunctions.percentageFunction(value), 1e-9);
        assertFalse(CappedFunctions.atCapFunction(value));
    }

    @Test
    void detectsValueAtCap() {
        assertTrue(CappedFunctions.atCapFunction(new CappedValue(100, 100)));
    }
}
