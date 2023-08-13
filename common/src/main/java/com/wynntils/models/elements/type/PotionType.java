/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.elements.type;

public enum PotionType {
    HEALING,
    MANA,
    XP,
    STRENGTH(Skill.STRENGTH),
    DEXTERITY(Skill.DEXTERITY),
    INTELLIGENCE(Skill.INTELLIGENCE),
    DEFENCE(Skill.DEFENCE),
    AGILITY(Skill.AGILITY);

    private final Skill skill;

    PotionType() {
        skill = null;
    }

    PotionType(Skill skill) {
        this.skill = skill;
    }

    public static PotionType fromSkill(Skill skill) {
        for (PotionType potionType : PotionType.values()) {
            if (potionType.skill == skill) return potionType;
        }
        return null;
    }

    public Skill getSkill() {
        return skill;
    }
}
