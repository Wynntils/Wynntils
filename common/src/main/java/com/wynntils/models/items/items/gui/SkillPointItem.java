/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.items.gui;

import com.wynntils.models.elements.type.Skill;
import com.wynntils.models.items.properties.CountedItemProperty;
import com.wynntils.utils.colors.CustomColor;

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
        return CustomColor.fromChatFormatting(skill.getColorCode());
    }

    @Override
    public String toString() {
        return "SkillPointItem{" + "skill=" + skill + ", skillPoints=" + skillPoints + '}';
    }
}
