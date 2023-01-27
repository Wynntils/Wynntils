/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.gearinfo;

import com.wynntils.models.gear.type.GearTier;
import com.wynntils.models.gear.type.GearType;
import com.wynntils.models.gearinfo.types.GearStat;
import com.wynntils.models.gearinfo.types.GearStatPossibleValues;
import com.wynntils.utils.type.Pair;
import java.util.List;

public record GearInfo(
        String name,
        GearType type,
        GearTier tier,
        int powderSlots,
        GearMetaInfo metaInfo,
        GearRequirements requirements,
        GearStatsFixed statsFixed,
        List<Pair<GearStat, GearStatPossibleValues>> statsIdentified) {}
