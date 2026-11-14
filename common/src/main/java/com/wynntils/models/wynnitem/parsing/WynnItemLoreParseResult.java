/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.wynnitem.parsing;

import com.wynntils.core.text.StyledText;
import com.wynntils.models.elements.type.Element;
import com.wynntils.models.gear.type.GearAttackSpeed;
import com.wynntils.models.gear.type.GearRestrictions;
import com.wynntils.models.gear.type.GearTier;
import com.wynntils.models.stats.type.DamageType;
import com.wynntils.utils.type.CappedValue;
import com.wynntils.utils.type.Pair;
import com.wynntils.utils.type.RangedValue;
import java.util.List;

public record WynnItemLoreParseResult(
        GearTier tier,
        String itemType,
        int dps,
        int health,
        GearAttackSpeed attackSpeed,
        List<Pair<DamageType, RangedValue>> damages,
        List<Pair<Element, Integer>> defences,
        CappedValue durability,
        CappedValue uses,
        int durationSeconds,
        GearRestrictions restrictions,
        StyledText lore,
        String creator) {}
