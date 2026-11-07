/*
 * Copyright © Wynntils 2024-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.gear.type;

import com.wynntils.models.stats.type.StatType;
import java.util.List;
import java.util.Map;
import net.minecraft.network.chat.Component;

/**
 * Represents an "instance" of a unique set worn.
 */
public record SetInstance(
        int equippedCount, int totalCount, Map<StatType, Integer> bonuses, String name, List<Component> majorIdLines) {
    public SetInstance(int equippedCount, int totalCount, Map<StatType, Integer> bonuses, String name) {
        this(equippedCount, totalCount, bonuses, name, List.of());
    }
}
