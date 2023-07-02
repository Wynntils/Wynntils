/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.tracker;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Handlers;
import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.scoreboard.ScoreboardPart;
import com.wynntils.models.quests.QuestInfo;
import com.wynntils.models.tracker.event.TrackerUpdatedEvent;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.utils.mc.StyledTextUtils;
import com.wynntils.utils.mc.type.Location;
import java.util.List;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class TrackerModel extends Model {
    private static final ScoreboardPart TRACKER_SCOREBOARD_PART = new TrackerScoreboardPart();

    private String trackedName;
    private String trackedType;
    private StyledText trackedTask;

    public TrackerModel() {
        super(List.of());

        Handlers.Scoreboard.addPart(TRACKER_SCOREBOARD_PART);
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onWorldStateChanged(WorldStateEvent e) {
        reset();
    }

    public String getTrackedName() {
        return trackedName;
    }

    public String getTrackedType() {
        return trackedType;
    }

    public StyledText getTrackedTask() {
        return trackedTask;
    }

    public Location getTrackedLocation() {
        if (trackedName == null) return null;

        return StyledTextUtils.extractLocation(trackedTask).orElse(null);
    }

    public QuestInfo getTrackedQuestInfo() {
        return Models.Quest.getQuestInfoFromName(trackedName).orElse(null);
    }

    void updateTrackerFromScoreboard(String type, String name, StyledText nextTask) {
        trackedType = type;
        trackedName = name;
        trackedTask = nextTask;

        WynntilsMod.postEvent(new TrackerUpdatedEvent(trackedType, trackedName, trackedTask));
    }

    private void reset() {
        trackedName = null;
        trackedType = null;
        trackedTask = null;
    }

}
