/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.items.game;

import com.wynntils.models.elements.type.Skill;
import com.wynntils.models.items.properties.LeveledItemProperty;
import com.wynntils.models.items.properties.UsesItemPropery;
import com.wynntils.utils.type.CappedValue;

public class SkillPotionItem extends GameItem implements UsesItemPropery, LeveledItemProperty {
    private final Skill skill;
    private final int level;
    private final CappedValue uses;

    public SkillPotionItem(Skill skill, int level, CappedValue uses) {
        this.skill = skill;
        this.level = level;
        this.uses = uses;
    }

    public Skill getSkill() {
        return skill;
    }

    @Override
    public int getLevel() {
        return level;
    }

    public CappedValue getUses() {
        return uses;
    }

    @Override
    public String toString() {
        return "SkillPotionItem{" + "skill=" + skill + ", level=" + level + ", uses=" + uses + '}';
    }
}
