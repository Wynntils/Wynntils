/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.model.item.game;

import com.wynntils.utils.CappedValue;
import com.wynntils.wynn.objects.Skill;

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
}
