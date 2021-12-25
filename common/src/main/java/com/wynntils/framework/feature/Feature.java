/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.framework.feature;

import com.wynntils.WynntilsMod;

public abstract class Feature {
    /** Called on a feature's activation */
    public void onEnable() {
        WynntilsMod.eventBus.register(this);
    }

    /** Called on a feature's deactivation */
    public void onDisable() {
        WynntilsMod.eventBus.unregister(this);
    }

    /** Subjective Performance impact of feature */
    public abstract PerformanceImpact getPerformanceImpact();

    /** Subjective Gameplay impact of feature */
    public abstract GameplayImpact getGameplayImpactImpact();

    /** Priority in which thing was created; features created first have lower priority */
    public abstract int getCreationPriority();
}
