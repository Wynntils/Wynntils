/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.item;

import com.wynntils.core.managers.Managers;
import com.wynntils.core.managers.Model;
import com.wynntils.wynn.item.SoulPointItemStack;
import com.wynntils.wynn.item.parsers.WynnItemMatchers;
import com.wynntils.wynn.model.item.ItemStackTransformManager.ItemStackTransformer;

public final class SoulPointItemStackModel extends Model {
    private static final ItemStackTransformer SOUL_POINT_TRANSFORMER =
            new ItemStackTransformer(WynnItemMatchers::isSoulPoint, SoulPointItemStack::new);

    public static void init() {
        Managers.ItemStackTransform.registerTransformer(SOUL_POINT_TRANSFORMER);
    }

    public static void disable() {
        Managers.ItemStackTransform.unregisterTransformer(SOUL_POINT_TRANSFORMER);
    }
}
