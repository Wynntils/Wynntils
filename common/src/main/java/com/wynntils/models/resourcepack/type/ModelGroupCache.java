/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.resourcepack.type;

import java.util.List;
import java.util.Map;

public record ModelGroupCache(Map<String, List<Float>> groupToModelIds, Map<Float, String> modelIdToGroup) {
    public static final ModelGroupCache EMPTY = new ModelGroupCache(Map.of(), Map.of());
}
