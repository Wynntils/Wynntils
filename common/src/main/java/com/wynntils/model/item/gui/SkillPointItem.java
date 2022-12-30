/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.model.item.gui;

import com.wynntils.model.item.properties.CountedItemProperty;

public class SkillPointItem extends GuiItem implements CountedItemProperty {
    private final int skillPoints;

    public SkillPointItem(int skillPoints) {
        this.skillPoints = skillPoints;
    }

    public int getSkillPoints() {
        return skillPoints;
    }

    @Override
    public int getCount() {
        return skillPoints;
    }
}
