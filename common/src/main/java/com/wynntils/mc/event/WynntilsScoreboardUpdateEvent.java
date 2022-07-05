/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import java.util.Map;
import java.util.Set;
import net.minecraft.server.ServerScoreboard;
import net.minecraftforge.eventbus.api.Event;

public class WynntilsScoreboardUpdateEvent extends Event {

    // This maps the change type to the line(s) it was caused by
    private final Map<ChangeType, Set<Change>> changeToLineMap;

    public WynntilsScoreboardUpdateEvent(Map<ChangeType, Set<Change>> changes) {
        this.changeToLineMap = changes;
    }

    public Map<ChangeType, Set<Change>> getChangeMap() {
        return changeToLineMap;
    }

    public record Change(String line, ServerScoreboard.Method method) {}

    public enum ChangeType {
        Quest,
        Party,
        Objective,
        GuildAttackTimer
    }
}
