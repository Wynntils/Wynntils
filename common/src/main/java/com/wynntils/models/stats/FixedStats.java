/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.stats;

import com.wynntils.models.concepts.Element;
import com.wynntils.models.concepts.Skill;
import com.wynntils.models.gear.type.GearAttackSpeed;
import com.wynntils.models.gearinfo.type.GearMajorId;
import com.wynntils.models.stats.type.DamageType;
import com.wynntils.utils.type.Pair;
import com.wynntils.utils.type.RangedValue;
import java.util.List;
import java.util.Optional;

public record FixedStats(
        int healthBuff,
        List<Pair<Skill, Integer>> skillBuffs,
        Optional<GearAttackSpeed> attackSpeed,
        List<GearMajorId> majorIds,
        List<Pair<DamageType, RangedValue>> damages,
        List<Pair<Element, Integer>> defences) {}
