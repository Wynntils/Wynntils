/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.scoreboard.event;

import com.wynntils.core.events.BaseEvent;
import com.wynntils.core.events.OperationCancelable;
import com.wynntils.handlers.scoreboard.ScoreboardSegment;

public class ScoreboardSegmentAdditionEvent extends BaseEvent implements OperationCancelable {
    private final ScoreboardSegment segment;

    public ScoreboardSegmentAdditionEvent(ScoreboardSegment segment) {
        this.segment = segment;
    }

    public ScoreboardSegment getSegment() {
        return segment;
    }
}
