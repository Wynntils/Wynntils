/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.wynnitem.parsing;

import com.wynntils.models.elements.type.Element;
import com.wynntils.models.elements.type.Powder;
import com.wynntils.models.gear.type.GearAttackSpeed;
import com.wynntils.models.gear.type.GearRequirements;
import com.wynntils.models.gear.type.GearTier;
import com.wynntils.models.gear.type.SetInstance;
import com.wynntils.models.stats.type.DamageType;
import com.wynntils.models.stats.type.ShinyStat;
import com.wynntils.models.stats.type.StatActualValue;
import com.wynntils.models.wynnitem.type.ItemEffect;
import com.wynntils.models.wynnitem.type.NamedItemEffect;
import com.wynntils.utils.type.Pair;
import com.wynntils.utils.type.RangedValue;
import java.util.List;
import java.util.Optional;

// Note that "rerolls" and "durabilityCurrent" actually reflect the same value.
// Its interpretation is determined what kind of item where parsed.
public record WynnItemParseResult(
        GearTier tier,
        String itemType,
        int health,
        int level,
        GearAttackSpeed attackSpeed,
        List<Pair<DamageType, RangedValue>> damages,
        List<Pair<Element, Integer>> defences,
        GearRequirements requirements,
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
        Optional<SetInstance> setInstance) {
    public static WynnItemParseResult fromInternalRoll(
            List<StatActualValue> identifications, List<Powder> powders, int rerolls) {
        return new WynnItemParseResult(
                null,
                null,
                0,
                0,
                null,
                List.of(),
                List.of(),
                null,
                identifications,
                List.of(),
                List.of(),
                powders,
                0,
                rerolls,
                0,
                0,
                Optional.empty(),
                true,
                Optional.empty());
    }
}
