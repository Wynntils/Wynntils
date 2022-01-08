/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.features;

import com.wynntils.core.webapi.WebManager;
import com.wynntils.utils.Utils;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public abstract class Feature {
    /** List of providers to mark for loading */
    protected List<Supplier<WebManager.StaticProvider>> apis = new ArrayList<>();

    /** Called on a feature's activation */
    public void onEnable() {
        if (!apis.isEmpty()) {
            for (Supplier<WebManager.StaticProvider> apiSupplier : apis) {
                apiSupplier.get().markToLoad();
            }

            WebManager.loadMarked(false);
        }

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
