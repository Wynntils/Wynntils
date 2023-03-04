/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.events;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Managers;
import net.minecraftforge.eventbus.api.Event;

public final class MixinHelper {
    public static <T extends Event> T post(T event) {
        if (Managers.Connection.onServer()) {
            WynntilsMod.postEvent(event);
        }
        return event;
    }

    public static void postAlways(Event event) {
        WynntilsMod.postEvent(event);
    }
}
