package com.wynntils.functions.generic;

import com.wynntils.core.WynntilsMod;
import com.wynntils.functions.ActivityFunctions;
import com.wynntils.templates.annotations.TemplateFunction;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Modifier;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class MathFunctionsTest {
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
    void performsBasicArithmetic() {
        assertEquals(5.0, MathFunctions.addFunction(2, 3));
        assertEquals(10.0, MathFunctions.addFunction(1, 2, 3, 4));
        assertEquals(-1.0, MathFunctions.subtractFunction(2, 3));
        assertEquals(6.0, MathFunctions.multiplyFunction(2, 3));
        assertEquals(24.0, MathFunctions.multiplyFunction(2, 3, 4));
        assertEquals(2.5, MathFunctions.divideFunction(5, 2));
        assertEquals(1.0, MathFunctions.moduloFunction(7, 3));
        assertEquals(8.0, MathFunctions.powerFunction(2, 3));
        assertEquals(3.0, MathFunctions.squareRootFunction(9));
    }

    @Test
    void findsMinimumAndMaximum() {
        assertEquals(5.0, MathFunctions.maxFunction(5, 2));
        assertEquals(9.0, MathFunctions.maxFunction(2, 9, 4));
        assertEquals(2.0, MathFunctions.minFunction(5, 2));
        assertEquals(2.0, MathFunctions.minFunction(5, 2, 9));
        assertEquals(0.0, MathFunctions.maxFunction(new double[0]));
        assertEquals(0.0, MathFunctions.minFunction(new double[0]));
    }

    @Test
    void roundsAndConvertsNumbers() {
        assertEquals(12.35, MathFunctions.roundFunction(12.345, 2), 1e-9);
        assertEquals(12, MathFunctions.integerFunction(12.9));
        assertEquals(12L, MathFunctions.longFunction(12.9));
        assertEquals(3.0, MathFunctions.absFunction(-3));
        assertEquals(3L, MathFunctions.floorFunction(3.9));
        assertEquals(4L, MathFunctions.ceilFunction(3.1));
    }

    @Test
    void randomValueStaysInsideRequestedRange() {
        for (int i = 0; i < 100; i++) {
            double value = MathFunctions.randomFunction(-5, 8);
            assertTrue(value >= -5 && value < 8);
        }
    }

    @Test
    void clampsMapsAndWrapsValues() {
        assertEquals(5.0, MathFunctions.clampFunction(5, 0, 10));
        assertEquals(0.0, MathFunctions.clampFunction(-5, 0, 10));
        assertEquals(10.0, MathFunctions.clampFunction(15, 10, 0));
        assertEquals(50.0, MathFunctions.mapFunction(5, 0, 10, 0, 100));
        assertEquals(20.0, MathFunctions.mapFunction(5, 1, 1, 20, 40));
        assertEquals(2.0, MathFunctions.wrapFunction(12, 0, 10));
        assertEquals(8.0, MathFunctions.wrapFunction(-2, 0, 10));
        assertEquals(4.0, MathFunctions.wrapFunction(10, 4, 4));
    }

    @Test
    void safelyDivides() {
        assertEquals(2.5, MathFunctions.safeDivideFunction(5, 2, -1));
        assertEquals(-1.0, MathFunctions.safeDivideFunction(5, 0, -1));
    }

    @Test
    void calculatesLogarithmsAndConstants() {
        assertEquals(1.0, MathFunctions.naturalLogFunction(Math.E), 1e-9);
        assertEquals(3.0, MathFunctions.logFunction(8, 2), 1e-9);
        assertEquals(Math.PI, MathFunctions.piFunction());
        assertEquals(Math.E, MathFunctions.eulerFunction());
    }

    @Test
    void convertsDecimalAndHexadecimal() {
        assertEquals("FF", MathFunctions.decToHexFunction(255));
        assertEquals("-FF", MathFunctions.decToHexFunction(-255));
        assertEquals("-8000000000000000", MathFunctions.decToHexFunction(Long.MIN_VALUE));

        assertEquals(255L, MathFunctions.hexToDecFunction("FF"));
        assertEquals(255L, MathFunctions.hexToDecFunction("0xFF"));
        assertEquals(255L, MathFunctions.hexToDecFunction("#FF"));
        assertEquals(-255L, MathFunctions.hexToDecFunction("-0xFF"));
        assertEquals(0L, MathFunctions.hexToDecFunction("not hex"));
        assertEquals(0L, MathFunctions.hexToDecFunction("FFFFFFFFFFFFFFFF"));
    }

    @Test
    void identifiesSpecialFloatingPointValues() {
        assertTrue(MathFunctions.isFiniteFunction(1.0));
        assertFalse(MathFunctions.isFiniteFunction(Double.POSITIVE_INFINITY));
        assertTrue(MathFunctions.isNanFunction(Double.NaN));
        assertTrue(MathFunctions.isInfiniteFunction(Double.NEGATIVE_INFINITY));
    }
}
