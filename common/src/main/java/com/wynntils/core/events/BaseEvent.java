/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.events;

import net.neoforged.bus.api.Event;

/**
 * This is the base event that all Wynntils events should extend.
 */
public abstract class BaseEvent extends Event {
    protected boolean cancelRequested = false;
}
