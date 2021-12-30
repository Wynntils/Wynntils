/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraftforge.eventbus.api.Event;

public abstract class PlayerInfoEvent extends Event {
    private final UUID id;

    public PlayerInfoEvent(UUID id) {
        this.id = id;
    }

    public UUID getId() {
        return id;
    }

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

    public static class PlayerLogOutEvent extends PlayerInfoEvent {
        public PlayerLogOutEvent(UUID id) {
            super(id);
        }
    }
}
