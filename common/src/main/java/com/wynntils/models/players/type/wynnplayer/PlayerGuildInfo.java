/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.players.type.wynnplayer;

import com.wynntils.models.guild.type.GuildRank;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public record PlayerGuildInfo(
        UUID guildUuid,
        String guildName,
        String guildPrefix,
        GuildRank guildRank,
        Optional<Integer> contributionRank,
        Optional<Long> contributionXp,
        Optional<Instant> guildJoinTimestamp) {}
