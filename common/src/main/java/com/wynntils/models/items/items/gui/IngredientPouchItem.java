/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.items.gui;

import com.wynntils.models.ingredients.type.IngredientInfo;
import com.wynntils.models.items.properties.CountedItemProperty;
import com.wynntils.utils.type.Pair;
import java.util.List;

public class IngredientPouchItem extends GuiItem implements CountedItemProperty {
    private final List<Pair<IngredientInfo, Integer>> ingredients;
    private final int count;

    public IngredientPouchItem(List<Pair<IngredientInfo, Integer>> ingredients) {
        this.ingredients = ingredients;
        this.count = ingredients.stream().mapToInt(Pair::b).sum();
    }

    public List<Pair<IngredientInfo, Integer>> getIngredients() {
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
