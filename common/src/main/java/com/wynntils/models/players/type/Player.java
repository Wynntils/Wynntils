/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.players.type;

public record Player(
        String username,
        boolean online,
        String server,
        String lastJoinTimestamp,
        String guildName,
        String guildPrefix,
        GuildRank guildRank,
        String guildJoinTimestamp) {}
