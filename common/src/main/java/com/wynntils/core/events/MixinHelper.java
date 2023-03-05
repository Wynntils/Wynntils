/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.events;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Managers;
import net.minecraftforge.eventbus.api.Event;

public final class MixinHelper {
    public static <T extends Event> T post(T event) {
        if (!onWynncraft()) return event;

        WynntilsMod.postEvent(event);
        return event;
    }

    public static boolean onWynncraft() {
        return Managers.Connection.onServer();
    }

    /**
     * Post event without checking if we are connected to a Wynncraft server
     */
    public static void postAlways(Event event) {
        WynntilsMod.postEvent(event);
    }
}
