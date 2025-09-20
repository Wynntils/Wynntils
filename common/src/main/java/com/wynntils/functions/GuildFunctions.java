/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions;

import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.functions.Function;
import com.wynntils.core.consumers.functions.arguments.Argument;
import com.wynntils.core.consumers.functions.arguments.FunctionArguments;
import com.wynntils.models.guild.type.GuildRank;
import com.wynntils.utils.type.CappedValue;
import java.util.List;

public class GuildFunctions {
    public static class CappedGuildLevelProgressFunction extends Function<CappedValue> {
        @Override
        public CappedValue getValue(FunctionArguments arguments) {
            return Models.Guild.getGuildLevelProgress();
        }
    }

    public static class CappedGuildObjectivesProgressFunction extends Function<CappedValue> {
        @Override
        public CappedValue getValue(FunctionArguments arguments) {
            return Models.Guild.getObjectivesCompletedProgress();
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

    public static class IsAlliedGuildFunction extends Function<Boolean> {
        @Override
        public Boolean getValue(FunctionArguments arguments) {
            return Models.Guild.isAllied(arguments.getArgument("guild").getStringValue());
        }

        @Override
        public FunctionArguments.Builder getArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(new Argument<>("guild", String.class, null)));
        }

        @Override
        public List<String> getAliases() {
            return List.of("is_allied", "is_ally");
        }
    }

    public static class ObjectiveStreakFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            return Models.Guild.getObjectiveStreak();
        }
    }

    public static class IsGuildMemberFunction extends Function<Boolean> {
        @Override
        public Boolean getValue(FunctionArguments arguments) {
            return Models.Guild.isGuildMember(arguments.getArgument("member").getStringValue());
        }

        @Override
        public FunctionArguments.Builder getArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(new Argument<>("member", String.class, null)));
        }
    }
}
