/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import com.wynntils.core.text.StyledText;
import net.minecraft.server.ServerScoreboard;
import net.minecraftforge.eventbus.api.Event;

public class ScoreboardSetScoreEvent extends Event {
    private final StyledText owner;
    private final String objectiveName;
    private final int score;
    private final ServerScoreboard.Method method;

    public ScoreboardSetScoreEvent(StyledText owner, String objectiveName, int score, ServerScoreboard.Method method) {
        this.owner = owner;
        this.objectiveName = objectiveName;
        this.score = score;
        this.method = method;
    }

    public StyledText getOwner() {
        return owner;
    }

    public String getObjectiveName() {
        return objectiveName;
    }

    public int getScore() {
        return score;
    }

    public ServerScoreboard.Method getMethod() {
        return method;
    }
}
