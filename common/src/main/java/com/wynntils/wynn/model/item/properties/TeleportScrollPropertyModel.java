/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.item.properties;

import com.wynntils.core.managers.Managers;
import com.wynntils.core.managers.Model;
import com.wynntils.wynn.item.parsers.WynnItemMatchers;
import com.wynntils.wynn.item.properties.TeleportScrollProperty;
import com.wynntils.wynn.model.item.ItemStackTransformManager.ItemPropertyWriter;

public final class TeleportScrollPropertyModel extends Model {
    private static final ItemPropertyWriter TELEPORT_SCROLL_WRITER =
            new ItemPropertyWriter(WynnItemMatchers::isTeleportScroll, TeleportScrollProperty::new);

    public static void init() {
        Managers.ItemStackTransform.registerProperty(TELEPORT_SCROLL_WRITER);
    }

    public static void disable() {
        Managers.ItemStackTransform.unregisterProperty(TELEPORT_SCROLL_WRITER);
    }
}
