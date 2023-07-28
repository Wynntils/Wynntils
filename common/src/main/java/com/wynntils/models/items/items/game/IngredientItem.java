/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.items.game;

import com.wynntils.models.ingredients.type.IngredientInfo;
import com.wynntils.models.items.properties.LeveledItemProperty;
import com.wynntils.models.items.properties.QualityTierItemProperty;

public class IngredientItem extends GameItem implements QualityTierItemProperty, LeveledItemProperty {
    private final IngredientInfo ingredientInfo;

    public IngredientItem(IngredientInfo ingredientInfo) {
        this.ingredientInfo = ingredientInfo;
    }

    public IngredientInfo getIngredientInfo() {
        return ingredientInfo;
    }

    @Override
    public int getQualityTier() {
        return ingredientInfo.tier();
    }

    @Override
    public int getLevel() {
        return ingredientInfo.level();
    }

    @Override
    public String toString() {
        return "IngredientItem{" + "ingredientInfo=" + ingredientInfo + '}';
    }
}
