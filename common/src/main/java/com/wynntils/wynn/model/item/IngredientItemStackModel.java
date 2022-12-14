/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.item;

import com.wynntils.core.managers.Model;
import com.wynntils.wynn.item.IngredientItemStack;
import com.wynntils.wynn.item.parsers.WynnItemMatchers;
import com.wynntils.wynn.model.item.ItemStackTransformManager.ItemStackTransformer;

public class IngredientItemStackModel extends Model {
    private static final ItemStackTransformer INGREDIENT_TRANSFORMER =
            new ItemStackTransformer(WynnItemMatchers::isIngredient, IngredientItemStack::new);

    public static void init() {
        ItemStackTransformManager.registerTransformer(INGREDIENT_TRANSFORMER);
    }

    public static void disable() {
        ItemStackTransformManager.unregisterTransformer(INGREDIENT_TRANSFORMER);
    }
}
