/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.guild.event;

import com.wynntils.core.events.BaseEvent;

public abstract class GuildEvent extends BaseEvent {
    /**
     * Fired upon the user joining a guild
     * @field guildName the name of the guild joined
     */
    public static final class Joined extends GuildEvent {
        private final String guildName;

        public Joined(String guildName) {
            this.guildName = guildName;
        }

        public String getGuildName() {
            return guildName;
        }
    }

    /**
     * Fired upon the user leaving their guild
     * @field guildName the name of the guiild left
     */
    public static final class Left extends GuildEvent {
        private final String guildName;

        public Left(String guildName) {
            this.guildName = guildName;
        }

        public String getGuildName() {
            return guildName;
        }
    }
}
