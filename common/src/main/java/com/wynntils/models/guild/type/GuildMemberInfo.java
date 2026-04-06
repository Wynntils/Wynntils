/*
 * Copyright © Wynntils 2024-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.guild.type;

import java.util.UUID;

public record GuildMemberInfo(
        UUID uuid,
        String username,
        GuildRank rank,
        boolean online,
        String server,
        long contributedXp,
        int contributionRank,
        String joinTimestamp) {}
