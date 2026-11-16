/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.profession.type;

import com.wynntils.utils.type.RangedValue;

public record RecipeInfo(
        String recipeName,
        RangedValue durabilityOrDuration,
        RangedValue healthOrDamage,
        RangedValue level,
        ProfessionType professionType,
        CraftedItemType craftedItemType,
        int xp) {}
