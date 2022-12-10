/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.item.properties;

import com.wynntils.core.managers.Model;
import com.wynntils.wynn.item.parsers.WynnItemMatchers;
import com.wynntils.wynn.item.properties.SkillPointProperty;
import com.wynntils.wynn.model.item.ItemStackTransformManager;
import com.wynntils.wynn.model.item.ItemStackTransformManager.ItemPropertyWriter;

public class SkillPointPropertyModel extends Model {
    private static final ItemPropertyWriter SKILL_POINT_WRITER =
            new ItemPropertyWriter(WynnItemMatchers::isSkillPoint, SkillPointProperty::new);

    public static void init() {
        ItemStackTransformManager.registerProperty(SKILL_POINT_WRITER);
    }

    public static void disable() {
        ItemStackTransformManager.unregisterProperty(SKILL_POINT_WRITER);
    }
}
