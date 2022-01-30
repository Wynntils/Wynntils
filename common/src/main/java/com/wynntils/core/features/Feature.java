/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.features;

import com.google.common.collect.ImmutableList;
import com.wynntils.core.Reference;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.webapi.WebManager;
import com.wynntils.mc.event.WebSetupEvent;
import com.wynntils.mc.utils.keybinds.KeyHolder;
import com.wynntils.mc.utils.keybinds.KeyManager;

/**
 * A single, modular feature that Wynntils provides that can be enabled or disabled. A feature
 * should never be a dependency for anything else.
 *
 * <p>Ex: Soul Point Timer
 */
public abstract class Feature {
    protected final ImmutableList<Condition> conditions;

    protected boolean enabled = false;

    /** List of web providers to mark for loading */
    protected final ImmutableList<WebProviderSupplier> apis;

    /** List of keybinds to load */
    protected final ImmutableList<KeySupplier> keybinds;

    public Feature() {
        ImmutableList.Builder<WebProviderSupplier> apis = new ImmutableList.Builder<>();
        ImmutableList.Builder<KeySupplier> keybinds = new ImmutableList.Builder<>();
        ImmutableList.Builder<Condition> conditions = new ImmutableList.Builder<>();
        init(apis, keybinds, conditions);
        this.apis = apis.build();
        if (!this.apis.isEmpty()) { // Requires web to be loaded
            conditions.add(new WebLoadedCondition());
        }

        this.keybinds = keybinds.build();
        this.conditions = conditions.build();

        if (this.conditions.isEmpty()) {
            enable();
        } else {
            this.conditions.forEach(Condition::init);
        }
    }

    protected void init(
            ImmutableList.Builder<WebProviderSupplier> apis,
            ImmutableList.Builder<KeySupplier> keybinds,
            ImmutableList.Builder<Condition> conditions) {
        // Override this
    }

    /**
     * Called to activate a feature
     *
     * <p>Returns whether the feature was successfully activated
     */
    public boolean enable() {
        if (enabled)
            throw new IllegalStateException("Feature can not be enabled as it already is enabled");

        if (!loadAPIs(false)) return false;

        WynntilsMod.getEventBus().register(this);
        WynntilsMod.getEventBus().register(this.getClass());

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

            for (WebProviderSupplier apiSupplier : apis) {
                apiSupplier.getProvider().markToLoad();
            }

            WebManager.loadMarked(async);
        }

        return true;
    }

    /** Called to try and enable a feature's keybinds */
    public void loadKeybinds() {
        keybinds.forEach(k -> KeyManager.registerKeybinding(k.getKeyHolder()));
    }

    /** Called to try and disable a feature's keybinds */
    public void unloadKeybinds() {
        keybinds.forEach(k -> KeyManager.unregisterKeybind(k.getKeyHolder()));
    }

    /** Called for a feature's deactivation */
    public void disable() {
        if (!enabled)
            throw new IllegalStateException(
                    "Feature can not be disabled as it already is disabled");

        unloadKeybinds();

        WynntilsMod.getEventBus().unregister(this);
        WynntilsMod.getEventBus().unregister(this.getClass());

        enabled = false;
    }

    /** Returns whether a feature is api dependent */
    public boolean isApiDependent() {
        return !apis.isEmpty();
    }

    /** Whether a feature is enabled */
    public boolean isEnabled() {
        return enabled;
    }

    /** Whether a feature can be enabled */
    private boolean canEnable() {
        for (Condition condition : conditions) {
            if (!condition.isSatisfied()) return false;
        }

        return true;
    }

    private void checkConditions() {
        if (canEnable() && !enabled) {
            enable();
        } else if (enabled) {
            disable();
        }
    }

    public class WebLoadedCondition extends Condition {
        @Override
        public void init() {
            if (WebManager.isSetup()) {
                setSatisfied(true);
                return;
            }

            WynntilsMod.getEventBus()
                    .<WebSetupEvent>addListener(
                            e -> {
                                setSatisfied(true);
                            });
        }
    }

    public abstract class Condition {
        boolean satisfied = false;

        public boolean isSatisfied() {
            return satisfied;
        }

        public abstract void init();

        public void setSatisfied(boolean satisfied) {
            this.satisfied = satisfied;
            checkConditions();
        }
    }

    @FunctionalInterface
    public interface KeySupplier {
        KeyHolder getKeyHolder();
    }

    @FunctionalInterface
    public interface WebProviderSupplier {
        WebManager.StaticProvider getProvider();
    }
}
