package com.wynntils.functions.generic;

import com.wynntils.core.WynntilsMod;
import com.wynntils.functions.ActivityFunctions;
import com.wynntils.templates.annotations.TemplateFunction;
import com.wynntils.utils.type.Time;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Modifier;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class TimeFunctionsTest {
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
    void createsAndReadsTime() {
        Time time = TimeFunctions.timeFunction(1_700_000_000L);

        assertEquals(1_700_000_000L, TimeFunctions.timestampFunction(time));
        assertEquals(time.toString(), TimeFunctions.timeStringFunction(time));
        assertEquals(time.toAbsoluteString(), TimeFunctions.absoluteTimeFunction(time));
    }

    @Test
    void calculatesOffsets() {
        Time first = Time.of(1_000L);
        Time second = Time.of(1_060L);

        assertEquals(first.getOffset(second), TimeFunctions.secondsBetweenFunction(second, first));
        assertEquals(first.offset(30).timestamp(), TimeFunctions.timeOffsetFunction(first, 30).timestamp());
    }

    @Test
    void secondsSinceUsesCurrentTime() {
        Time time = Time.now();
        long offset = TimeFunctions.secondsSinceFunction(time);

        assertTrue(Math.abs(offset) <= 1);
    }

    @Test
    void formatsTimeWithCustomPattern() {
        Time epoch = Time.of(0L);

        assertNotNull(TimeFunctions.formatTimeAdvancedFunction(epoch, "yyyy-MM-dd"));
        assertEquals("Invalid Format", TimeFunctions.formatTimeAdvancedFunction(epoch, "'"));
    }
}
