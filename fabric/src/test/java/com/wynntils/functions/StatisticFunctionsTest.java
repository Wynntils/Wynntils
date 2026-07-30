package com.wynntils.functions;

import com.wynntils.core.WynntilsMod;
import com.wynntils.templates.annotations.TemplateFunction;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class StatisticFunctionsTest {

    @BeforeAll
    public static void setup() {
        WynntilsMod.setupTestEnv();
    }

    @Test
    void allPublicStaticMethodsAreTemplateFunctions() {
        Arrays.stream(StatisticFunctions.class.getDeclaredMethods())
                .filter(method -> Modifier.isPublic(method.getModifiers()))
                .filter(method -> Modifier.isStatic(method.getModifiers()))
                .forEach(method -> assertNotNull(
                        method.getAnnotation(TemplateFunction.class),
                        () -> method.getName() + " is missing @TemplateFunction"));
    }

    @Test
    void invalidStatisticKeysReturnFallbackValues() {
        assertEquals(0L, StatisticFunctions.statisticsTotalFunction("not_a_statistic", false));
        assertEquals(0L, StatisticFunctions.statisticsCountFunction("not_a_statistic", false));
        assertEquals(0L, StatisticFunctions.statisticsMinFunction("not_a_statistic", false));
        assertEquals(0L, StatisticFunctions.statisticsMaxFunction("not_a_statistic", false));
        assertEquals(0L, StatisticFunctions.statisticsAverageFunction("not_a_statistic", false));
        assertEquals(0L, StatisticFunctions.statisticsFirstModifiedFunction("not_a_statistic", false));
        assertEquals(0L, StatisticFunctions.statisticsLastModifiedFunction("not_a_statistic", false));
        assertNotNull(StatisticFunctions.statisticsFirstModifiedTimeFunction("not_a_statistic", false));
        assertNotNull(StatisticFunctions.statisticsLastModifiedTimeFunction("not_a_statistic", false));
    }
}
