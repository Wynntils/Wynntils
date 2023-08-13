/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.itemfilter.filters;

import com.wynntils.models.items.WynnItem;
import com.wynntils.models.items.items.game.GatheringToolItem;
import com.wynntils.models.items.items.game.IngredientItem;
import com.wynntils.models.items.items.game.MaterialItem;
import com.wynntils.models.profession.type.ProfessionType;
import com.wynntils.services.itemfilter.type.ItemFilter;
import com.wynntils.services.itemfilter.type.ItemFilterMatcher;
import com.wynntils.utils.type.ErrorOr;
import java.util.List;

public class ProfessionItemFilter extends ItemFilter {
    public ProfessionItemFilter() {
        super(List.of("prof"));
    }

    @Override
    public ErrorOr<ItemFilterMatcher> createMatcher(String inputString) {
        ProfessionType profession = ProfessionType.fromString(inputString);
        if (profession == null) {
            return ErrorOr.error(getTranslation("invalidProfession", inputString));
        } else {
            return ErrorOr.of(new ProfessionItemFilterMatcher(profession));
        }
    }

    private static class ProfessionItemFilterMatcher implements ItemFilterMatcher {
        private final ProfessionType profession;

        protected ProfessionItemFilterMatcher(ProfessionType profession) {
            this.profession = profession;
        }

        @Override
        public boolean matches(WynnItem wynnItem) {
            if (profession == null) {
                return false;
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
}
