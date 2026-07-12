/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.items.gui;

import com.google.common.collect.Streams;
import com.wynntils.core.text.StyledText;
import com.wynntils.models.ingredients.type.IngredientInfo;
import com.wynntils.models.items.properties.CountedItemProperty;
import com.wynntils.utils.type.Pair;
import com.wynntils.utils.type.RangedValue;
import java.util.List;

public class IngredientPouchItem extends GuiItem implements CountedItemProperty {
    private final List<Pair<IngredientInfo, Integer>> ingredients;
    private final List<Pair<StyledText, Integer>> otherItems;
    private final int count;
    private final RangedValue sellRange;
    private final boolean isUltIronman;

    public IngredientPouchItem(
            List<Pair<IngredientInfo, Integer>> ingredients,
            List<Pair<StyledText, Integer>> otherItems,
            RangedValue sellRange,
            boolean isUltIronman) {
        this.ingredients = ingredients;
        this.otherItems = otherItems;
        this.count = Streams.concat(ingredients.stream(), otherItems.stream())
                .mapToInt(Pair::b)
                .sum();
        this.sellRange = sellRange;
        this.isUltIronman = isUltIronman;
    }

    public List<Pair<IngredientInfo, Integer>> getIngredients() {
        return ingredients;
    }

    public List<Pair<StyledText, Integer>> getOtherItems() {
        return otherItems;
    }

    @Override
    public int getCount() {
        return count;
    }

    public RangedValue getSellRange() {
        return sellRange;
    }

    public boolean isUltIronman() {
        return isUltIronman;
    }

    @Override
    public String toString() {
        return "IngredientPouchItem{" + "count=" + count + ", ingredients=" + ingredients + ", otherItems=" + otherItems
                + ", sellRange=" + sellRange + ", isUltIronman=" + isUltIronman + '}';
    }
}
