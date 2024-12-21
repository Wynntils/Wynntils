/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.itemfilter.statproviders;

import com.wynntils.models.ingredients.type.IngredientPosition;
import com.wynntils.models.items.WynnItem;
import com.wynntils.models.items.items.game.IngredientItem;
import com.wynntils.services.itemfilter.type.ItemProviderType;
import com.wynntils.services.itemfilter.type.ItemStatProvider;
import com.wynntils.utils.EnumUtils;
import com.wynntils.utils.StringUtils;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class IngredientEffectivenessStatProvider extends ItemStatProvider<Integer> {
    private final IngredientPosition ingredientPosition;

    public IngredientEffectivenessStatProvider(IngredientPosition ingredientPosition) {
        this.ingredientPosition = ingredientPosition;
    }

    @Override
    public String getName() {
        return "ingredientEffectiveness" + StringUtils.capitalizeFirst(ingredientPosition.getApiName());
    }

    @Override
    public String getDisplayName() {
        return EnumUtils.toNiceString(ingredientPosition) + " Ingredient Effectiveness";
    }

    @Override
    public String getDescription() {
        return "Ingredient effectiveness to ingredients " + ingredientPosition.getDisplayName() + " this one";
    }

    @Override
    public Optional<Integer> getValue(WynnItem wynnItem) {
        if (wynnItem instanceof IngredientItem ingredientItem) {
            return ingredientItem.getIngredientInfo().positionModifiers().entrySet().stream()
                    .filter(modifier -> modifier.getKey() == ingredientPosition)
                    .map(Map.Entry::getValue)
                    .findFirst();
        }

        return Optional.empty();
    }

    @Override
    public List<ItemProviderType> getFilterTypes() {
        return List.of(ItemProviderType.INGREDIENT);
    }
}
