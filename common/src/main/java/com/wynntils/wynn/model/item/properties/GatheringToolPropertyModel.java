/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.item.properties;

import com.wynntils.core.managers.Managers;
import com.wynntils.core.managers.Model;
import com.wynntils.wynn.item.parsers.WynnItemMatchers;
import com.wynntils.wynn.item.properties.GatheringToolProperty;
import com.wynntils.wynn.model.item.ItemStackTransformManager.ItemPropertyWriter;

public class GatheringToolPropertyModel extends Model {
    private static final ItemPropertyWriter GATHERING_TOOL_WRITER =
            new ItemPropertyWriter(WynnItemMatchers::isGatheringTool, GatheringToolProperty::new);

    public static void init() {
        Managers.ITEM_STACK_TRANSFORM.registerProperty(GATHERING_TOOL_WRITER);
    }

    public static void disable() {
        Managers.ITEM_STACK_TRANSFORM.unregisterProperty(GATHERING_TOOL_WRITER);
    }
}
