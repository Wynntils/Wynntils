/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.item.properties;

import com.wynntils.core.managers.Managers;
import com.wynntils.core.managers.Model;
import com.wynntils.wynn.item.parsers.WynnItemMatchers;
import com.wynntils.wynn.item.properties.PowderTierProperty;
import com.wynntils.wynn.model.item.ItemStackTransformManager.ItemPropertyWriter;

public final class PowderTierPropertyModel extends Model {
    private static final ItemPropertyWriter POWDER_TIER_WRITER =
            new ItemPropertyWriter(WynnItemMatchers::isPowder, PowderTierProperty::new);

    @Override
    public void init() {
        Managers.ItemStackTransform.registerProperty(POWDER_TIER_WRITER);
    }

    @Override
    public void disable() {
        Managers.ItemStackTransform.unregisterProperty(POWDER_TIER_WRITER);
    }
}
