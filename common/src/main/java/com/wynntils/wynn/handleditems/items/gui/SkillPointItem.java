/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.handleditems.items.gui;

import com.wynntils.utils.CustomColor;
import com.wynntils.wynn.handleditems.properties.CountedItemProperty;
import com.wynntils.wynn.objects.Skill;

public class SkillPointItem extends GuiItem implements CountedItemProperty {
    private final Skill skill;
    private final int skillPoints;

    public SkillPointItem(Skill skill, int skillPoints) {
        this.skill = skill;
        this.skillPoints = skillPoints;
    }

    public Skill getSkill() {
        return skill;
    }

    public int getSkillPoints() {
        return skillPoints;
    }

    @Override
    public int getCount() {
        return skillPoints;
    }

    @Override
    public CustomColor getCountColor() {
        return CustomColor.fromChatFormatting(skill.getColor());
    }

    @Override
    public String toString() {
        return "SkillPointItem{" + "skill=" + skill + ", skillPoints=" + skillPoints + '}';
    }
}
