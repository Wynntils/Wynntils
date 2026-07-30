package com.wynntils.functions;

import com.wynntils.core.WynntilsMod;
import com.wynntils.templates.annotations.TemplateFunction;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class StatusEffectFunctionsTest {

    @BeforeAll
    public static void setup() {
        WynntilsMod.setupTestEnv();
    }

    @Test
    void allPublicStaticMethodsAreTemplateFunctions() {
        Arrays.stream(StatusEffectFunctions.class.getDeclaredMethods())
                .filter(method -> Modifier.isPublic(method.getModifiers()))
                .filter(method -> Modifier.isStatic(method.getModifiers()))
                .forEach(method -> assertNotNull(
                        method.getAnnotation(TemplateFunction.class),
                        () -> method.getName() + " is missing @TemplateFunction"));
    }

    @Test
    void unknownEffectsReturnFallbackValues() {
        assertFalse(StatusEffectFunctions.statusEffectActiveFunction("definitely-not-an-effect"));
        assertNotNull(StatusEffectFunctions.statusEffectDurationFunction("definitely-not-an-effect"));
        assertNotNull(StatusEffectFunctions.statusEffectModiferFunction("definitely-not-an-effect"));
        assertEquals("", StatusEffectFunctions.statusEffectPrefixFunction("definitely-not-an-effect"));
    }

    @Test
    void statusEffectListIsNeverNull() {
        assertNotNull(StatusEffectFunctions.statusEffectsFunction());
    }
}
