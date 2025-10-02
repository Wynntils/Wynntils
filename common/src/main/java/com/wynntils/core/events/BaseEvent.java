/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.events;

import net.neoforged.bus.api.Event;

public class BaseEvent extends Event {
    protected boolean isOperationCanceled = false;
}
