package com.wynntils.functions;

import com.wynntils.core.WynntilsMod;
import com.wynntils.templates.annotations.TemplateFunction;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class LootrunFunctionsTest {

    @BeforeAll
    public static void setup() {
        WynntilsMod.setupTestEnv();
    }

    @Test
    void allPublicStaticMethodsAreTemplateFunctions() {
        Arrays.stream(LootrunFunctions.class.getDeclaredMethods())
                .filter(method -> Modifier.isPublic(method.getModifiers()))
                .filter(method -> Modifier.isStatic(method.getModifiers()))
                .forEach(method -> assertNotNull(
                        method.getAnnotation(TemplateFunction.class),
                        () -> method.getName() + " is missing @TemplateFunction"));
    }

    @Test
    void invalidBeaconColorsReturnFallbackValues() {
        assertEquals(-1, LootrunFunctions.lootrunBeaconCountFunction("not_a_color"));
        assertEquals("", LootrunFunctions.lootrunTaskNameFunction("not_a_color"));
        assertNotNull(LootrunFunctions.lootrunTaskLocationFunction("not_a_color"));
        assertEquals("", LootrunFunctions.lootrunTaskTypeFunction("not_a_color"));
        assertFalse(LootrunFunctions.lootrunBeaconVibrantFunction("not_a_color"));
    }

    @Test
    void emptyMythicHistoryUsesSafeFallbacks() {
        assertTrue(LootrunFunctions.highestDryStreakFunction() >= 0);
        assertTrue(LootrunFunctions.lastDryStreakFunction() >= 0);
        assertNotNull(LootrunFunctions.lastMythicFunction());
    }
}
