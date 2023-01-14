/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.gear;

import com.wynntils.utils.Pair;
import com.wynntils.utils.RangedValue;
import com.wynntils.wynn.gear.types.GearDamageType;
import com.wynntils.wynn.objects.Element;
import com.wynntils.wynn.objects.Skill;
import com.wynntils.wynn.objects.profiles.item.GearAttackSpeed;
import java.util.List;
import java.util.Optional;

// FIXME: replace String with MajorIdentification for majorIds
public record GearStatsFixed(
        int healthBuff,
        List<Pair<Skill, Integer>> skillBuffs,
        Optional<GearAttackSpeed> attackSpeed,
        List<String> majorIds,
        List<Pair<GearDamageType, RangedValue>> damages,
        List<Pair<Element, Integer>> defences) {}
