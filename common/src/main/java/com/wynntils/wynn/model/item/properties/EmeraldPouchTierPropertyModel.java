/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.item.properties;

import com.wynntils.core.managers.Model;
import com.wynntils.wynn.item.parsers.WynnItemMatchers;
import com.wynntils.wynn.item.properties.EmeraldPouchTierProperty;
import com.wynntils.wynn.model.item.ItemStackTransformManager;
import com.wynntils.wynn.model.item.ItemStackTransformManager.ItemPropertyWriter;

public class EmeraldPouchTierPropertyModel extends Model {
    private static final ItemPropertyWriter EMERALD_POUCH_TIER_WRITER =
            new ItemPropertyWriter(WynnItemMatchers::isEmeraldPouch, EmeraldPouchTierProperty::new);

    public static void init() {
        ItemStackTransformManager.registerProperty(EMERALD_POUCH_TIER_WRITER);
    }

    public static void disable() {
        ItemStackTransformManager.unregisterProperty(EMERALD_POUCH_TIER_WRITER);
    }
}
