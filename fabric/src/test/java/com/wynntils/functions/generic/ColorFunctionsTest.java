package com.wynntils.functions.generic;

import com.wynntils.core.WynntilsMod;
import com.wynntils.functions.ActivityFunctions;
import com.wynntils.templates.annotations.TemplateFunction;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.colors.WynncraftShaderColor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Modifier;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class ColorFunctionsTest {
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
    void createsColorsFromRgbAndHex() {
        CustomColor rgb = ColorFunctions.fromRgbFunction(12, 34, 56);
        CustomColor hex = ColorFunctions.fromHexFunction(rgb.toHexString());

        assertEquals(rgb, hex);
        assertEquals(rgb.toHexString(), ColorFunctions.toHexStringFunction(rgb));
    }

    @Test
    void createsColorFromPercentages() {
        assertEquals(new CustomColor(0.25f, 0.5f, 0.75f),
                ColorFunctions.fromRgbPercentFunction(0.25, 0.5, 0.75));
    }

    @Test
    void returnsKnownShaderColors() {
        assertEquals(WynncraftShaderColor.RAINBOW.color, ColorFunctions.rainbowShaderFunction());
        assertEquals(WynncraftShaderColor.GRADIENT.color, ColorFunctions.gradientShaderFunction());
        assertEquals(WynncraftShaderColor.GRADIENT_2.color, ColorFunctions.gradientShaderFunction(2));
        assertEquals(WynncraftShaderColor.FADE.color, ColorFunctions.fadeShaderFunction());
        assertEquals(WynncraftShaderColor.BLINK.color, ColorFunctions.blinkShaderFunction());
        assertEquals(WynncraftShaderColor.SHINE.color, ColorFunctions.shineShaderFunction());
    }

    @Test
    void unknownShaderReturnsNone() {
        assertEquals(CustomColor.NONE, ColorFunctions.wynncraftShaderFunction("definitely_not_a_shader"));
    }

    @Test
    void colorShiftsReturnColors() {
        CustomColor color = new CustomColor(100, 120, 140);

        assertNotNull(ColorFunctions.hueShiftFunction(color, 30));
        assertNotNull(ColorFunctions.saturationShiftFunction(color, 0.2));
        assertNotNull(ColorFunctions.brightnessShiftFunction(color, 0.2));
    }
}
