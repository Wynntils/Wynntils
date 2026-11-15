/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.wynnitem.parsing;

import com.wynntils.models.elements.type.Element;
import com.wynntils.models.gear.type.GearAttackSpeed;
import com.wynntils.models.gear.type.GearInstanceRequirements;
import com.wynntils.models.gear.type.GearRequirements;
import com.wynntils.models.gear.type.GearRestrictions;
import com.wynntils.models.gear.type.GearTier;
import com.wynntils.models.stats.type.DamageType;
import com.wynntils.models.stats.type.ShinyStat;
import com.wynntils.models.stats.type.StatActualValue;
import com.wynntils.models.wynnitem.type.ItemEffect;
import com.wynntils.models.wynnitem.type.NamedItemEffect;
import com.wynntils.utils.type.CappedValue;
import com.wynntils.utils.type.Pair;
import com.wynntils.utils.type.RangedValue;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.chat.Component;

public record WynnItemStatsParseResult(
        GearTier tier,
        String itemType,
        int dps,
        int health,
        int level,
        GearAttackSpeed attackSpeed,
        List<Pair<DamageType, RangedValue>> damages,
        List<Pair<Element, Integer>> defences,
        GearRequirements requirements,
        List<StatActualValue> identifications,
        List<NamedItemEffect> namedEffects,
        List<ItemEffect> effects,
        int rerolls,
        CappedValue durability,
        CappedValue uses,
        int durationSeconds,
        String lore,
        String creator,
        Optional<ShinyStat> shinyStat,
        GearInstanceRequirements gearInstanceRequirements,
        List<Component> majorIdLines,
        GearRestrictions restrictions) {}
