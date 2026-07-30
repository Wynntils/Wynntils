package com.wynntils.functions;

import com.wynntils.core.WynntilsMod;
import com.wynntils.templates.annotations.TemplateFunction;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class EnvironmentFunctionsTest {

    @BeforeAll
    public static void setup() {
        WynntilsMod.setupTestEnv();
    }

    @Test
    void allPublicStaticMethodsAreTemplateFunctions() {
        Arrays.stream(EnvironmentFunctions.class.getDeclaredMethods())
                .filter(method -> Modifier.isPublic(method.getModifiers()))
                .filter(method -> Modifier.isStatic(method.getModifiers()))
                .forEach(method -> assertNotNull(
                        method.getAnnotation(TemplateFunction.class),
                        () -> method.getName() + " is missing @TemplateFunction"));
    }

    @Test
    void environmentValuesHaveSaneRanges() {
        assertNotNull(EnvironmentFunctions.nowFunction());
        assertFalse(EnvironmentFunctions.clockFunction().isBlank());
        assertTrue(EnvironmentFunctions.clockMFunction().matches("\\d{2}:\\d{2}:\\d{2}"));
        assertTrue(EnvironmentFunctions.memoryMaxFunction() >= 0);
        assertTrue(EnvironmentFunctions.memoryUsedFunction() >= 0);
        assertTrue(EnvironmentFunctions.memoryPercentFunction() >= 0);
        assertNotNull(EnvironmentFunctions.wynntilsVersionFunction());
        assertNotNull(EnvironmentFunctions.minecraftVersionFunction());
        assertNotNull(EnvironmentFunctions.wynncraftVersionFunction());
    }
}
