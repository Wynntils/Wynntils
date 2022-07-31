/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import net.minecraft.server.ServerScoreboard;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@Cancelable
public class ScoreboardSetScoreEvent extends Event {
    private final String owner;
    private final String objectiveName;
    private final int score;
    private final ServerScoreboard.Method method;

    public ScoreboardSetScoreEvent(String owner, String objectiveName, int score, ServerScoreboard.Method method) {
        this.owner = owner;
        this.objectiveName = objectiveName;
        this.score = score;
        this.method = method;
    }

    public String getOwner() {
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
