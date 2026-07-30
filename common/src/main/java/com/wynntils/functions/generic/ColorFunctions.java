/*
 * Copyright © Wynntils 2025-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions.generic;

import com.wynntils.templates.annotations.TemplateFunction;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.colors.WynncraftShaderColor;

@SuppressWarnings("unused") // Functions are accessed via reflection
public class ColorFunctions {
    @TemplateFunction(name = "from_rgb", isPure = true)
    public static CustomColor fromRgbFunction(int r, int g, int b) {
        return new CustomColor(r, g, b);
    }

    @TemplateFunction(name = "from_rgb_percent", isPure = true)
    public static CustomColor fromRgbPercentFunction(double r, double g, double b) {
        return new CustomColor((float) r, (float) g, (float) b);
    }

    @TemplateFunction(name = "from_hex", isPure = true)
    public static CustomColor fromHexFunction(String hex) {
        return CustomColor.fromHexString(hex);
    }

    @TemplateFunction(name = "hue_shift")
    public static CustomColor hueShiftFunction(CustomColor color, double degree) {
        return color.hueShift((float) degree);
    }

    @TemplateFunction(name = "saturation_shift")
    public static CustomColor saturationShiftFunction(CustomColor color, double degree) {
        return color.saturationShift((float) degree);
    }

    @TemplateFunction(name = "brightness_shift")
    public static CustomColor brightnessShiftFunction(CustomColor color, double degree) {
        return color.brightnessShift((float) degree);
    }

    @TemplateFunction(name = "rainbow_shader", isPure = true)
    public static CustomColor rainbowShaderFunction() {
        return WynncraftShaderColor.RAINBOW.color;
    }

    @TemplateFunction(name = "gradient_shader", isPure = true)
    public static CustomColor gradientShaderFunction() {
        return gradientShaderFunction(1);
    }

    @TemplateFunction(name = "gradient_shader", isPure = true)
    public static CustomColor gradientShaderFunction(int style) {
        return switch (style) {
            case 2 -> WynncraftShaderColor.GRADIENT_2.color;
            default -> WynncraftShaderColor.GRADIENT.color;
        };
    }

    @TemplateFunction(name = "fade_shader", isPure = true)
    public static CustomColor fadeShaderFunction() {
        return WynncraftShaderColor.FADE.color;
    }

    @TemplateFunction(name = "blink_shader", isPure = true)
    public static CustomColor blinkShaderFunction() {
        return WynncraftShaderColor.BLINK.color;
    }

    @TemplateFunction(name = "shine_shader", isPure = true)
    public static CustomColor shineShaderFunction() {
        return WynncraftShaderColor.SHINE.color;
    }

    @TemplateFunction(name = "to_hex_string")
    public static String toHexStringFunction(CustomColor color) {
        return color.toHexString();
    }

    @TemplateFunction(name = "wynncraft_shader", isPure = true)
    public static CustomColor wynncraftShaderFunction(String shaderName) {
        WynncraftShaderColor shaderColor = WynncraftShaderColor.fromString(shaderName);
        return shaderColor == null ? CustomColor.NONE : shaderColor.color;
    }
}
