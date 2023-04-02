/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import com.wynntils.utils.mc.type.CodedString;
import net.minecraft.server.ServerScoreboard;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@Cancelable
public class ScoreboardSetScoreEvent extends Event {
    private final CodedString owner;
    private final String objectiveName;
    private final int score;
    private final ServerScoreboard.Method method;

    public ScoreboardSetScoreEvent(CodedString owner, String objectiveName, int score, ServerScoreboard.Method method) {
        this.owner = owner;
        this.objectiveName = objectiveName;
        this.score = score;
        this.method = method;
    }

    public CodedString getOwner() {
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
