/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.items.game;

import com.wynntils.models.elements.type.Skill;
import com.wynntils.models.items.properties.UsesItemPropery;
import com.wynntils.models.wynnitem.type.ItemEffect;
import com.wynntils.utils.type.CappedValue;
import java.util.List;

public class SkillPotionItem extends GameItem implements UsesItemPropery {
    private final Skill skill;
    private final List<ItemEffect> effects;
    private final CappedValue uses;

    public SkillPotionItem(Skill skill, List<ItemEffect> effects, CappedValue uses) {
        this.skill = skill;
        this.effects = effects;
        this.uses = uses;
    }

    public Skill getSkill() {
        return skill;
    }

    public List<ItemEffect> getEffects() {
        return effects;
    }

    public CappedValue getUses() {
        return uses;
    }

    @Override
    public String toString() {
        return "SkillPotionItem{" + "skill=" + skill + ", effects=" + effects + ", uses=" + uses + '}';
    }
}
