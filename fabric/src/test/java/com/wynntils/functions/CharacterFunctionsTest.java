package com.wynntils.functions;

import com.wynntils.core.WynntilsMod;
import com.wynntils.templates.annotations.TemplateFunction;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class CharacterFunctionsTest {

    @BeforeAll
    public static void setup() {
        WynntilsMod.setupTestEnv();
    }

    @Test
    void allPublicStaticMethodsAreTemplateFunctions() {
        Arrays.stream(CharacterFunctions.class.getDeclaredMethods())
                .filter(method -> Modifier.isPublic(method.getModifiers()))
                .filter(method -> Modifier.isStatic(method.getModifiers()))
                .forEach(method -> assertNotNull(
                        method.getAnnotation(TemplateFunction.class),
                        () -> method.getName() + " is missing @TemplateFunction"));
    }

    @Test
    void invalidAbilityAndIndexesReturnFallbackValues() {
        assertEquals(-1.0f, CharacterFunctions.abilityCooldownFunction("not_an_ability", false));
        assertEquals(-1, CharacterFunctions.ophanimOrb(-1));
        assertEquals(-1, CharacterFunctions.mirrorImageCloneFunction(-1));
        assertEquals("", CharacterFunctions.personalObjectiveGoalFunction(-1));
        assertFalse(CharacterFunctions.personalObjectiveEventBonusFunction(-1));
        assertNotNull(CharacterFunctions.personalObjectiveScoreFunction(-1));
        assertNotNull(CharacterFunctions.equippedAspectFunction(-1));
    }
}
