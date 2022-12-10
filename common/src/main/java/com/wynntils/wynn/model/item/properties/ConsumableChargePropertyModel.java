/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.item.properties;

import com.wynntils.core.managers.Model;
import com.wynntils.wynn.item.parsers.WynnItemMatchers;
import com.wynntils.wynn.item.properties.ConsumableChargeProperty;
import com.wynntils.wynn.model.item.ItemStackTransformManager;
import com.wynntils.wynn.model.item.ItemStackTransformManager.ItemPropertyWriter;

public class ConsumableChargePropertyModel extends Model {
    private static final ItemPropertyWriter CONSUMABLE_CHARGE_WRITER =
            new ItemPropertyWriter(WynnItemMatchers::isConsumable, ConsumableChargeProperty::new);

    public static void init() {
        ItemStackTransformManager.registerProperty(CONSUMABLE_CHARGE_WRITER);
    }

    public static void disable() {
        ItemStackTransformManager.unregisterProperty(CONSUMABLE_CHARGE_WRITER);
    }
}
