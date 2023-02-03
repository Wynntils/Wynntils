/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.gear.ingredients;

import com.wynntils.models.concepts.ProfessionType;
import com.wynntils.models.gear.type.GearMaterial;
import com.wynntils.models.stats.type.StatPossibleValues;
import com.wynntils.models.stats.type.StatType;
import com.wynntils.utils.type.Pair;
import java.util.List;
import java.util.Optional;

/*
json fields:
     "name"
     "tier"
     "skills"
     "level"

     "sprite"
     "displayName"

     "itemOnlyIDs"
     "consumableOnlyIDs"
     "identifications"
     "ingredientPositionModifiers"
*/
public record IngredientInfo(
        String name,
        int tier,
        int level,
        Optional<String> apiName,
        GearMaterial material,
        List<ProfessionType> professions,
        List<Pair<StatType, StatPossibleValues>> variableStats) {}
