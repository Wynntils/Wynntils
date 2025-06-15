/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.itemfilter.statproviders;

import com.wynntils.models.elements.type.Skill;
import com.wynntils.models.items.WynnItem;
import com.wynntils.models.items.items.game.GearItem;
import com.wynntils.services.itemfilter.type.ItemProviderType;
import com.wynntils.services.itemfilter.type.ItemStatProvider;
import com.wynntils.utils.type.Pair;
import java.util.List;
import java.util.Optional;

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
    public String getDisplayName() {
        return skill.getDisplayName() + " Required";
    }

    @Override
    public String getDescription() {
        return getTranslation("description", skill.getDisplayName());
    }

    @Override
    public Optional<Integer> getValue(WynnItem wynnItem) {
        if (!(wynnItem instanceof GearItem gearItem)) return Optional.empty();

        return gearItem.getItemInfo().requirements().skills().stream()
                .filter(pair -> pair.key() == skill)
                .map(Pair::value)
                .findFirst();
    }

    @Override
    public List<ItemProviderType> getFilterTypes() {
        return List.of(ItemProviderType.GEAR);
    }

    @Override
    public List<String> getAliases() {
        if (skill == Skill.DEFENCE) {
            return List.of("defenseReq");
        }

        return super.getAliases();
    }
}
