/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user.inventory;

import com.wynntils.core.features.UserFeature;
import com.wynntils.core.features.properties.FeatureCategory;
import com.wynntils.core.features.properties.FeatureInfo;
import com.wynntils.core.managers.Model;
import com.wynntils.wynn.model.item.IntelligenceSkillPointsItemStackModel;
import java.util.List;

@FeatureInfo(category = FeatureCategory.INVENTORY)
public class IntelligenceManaTableFeature extends UserFeature {
    @Override
    public List<Class<? extends Model>> getModelDependencies() {
        return List.of(IntelligenceSkillPointsItemStackModel.class);
    }
}
