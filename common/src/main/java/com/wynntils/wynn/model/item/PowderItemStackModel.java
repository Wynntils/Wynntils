/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.item;

import com.wynntils.core.managers.Model;
import com.wynntils.wynn.item.PowderItemStack;
import com.wynntils.wynn.item.parsers.WynnItemMatchers;
import com.wynntils.wynn.model.item.ItemStackTransformManager.ItemStackTransformer;

public class PowderItemStackModel extends Model {
    private static final ItemStackTransformer POWDER_TRANSFORMER =
            new ItemStackTransformer(WynnItemMatchers::isPowder, PowderItemStack::new);

    public static void init() {
        ItemStackTransformManager.registerTransformer(POWDER_TRANSFORMER);
    }

    public static void disable() {
        ItemStackTransformManager.unregisterTransformer(POWDER_TRANSFORMER);
    }
}
