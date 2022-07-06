/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wc.event;

import java.util.Set;
import net.minecraft.server.ServerScoreboard;
import net.minecraftforge.eventbus.api.Event;

public class WynntilsScoreboardUpdateEvent extends Event {
    private final Set<Change> changes;

    public WynntilsScoreboardUpdateEvent(Set<Change> changes) {
        this.changes = changes;
    }

    public Set<Change> getChanges() {
        return changes;
    }

    public record Change(String line, ServerScoreboard.Method method) {}

    public static class QuestChange extends WynntilsScoreboardUpdateEvent {
        public QuestChange(Set<Change> changes) {
            super(changes);
        }
    }

    public static class PartyChange extends WynntilsScoreboardUpdateEvent {
        public PartyChange(Set<Change> changes) {
            super(changes);
        }
    }

    public static class ObjectiveChange extends WynntilsScoreboardUpdateEvent {
        public ObjectiveChange(Set<Change> changes) {
            super(changes);
        }
    }

    public static class GuildAttackTimerChange extends WynntilsScoreboardUpdateEvent {
        public GuildAttackTimerChange(Set<Change> changes) {
            super(changes);
        }
    }

    public enum ChangeType {
        Quest,
        Party,
        Objective,
        GuildAttackTimer;

        public WynntilsScoreboardUpdateEvent toEvent(Set<Change> changes) {
            switch (this) {
                case Quest -> {
                    return new WynntilsScoreboardUpdateEvent.QuestChange(changes);
                }
                case Party -> {
                    return new WynntilsScoreboardUpdateEvent.PartyChange(changes);
                }
                case Objective -> {
                    return new WynntilsScoreboardUpdateEvent.ObjectiveChange(changes);
                }
                case GuildAttackTimer -> {
                    return new WynntilsScoreboardUpdateEvent.GuildAttackTimerChange(changes);
                }
            }

            return new WynntilsScoreboardUpdateEvent(changes);
        }
    }
}
