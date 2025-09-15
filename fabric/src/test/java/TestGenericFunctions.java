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
    public void math_testAddInt() {
        assertTemplateResult("{int(add(5;6))}", "11");
    }
}
