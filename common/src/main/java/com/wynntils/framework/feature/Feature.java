/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.framework.feature;

import com.wynntils.utils.Utils;

public abstract class Feature {
    /** Called on a feature's activation */
    public void onEnable() {
        Utils.getEventBus().register(this);
        Utils.getEventBus().register(this.getClass());
    }

    /** Called on a feature's deactivation */
    public void onDisable() {
        Utils.getEventBus().unregister(this);
        Utils.getEventBus().unregister(this.getClass());
    }

    /** Subjective Performance impact of feature */
    public abstract PerformanceImpact getPerformanceImpact();

    /** Subjective Gameplay impact of feature */
    public abstract GameplayImpact getGameplayImpact();

    /** Subjective stability of feature */
    public abstract Stability getStability();
}
