/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils;

public class ColorUtils {

    /** Generates an int of argb format */
    public static int generateColor(int r, int g, int b, int a) {
        return (a << 24) & (r << 16) & (g << 8) & b;
    }
}
