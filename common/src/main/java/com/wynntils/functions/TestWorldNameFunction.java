/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions;

import com.wynntils.core.functions.EnableableFunction;
import com.wynntils.wc.event.WorldStateEvent;
import com.wynntils.wc.model.WorldState;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class TestWorldNameFunction implements EnableableFunction<String> {
    private String currentWorldName;

    @Override
    public String getValue(String argument) {
        return currentWorldName;
    }

    @Override
    public String getName() {
        return "worldname";
    }

    @Override
    public String getDescription() {
        return "Get the current world name";
    }

    @SubscribeEvent
    public void onWorldStateUpdate(WorldStateEvent e) {
        if (e.getNewState() == WorldState.State.WORLD) {
            currentWorldName = e.getWorldName();
        }
    }
}
