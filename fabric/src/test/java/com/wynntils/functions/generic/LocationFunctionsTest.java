package com.wynntils.functions.generic;

import com.wynntils.core.WynntilsMod;
import com.wynntils.functions.ActivityFunctions;
import com.wynntils.templates.annotations.TemplateFunction;
import com.wynntils.utils.mc.type.Location;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Modifier;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class LocationFunctionsTest {
    @BeforeAll
    public static void setup() {
        WynntilsMod.setupTestEnv();
    }

    @Test
    void allPublicStaticMethodsAreTemplateFunctions() {
        Arrays.stream(ActivityFunctions.class.getDeclaredMethods())
                .filter(method -> Modifier.isPublic(method.getModifiers()))
                .filter(method -> Modifier.isStatic(method.getModifiers()))
                .forEach(method -> assertNotNull(
                        method.getAnnotation(TemplateFunction.class),
                        () -> method.getName() + " is missing @TemplateFunction"));
    }

    @Test
    void createsAndReadsLocation() {
        Location location = LocationFunctions.locationFunction(10, 20, 30);

        assertEquals(10, LocationFunctions.xFunction(location));
        assertEquals(20, LocationFunctions.yFunction(location));
        assertEquals(30, LocationFunctions.zFunction(location));
    }

    @Test
    void calculatesThreeDimensionalDistance() {
        Location from = new Location(0, 0, 0);
        Location to = new Location(3, 4, 12);

        assertEquals(13.0, LocationFunctions.distanceFunction(from, to), 1e-9);
    }
}
