/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.wynnitem.parsing;

import com.wynntils.models.elements.type.Element;
import com.wynntils.models.elements.type.Powder;
import com.wynntils.models.elements.type.PowderSpecialTierInfo;
import com.wynntils.models.gear.type.GearAttackSpeed;
import com.wynntils.models.gear.type.GearRestrictions;
import com.wynntils.models.gear.type.GearTier;
import com.wynntils.models.gear.type.SetInstance;
import com.wynntils.models.stats.type.DamageType;
import com.wynntils.utils.type.CappedValue;
import com.wynntils.utils.type.Pair;
import com.wynntils.utils.type.RangedValue;
import java.util.List;
import java.util.Optional;

public record WynnItemMiscStatsParseResult(
        GearTier tier,
        String itemType,
        int dps,
        int health,
        GearAttackSpeed attackSpeed,
        List<Pair<DamageType, RangedValue>> damages,
        List<Pair<Element, Integer>> defences,
        List<Pair<Powder, Integer>> powders,
        int powderSlots,
        CappedValue durability,
        Optional<PowderSpecialTierInfo> powderSpecial,
        Optional<SetInstance> setInstance,
        GearRestrictions restrictions) {}
