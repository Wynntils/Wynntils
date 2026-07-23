package com.wynntils.functions;

import com.wynntils.core.WynntilsMod;
import com.wynntils.templates.annotations.TemplateFunction;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Modifier;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class WynnAlphabetFunctionsTest {

    @BeforeAll
    public static void setup() {
        WynntilsMod.setupTestEnv();
    }

    @Test
    void allPublicStaticMethodsAreTemplateFunctions() {
        Arrays.stream(WynnAlphabetFunctions.class.getDeclaredMethods())
                .filter(method -> Modifier.isPublic(method.getModifiers()))
                .filter(method -> Modifier.isStatic(method.getModifiers()))
                .forEach(method -> assertNotNull(
                        method.getAnnotation(TemplateFunction.class),
                        () -> method.getName() + " is missing @TemplateFunction"));
    }

    @Test
    void transcriptionReturnsNonNullStrings() {
        assertNotNull(WynnAlphabetFunctions.transcribeGavellianFunction(""));
        assertNotNull(WynnAlphabetFunctions.transcribeWynnicFunction(""));
        assertNotNull(WynnAlphabetFunctions.transcribeGavellianFunction("Hello World"));
        assertNotNull(WynnAlphabetFunctions.transcribeWynnicFunction("Hello World"));
    }

    @Test
    void transcriptionIsCaseInsensitive() {
        assertEquals(
                WynnAlphabetFunctions.transcribeGavellianFunction("hello"),
                WynnAlphabetFunctions.transcribeGavellianFunction("HELLO"));

        assertEquals(
                WynnAlphabetFunctions.transcribeWynnicFunction("hello"),
                WynnAlphabetFunctions.transcribeWynnicFunction("HELLO"));
    }
}
