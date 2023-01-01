/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.features;

import com.google.common.collect.ImmutableList;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.features.properties.FeatureInfo;
import com.wynntils.core.features.properties.FeatureInfo.Stability;

/** Feature for debugging */
@FeatureInfo(stability = Stability.UNSTABLE)
public abstract class DebugFeature extends UserFeature {
    @Override
    protected void onInit(ImmutableList.Builder<Condition> conditions) {
        conditions.add(new DevelopmentCondition());
    }

    public static class DevelopmentCondition extends Condition {
        @Override
        public void init() {
            setSatisfied(WynntilsMod.isDevelopmentEnvironment());
        }
    }
}
