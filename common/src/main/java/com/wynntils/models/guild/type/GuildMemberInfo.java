/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.guild.type;

public record GuildMemberInfo(
        String username,
        GuildRank rank,
        boolean online,
        String server,
        long contributedXp,
        int contributionRank,
        String joinTimestamp) {}
