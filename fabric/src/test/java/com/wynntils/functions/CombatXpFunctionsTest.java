package com.wynntils.functions;

import com.wynntils.core.WynntilsMod;
import com.wynntils.templates.annotations.TemplateFunction;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class CombatXpFunctionsTest {

    @BeforeAll
    public static void setup() {
        WynntilsMod.setupTestEnv();
    }

    @Test
    void allPublicStaticMethodsAreTemplateFunctions() {
        Arrays.stream(CombatXpFunctions.class.getDeclaredMethods())
                .filter(method -> Modifier.isPublic(method.getModifiers()))
                .filter(method -> Modifier.isStatic(method.getModifiers()))
                .forEach(method -> assertNotNull(
                        method.getAnnotation(TemplateFunction.class),
                        () -> method.getName() + " is missing @TemplateFunction"));
    }

    @Test
    void numericAndFormattedXpValuesRemainConsistent() {
        assertEquals(CombatXpFunctions.cappedLevelFunction().current(), CombatXpFunctions.levelFunction());
        assertEquals(CombatXpFunctions.cappedXpFunction().current(), CombatXpFunctions.xpRawFunction());
        assertEquals(CombatXpFunctions.cappedXpFunction().max(), CombatXpFunctions.xpReqRawFunction());
        assertTrue(Double.isFinite(CombatXpFunctions.xpPercentageFunction()));
        assertTrue(Double.isFinite(CombatXpFunctions.xpPercentagePerMinuteFunction()));
    }
}
