/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.item.properties;

import com.wynntils.core.managers.Managers;
import com.wynntils.core.managers.Model;
import com.wynntils.wynn.item.properties.SearchOverlayProperty;
import com.wynntils.wynn.model.item.ItemStackTransformManager.ItemPropertyWriter;

public class SearchOverlayPropertyModel extends Model {
    private static final ItemPropertyWriter SEARCH_OVERLAY_WRITER =
            new ItemPropertyWriter(itemstack -> true, SearchOverlayProperty::new);

    public static void init() {
        Managers.ITEM_STACK_TRANSFORM.registerProperty(SEARCH_OVERLAY_WRITER);
    }

    public static void disable() {
        Managers.ITEM_STACK_TRANSFORM.unregisterProperty(SEARCH_OVERLAY_WRITER);
    }
}
