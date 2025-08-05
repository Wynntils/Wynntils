/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.colors;

public final class CommonColors {
    public static final CustomColor BLACK = CustomColor.fromInt(0x000000);
    public static final CustomColor RED = CustomColor.fromInt(0xff0000);
    public static final CustomColor GREEN = CustomColor.fromInt(0x00ff00);
    public static final CustomColor BLUE = CustomColor.fromInt(0x0000ff);
    public static final CustomColor YELLOW = CustomColor.fromInt(0xffff00);
    public static final CustomColor BROWN = CustomColor.fromInt(0x563100);
    public static final CustomColor PURPLE = CustomColor.fromInt(0xb200ff);
    public static final CustomColor CYAN = CustomColor.fromInt(0x438e82);
    public static final CustomColor AQUA = CustomColor.fromInt(0x00ffff);
    public static final CustomColor DARK_AQUA = CustomColor.fromInt(0x00cccc);
    public static final CustomColor LIGHT_GRAY = CustomColor.fromInt(0xadadad);
    public static final CustomColor GRAY = CustomColor.fromInt(0x636363);
    public static final CustomColor DARK_GRAY = CustomColor.fromInt(0x101010);
    public static final CustomColor TITLE_GRAY = CustomColor.fromInt(0x404040);
    public static final CustomColor PINK = CustomColor.fromInt(0xffb7b7);
    public static final CustomColor LIGHT_GREEN = CustomColor.fromInt(0x49ff59);
    public static final CustomColor LIGHT_BLUE = CustomColor.fromInt(0x00e9ff);
    public static final CustomColor MAGENTA = CustomColor.fromInt(0xff0083);
    public static final CustomColor ORANGE = CustomColor.fromInt(0xff9000);
    public static final CustomColor WHITE = CustomColor.fromInt(0xffffff);

    // Wynncraft's custom effects done via resourcepack shaders

    // Animated rainbow
    public static final CustomColor RAINBOW = CustomColor.fromInt(0x00f000);
    // Animated gradient from #f56217 to #0b486b
    public static final CustomColor GRADIENT = CustomColor.fromInt(0x00f004);
    // Smooth animated fade from #5af082 to black
    public static final CustomColor FADE = CustomColor.fromInt(0x00f008);
    // Animated blinking between #e63232 and black
    public static final CustomColor BLINK = CustomColor.fromInt(0x00f00c);
    // Animated gradient from #560505ff to #8a0303ff
    public static final CustomColor GRADIENT_2 = CustomColor.fromInt(0x00f010);
    // Animated shine effect between #a3cc52ff to #ffffd2ff
    public static final CustomColor SHINE = CustomColor.fromInt(0x00f014);
}
