/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.scoreboard.event;

import com.wynntils.handlers.scoreboard.ScoreboardPart;
import com.wynntils.handlers.scoreboard.ScoreboardSegment;
import com.wynntils.utils.type.Pair;
import java.util.Collections;
import java.util.List;
import net.neoforged.bus.api.Event;

public class ScoreboardUpdatedEvent extends Event {
    private final List<Pair<ScoreboardPart, ScoreboardSegment>> scoreboardSegments;

    public ScoreboardUpdatedEvent(List<Pair<ScoreboardPart, ScoreboardSegment>> scoreboardSegments) {
        this.scoreboardSegments = scoreboardSegments;
    }

    public List<Pair<ScoreboardPart, ScoreboardSegment>> getScoreboardSegments() {
        return Collections.unmodifiableList(scoreboardSegments);
    }
}
