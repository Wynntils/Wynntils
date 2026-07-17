/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions;

import com.wynntils.core.components.Models;
import com.wynntils.models.players.WynntilsUser;
import com.wynntils.models.players.type.PartyMember;
import com.wynntils.utils.mc.McUtils;
import java.util.List;
import net.minecraft.network.chat.Component;
import com.wynntils.templates.annotations.TemplateFunction;

//Functions are accessed via reflection
@SuppressWarnings("unused")
public class SocialFunctions {

    @TemplateFunction(name = "friends")
    public int friendsFunction() {
        return Models.Friends.getFriends().size();
    }


    @TemplateFunction(name = "party_members")
    public int partyMembersFunction() {
        return partyMembersFunction(false);
    }

    @TemplateFunction(name = "party_members")
    public int partyMembersFunction(boolean includeOffline) {
        if (includeOffline) {
            return Models.Party.getPartyMembers().size();
        } else {
            return Models.Party.getPartyMembers().size() - Models.Party.getOfflineMembers().size();
        }
    }

    @TemplateFunction(name = "scoreboard_party_members", aliases = { "sb_party_members" })
    public int scoreboardPartyMembersFunction() {
        return Models.Party.getSbPartyMemberCount();
    }

    @TemplateFunction(name = "party_leader")
    public String partyLeaderFunction() {
        return Models.Party.getPartyLeader().orElse("");
    }

    @TemplateFunction(name = "party_member_name")
    public String partyMemberNameFunction(int index) {
        PartyMember member = Models.Party.getSbPartyMember(index);
        return member != PartyMember.EMPTY ? member.name() : "";
    }

    @TemplateFunction(name = "party_member_health")
    public int partyMemberHealthFunction(int index) {
        PartyMember member = Models.Party.getSbPartyMember(index);
        return member != PartyMember.EMPTY ? member.health() : 0;
    }

    @TemplateFunction(name = "party_member_level")
    public int partyMemberLevelFunction(int index) {
        PartyMember member = Models.Party.getSbPartyMember(index);
        return member != PartyMember.EMPTY ? member.level() : 0;
    }

    @TemplateFunction(name = "is_party_member_online")
    public boolean isPartyMemberOnlineFunction(int index) {
        PartyMember member = Models.Party.getSbPartyMember(index);
        return member != PartyMember.EMPTY ? member.online() : false;
    }

    @TemplateFunction(name = "is_party_member_alive")
    public boolean isPartyMemberAliveFunction(int index) {
        PartyMember member = Models.Party.getSbPartyMember(index);
        return member != PartyMember.EMPTY ? member.alive() : false;
    }

    @TemplateFunction(name = "party_total_level")
    public int partyTotalLevelFunction() {
        return Models.Party.getPartyTotalLevel();
    }

    @TemplateFunction(name = "is_friend")
    public boolean isFriendFunction(String player) {
        return Models.Friends.isFriend(player);
    }

    @TemplateFunction(name = "is_party_member")
    public boolean isPartyMemberFunction(String player) {
        return Models.Party.getPartyMembers().contains(player);
    }

    @TemplateFunction(name = "wynntils_role")
    public String wynntilsRoleFunction() {
        WynntilsUser player = Models.Player.getWynntilsUser(McUtils.player());
        if (player == null)
            return "";
        Component component = player.accountType().getComponent();
        if (component == null)
            return "";
        return component.getString();
    }

    @TemplateFunction(name = "player_name")
    public String playerNameFunction() {
        return McUtils.playerName();
    }

    @TemplateFunction(name = "player_uuid", aliases = { "uuid" })
    public String playerUuidFunction() {
        return McUtils.player().getStringUUID();
    }
}
