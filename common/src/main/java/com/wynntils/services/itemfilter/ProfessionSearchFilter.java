/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.itemfilter;

import com.wynntils.models.items.WynnItem;
import com.wynntils.models.items.items.game.GatheringToolItem;
import com.wynntils.models.items.items.game.IngredientItem;
import com.wynntils.models.items.items.game.MaterialItem;
import com.wynntils.models.profession.type.ProfessionType;

public class ProfessionSearchFilter extends ItemFilter {
    private ProfessionType profession;

    public ProfessionSearchFilter(String searchString) {
        super(searchString);
    }

    @Override
    public void prepare() throws InvalidSyntaxException {
        profession = ProfessionType.fromString(searchString);
        if (profession == null) {
            throw new InvalidSyntaxException("feature.wynntils.itemFilter.profession.invalidProfession", searchString);
        }
    }

    @Override
    public boolean matches(WynnItem wynnItem) throws IllegalStateException {
        if (profession == null) {
            throw new IllegalStateException("ProfessionSearchFilter must be prepared before use");
        } else if (wynnItem instanceof IngredientItem ingredient) {
            return ingredient.getIngredientInfo().professions().contains(profession);
        } else if (wynnItem instanceof MaterialItem materialItem) {
            return materialItem
                            .getMaterialProfile()
                            .getResourceType()
                            .getMaterialType()
                            .getProfessionType()
                    == profession;
        } else if (wynnItem instanceof GatheringToolItem gatheringToolItem) {
            return gatheringToolItem.getToolProfile().toolType().getProfessionType() == profession;
        }

        return false;
    }
}
