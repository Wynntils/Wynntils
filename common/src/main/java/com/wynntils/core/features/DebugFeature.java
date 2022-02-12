/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.features;

import com.google.common.collect.ImmutableList;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.features.properties.FeatureInfo;
import com.wynntils.core.features.properties.GameplayImpact;
import com.wynntils.core.features.properties.PerformanceImpact;
import com.wynntils.core.features.properties.Stability;

/** Feature for debugging */
@FeatureInfo(
        stability = Stability.UNSTABLE,
        gameplay = GameplayImpact.MEDIUM,
        performance = PerformanceImpact.MEDIUM)
public abstract class DebugFeature extends Feature {
    @Override
    protected void init(ImmutableList.Builder<Condition> conditions) {
        conditions.add(new DevelopmentCondition());
    }

    public class DevelopmentCondition extends Condition {

        @Override
        public void init() {
            setSatisfied(WynntilsMod.developmentEnvironment);
        }
    }
}
