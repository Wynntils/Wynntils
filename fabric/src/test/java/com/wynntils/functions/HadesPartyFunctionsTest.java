package com.wynntils.functions;

import com.wynntils.core.WynntilsMod;
import com.wynntils.templates.annotations.TemplateFunction;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class HadesPartyFunctionsTest {

    @BeforeAll
    public static void setup() {
        WynntilsMod.setupTestEnv();
    }

    @Test
    void allPublicStaticMethodsAreTemplateFunctions() {
        Arrays.stream(HadesPartyFunctions.class.getDeclaredMethods())
                .filter(method -> Modifier.isPublic(method.getModifiers()))
                .filter(method -> Modifier.isStatic(method.getModifiers()))
                .forEach(method -> assertNotNull(
                        method.getAnnotation(TemplateFunction.class),
                        () -> method.getName() + " is missing @TemplateFunction"));
    }

    @Test
    void invalidMemberIndexesReturnFallbackValues() {
        assertNotNull(HadesPartyFunctions.hadesPartyMemberHealthFunction(-1));
        assertNotNull(HadesPartyFunctions.hadesPartyMemberManaFunction(-1));
        assertNotNull(HadesPartyFunctions.hadesPartyMemberLocationFunction(-1));
        assertEquals("", HadesPartyFunctions.hadesPartyMemberNameFunction(-1));
        assertEquals("", HadesPartyFunctions.hadesPartyMemberUuidFunction(-1));
    }
}
