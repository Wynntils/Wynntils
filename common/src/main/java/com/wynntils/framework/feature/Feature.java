package com.wynntils.framework.feature;

import com.wynntils.WynntilsMod;

public abstract class Feature {
    public void onEnable() {
        WynntilsMod.eventBus.register(this);
    }

    public void onDisable() {
        WynntilsMod.eventBus.unregister(this);
    }

    public abstract PerformanceImpact getPerformanceImpact();

    public abstract GameplayImpact getGameplayImpactImpact();

    public abstract int getCreationPriority();
}
