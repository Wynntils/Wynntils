/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.colors;

import java.util.Locale;

public enum WynncraftShaderTriggerColors {
    BLINK(0x00f00c), // Animated blinking which toggles between #e63232 and transparent
    FADE(0x00f008), // Animated fade from #5af082 to transparent
    FADE_2(0x00f018), // Animated fade effect from a vertical gradient to transparent
    GRADIENT(0x00f004), // Animated gradient from #f56217 to #0b486b
    GRADIENT_2(0x00f010), // Animated gradient from #560505ff to #8a0303ff
    ITALIC(0x00f01c), // "Italic" effect which displaces characters and colors them to #55ffff
    ITALIC_2(0x00f020), // "Italic" effect which displaces characters and colors them to #55ffff
    RAINBOW(0x00f000), // Animated rainbow
    SHINE(0x00f014), // Animated shine effect between #a3cc52ff to #ffffd2ff
    WARP(0x00f024); // Animated Warp effect which makes the text look like wave and color it to #c6c6c6

    public final CustomColor color;

    WynncraftShaderTriggerColors(int intColor) {
        this.color = CustomColor.fromInt(intColor);
    }

    public static WynncraftShaderTriggerColors fromString(String name) {
        try {
            return valueOf(name.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
