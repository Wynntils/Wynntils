/*
 * Copyright © Wynntils 2021-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import com.wynntils.core.events.BaseEvent;
import java.util.UUID;
import net.minecraft.network.chat.Component;

/** Fires for changes in player info in scoreboard */
public abstract class PlayerInfoEvent extends BaseEvent {
    private final UUID id;

    protected PlayerInfoEvent(UUID id) {
        this.id = id;
    }

    public UUID getId() {
        return id;
    }

    /** Fires on change to name in scoreboard */
    public static class PlayerDisplayNameChangeEvent extends PlayerInfoEvent {
        private final Component displayName;

        public PlayerDisplayNameChangeEvent(UUID id, Component displayName) {
            super(id);
            this.displayName = displayName;
        }

        public Component getDisplayName() {
            return displayName;
        }
    }

    /** Fires on addition of name to scoreboard */
    public static class PlayerLogInEvent extends PlayerInfoEvent {
        private final String name;

        public PlayerLogInEvent(UUID id, String name) {
            super(id);
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    /** Fires on removal of name to scoreboard */
    public static class PlayerLogOutEvent extends PlayerInfoEvent {
        public PlayerLogOutEvent(UUID id) {
            super(id);
        }
    }
}
