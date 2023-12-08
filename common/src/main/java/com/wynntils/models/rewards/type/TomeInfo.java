/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.rewards.type;

import com.wynntils.models.elements.type.Skill;
import com.wynntils.models.gear.type.GearMetaInfo;
import com.wynntils.models.gear.type.GearTier;
import com.wynntils.models.rewards.TomeType;
import com.wynntils.models.stats.type.StatPossibleValues;
import com.wynntils.models.stats.type.StatType;
import com.wynntils.utils.type.Pair;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public record TomeInfo(
        String name,
        TomeType type,
        TomeVariant variant,
        GearTier tier,
        GearMetaInfo metaInfo,
        TomeRequirements requirements,
        List<Pair<Skill, Integer>> skillBonuses,
        List<Pair<StatType, StatPossibleValues>> variableStats) {
    public StatPossibleValues getPossibleValues(StatType statType) {
        return this.variableStats().stream()
                .filter(p -> p.key().equals(statType))
                .findFirst()
                .map(Pair::value)
                .orElse(null);
    }

    public Map<StatType, StatPossibleValues> getVariableStatsMap() {
        return variableStats().stream().collect(Collectors.toMap(Pair::key, Pair::value));
    }

    public List<StatPossibleValues> getPossibleValueList() {
        return variableStats().stream().map(Pair::value).toList();
    }
}
