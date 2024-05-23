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

public class GuildFunctions {
    public static class CappedGuildLevelProgressFunction extends Function<CappedValue> {
        @Override
        public CappedValue getValue(FunctionArguments arguments) {
            return Models.Guild.getGuildLevelProgress();
        }
    }

    public static class GuildLevelFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            return Models.Guild.getGuildLevel();
        }
    }

    public static class GuildNameFunction extends Function<String> {
        @Override
        public String getValue(FunctionArguments arguments) {
            return Models.Guild.getGuildName();
        }
    }

    public static class GuildRankFunction extends Function<String> {
        @Override
        public String getValue(FunctionArguments arguments) {
            GuildRank guildRank = Models.Guild.getGuildRank();
            if (guildRank == null) return "";
            return guildRank.getName();
        }
    }
}
