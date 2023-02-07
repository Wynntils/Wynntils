/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.gear.parsing;

import com.wynntils.models.concepts.Powder;
import com.wynntils.models.gear.type.GearTier;
import com.wynntils.models.gear.type.GearType;
import com.wynntils.models.stats.type.StatActualValue;
import java.util.List;

// Note that "rerolls" and "durabilityCurrent" actually reflect the same value.
// Its interpretation is determined what kind of item where parsed.
public record GearParseResult(
        GearTier tier,
        GearType gearType,
        List<StatActualValue> identifications,
        List<Powder> powders,
        int rerolls,
        int durabilityCurrent,
        int durabilityMax) {}
