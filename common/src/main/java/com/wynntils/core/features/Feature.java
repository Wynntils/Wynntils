/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.features;

import com.wynntils.core.webapi.WebManager;
import com.wynntils.mc.utils.keybinds.KeyHolder;
import com.wynntils.mc.utils.keybinds.KeyManager;
import com.wynntils.utils.Utils;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * A single, modular feature that Wynntils provides that can be enabled or disabled. A feature
 * should never be a dependency for anything else.
 *
 * <p>Ex: Soul Point Timer
 */
public abstract class Feature {
    protected boolean enabled = false;

    /** List of web providers to mark for loading */
    protected List<Supplier<WebManager.StaticProvider>> apis = new ArrayList<>();

    /** List of keybinds to load */
    protected List<Supplier<KeyHolder>> keybinds = new ArrayList<>();

    /**
     * Called for a feature's activation
     *
     * <p>Returns whether the feature was successfully activated
     */
    public boolean onEnable() {
        if (enabled)
            throw new IllegalStateException("Feature can not be enabled as it already is enabled");

        if (!loadAPIs(false)) return false;

        Utils.getEventBus().register(this);
        Utils.getEventBus().register(this.getClass());

        loadKeybinds();

        enabled = true;
        return true;
    }

    /**
     * Called to try and enable the apis the feature is dependent on Returns if feature can be
     * safely activated
     */
    public boolean loadAPIs(boolean async) {
        if (!apis.isEmpty()) {
            if (!WebManager.isSetup()) return false;

            for (Supplier<WebManager.StaticProvider> apiSupplier : apis) {
                apiSupplier.get().markToLoad();
            }

            WebManager.loadMarked(async);
        }

        return true;
    }

    /** Called to try and enable a feature's keybinds */
    public void loadKeybinds() {
        keybinds.forEach(k -> KeyManager.registerKeybinding(k.get()));
    }

    /** Called to try and disable a feature's keybinds */
    public void unloadKeybinds() {
        keybinds.forEach(k -> KeyManager.unregisterKeybind(k.get()));
    }

    /** Called for a feature's deactivation */
    public void onDisable() {
        if (!enabled)
            throw new IllegalStateException(
                    "Feature can not be disabled as it already is disabled");

        unloadKeybinds();

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
