/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.gear.type;

import com.wynntils.models.stats.type.StatType;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Map;

// wynncraftCount can eventually be removed when Wynncraft fixes their double ring bug...
// Essentially if you have two of the same ring on, it's only counted once
public record SetInstance(SetInfo setInfo, int wynncraftCount, int trueCount, Map<StatType, Integer> trueCountBonuses) {
}
