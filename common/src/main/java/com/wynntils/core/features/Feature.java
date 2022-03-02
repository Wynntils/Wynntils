/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.features;

import com.google.common.collect.ImmutableList;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.webapi.WebManager;
import com.wynntils.mc.event.WebSetupEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * A single, modular feature that Wynntils provides that can be enabled or disabled. A feature
 * should never be a dependency for anything else.
 *
 * <p>Ex: Soul Point Timer
 */
public abstract class Feature {
    private ImmutableList<Condition> conditions;
    private final String name;

    protected boolean enabled = false;

    protected Feature(String name) {
        this.name = name;
    }

    public final void init() {
        ImmutableList.Builder<Condition> conditions = new ImmutableList.Builder<>();

        onInit(conditions);

        this.conditions = conditions.build();

        if (this.conditions.isEmpty()) {
            enable();
        } else {
            this.conditions.forEach(Condition::init);
        }
    }

    /** Called on init of Feature */
    protected abstract void onInit(ImmutableList.Builder<Condition> conditions);

    /**
     * Called on enabling of Feature
     *
     * <p>Return false to cancel enabling, return true to continue. Note that if a feature's enable
     * is cancelled it isn't called again by the conditions and must be done so manually, likely by
     * the user.
     */
    protected abstract boolean onEnable();

    /** Called on disabling of Feature */
    protected abstract void onDisable();

    /** Called to activate a feature */
    public final void enable() {
        if (enabled)
            throw new IllegalStateException("Feature can not be enabled as it already is enabled");

        if (!onEnable()) {
            return;
        }

        enabled = true;
    }

    /** Called for a feature's deactivation */
    public final void disable() {
        if (!enabled)
            throw new IllegalStateException(
                    "Feature can not be disabled as it already is disabled");

        onDisable();

        enabled = false;
    }

    /** Whether a feature is enabled */
    public final boolean isEnabled() {
        return enabled;
    }

    /** Whether a feature can be enabled */
    public boolean canEnable() {
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

            WynntilsMod.getEventBus().register(this);
        }

        @SubscribeEvent
        public void onWebSetup(WebSetupEvent e) {
            setSatisfied(true);
            WynntilsMod.getEventBus().unregister(this);
        }
    }

    public String getName() {
        return name;
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
