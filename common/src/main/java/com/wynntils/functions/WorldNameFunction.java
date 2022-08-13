/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions;

import com.wynntils.core.functions.ActiveFunction;
import com.wynntils.wc.event.WorldStateEvent;
import com.wynntils.wc.model.WorldStateManager;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class WorldNameFunction extends ActiveFunction<String> {
    private String currentWorldName;

    @Override
    public String getValue(String argument) {
        return currentWorldName;
    }

    @SubscribeEvent
    public void onWorldStateUpdate(WorldStateEvent e) {
        if (e.getNewState() == WorldStateManager.State.WORLD) {
            currentWorldName = e.getWorldName();
        }
    }
}
