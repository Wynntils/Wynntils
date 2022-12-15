/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.item.properties;

import com.wynntils.core.managers.Managers;
import com.wynntils.core.managers.Model;
import com.wynntils.wynn.item.parsers.WynnItemMatchers;
import com.wynntils.wynn.item.properties.DungeonKeyProperty;
import com.wynntils.wynn.model.item.ItemStackTransformManager.ItemPropertyWriter;

public final class DungeonKeyPropertyModel extends Model {
    private static final ItemPropertyWriter DUNGEON_KEY_WRITER =
            new ItemPropertyWriter(WynnItemMatchers::isDungeonKey, DungeonKeyProperty::new);

    public static void init() {
        Managers.ItemStackTransform.registerProperty(DUNGEON_KEY_WRITER);
    }

    public static void disable() {
        Managers.ItemStackTransform.unregisterProperty(DUNGEON_KEY_WRITER);
    }
}
