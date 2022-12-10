/*
 * Copyright © Wynntils 2021-2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user;

import com.wynntils.core.features.UserFeature;
import com.wynntils.core.features.properties.FeatureInfo;
import com.wynntils.core.features.properties.FeatureInfo.Stability;
import com.wynntils.core.managers.Model;
import com.wynntils.wynn.model.item.SoulPointItemStackModel;
import java.util.List;

@FeatureInfo(stability = Stability.STABLE)
public class SoulPointTimerFeature extends UserFeature {

    @Override
    public List<Class<? extends Model>> getModelDependencies() {
        return List.of(SoulPointItemStackModel.class);
    }

    public static SoulPointTimerFeature INSTANCE;
}
