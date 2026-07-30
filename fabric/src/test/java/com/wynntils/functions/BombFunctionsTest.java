package com.wynntils.functions;

import com.wynntils.core.WynntilsMod;
import com.wynntils.templates.annotations.TemplateFunction;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Modifier;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class BombFunctionsTest {

    @BeforeAll
    public static void setup() {
        WynntilsMod.setupTestEnv();
    }

    @Test
    void allPublicStaticMethodsAreTemplateFunctions() {
        Arrays.stream(BombFunctions.class.getDeclaredMethods())
                .filter(method -> Modifier.isPublic(method.getModifiers()))
                .filter(method -> Modifier.isStatic(method.getModifiers()))
                .forEach(method -> assertNotNull(
                        method.getAnnotation(TemplateFunction.class),
                        () -> method.getName() + " is missing @TemplateFunction"));
    }

    @Test
    void invalidIndexesReturnFallbackValues() {
        assertEquals("", BombFunctions.bombFormattedStringFunction(-1, false, "start"));
        assertEquals("", BombFunctions.bombTypeFunction(-1, false, "start"));
        assertEquals("", BombFunctions.bombWorldFunction(-1, false, "start"));
        assertEquals(-1d, BombFunctions.bombLengthFunction(-1, false, "start"));
        assertEquals("", BombFunctions.bombOwnerFunction(-1, false, "start"));
        assertNotNull(BombFunctions.bombStartTimeFunction(-1, false, "start"));
        assertNotNull(BombFunctions.bombEndTimeFunction(-1, false, "start"));
        assertNotNull(BombFunctions.bombRemaingTimeFunction(-1, false, "start"));
    }
}
