/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions;

import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.functions.Function;
import com.wynntils.core.consumers.functions.arguments.FunctionArguments;
import java.util.List;

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
                    List.of(new FunctionArguments.Argument<>("includeOffline", Boolean.class, true)));
        }
    }

    public static class PartyLeaderFunction extends Function<String> {
        @Override
        public String getValue(FunctionArguments arguments) {
            return Models.Party.getPartyLeader().orElse("");
        }
    }
}
