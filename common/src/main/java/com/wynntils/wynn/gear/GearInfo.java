/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.gear;

import com.wynntils.wynn.objects.profiles.item.GearTier;
import com.wynntils.wynn.objects.profiles.item.GearType;

public record GearInfo(
        String name,
        GearType type,
        GearTier tier,
        int powderSlots,
        boolean idsAreFixed,
        GearMetaInfo metaInfo,
        GearRequirements requirements,
        GearStatsFixed statsFixed,
        GearStatsIdentified statsIdentified) {}
