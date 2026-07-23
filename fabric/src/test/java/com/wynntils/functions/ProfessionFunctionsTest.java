package com.wynntils.functions;

import com.wynntils.core.WynntilsMod;
import com.wynntils.templates.annotations.TemplateFunction;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class ProfessionFunctionsTest {

    @BeforeAll
    public static void setup() {
        WynntilsMod.setupTestEnv();
    }

    @Test
    void allPublicStaticMethodsAreTemplateFunctions() {
        Arrays.stream(ProfessionFunctions.class.getDeclaredMethods())
                .filter(method -> Modifier.isPublic(method.getModifiers()))
                .filter(method -> Modifier.isStatic(method.getModifiers()))
                .forEach(method -> assertNotNull(
                        method.getAnnotation(TemplateFunction.class),
                        () -> method.getName() + " is missing @TemplateFunction"));
    }

    @Test
    void invalidProfessionReturnsDocumentedFallbacks() {
        assertNotNull(ProfessionFunctions.professionXpFunction("not_a_profession"));
        assertEquals(-1, ProfessionFunctions.professionLevelFunction("not_a_profession"));
        assertEquals(-1.0, ProfessionFunctions.professionPercentageFunction("not_a_profession"));
        assertEquals(-1, ProfessionFunctions.professionXpPerMinuteRawFunction("not_a_profession"));
        assertEquals("Invalid profession", ProfessionFunctions.professionXpPerMinuteFunction("not_a_profession"));
    }

    @Test
    void missingHarvestDataUsesSafeFallbacks() {
        assertNotNull(ProfessionFunctions.lastHarvestResourceTypeFunction());
        assertNotNull(ProfessionFunctions.lastHarvestMaterialTypeFunction());
        assertNotNull(ProfessionFunctions.lastHarvestMaterialNameFunction());
    }
}
