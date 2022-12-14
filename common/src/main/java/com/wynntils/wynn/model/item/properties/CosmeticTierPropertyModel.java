/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.item.properties;

import com.wynntils.core.managers.Managers;
import com.wynntils.core.managers.Model;
import com.wynntils.wynn.item.parsers.WynnItemMatchers;
import com.wynntils.wynn.item.properties.CosmeticTierProperty;
import com.wynntils.wynn.model.item.ItemStackTransformManager.ItemPropertyWriter;

public class CosmeticTierPropertyModel extends Model {
    private static final ItemPropertyWriter COSMETIC_TIER_PROPERTY_WRITER =
            new ItemPropertyWriter(WynnItemMatchers::isCosmetic, CosmeticTierProperty::new);

    public static void init() {
        Managers.ItemStackTransform.registerProperty(COSMETIC_TIER_PROPERTY_WRITER);
    }

    public static void disable() {
        Managers.ItemStackTransform.unregisterProperty(COSMETIC_TIER_PROPERTY_WRITER);
    }
}
