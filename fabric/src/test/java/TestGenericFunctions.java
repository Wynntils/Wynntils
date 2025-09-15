/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Managers;
import com.wynntils.core.text.StyledText;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class TestGenericFunctions {
    @BeforeAll
    public static void setup() {
        WynntilsMod.setupTestEnv();
    }

    private static void assertTemplateResult(String template, String expected) {
        StyledText[] result = Managers.Function.doFormatLines(template);

        Assertions.assertEquals(1, result.length, "Too many lines");
        String str = result[0].getString();
        Assertions.assertEquals(expected, str, "Incorrect template result");
    }

    @Test
    public void testMathFunctions() {
        assertTemplateResult("{int(add(5;6))}", "11");
    }

    @Test
    public void testTimeFunctions() {
        assertTemplateResult("{now}", "now");
        assertTemplateResult("{absolute_time(time(0))}", "1970-01-01 01:00");
        assertTemplateResult("{absolute_time(offset(time(0);60))}", "1970-01-01 01:01");

        // FIXME: why are longs returned as decimal numbers?
        assertTemplateResult("{timestamp(time(0))}", "0.00");

        assertTemplateResult("{offset(now;1)}", "in 1 second");
        assertTemplateResult("{offset(now;-125)}", "2 minutes ago");

        long then = System.currentTimeMillis() - 10000;
        assertTemplateResult("{time(" + then + ")}", "10 seconds ago");
        long soon = System.currentTimeMillis() + 60000;
        assertTemplateResult("{time(" + soon + ")}", "in 1 minute");
    }
}
