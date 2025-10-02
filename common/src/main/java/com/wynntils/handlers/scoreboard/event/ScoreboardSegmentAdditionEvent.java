/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.scoreboard.event;

import com.wynntils.core.events.BaseEvent;
import com.wynntils.core.events.CancelRequestable;
import com.wynntils.handlers.scoreboard.ScoreboardSegment;

public final class ScoreboardSegmentAdditionEvent extends BaseEvent implements CancelRequestable {
    private final ScoreboardSegment segment;

    public ScoreboardSegmentAdditionEvent(ScoreboardSegment segment) {
        this.segment = segment;
    }

    public ScoreboardSegment getSegment() {
        return segment;
    }
}
