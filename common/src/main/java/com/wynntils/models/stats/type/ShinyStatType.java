/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.stats.type;

public record ShinyStatType(int id, String key, String displayName, StatUnit statUnit) {
    public static final ShinyStatType UNKNOWN = new ShinyStatType(0, "unknown", "Unknown", StatUnit.RAW);
}
