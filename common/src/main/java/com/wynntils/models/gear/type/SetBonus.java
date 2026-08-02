/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.gear.type;

import com.wynntils.models.stats.type.StatType;
import java.util.List;
import java.util.Map;

public record SetBonus(List<String> major, Map<StatType, Integer> minor) {
    public static final SetBonus EMPTY = new SetBonus(List.of(), Map.of());
}
