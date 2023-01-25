/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions;

import com.wynntils.core.components.Models;
import com.wynntils.core.functions.Function;

public class SocialFunctions {
    public static class OnlineFriendsFunction extends Function<Integer> {
        @Override
        public Integer getValue(String argument) {
            return Models.PlayerRelations.getFriends().size();
        }
    }

    public static class OnlinePartyMembersFunction extends Function<Integer> {
        @Override
        public Integer getValue(String argument) {
            return Models.PlayerRelations.getPartyMembers().size();
        }
    }
}
