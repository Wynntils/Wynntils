/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.functionalities;

import com.wynntils.core.WynntilsMod;
import com.wynntils.mc.utils.ComponentUtils;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;

/**
 * A functionality that Wynntils provides. Functionalities are instance based.
 *
 * <p>Use a functionality if you need to work with events, but the events do not correlate directly to any feature.
 *
 * <p>Ex: Lootrun Functionality
 */
public abstract class Functionality {
    protected static Functionality INSTANCE = null;

    protected boolean registeredToEventBus = false;

    /**
     * Call this to register Functionality class to the EventBus.
     *
     * <p>Since this method registers the class of the Functionality, only static events will be registered.
     */
    public final void registerToEventBus() {
        if (registeredToEventBus)
            throw new IllegalStateException(
                    "Functionality cannot be registered as it is already registered to event bus.");

        registeredToEventBus = true;
        WynntilsMod.getEventBus().register(this.getClass());
    }

    /**
     * Call this to unregister Functionality from the EventBus.
     */
    public final void unregisterFromEventBus() {
        if (!registeredToEventBus)
            throw new IllegalStateException(
                    "Functionality cannot be unregistered as it is not registered to event bus.");

        registeredToEventBus = false;
        WynntilsMod.getEventBus().unregister(this.getClass());
    }

    public static Functionality getInstance() {
        return INSTANCE;
    }

    public boolean isInstantiated() {
        return INSTANCE != null;
    }

    /** Gets the name of a feature */
    public String getName() {
        return ComponentUtils.getFormatted(getNameComponent());
    }

    public MutableComponent getNameComponent() {
        return TextComponent.EMPTY.copy();
    }
}
