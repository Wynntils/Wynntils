/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.containers.type;

import com.wynntils.models.gear.type.GearTier;
import com.wynntils.utils.mc.type.Location;
import com.wynntils.utils.type.RangedValue;
import java.util.Map;

public record MythicFind(
        String itemName,
        RangedValue levelRange, // Added in later patch
        int chestCount,
        int dryCount,
        int dryBoxes,
        int dryEmeralds, // Added in later patch
        Map<GearTier, Integer> dryItemTiers, // Added in later patch
        Location chestCoordinate,
        long timestamp) {}
