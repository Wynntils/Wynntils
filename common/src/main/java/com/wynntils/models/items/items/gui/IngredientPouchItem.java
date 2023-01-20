/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.items.gui;

import com.wynntils.models.ingredients.profile.IngredientProfile;
import com.wynntils.models.items.properties.CountedItemProperty;
import com.wynntils.utils.type.Pair;
import java.util.List;

public class IngredientPouchItem extends GuiItem implements CountedItemProperty {
    private final List<Pair<IngredientProfile, Integer>> ingredients;
    private final int count;

    public IngredientPouchItem(List<Pair<IngredientProfile, Integer>> ingredients) {
        this.ingredients = ingredients;
        this.count = ingredients.stream().mapToInt(Pair::b).sum();
    }

    public List<Pair<IngredientProfile, Integer>> getIngredients() {
        return ingredients;
    }

    @Override
    public int getCount() {
        return count;
    }

    @Override
    public String toString() {
        return "IngredientPouchItem{" + "count=" + count + ", ingredients=" + ingredients + '}';
    }
}
