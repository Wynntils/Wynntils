/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.items.game;

import com.wynntils.models.ingredients.IngredientInfo;
import com.wynntils.models.items.properties.QualityTierItemProperty;

public class IngredientItem extends GameItem implements QualityTierItemProperty {
    private final IngredientInfo ingredientInfo;

    public IngredientItem(IngredientInfo ingredientInfo) {
        this.ingredientInfo = ingredientInfo;
    }

    public IngredientInfo getIngredientInfo() {
        return ingredientInfo;
    }

    public int getQualityTier() {
        return ingredientInfo.tier();
    }

    @Override
    public String toString() {
        return "IngredientItem{" + "ingredientInfo=" + ingredientInfo + '}';
    }
}
