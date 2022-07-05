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
import com.wynntils.wc.utils.scoreboard.quests.QuestManager;
import net.minecraft.server.ServerScoreboard;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ScoreboardManager {

    public static void init() {
        WynntilsMod.getEventBus().register(ScoreboardManager.class);
    }

    @SubscribeEvent
    public static void onSetScore(ScoreboardSetScoreEvent event) {
        if (event.getMethod() == ServerScoreboard.Method.CHANGE) {
            // TODO: It can be wasteful to do all these parsing on every change event
            // since when the player is in a party, this event is fired a lot of times to
            // account for the health updates in the overlay.
            // Make sure to fix this when PartyManager is implemented and filter out those "useless" events.
            // Same behavior can also happen with certain (guild/daily) objectives in my findings.

            ObjectiveManager.tryUpdateObjective(event.getOwner());

            QuestManager.tryParseQuest();
        } else { // Method is REMOVE
            ObjectiveManager.tryRemoveObjective(event.getOwner());

            QuestManager.checkIfTrackingStopped(event.getOwner());
        }
    }

    @SubscribeEvent
    public static void onWorldStateChange(WorldStateEvent event) {
        if (event.getNewState() == WorldState.State.WORLD) return;

        ObjectiveManager.resetObjectives();
    }
}
