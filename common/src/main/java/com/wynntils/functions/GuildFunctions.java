/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions;

import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.functions.Function;
import com.wynntils.core.consumers.functions.arguments.FunctionArguments;
import com.wynntils.models.players.type.GuildRank;
import com.wynntils.utils.type.CappedValue;
import java.util.List;

public class GuildFunctions {
    public static class CappedGuildLevelPercentageFunction extends Function<CappedValue> {
        @Override
        public CappedValue getValue(FunctionArguments arguments) {
            return new CappedValue(Models.Guild.getGuildLevelPercentage(), 100);
        }

        @Override
        protected List<String> getAliases() {
            return List.of("capped_guild_level_percentage");
        }
    }

    public static class GuildLevelFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            return Models.Guild.getGuildLevel();
        }

        @Override
        protected List<String> getAliases() {
            return List.of("guild_level");
        }
    }

    public static class GuildLevelPercentageFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            return Models.Guild.getGuildLevelPercentage();
        }

        @Override
        protected List<String> getAliases() {
            return List.of("guild_level_percentage");
        }
    }

    public static class GuildMemberFunction extends Function<Boolean> {
        @Override
        public Boolean getValue(FunctionArguments arguments) {
            return !Models.Guild.getGuildName().isEmpty();
        }

        @Override
        protected List<String> getAliases() {
            return List.of("guild_member");
        }
    }

    public static class GuildNameFunction extends Function<String> {
        @Override
        public String getValue(FunctionArguments arguments) {
            return Models.Guild.getGuildName();
        }

        @Override
        protected List<String> getAliases() {
            return List.of("guild_name");
        }
    }

    public static class GuildRankFunction extends Function<String> {
        @Override
        public String getValue(FunctionArguments arguments) {
            GuildRank guildRank = Models.Guild.getGuildRank();
            if (guildRank == null) return "";
            return guildRank.getName();
        }

        @Override
        protected List<String> getAliases() {
            return List.of("guild_rank");
        }
    }
}
