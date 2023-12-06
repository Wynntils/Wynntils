/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.stats.type;

import com.wynntils.models.elements.type.Element;
import com.wynntils.models.elements.type.Skill;
import com.wynntils.models.gear.type.GearAttackSpeed;
import com.wynntils.models.gear.type.GearMajorId;
import com.wynntils.utils.type.Pair;
import com.wynntils.utils.type.RangedValue;
import java.util.List;
import java.util.Optional;

public record FixedStats(
        int healthBuff,
        List<Pair<Skill, Integer>> skillBonuses,
        Optional<GearAttackSpeed> attackSpeed,
        Optional<GearMajorId> majorIds,
        List<Pair<DamageType, RangedValue>> damages,
        List<Pair<Element, Integer>> defences) {
    public int getSkillBonus(Skill skill) {
        for (Pair<Skill, Integer> skillBonusValue : skillBonuses) {
            if (skillBonusValue.key() == skill) {
                return skillBonusValue.value();
            }
        }

        return 0;
    }
}
