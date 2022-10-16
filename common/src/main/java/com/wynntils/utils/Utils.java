/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Random;
import net.minecraft.Util;

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

    /**
     * Open the specified URL in the user's browser if possible, otherwise copy it to the clipboard
     * and send it to chat.
     * @param url The url to open
     */
    public static void openUrl(String url) {
        Util.getPlatform().openUri(url);
    }

    public static String encodeUrl(String url) {
        return URLEncoder.encode(url, StandardCharsets.UTF_8);
    }
}
