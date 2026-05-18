/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions;

import com.wynntils.core.components.Models;

import com.wynntils.models.players.WynntilsUser;
import com.wynntils.templates.annotations.TemplateFunction;
import com.wynntils.utils.mc.McUtils;
import java.util.List;
import net.minecraft.network.chat.Component;

@SuppressWarnings("unused") // Functions are accessed via reflection
public class SocialFunctions {

    @TemplateFunction(name = "friends")
    public static int friendsFunction() {
        return Models.Friends.getFriends().size();
    }

    @TemplateFunction(name = "party_members")
    public static int partyMembersFunction(boolean includeOffline) {
        if (includeOffline) {
            return Models.Party.getPartyMembers().size();
        } else {
            return Models.Party.getPartyMembers().size()
                    - Models.Party.getOfflineMembers().size();
        }
    }

    @TemplateFunction(name = "party_members")
    public static int partyMembersFunction() {
        return partyMembersFunction(true);
    }

    @TemplateFunction(name = "party_leader")
    public static String partyLeaderFunction() {
        return Models.Party.getPartyLeader().orElse("");

    }

    @TemplateFunction(name = "is_friend")
    public static boolean isFriendFunction(String player) {
        return Models.Friends.isFriend(player);
    }

    @TemplateFunction(name = "is_party_member")
    public static boolean isPartyMemberFunction(String player) {
        return Models.Party.getPartyMembers().contains(player);
    }

    @TemplateFunction(name = "wynntils_role")
    public static String wynntilsRoleFunction() {
        WynntilsUser player = Models.Player.getWynntilsUser(McUtils.player());
        if (player == null) return "";

        Component component = player.accountType().getComponent();
        if (component == null) return "";

        return component.getString();
    }


    @TemplateFunction(name = "player_name")
    public static String playerNameFunction() {
        return McUtils.playerName();

    }

    @TemplateFunction(name = "player_uuid", aliases = "uuid")
    public static String playerUuidFunction() {
        return McUtils.player().getStringUUID();
    }
}
