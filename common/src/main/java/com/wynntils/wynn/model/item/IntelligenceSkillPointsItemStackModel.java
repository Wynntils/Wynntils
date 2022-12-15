/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.item;

import com.wynntils.core.managers.Managers;
import com.wynntils.core.managers.Model;
import com.wynntils.wynn.item.IntelligenceSkillPointsItemStack;
import com.wynntils.wynn.item.parsers.WynnItemMatchers;
import com.wynntils.wynn.model.item.ItemStackTransformManager.ItemStackTransformer;

public final class IntelligenceSkillPointsItemStackModel extends Model {
    private static final ItemStackTransformer INTELLIGENCE_SKILL_POINTS_TRANSFORMER = new ItemStackTransformer(
            WynnItemMatchers::isIntelligenceSkillPoints, IntelligenceSkillPointsItemStack::new);

    public void init() {
        Managers.ItemStackTransform.registerTransformer(INTELLIGENCE_SKILL_POINTS_TRANSFORMER);
    }

    public void disable() {
        Managers.ItemStackTransform.unregisterTransformer(INTELLIGENCE_SKILL_POINTS_TRANSFORMER);
    }
}
