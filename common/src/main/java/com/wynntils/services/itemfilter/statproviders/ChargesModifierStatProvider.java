/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.itemfilter.statproviders;

import com.wynntils.models.items.WynnItem;
import com.wynntils.models.items.items.game.IngredientItem;
import com.wynntils.services.itemfilter.type.ItemProviderType;
import com.wynntils.services.itemfilter.type.ItemStatProvider;
import java.util.List;
import java.util.Optional;

public class ChargesModifierStatProvider extends ItemStatProvider<Integer> {
    @Override
    public Optional<Integer> getValue(WynnItem wynnItem) {
        if (!(wynnItem instanceof IngredientItem ingredientItem)) return Optional.empty();

        int charges = ingredientItem.getIngredientInfo().charges();

        return charges == 0 ? Optional.empty() : Optional.of(charges);
    }

    @Override
    public List<ItemProviderType> getFilterTypes() {
        return List.of(ItemProviderType.INGREDIENT);
    }
}
