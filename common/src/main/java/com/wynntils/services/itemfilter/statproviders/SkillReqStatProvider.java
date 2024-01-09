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

public class SkillReqStatProvider extends ItemStatProvider<Integer> {
    private final Skill skill;

    public SkillReqStatProvider(Skill skill) {
        this.skill = skill;
    }

    @Override
    public String getName() {
        return skill.getApiName() + "Req";
    }

    @Override
    public String getDescription() {
        return getTranslation("description", skill.getDisplayName());
    }

    @Override
    public List<Integer> getValue(WynnItem wynnItem) {
        if (!(wynnItem instanceof GearItem gearItem)) return List.of();

        return gearItem.getItemInfo().requirements().skills().stream()
                .filter(pair -> pair.key() == skill)
                .map(Pair::value)
                .toList();
    }
}
