/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.rewards.type;

import com.wynntils.models.gear.type.GearMetaInfo;
import com.wynntils.models.gear.type.GearTier;
import com.wynntils.models.stats.type.StatPossibleValues;
import com.wynntils.models.stats.type.StatType;
import com.wynntils.utils.type.Pair;
import java.util.List;

// A note about baseStats and variableStats:
// baseStats are the stats that are only affected within the CharmRequirement's level range.
// baseStats looks just like variableStats, when displayed in-game.
// variableStats are the stats that are regular identifications.
public record CharmInfo(
        String name,
        GearTier tier,
        GearMetaInfo metaInfo,
        CharmRequirements requirements,
        List<Pair<StatType, StatPossibleValues>> baseStats,
        List<Pair<StatType, StatPossibleValues>> variableStats) {}
