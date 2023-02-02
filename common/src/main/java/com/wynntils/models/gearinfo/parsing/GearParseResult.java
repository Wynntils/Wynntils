/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.gearinfo.parsing;

import com.wynntils.models.concepts.Powder;
import com.wynntils.models.gearinfo.type.GearTier;
import com.wynntils.models.gearinfo.type.GearType;
import com.wynntils.models.stats.type.StatActualValue;
import java.util.List;

// "tierCount" depends on what item were parsed. For identified items, it is the
// number of rerolls. For crafted items, it is the current durability
public record GearParseResult(
        GearTier tier,
        GearType gearType,
        List<StatActualValue> identifications,
        List<Powder> powders,
        int tierCount,
        int durabilityMax) {}
