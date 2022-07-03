/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wc.utils.scoreboard;

import com.wynntils.core.WynntilsMod;
import com.wynntils.mc.event.ScoreboardSetScoreEvent;
import com.wynntils.wc.event.WorldStateEvent;
import com.wynntils.wc.model.WorldState;
import com.wynntils.wc.utils.scoreboard.objectives.ObjectiveManager;
import net.minecraft.server.ServerScoreboard;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ScoreboardManager {

    public static void init() {
        WynntilsMod.getEventBus().register(ScoreboardManager.class);
    }

    @SubscribeEvent
    public static void onSetScore(ScoreboardSetScoreEvent event) {
        if (event.getMethod() == ServerScoreboard.Method.CHANGE) {
            ObjectiveManager.tryUpdateObjective(event.getOwner());
        } else { // Method is REMOVE
            ObjectiveManager.tryRemoveObjective(event.getOwner());
        }
    }

    @SubscribeEvent
    public static void onWorldStateChange(WorldStateEvent event) {
        if (event.getNewState() == WorldState.State.WORLD) return;

        ObjectiveManager.resetObjectives();
    }
}
