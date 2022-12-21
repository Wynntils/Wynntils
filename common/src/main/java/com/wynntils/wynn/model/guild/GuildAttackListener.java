/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.guild;

import com.wynntils.core.managers.Models;
import com.wynntils.handlers.scoreboard.ScoreboardListener;
import com.wynntils.handlers.scoreboard.Segment;
import com.wynntils.wynn.model.scoreboard.ScoreboardModel;

public class GuildAttackListener implements ScoreboardListener {
    @Override
    public void onSegmentChange(Segment newValue, ScoreboardModel.SegmentType segmentType) {
        Models.GuildAttackTimer.processChanges(newValue);
    }

    @Override
    public void onSegmentRemove(Segment segment, ScoreboardModel.SegmentType segmentType) {
        Models.GuildAttackTimer.resetTimers();
    }

    @Override
    public void reset() {
        Models.GuildAttackTimer.resetTimers();
    }
}
