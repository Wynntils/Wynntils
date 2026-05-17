/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions;

import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.functions.Function;
import com.wynntils.core.consumers.functions.arguments.Argument;
import com.wynntils.core.consumers.functions.arguments.FunctionArguments;
import com.wynntils.models.players.WynntilsUser;
import com.wynntils.models.players.type.PartyMember;
import com.wynntils.utils.mc.McUtils;
import java.util.List;
import net.minecraft.network.chat.Component;

public class SocialFunctions {
    public static class FriendsFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            return Models.Friends.getFriends().size();
        }
    }

    public static class PartyMembersFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            if (arguments.getArgument("includeOffline").getBooleanValue()) {
                return Models.Party.getPartyMembers().size();
            } else {
                return Models.Party.getPartyMembers().size()
                        - Models.Party.getOfflineMembers().size();
            }
        }

        @Override
        public FunctionArguments.Builder getArgumentsBuilder() {
            return new FunctionArguments.OptionalArgumentBuilder(
                    List.of(new Argument<>("includeOffline", Boolean.class, true)));
        }
    }

    public static class PartyLeaderFunction extends Function<String> {
        @Override
        public String getValue(FunctionArguments arguments) {
            return Models.Party.getPartyLeader().orElse("");
        }
    }

    public static class PartyMemberNameFunction extends Function<String> {
        @Override
        public String getValue(FunctionArguments arguments) {
            PartyMember member =
                    Models.Party.getSbPartyMember(arguments.getArgument("index").getIntegerValue());
            return member != PartyMember.EMPTY ? member.name() : "";
        }

        @Override
        public FunctionArguments.Builder getArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(new Argument<>("index", Integer.class, null)));
        }
    }

    public static class PartyMemberHealthFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            PartyMember member =
                    Models.Party.getSbPartyMember(arguments.getArgument("index").getIntegerValue());
            return member != PartyMember.EMPTY ? member.health() : 0;
        }

        @Override
        public FunctionArguments.Builder getArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(new Argument<>("index", Integer.class, null)));
        }
    }

    public static class PartyMemberLevelFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            PartyMember member =
                    Models.Party.getSbPartyMember(arguments.getArgument("index").getIntegerValue());
            return member != PartyMember.EMPTY ? member.level() : 0;
        }

        @Override
        public FunctionArguments.Builder getArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(new Argument<>("index", Integer.class, null)));
        }
    }

    public static class IsPartyMemberOnlineFunction extends Function<Boolean> {
        @Override
        public Boolean getValue(FunctionArguments arguments) {
            PartyMember member =
                    Models.Party.getSbPartyMember(arguments.getArgument("index").getIntegerValue());
            return member != PartyMember.EMPTY ? member.online() : false;
        }

        @Override
        public FunctionArguments.Builder getArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(new Argument<>("index", Integer.class, null)));
        }
    }

    public static class IsPartyMemberAliveFunction extends Function<Boolean> {
        @Override
        public Boolean getValue(FunctionArguments arguments) {
            PartyMember member =
                    Models.Party.getSbPartyMember(arguments.getArgument("index").getIntegerValue());
            return member != PartyMember.EMPTY ? member.alive() : false;
        }

        @Override
        public FunctionArguments.Builder getArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(new Argument<>("index", Integer.class, null)));
        }
    }

    public static class PartyTotalLevelFunction extends Function<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            return Models.Party.getPartyTotalLevel();
        }
    }

    public static class IsFriendFunction extends Function<Boolean> {
        @Override
        public Boolean getValue(FunctionArguments arguments) {
            return Models.Friends.isFriend(arguments.getArgument("player").getStringValue());
        }

        @Override
        public FunctionArguments.Builder getArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(new Argument<>("player", String.class, null)));
        }
    }

    public static class IsPartyMemberFunction extends Function<Boolean> {
        @Override
        public Boolean getValue(FunctionArguments arguments) {
            return Models.Party.getPartyMembers()
                    .contains(arguments.getArgument("player").getStringValue());
        }

        @Override
        public FunctionArguments.Builder getArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(new Argument<>("player", String.class, null)));
        }
    }

    public static class WynntilsRoleFunction extends Function<String> {
        @Override
        public String getValue(FunctionArguments arguments) {
            WynntilsUser player = Models.Player.getWynntilsUser(McUtils.player());
            if (player == null) return "";

            Component component = player.accountType().getComponent();
            if (component == null) return "";

            return component.getString();
        }
    }

    public static class PlayerNameFunction extends Function<String> {
        @Override
        public String getValue(FunctionArguments arguments) {
            return McUtils.playerName();
        }
    }

    public static class PlayerUuidFunction extends Function<String> {
        @Override
        public String getValue(FunctionArguments arguments) {
            return McUtils.player().getStringUUID();
        }

        @Override
        protected List<String> getAliases() {
            return List.of("uuid");
        }
    }
}
