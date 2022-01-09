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
    protected boolean enabled;

    /** List of providers to mark for loading */
    protected List<Supplier<WebManager.StaticProvider>> apis = new ArrayList<>();

    /** Called on a feature's activation; Returns whether it was a success */
    public void onEnable() {
        if (enabled)
            throw new IllegalStateException("Feature can not be enabled as it already is enabled");

        if (!apis.isEmpty()) {
            if (!WebManager.isSetup()) return;

            for (Supplier<WebManager.StaticProvider> apiSupplier : apis) {
                apiSupplier.get().markToLoad();
            }

            WebManager.loadMarked(false);
        }

        Utils.getEventBus().register(this);
        Utils.getEventBus().register(this.getClass());

        enabled = true;
    }

    /** Called on a feature's deactivation */
    public void onDisable() {
        if (!enabled)
            throw new IllegalStateException(
                    "Feature can not be disabled as it already is disabled");

        Utils.getEventBus().unregister(this);
        Utils.getEventBus().unregister(this.getClass());

        enabled = false;
    }

    /** Subjective Performance impact of feature */
    public abstract PerformanceImpact getPerformanceImpact();

    /** Subjective Gameplay impact of feature */
    public abstract GameplayImpact getGameplayImpact();

    /** Subjective stability of feature */
    public abstract Stability getStability();
}
