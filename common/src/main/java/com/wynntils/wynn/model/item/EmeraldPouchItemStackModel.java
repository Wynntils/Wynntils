/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.item;

import com.wynntils.core.managers.Model;
import com.wynntils.wynn.item.EmeraldPouchItemStack;
import com.wynntils.wynn.item.parsers.WynnItemMatchers;
import com.wynntils.wynn.model.item.ItemStackTransformManager.ItemStackTransformer;

public class EmeraldPouchItemStackModel extends Model {
    private static final ItemStackTransformer EMERALDPOUCH_TRANSFORMER =
            new ItemStackTransformer(WynnItemMatchers::isEmeraldPouch, EmeraldPouchItemStack::new);

    public static void init() {
        ItemStackTransformManager.registerTransformer(EMERALDPOUCH_TRANSFORMER);
    }

    public static void disable() {
        ItemStackTransformManager.unregisterTransformer(EMERALDPOUCH_TRANSFORMER);
    }
}
