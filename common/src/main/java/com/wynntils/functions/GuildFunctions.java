/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions;

import com.wynntils.core.components.Models;
import com.wynntils.models.guild.type.GuildRank;
import com.wynntils.models.players.type.wynnplayer.PlayerGuildInfo;
import com.wynntils.models.players.type.wynnplayer.WynnPlayerInfo;
import com.wynntils.templates.annotations.TemplateFunction;
import com.wynntils.utils.type.CappedValue;

import java.util.Optional;

@SuppressWarnings("unused") // Functions are accessed via reflection
public class GuildFunctions {

    @TemplateFunction(name = "capped_guild_level_progress")
    public static CappedValue cappedGuildLevelProgressFunction() {
        return Models.Guild.getGuildLevelProgress();
    }

    @TemplateFunction(name = "capped_guild_objectives_progress")
    public static CappedValue cappedGuildObjectivesProgressFunction() {
        return Models.Guild.getObjectivesCompletedProgress();
    }

    @TemplateFunction(name = "guild_level")
    public static int guildLevelFunction() {
        return Models.Guild.getGuildLevel();
    }

    @TemplateFunction(name = "guild_name")
    public static String guildNameFunction() {
        return Models.Guild.getGuildName();
    }

    @TemplateFunction(name = "guild_rank")
    public static String guildRankFunction() {
        GuildRank guildRank = Models.Guild.getGuildRank();
        if (guildRank == null) return "";
        return guildRank.getName();
    }

    @TemplateFunction(name = "is_allied_guild", aliases = {"is_allied", "is_ally"})
    public static boolean isAlliedGuildFunction(String guild) {
        return Models.Guild.isAllied(guild);
    }

    @TemplateFunction(name = "objective_streak")
    public static int objectiveStreakFunction() {
        return Models.Guild.getObjectiveStreak();
    }

    @TemplateFunction(name = "is_guild_member")
    public static boolean isGuildMemberFunction(String member) {
        return Models.Guild.isGuildMember(member);
    }

    @TemplateFunction(name = "contributed_guild_xp")
    public static long contributedGuildXpFunction() {
        WynnPlayerInfo playerInfo = Models.Account.getPlayerInfo();

        if (playerInfo == null) return 0L;

        Optional<PlayerGuildInfo> guildInfoOpt = playerInfo.guildInfo();

        return guildInfoOpt.map(playerGuildInfo -> playerGuildInfo.contributionXp().orElse(0L)).orElse(0L);
    }

    @TemplateFunction(name = "contributed_rank")
    public static int contributedRankFunction() {
        WynnPlayerInfo playerInfo = Models.Account.getPlayerInfo();

        if (playerInfo == null) return 0;

        Optional<PlayerGuildInfo> guildInfoOpt = playerInfo.guildInfo();

        return guildInfoOpt.map(playerGuildInfo -> playerGuildInfo.contributionRank().orElse(0)).orElse(0);
    }
}
