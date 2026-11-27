/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.profession.type;

import com.wynntils.models.elements.type.Element;
import com.wynntils.models.elements.type.Skill;
import com.wynntils.models.gear.type.GearAttackSpeed;
import com.wynntils.models.stats.type.DamageType;
import com.wynntils.utils.type.Pair;
import com.wynntils.utils.type.RangedValue;
import java.util.List;

public class CraftedItemRecipe {
    private final CraftedItemType craftedItemType;
    private final RangedValue level;
    private final RangedValue health;
    private final RangedValue durabilityOrDuration;
    private final Pair<Double, Double> averageDps;
    private final GearAttackSpeed attackSpeed;
    private final List<Pair<DamageType, List<RangedValue>>> damages;
    private final List<Pair<Element, Integer>> defences;
    private final List<Pair<Skill, Integer>> skillRequirements;

    public CraftedItemRecipe(
            CraftedItemType craftedItemType,
            Pair<Double, Double> averageDps,
            GearAttackSpeed attackSpeed,
            List<Pair<DamageType, List<RangedValue>>> damages,
            RangedValue health,
            RangedValue level,
            RangedValue durabilityOrDuration,
            List<Pair<Element, Integer>> defences,
            List<Pair<Skill, Integer>> skillRequirements) {
        this.craftedItemType = craftedItemType;
        this.averageDps = averageDps;
        this.attackSpeed = attackSpeed;
        this.damages = damages;
        this.health = health;
        this.level = level;
        this.durabilityOrDuration = durabilityOrDuration;
        this.defences = defences;
        this.skillRequirements = skillRequirements;
    }

    public CraftedItemType getCraftedItemType() {
        return craftedItemType;
    }

    public Pair<Double, Double> getAverageDps() {
        return averageDps;
    }

    public GearAttackSpeed getAttackSpeed() {
        return attackSpeed;
    }

    public List<Pair<DamageType, List<RangedValue>>> getDamages() {
        return damages;
    }

    public RangedValue getHealth() {
        return health;
    }

    public List<Pair<Element, Integer>> getDefences() {
        return defences;
    }

    public RangedValue getLevel() {
        return level;
    }

    public RangedValue getDurabilityOrDuration() {
        return durabilityOrDuration;
    }

    public List<Pair<Skill, Integer>> getSkillRequirements() {
        return skillRequirements;
    }
}
