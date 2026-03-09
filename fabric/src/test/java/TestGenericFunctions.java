/*
 * Copyright © Wynntils 2025-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Managers;
import com.wynntils.core.text.StyledText;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;

public class TestGenericFunctions {
    @BeforeAll
    public static void setup() {
        WynntilsMod.setupTestEnv();
        java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("UTC"));
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

        assertTemplateResult("{abs(-12.5)}", "12.5");
        assertTemplateResult("{floor(3.9)}", "3");
        assertTemplateResult("{ceil(3.1)}", "4");
        assertTemplateResult("{clamp(50;0;10)}", "10");
        assertTemplateResult("{safe_div(10;0;99)}", "99");
        assertTemplateResult("{map(15;10;20;0;100)}", "50");
        assertTemplateResult("{wrap(-1;0;10)}", "9");

        assertTemplateResult("{equals(round(ln(e());6);1)}", "true");
        assertTemplateResult("{equals(round(log(1000;10);6);3)}", "true");
        assertTemplateResult("{equals(round(pi();6);3.141593)}", "true");
        assertTemplateResult("{equals(round(e();6);2.718282)}", "true");

        assertTemplateResult("{dec_to_hex(48879)}", "BEEF");
        assertTemplateResult("{hex_to_dec(\"BEEF\")}", "48879");
        assertTemplateResult("{hex_to_dec(\"0x10\")}", "16");
        assertTemplateResult("{hex_to_dec(\"#10\")}", "16");
        assertTemplateResult("{hex_to_dec(\"invalid\")}", "0");

        assertTemplateResult("{is_finite(123)}", "true");
        assertTemplateResult("{is_finite(div(1;0))}", "false");
        assertTemplateResult("{is_nan(div(0;0))}", "true");
        assertTemplateResult("{is_infinite(div(1;0))}", "true");
    }

    @Test
    public void testLongFunctionsFormatting() {
        assertTemplateResult("{long(42)}", "42");
        assertTemplateResult("{parse_long(\"12345678900\")}", "12345678900");
        assertTemplateResult("{parse_long(\"invalid\")}", "0");
        assertTemplateResult("{timestamp(time(0))}", "0");
    }

    @DisabledIfEnvironmentVariable(named = "CI", matches = ".+")
    @Test
    public void testTimeFunctions() {
        assertTemplateResult("{now}", "now");

        // Test Time.NONE
        assertTemplateResult("{absolute_time(time(-1))}", "");

        assertTemplateResult("{absolute_time(time(0))}", "1970-01-01 00:00");
        assertTemplateResult("{absolute_time(offset(time(0);60))}", "1970-01-01 00:01");

        assertTemplateResult("{timestamp(time(0))}", "0");

        assertTemplateResult("{offset(now;-1)}", "1 second ago");
        assertTemplateResult("{offset(now;-125)}", "2 minutes ago");
        assertTemplateResult("{time_offset(now;190)}", "in 3 minutes");

        assertTemplateResult("{seconds_between(now;offset(now;5))}", "5");
        assertTemplateResult("{seconds_between(offset(now;60);now)}", "-60");

        assertTemplateResult("{seconds_since(offset(now;-17))}", "17");

        long then = System.currentTimeMillis() - 10000;
        assertTemplateResult("{time(" + then + ")}", "10 seconds ago");
        long soon = System.currentTimeMillis() + 60000 + 500; // add a bit of a margin
        assertTemplateResult("{time(" + soon + ")}", "in 1 minute");

        assertTemplateResult("{concat(time_str(now);time_str(now))}", "nownow");
    }

    @Test
    public void testConditions() {
        assertTemplateResult("{if(equals(3;3);add(1;1);add(2;2))}", "2.00");
        assertTemplateResult("{if(equals(2;3);add(1;1);add(2;2))}", "4.00");
        assertTemplateResult("{if(equals(3;3);\"hej\";add(2;2))}", "hej");
        assertTemplateResult("{cap(if(equals(3;3);capped(1;2);capped(3;4)))}", "2");
    }
}
