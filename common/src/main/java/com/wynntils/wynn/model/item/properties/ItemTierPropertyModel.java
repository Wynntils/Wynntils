/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.item.properties;

import com.wynntils.core.managers.Managers;
import com.wynntils.core.managers.Model;
import com.wynntils.wynn.item.parsers.WynnItemMatchers;
import com.wynntils.wynn.item.properties.ItemTierProperty;
import com.wynntils.wynn.model.item.ItemStackTransformManager.ItemPropertyWriter;

public class ItemTierPropertyModel extends Model {
    private static final ItemPropertyWriter ITEM_TIER_WRITER =
            new ItemPropertyWriter(WynnItemMatchers::isTieredItem, ItemTierProperty::new);

    public static void init() {
        Managers.ITEM_STACK_TRANSFORM.registerProperty(ITEM_TIER_WRITER);
    }

    public static void disable() {
        Managers.ITEM_STACK_TRANSFORM.unregisterProperty(ITEM_TIER_WRITER);
    }
}
