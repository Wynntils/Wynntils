/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.ingredients;

import com.wynntils.models.elements.type.Skill;
import com.wynntils.models.gear.type.GearMaterial;
import com.wynntils.models.profession.type.ProfessionType;
import com.wynntils.models.stats.type.StatType;
import com.wynntils.utils.type.Pair;
import com.wynntils.utils.type.RangedValue;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public record IngredientInfo(
        String name,
        int tier,
        int level,
        Optional<String> apiName,
        GearMaterial material,
        List<ProfessionType> professions,
        List<Pair<Skill, Integer>> skillRequirements,
        Map<IngredientPosition, Integer> positionModifiers,
        int duration,
        int charges,
        int durabilityModifier,
        List<Pair<StatType, RangedValue>> variableStats) {}
