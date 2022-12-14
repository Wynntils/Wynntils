/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.item;

import com.wynntils.core.managers.Managers;
import com.wynntils.core.managers.Model;
import com.wynntils.wynn.item.EmeraldPouchItemStack;
import com.wynntils.wynn.item.parsers.WynnItemMatchers;
import com.wynntils.wynn.model.item.ItemStackTransformManager.ItemStackTransformer;

public class EmeraldPouchItemStackModel extends Model {
    private static final ItemStackTransformer EMERALD_POUCH_TRANSFORMER =
            new ItemStackTransformer(WynnItemMatchers::isEmeraldPouch, EmeraldPouchItemStack::new);

    public static void init() {
        Managers.ITEM_STACK_TRANSFORM.registerTransformer(EMERALD_POUCH_TRANSFORMER);
    }

    public static void disable() {
        Managers.ITEM_STACK_TRANSFORM.unregisterTransformer(EMERALD_POUCH_TRANSFORMER);
    }
}
