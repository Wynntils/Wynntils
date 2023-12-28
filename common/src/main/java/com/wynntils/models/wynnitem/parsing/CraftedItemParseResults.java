/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.wynnitem.parsing;

import com.wynntils.models.elements.type.Element;
import com.wynntils.models.gear.type.ConsumableType;
import com.wynntils.models.gear.type.GearAttackSpeed;
import com.wynntils.models.gear.type.GearRequirements;
import com.wynntils.models.stats.type.DamageType;
import com.wynntils.utils.type.Pair;
import com.wynntils.utils.type.RangedValue;
import java.util.List;

public record CraftedItemParseResults(
        String name,
        ConsumableType consumableType,
        int effectStrength,
        GearAttackSpeed attackSpeed,
        List<Pair<DamageType, RangedValue>> damages,
        List<Pair<Element, Integer>> defences,
        GearRequirements requirements) {}
