/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.item.properties;

import com.wynntils.core.managers.Managers;
import com.wynntils.core.managers.Model;
import com.wynntils.wynn.item.parsers.WynnItemMatchers;
import com.wynntils.wynn.item.properties.SkillPointProperty;
import com.wynntils.wynn.model.item.ItemStackTransformManager.ItemPropertyWriter;

public final class SkillPointPropertyModel extends Model {
    private static final ItemPropertyWriter SKILL_POINT_WRITER =
            new ItemPropertyWriter(WynnItemMatchers::isSkillPoint, SkillPointProperty::new);

    public void init() {
        Managers.ItemStackTransform.registerProperty(SKILL_POINT_WRITER);
    }

    public void disable() {
        Managers.ItemStackTransform.unregisterProperty(SKILL_POINT_WRITER);
    }
}
