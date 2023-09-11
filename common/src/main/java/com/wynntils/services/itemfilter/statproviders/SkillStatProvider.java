/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.itemfilter.statproviders;

import com.wynntils.models.elements.type.Skill;
import com.wynntils.models.items.WynnItem;
import com.wynntils.models.items.items.game.GearItem;
import com.wynntils.services.itemfilter.type.ItemStatProvider;
import com.wynntils.utils.type.Pair;
import java.util.List;

public class SkillStatProvider extends ItemStatProvider<Integer> {
    private final Skill skill;

    public SkillStatProvider(Skill skill) {
        this.skill = skill;
    }

    @Override
    public String getName() {
        return skill.getApiName();
    }

    @Override
    public String getDescription() {
        return getTranslation("description", skill.getDisplayName());
    }

    @Override
    public List<Integer> getValue(WynnItem wynnItem) {
        if (!(wynnItem instanceof GearItem gearItem)) return List.of();

        return gearItem.getGearInfo().fixedStats().skillBonuses().stream()
                .filter(pair -> pair.key() == skill)
                .map(Pair::value)
                .toList();
    }
}
