package com.wynntils.functions;

import com.wynntils.core.WynntilsMod;
import com.wynntils.templates.annotations.TemplateFunction;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Modifier;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class ActivityFunctionsTest {

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
    void activityFallbacksAreSafeWithoutTrackedActivity() {
        assertNotNull(ActivityFunctions.activityNameFunction());
        assertNotNull(ActivityFunctions.activityTaskFunction());
        assertNotNull(ActivityFunctions.activityTaskFunction(false));
        assertNotNull(ActivityFunctions.activityTypeFunction());
        assertNotNull(ActivityFunctions.activityColorFunction());
        assertNotNull(ActivityFunctions.activityIconFunction());
    }
}
