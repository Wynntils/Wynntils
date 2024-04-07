/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.wynnitem.parsing;

import com.wynntils.models.elements.type.Powder;
import com.wynntils.models.gear.type.GearTier;
import com.wynntils.models.gear.type.SetInstance;
import com.wynntils.models.stats.type.ShinyStat;
import com.wynntils.models.stats.type.StatActualValue;
import com.wynntils.models.wynnitem.type.ItemEffect;
import com.wynntils.models.wynnitem.type.NamedItemEffect;
import java.util.List;
import java.util.Optional;

// Note that "rerolls" and "durabilityCurrent" actually reflect the same value.
// Its interpretation is determined what kind of item where parsed.
public record WynnItemParseResult(
        GearTier tier,
        String itemType,
        int health,
        int level,
        List<StatActualValue> identifications,
        List<NamedItemEffect> namedEffects,
        List<ItemEffect> effects,
        List<Powder> powders,
        int powderSlots,
        int rerolls,
        int durabilityCurrent,
        int durabilityMax,
        Optional<ShinyStat> shinyStat,
        boolean allRequirementsMet,
        Optional<SetInstance> setInstance) {}
