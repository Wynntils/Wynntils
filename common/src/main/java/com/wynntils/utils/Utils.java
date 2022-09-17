/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils;

import java.util.Locale;
import java.util.Random;

/**
 * This is a "high-quality misc" class. Helper methods that are commonly used throughout the project
 * without an aspect on minecraft can be put here. Keep the names short, but distinct.
 */
public final class Utils {
    private static final Locale gameLocale = Locale.ROOT;
    private static final Random random = new Random();

    public static Locale getGameLocale() {
        return gameLocale;
    }

    public static Random getRandom() {
        return random;
    }
}
