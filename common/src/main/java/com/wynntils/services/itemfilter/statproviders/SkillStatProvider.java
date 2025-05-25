/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.itemfilter.statproviders;

import com.wynntils.models.elements.type.Skill;
import com.wynntils.models.items.WynnItem;
import com.wynntils.models.items.properties.IdentifiableItemProperty;
import com.wynntils.models.stats.type.SkillStatType;
import com.wynntils.models.stats.type.StatPossibleValues;
import com.wynntils.services.itemfilter.type.ItemProviderType;
import com.wynntils.services.itemfilter.type.ItemStatProvider;
import java.util.List;
import java.util.Optional;

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
    public String getDisplayName() {
        return skill.getDisplayName();
    }

    @Override
    public String getDescription() {
        return getTranslation("description", skill.getDisplayName());
    }

    @Override
    public Optional<Integer> getValue(WynnItem wynnItem) {
        if (!(wynnItem instanceof IdentifiableItemProperty<?, ?> identifiableItemProperty)) return Optional.empty();

        return identifiableItemProperty.getPossibleValues().stream()
                .filter(id -> id.statType() instanceof SkillStatType)
                .filter(id -> ((SkillStatType) id.statType()).getSkill() == skill)
                .map(StatPossibleValues::baseValue)
                .findFirst();
    }

    @Override
    public List<ItemProviderType> getFilterTypes() {
        // Skill stats are either fixed in gear
        return List.of(ItemProviderType.GEAR);
    }

    @Override
    public List<String> getAliases() {
        if (skill == Skill.DEFENCE) {
            return List.of("defense");
        }

        return super.getAliases();
    }
}
