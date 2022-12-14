/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user.tooltips;

import com.wynntils.core.config.Config;
import com.wynntils.core.features.UserFeature;
import com.wynntils.core.features.properties.FeatureCategory;
import com.wynntils.core.features.properties.FeatureInfo;
import com.wynntils.core.features.properties.FeatureInfo.Stability;
import com.wynntils.core.managers.Model;
import com.wynntils.wynn.item.ItemStackTransformModel;
import java.util.List;

@FeatureInfo(stability = Stability.STABLE, category = FeatureCategory.TOOLTIPS)
public class ItemGuessFeature extends UserFeature {
    public static ItemGuessFeature INSTANCE;

    @Config
    public boolean showGuessesPrice = true;

    @Override
    public List<Class<? extends Model>> getModelDependencies() {
        return List.of(ItemStackTransformModel.class);
    }
}
