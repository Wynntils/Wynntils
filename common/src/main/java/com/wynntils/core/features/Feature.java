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

/**
 * A single, modular feature that Wynntils provides that can be enabled or disabled. A feature should never be a
 * dependency for anything else.
 *
 * Ex: Soul Point Timer
 */
public abstract class Feature {
    protected boolean enabled = false;

    /** List of providers to mark for loading */
    protected List<Supplier<WebManager.StaticProvider>> apis = new ArrayList<>();

    /**
     * Called for a feature's activation
     *
     * <p>Returns whether the feature was successfully activated
     */
    public boolean onEnable() {
        if (enabled)
            throw new IllegalStateException("Feature can not be enabled as it already is enabled");

        if (!tryEnableAPIS(false)) return false;

        Utils.getEventBus().register(this);
        Utils.getEventBus().register(this.getClass());

        enabled = true;
        return true;
    }

    /**
     * Called to try and enable the apis the feature is dependent on Returns if feature can be
     * safely activated
     */
    public boolean tryEnableAPIS(boolean async) {
        if (!apis.isEmpty()) {
            if (!WebManager.isSetup()) return false;

            for (Supplier<WebManager.StaticProvider> apiSupplier : apis) {
                apiSupplier.get().markToLoad();
            }

            WebManager.loadMarked(async);
        }

        return true;
    }

    /** Called for a feature's deactivation */
    public void onDisable() {
        if (!enabled)
            throw new IllegalStateException(
                    "Feature can not be disabled as it already is disabled");

        Utils.getEventBus().unregister(this);
        Utils.getEventBus().unregister(this.getClass());

        enabled = false;
    }

    /** Returns whether a feature is api dependent */
    public boolean isApiDependent() {
        return !apis.isEmpty();
    }

    /** Subjective Performance impact of feature */
    public abstract PerformanceImpact getPerformanceImpact();

    /** Subjective Gameplay impact of feature */
    public abstract GameplayImpact getGameplayImpact();

    /** Subjective stability of feature */
    public abstract Stability getStability();

    /** Whether a feature is enabled */
    public boolean isEnabled() {
        return enabled;
    }
}
