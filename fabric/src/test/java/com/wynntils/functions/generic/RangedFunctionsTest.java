package com.wynntils.functions.generic;

import com.wynntils.core.WynntilsMod;
import com.wynntils.functions.ActivityFunctions;
import com.wynntils.templates.annotations.TemplateFunction;
import com.wynntils.utils.type.RangedValue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Modifier;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class RangedFunctionsTest {
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
    void createsAndReadsRange() {
        RangedValue range = RangedFunctions.rangedFunction(12, 34);

        assertEquals(12, RangedFunctions.rangeLowFunction(range));
        assertEquals(34, RangedFunctions.rangeHighFunction(range));
    }
}
