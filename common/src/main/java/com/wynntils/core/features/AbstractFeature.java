/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.features;

public abstract class AbstractFeature extends Feature {
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
}
