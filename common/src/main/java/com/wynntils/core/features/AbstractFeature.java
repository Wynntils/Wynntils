/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.features;

public abstract class AbstractFeature extends Feature {
    @Override
    public PerformanceImpact getPerformanceImpact() {
        return PerformanceImpact.Medium;
    }

    @Override
    public GameplayImpact getGameplayImpact() {
        return GameplayImpact.Medium;
    }

    @Override
    public Stability getStability() {
        return Stability.Unstable;
    }
}
