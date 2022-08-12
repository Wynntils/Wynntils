/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.features;

import com.google.common.collect.ImmutableList;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.config.ConfigHolder;
import com.wynntils.core.features.properties.FeatureInfo;
import com.wynntils.core.features.properties.FeatureInfo.Stability;
import com.wynntils.core.managers.Model;

/** Feature for debugging */
@FeatureInfo(stability = Stability.UNSTABLE)
public abstract class DebugFeature extends Feature {
    @Override
    protected void onInit(
            ImmutableList.Builder<Condition> conditions, ImmutableList.Builder<Class<? extends Model>> dependencies) {
        conditions.add(new DevelopmentCondition());
    }

    @Override
    public final void updateConfigOption(ConfigHolder configHolder) {
        onConfigUpdate(configHolder);
    }

    public static class DevelopmentCondition extends Condition {
        @Override
        public void init() {
            setSatisfied(WynntilsMod.isDevelopmentEnvironment());
        }
    }
}
