/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.containers.type;

import com.wynntils.models.gear.type.GearTier;
import com.wynntils.utils.mc.type.Location;
import java.util.Map;

public record MythicFind(
        String itemName,
        int chestCount,
        int dryCount,
        int dryBoxes,
        int dryEmeralds, // Added in later patch
        Map<GearTier, Integer> dryItemTiers, // Added in later patch
        long timestamp,
        Location chestCoordinate) {}
