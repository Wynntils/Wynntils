/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.features;

import com.google.common.collect.ImmutableList;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.webapi.WebManager;
import com.wynntils.mc.event.WebSetupEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * A single, modular feature that Wynntils provides that can be enabled or disabled. A feature
 * should never be a dependency for anything else.
 *
 * <p>Ex: Soul Point Timer
 */
public abstract class Feature {
    protected final ImmutableList<Condition> conditions;

    protected boolean enabled = false;

    public Feature() {
        ImmutableList.Builder<Condition> conditions = new ImmutableList.Builder<>();

        init(conditions);

        this.conditions = conditions.build();

        if (this.conditions.isEmpty()) {
            enable();
        } else {
            this.conditions.forEach(Condition::init);
        }
    }

    protected void init(ImmutableList.Builder<Condition> conditions) {
        // Override this
    }

    protected void onEnable() {
        // Override this
    }

    protected void onDisable() {
        // Override this
    }

    /**
     * Called to activate a feature
     *
     * <p>Returns whether the feature was successfully activated
     */
    public final void enable() {
        if (enabled)
            throw new IllegalStateException("Feature can not be enabled as it already is enabled");

        WynntilsMod.getEventBus().register(this);
        onEnable();

        enabled = true;
    }

    /** Called for a feature's deactivation */
    public final void disable() {
        if (!enabled)
            throw new IllegalStateException(
                    "Feature can not be disabled as it already is disabled");

        WynntilsMod.getEventBus().unregister(this);
        onDisable();

        enabled = false;
    }

    /** Whether a feature is enabled */
    public final boolean isEnabled() {
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

    public class WebCondition extends Condition {
        private List<Supplier<Boolean>> apis;

        public WebCondition(List<Supplier<Boolean>> apis) {
            this.apis = apis;
        }

        public WebCondition(Supplier<Boolean> api) {
            this.apis = Collections.singletonList(api);
        }

        @Override
        public void init() {
            if (WebManager.isSetup() && checkLoaded()) {
                return;
            }

            WynntilsMod.getEventBus().register(this);
        }

        @SubscribeEvent
        public void onApiLoad(WebSetupEvent e) {
            checkLoaded();
        }

        private boolean checkLoaded() {
            List<Supplier<Boolean>> newList = new ArrayList<>();

            for (Supplier<Boolean> api : apis) {
                if (!api.get()) {
                    newList.add(api);
                }
            }

            apis = newList;

            if (newList.isEmpty()) {
                setSatisfied(true);
                WynntilsMod.getEventBus().unregister(this);
                return true;
            }

            return false;
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
}
