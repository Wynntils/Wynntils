package com.wynntils.functions.generic;

import com.wynntils.core.WynntilsMod;
import com.wynntils.functions.ActivityFunctions;
import com.wynntils.templates.annotations.TemplateFunction;
import com.wynntils.utils.StringUtils;
import com.wynntils.utils.type.CappedValue;
import com.wynntils.utils.type.RangedValue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Modifier;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class StringFunctionsTest {
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
    void formatsValues() {
        assertEquals(StringUtils.integerToShortString(1_500), StringFunctions.formatFunction(1_500));
        assertEquals(StringUtils.integerToShortString(1_500) + "/" + StringUtils.integerToShortString(2_000),
                StringFunctions.formatCappedFunction(new CappedValue(1_500, 2_000)));
        assertEquals(StringUtils.integerToShortString(1_500) + "-" + StringUtils.integerToShortString(2_000),
                StringFunctions.formatRangedFunction(new RangedValue(1_500, 2_000)));
        assertEquals(StringUtils.formatDuration(90), StringFunctions.formatDurationFunction(90));
        assertEquals(StringUtils.formatDateTime(0), StringFunctions.formatDateFunction(0));
    }

    @Test
    void convertsAndCombinesStrings() {
        assertEquals("123", StringFunctions.stringFunction(123));
        assertEquals("hello world", StringFunctions.concatFunction("hello ", "world"));
        assertEquals("abc", StringFunctions.concatFunction("a", "b", "c"));
        assertEquals("", StringFunctions.concatFunction(new String[0]));
        assertTrue(StringFunctions.stringEqualsFunction("same", "same"));
        assertFalse(StringFunctions.stringEqualsFunction("same", "different"));
        assertTrue(StringFunctions.stringContainsFunction("abcdef", "cde"));
    }

    @Test
    void parsesNumbersWithFallbacks() {
        assertEquals(123, StringFunctions.parseIntegerFunction("123"));
        assertEquals(0, StringFunctions.parseIntegerFunction("nope"));
        assertEquals(1234567890123L, StringFunctions.parseLongFunction("1234567890123"));
        assertEquals(0L, StringFunctions.parseLongFunction("nope"));
        assertEquals(12.5, StringFunctions.parseDoubleFunction("12.5"));
        assertEquals(0.0, StringFunctions.parseDoubleFunction("nope"));
    }

    @Test
    void repeatsAndPadsStrings() {
        assertEquals("ababab", StringFunctions.repeat("ab", 3));
        assertEquals("", StringFunctions.repeat("ab", -1));
        assertEquals("3/10", StringFunctions.cappedStringFunction(new CappedValue(3, 10), "/"));
        assertEquals("00042", StringFunctions.leadingZerosFunction(42, 5));
    }

    @Test
    void handlesRegularExpressions() {
        assertTrue(StringFunctions.regexMatchFunction("abc123", "[a-z]+\\d+"));
        assertFalse(StringFunctions.regexMatchFunction("abc", "["));
        assertTrue(StringFunctions.regexFindFunction("abc123", "\\d+"));
        assertFalse(StringFunctions.regexFindFunction("abc", "["));
        assertEquals("abc#", StringFunctions.regexReplaceFunction("abc123", "\\d+", "#"));
        assertNotNull(StringFunctions.regexReplaceFunction("abc", "[", "x"));
    }

    @Test
    void convertsRomanNumeralsAndCodepoints() {
        assertEquals("XIV", StringFunctions.toRomanNumeralsFunction(14));
        assertEquals("😀", StringFunctions.fromCodepointFunction(0x1F600));
        assertEquals("Invalid Codepoint", StringFunctions.fromCodepointFunction(-1));
    }
}
