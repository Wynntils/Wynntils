package com.wynntils.functions;

import com.wynntils.core.WynntilsMod;
import com.wynntils.templates.annotations.TemplateFunction;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class CombatFunctionsTest {

    @BeforeAll
    public static void setup() {
        WynntilsMod.setupTestEnv();
    }

    @Test
    void allPublicStaticMethodsAreTemplateFunctions() {
        Arrays.stream(CombatFunctions.class.getDeclaredMethods())
                .filter(method -> Modifier.isPublic(method.getModifiers()))
                .filter(method -> Modifier.isStatic(method.getModifiers()))
                .forEach(method -> assertNotNull(
                        method.getAnnotation(TemplateFunction.class),
                        () -> method.getName() + " is missing @TemplateFunction"));
    }

    @Test
    void invalidSpellInputsReturnFallbackValues() {
        assertEquals(-1, CombatFunctions.ticksSinceSpecificSpellFunction("not_a_spell"));
    }

    @Test
    void convenienceOverloadsMatchTheirDefaults() {
        assertEquals(CombatFunctions.areaDamageAverageFunction(10), CombatFunctions.areaDamageAverageFunction());
        assertEquals(CombatFunctions.totalAreaDamageFunction(10), CombatFunctions.totalAreaDamageFunction());
        assertEquals(CombatFunctions.killsPerMinuteFunction(true), CombatFunctions.killsPerMinuteFunction());
        assertEquals(CombatFunctions.lastSpellNameFunction(false), CombatFunctions.lastSpellNameFunction());
        assertEquals(CombatFunctions.ticksSinceLastSpellFunction(false), CombatFunctions.ticksSinceLastSpellFunction());
    }
}
