/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.items.game;

import com.wynntils.models.ingredients.profile.IngredientProfile;
import com.wynntils.models.items.properties.QualityTierItemProperty;

public class IngredientItem extends GameItem implements QualityTierItemProperty {
    private final IngredientProfile ingredientProfile;

    public IngredientItem(IngredientProfile ingredientProfile) {
        this.ingredientProfile = ingredientProfile;
    }

    public IngredientProfile getIngredientProfile() {
        return ingredientProfile;
    }

    public int getQualityTier() {
        return ingredientProfile.getTier().getTierInt();
    }

    @Override
    public String toString() {
        return "IngredientItem{" + "ingredientProfile=" + ingredientProfile + '}';
    }
}
