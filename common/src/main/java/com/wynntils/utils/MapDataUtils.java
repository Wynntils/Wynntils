/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils;

import java.util.Locale;

public final class MapDataUtils {
    public static String sanitizeFeatureId(String featureId) {
        return featureId.replace(" ", "-").replaceAll("[^a-zA-Z\\-]", "").toLowerCase(Locale.ROOT);
    }
}
