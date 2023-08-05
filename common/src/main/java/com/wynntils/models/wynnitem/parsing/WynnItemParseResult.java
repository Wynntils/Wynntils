/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.wynnitem.parsing;

import com.wynntils.models.elements.type.Powder;
import com.wynntils.models.gear.type.GearTier;
import com.wynntils.models.stats.type.StatActualValue;
import com.wynntils.models.wynnitem.type.ItemEffect;
import com.wynntils.utils.type.Pair;
import java.util.List;
import java.util.Optional;

// Note that "rerolls" and "durabilityCurrent" actually reflect the same value.
// Its interpretation is determined what kind of item where parsed.
public record WynnItemParseResult(
        GearTier tier,
        String itemType,
        int level,
        List<StatActualValue> identifications,
        List<ItemEffect> effects,
        List<Powder> powders,
        int rerolls,
        int durabilityCurrent,
        int durabilityMax,
        Optional<Pair<String, Integer>> shinyStat) {}
