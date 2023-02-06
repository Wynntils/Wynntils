/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.ingredientinfo;

import com.wynntils.models.concepts.ProfessionType;
import com.wynntils.models.concepts.Skill;
import com.wynntils.models.gear.type.GearMaterial;
import com.wynntils.models.ingredients.profile.IngredientIdentificationContainer;
import com.wynntils.models.ingredients.profile.IngredientItemModifiers;
import com.wynntils.models.ingredients.profile.IngredientModifiers;
import com.wynntils.models.ingredients.type.IngredientTier;
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
        List<Pair<StatType, RangedValue>> variableStats) {
    //FIXME: remove those
    public IngredientTier getTier() {
        return IngredientTier.TIER_1;
    }

    public Map<String, IngredientIdentificationContainer> getStatuses() {
        return Map.of();
    }

    public IngredientModifiers getIngredientModifiers() {
        return new IngredientModifiers();
    }

    public IngredientItemModifiers getItemModifiers() {
        return new IngredientItemModifiers();
    }
}
