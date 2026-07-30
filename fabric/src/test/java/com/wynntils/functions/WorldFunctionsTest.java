package com.wynntils.functions;

import com.wynntils.core.WynntilsMod;
import com.wynntils.templates.annotations.TemplateFunction;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Modifier;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class WorldFunctionsTest {

    @BeforeAll
    public static void setup() {
        WynntilsMod.setupTestEnv();
    }

    @Test
    void allPublicStaticMethodsAreTemplateFunctions() {
        Arrays.stream(WorldFunctions.class.getDeclaredMethods())
                .filter(method -> Modifier.isPublic(method.getModifiers()))
                .filter(method -> Modifier.isStatic(method.getModifiers()))
                .forEach(method -> assertNotNull(
                        method.getAnnotation(TemplateFunction.class),
                        () -> method.getName() + " is missing @TemplateFunction"));
    }

    @Test
    void missingTotemsReturnFallbackValues() {
        assertEquals("", WorldFunctions.gatheringTotemOwnerFunction(0));
        assertEquals(0.0d, WorldFunctions.gatheringTotemDistanceFunction(0));
        assertNotNull(WorldFunctions.gatheringTotemFunction(0));
        assertEquals("", WorldFunctions.gatheringTotemTimeLeftFunction(0));

        assertEquals("", WorldFunctions.mobTotemOwnerFunction(0));
        assertEquals(0.0d, WorldFunctions.mobTotemDistanceFunction(0));
        assertNotNull(WorldFunctions.mobTotemFunction(0));
        assertEquals("", WorldFunctions.mobTotemTimeLeftFunction(0));
    }

    @Test
    void invalidGatekeeperIndexesReturnFallbackValues() {
        assertNotNull(WorldFunctions.tokenGatekeeperDepositedFunction(0));
        assertNotNull(WorldFunctions.tokenGatekeeperFunction(0));
        assertEquals("", WorldFunctions.tokenGatekeeperTypeFunction(0));
    }

    @Test
    void worldFunctionsReturnNonNullValues() {
        assertNotNull(WorldFunctions.currentWorldFunction());
        assertNotNull(WorldFunctions.worldUptimeFunction(""));
        assertNotNull(WorldFunctions.newestWorldFunction());
        assertNotNull(WorldFunctions.worldStateFunction());
    }
}
