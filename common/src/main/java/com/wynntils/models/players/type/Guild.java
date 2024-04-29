/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.players.type;

import java.util.List;
import java.util.stream.Collectors;

public record Guild(
        String name,
        String prefix,
        int level,
        int xpPercent,
        int territories,
        long wars,
        String createdTimestamp,
        int totalMembers,
        int onlineMembers,
        List<GuildMember> guildMembers) {
    public List<GuildMember> getOnlineMembersbyRank(GuildRank guildRank) {
        return guildMembers.stream()
                .filter(guildMember -> guildMember.rank() == guildRank && guildMember.online())
                .collect(Collectors.toList());
    }
}
