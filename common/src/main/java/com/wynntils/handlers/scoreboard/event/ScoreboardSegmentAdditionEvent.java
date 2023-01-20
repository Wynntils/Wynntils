/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.scoreboard.event;

import com.wynntils.core.events.EventThread;
import com.wynntils.handlers.scoreboard.ScoreboardSegment;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@Cancelable
@EventThread(EventThread.Type.WORKER)
public class ScoreboardSegmentAdditionEvent extends Event {
    private final ScoreboardSegment segment;

    public ScoreboardSegmentAdditionEvent(ScoreboardSegment segment) {
        this.segment = segment;
    }

    public ScoreboardSegment getSegment() {
        return segment;
    }
}
