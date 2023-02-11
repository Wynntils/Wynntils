/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.items.game;

import com.wynntils.models.elements.type.Skill;
import com.wynntils.models.items.properties.UsesItemPropery;
import com.wynntils.utils.type.CappedValue;

public class SkillPotionItem extends GameItem implements UsesItemPropery {
    private final Skill skill;
    private final CappedValue uses;

    public SkillPotionItem(Skill skill, CappedValue uses) {
        this.skill = skill;
        this.uses = uses;
    }

    public Skill getSkill() {
        return skill;
    }

    public CappedValue getUses() {
        return uses;
    }

    @Override
    public String toString() {
        return "SkillPotionItem{" + "skill=" + skill + ", uses=" + uses + '}';
    }
}
