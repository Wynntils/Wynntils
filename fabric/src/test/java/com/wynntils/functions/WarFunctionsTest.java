package com.wynntils.functions;

import com.wynntils.core.WynntilsMod;
import com.wynntils.templates.annotations.TemplateFunction;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class WarFunctionsTest {

    @BeforeAll
    public static void setup() {
        WynntilsMod.setupTestEnv();
    }

    @Test
    void allPublicStaticMethodsAreTemplateFunctions() {
        Arrays.stream(WarFunctions.class.getDeclaredMethods())
                .filter(method -> Modifier.isPublic(method.getModifiers()))
                .filter(method -> Modifier.isStatic(method.getModifiers()))
                .forEach(method -> assertNotNull(
                        method.getAnnotation(TemplateFunction.class),
                        () -> method.getName() + " is missing @TemplateFunction"));
    }

    @Test
    void missingWarStateUsesSafeFallbacks() {
        assertNotNull(WarFunctions.towerOwnerFunction());
        assertNotNull(WarFunctions.towerTerritoryFunction());
        assertNotNull(WarFunctions.initialTowerDamageFunction());
        assertNotNull(WarFunctions.currentTowerDamageFunction());
        assertNotNull(WarFunctions.warStartFunction());
        assertNotNull(WarFunctions.towerDpsFunction());
        assertNotNull(WarFunctions.estimatedWarEndFunction());
    }

    @Test
    void convenienceWarCountUsesDefaultWindow() {
        assertEquals(WarFunctions.warsSinceFunction(1), WarFunctions.warsSinceFunction());
    }
}
