/*
 * Copyright © Wynntils 2021-2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user;

import com.google.common.collect.ImmutableList;
import com.wynntils.core.features.UserFeature;
import com.wynntils.core.features.properties.FeatureInfo;
import com.wynntils.core.features.properties.FeatureInfo.Stability;
import com.wynntils.core.managers.Model;
import com.wynntils.wc.item.ItemStackTransformModel;

@FeatureInfo(stability = Stability.STABLE)
public class SoulPointTimerFeature extends UserFeature {
    @Override
    protected void onInit(
            ImmutableList.Builder<Condition> conditions, ImmutableList.Builder<Class<? extends Model>> dependencies) {
        dependencies.add(ItemStackTransformModel.class);
    }

    public static SoulPointTimerFeature INSTANCE;
}
