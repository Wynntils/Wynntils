/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.features;

import com.google.common.collect.ImmutableList;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.features.properties.GameplayImpact;
import com.wynntils.core.features.properties.PerformanceImpact;
import com.wynntils.core.features.properties.Stability;

/** Feature for debugging */
public abstract class DebugFeature extends Feature {
    @Override
    protected void init(
            ImmutableList.Builder<WebProviderSupplier> apis,
            ImmutableList.Builder<KeySupplier> keybinds,
            ImmutableList.Builder<Condition> conditions) {
        conditions.add(new DevelopmentCondition());
    }

    @Override
    public PerformanceImpact getPerformanceImpact() {
        return PerformanceImpact.MEDIUM;
    }

    @Override
    public GameplayImpact getGameplayImpact() {
        return GameplayImpact.MEDIUM;
    }

    @Override
    public Stability getStability() {
        return Stability.UNSTABLE;
    }

    public class DevelopmentCondition extends Condition {

        @Override
        public void init() {
            setSatisfied(WynntilsMod.developmentEnvironment);
        }
    }
}
