/*
 * Copyright © Wynntils 2021-2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.debug;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.features.DebugFeature;
import com.wynntils.mc.event.ResourcePackEvent;
import com.wynntils.wc.event.WorldStateEvent;
import com.wynntils.wc.model.WorldState.State;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ConnectionProgressFeature extends DebugFeature {

    @SubscribeEvent
    public void onResourcePack(ResourcePackEvent e) {
        WynntilsMod.info("Connection confirmed");
    }

    @SubscribeEvent
    public void onStateChange(WorldStateEvent e) {
        if (e.getNewState() == State.WORLD) {
            WynntilsMod.info("Entering world " + e.getWorldName());
        } else if (e.getOldState() == State.WORLD) {
            WynntilsMod.info("Leaving world");
        }
        String msg =
                switch (e.getNewState()) {
                    case NOT_CONNECTED -> "Disconnected";
                    case CONNECTING -> "Connecting";
                    case CHARACTER_SELECTION -> "In character selection";
                    case HUB -> "On Hub";
                    default -> null;
                };

        if (msg != null) {
            WynntilsMod.info("WorldState change: " + msg);
        }
    }
}
