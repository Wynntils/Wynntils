package com.wynntils.functions;

import com.wynntils.core.WynntilsMod;
import com.wynntils.templates.annotations.TemplateFunction;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class SpellFunctionsTest {

    @BeforeAll
    public static void setup() {
        WynntilsMod.setupTestEnv();
    }

    @Test
    void allPublicStaticMethodsAreTemplateFunctions() {
        Arrays.stream(SpellFunctions.class.getDeclaredMethods())
                .filter(method -> Modifier.isPublic(method.getModifiers()))
                .filter(method -> Modifier.isStatic(method.getModifiers()))
                .forEach(method -> assertNotNull(
                        method.getAnnotation(TemplateFunction.class),
                        () -> method.getName() + " is missing @TemplateFunction"));
    }

    @Test
    void invalidTotemIndexesReturnFallbackValues() {
        assertEquals("", SpellFunctions.shamanTotemStateFunction(-1));
        assertEquals("", SpellFunctions.shamanTotemLocationFunction(-1));
        assertEquals(0, SpellFunctions.shamanTotemTimeLeftFunction(-1));
        assertEquals(0d, SpellFunctions.shamanTotemDistanceFunction(-1));
        assertEquals(0, SpellFunctions.shamanTotemTransfusedAmountFunction(-1));
        assertEquals("", SpellFunctions.shamanTotemPoisonAmountFunction(-1));
    }

    @Test
    void defaultMaskOverloadMatchesExplicitArguments() {
        assertEquals(SpellFunctions.shamanMaskFunction(true, false), SpellFunctions.shamanMaskFunction());
    }
}
