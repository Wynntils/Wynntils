/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.gear2;

import com.wynntils.models.gear.type.GearTier;
import com.wynntils.models.gear.type.GearType;
import com.wynntils.models.gear2.types.GearStat;
import com.wynntils.utils.type.Pair;
import com.wynntils.utils.type.RangedValue;
import java.util.List;

public record GearInfo(
        String name,
        GearType type,
        GearTier tier,
        int powderSlots,
        GearMetaInfo metaInfo,
        GearRequirements requirements,
        GearStatsFixed statsFixed,
        List<Pair<GearStat, RangedValue>> statsIdentified) {}
