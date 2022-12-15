/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.item.properties;

import com.wynntils.core.managers.Managers;
import com.wynntils.core.managers.Model;
import com.wynntils.wynn.item.parsers.WynnItemMatchers;
import com.wynntils.wynn.item.properties.EmeraldPouchTierProperty;
import com.wynntils.wynn.model.item.ItemStackTransformManager.ItemPropertyWriter;

public final class EmeraldPouchTierPropertyModel extends Model {
    private static final ItemPropertyWriter EMERALD_POUCH_TIER_WRITER =
            new ItemPropertyWriter(WynnItemMatchers::isEmeraldPouch, EmeraldPouchTierProperty::new);

    @Override
    public void init() {
        Managers.ItemStackTransform.registerProperty(EMERALD_POUCH_TIER_WRITER);
    }

    @Override
    public void disable() {
        Managers.ItemStackTransform.unregisterProperty(EMERALD_POUCH_TIER_WRITER);
    }
}
