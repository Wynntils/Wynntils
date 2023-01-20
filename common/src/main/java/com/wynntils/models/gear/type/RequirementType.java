/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.gear.type;

import com.wynntils.models.concepts.Skill;

public enum RequirementType {
    QUEST("Quest Req: "),
    CLASSTYPE("Class Req: "),
    LEVEL("Combat Lv. Min: "),
    STRENGTH("Strength Min: "),
    AGILITY("Agility Min: "),
    DEFENSE("Defence Min: "),
    INTELLIGENCE("Intelligence Min: "),
    DEXTERITY("Dexterity Min: ");

    private final String lore;

    RequirementType(String lore) {
        this.lore = lore;
    }

    public String asLore() {
        return lore;
    }

    public Skill getSkill() {
        return switch (this) {
            case STRENGTH -> Skill.STRENGTH;
            case DEXTERITY -> Skill.DEXTERITY;
            case INTELLIGENCE -> Skill.INTELLIGENCE;
            case DEFENSE -> Skill.DEFENCE;
            case AGILITY -> Skill.AGILITY;
            default -> null;
        };
    }
}
