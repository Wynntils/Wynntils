/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user.map;

import com.wynntils.core.features.UserFeature;
import com.wynntils.core.features.properties.FeatureCategory;
import com.wynntils.core.features.properties.FeatureInfo;
import com.wynntils.core.managers.Model;
import com.wynntils.wynn.model.territory.GuildTerritoryModel;
import java.util.List;

@FeatureInfo(category = FeatureCategory.MAP)
public class GuildMapFeature extends UserFeature {
    @Override
    public List<Class<? extends Model>> getModelDependencies() {
        return List.of(GuildTerritoryModel.class);
    }
}
