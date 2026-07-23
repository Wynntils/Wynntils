package com.wynntils.functions;

import com.wynntils.core.WynntilsMod;
import com.wynntils.templates.annotations.TemplateFunction;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class RaidFunctionsTest {

    @BeforeAll
    public static void setup() {
        WynntilsMod.setupTestEnv();
    }

    @Test
    void allPublicStaticMethodsAreTemplateFunctions() {
        Arrays.stream(RaidFunctions.class.getDeclaredMethods())
                .filter(method -> Modifier.isPublic(method.getModifiers()))
                .filter(method -> Modifier.isStatic(method.getModifiers()))
                .forEach(method -> assertNotNull(
                        method.getAnnotation(TemplateFunction.class),
                        () -> method.getName() + " is missing @TemplateFunction"));
    }

    @Test
    void absentRaidUsesSafeFallbacks() {
        assertNotNull(RaidFunctions.currentRaidFunction());
        assertNotNull(RaidFunctions.currentRaidRoomNameFunction());
        assertNotNull(RaidFunctions.currentRaidStartFunction());
        assertNotNull(RaidFunctions.currentRaidRoomStartFunction());
        assertNotNull(RaidFunctions.raidChallengesFunction());
    }

    @Test
    void invalidGambitIndexesReturnEmptyStrings() {
        assertNotNull(RaidFunctions.chosenGambitFunction(-1));
        assertNotNull(RaidFunctions.chosenBuffFunction(-1));
    }
}
