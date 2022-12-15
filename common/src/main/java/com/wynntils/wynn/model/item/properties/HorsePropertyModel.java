/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.item.properties;

import com.wynntils.core.managers.Managers;
import com.wynntils.core.managers.Model;
import com.wynntils.wynn.item.parsers.WynnItemMatchers;
import com.wynntils.wynn.item.properties.HorseProperty;
import com.wynntils.wynn.model.item.ItemStackTransformManager.ItemPropertyWriter;

public final class HorsePropertyModel extends Model {
    private static final ItemPropertyWriter HORSE_WRITER =
            new ItemPropertyWriter(WynnItemMatchers::isHorse, HorseProperty::new);

    public void init() {
        Managers.ItemStackTransform.registerProperty(HORSE_WRITER);
    }

    public void disable() {
        Managers.ItemStackTransform.unregisterProperty(HORSE_WRITER);
    }
}
