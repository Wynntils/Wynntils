/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.players;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.handlers.chat.MessageType;
import com.wynntils.handlers.chat.event.ChatMessageReceivedEvent;
import com.wynntils.models.players.event.RelationsUpdateEvent;
import com.wynntils.models.players.hades.event.HadesEvent;
import com.wynntils.models.worlds.WorldStateModel;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.models.worlds.type.WorldState;
import com.wynntils.screens.partymanagement.PartyManagementScreen;
import com.wynntils.utils.mc.ComponentUtils;
import com.wynntils.utils.mc.McUtils;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * This model handles the player's party relations.
 */
public final class PartyModel extends Model {
    private static final Pattern PARTY_LIST_MESSAGE_PATTERN = Pattern.compile("§eParty members: (.*)");
    private static final Pattern PARTY_LIST_LEADER_PATTERN = Pattern.compile("§r§b(.+)");
    private static final Pattern PARTY_NO_LIST_MESSAGE_PATTERN = Pattern.compile("§eYou must be in a party to list\\.");
    private static final Pattern PARTY_OTHER_LEAVE_MESSAGE_PATTERN = Pattern.compile("§e(.+) has left the party\\.");
    private static final Pattern PARTY_OTHER_JOIN_MESSAGE_PATTERN = Pattern.compile("§e(.+) has joined the party\\.");
    private static final Pattern PARTY_OTHER_JOIN_SWITCH_MESSAGE_PATTERN =
            Pattern.compile("§eSay hello to (.+) which just joined your party!");
    private static final Pattern PARTY_OTHER_PROMOTED =
            Pattern.compile("§eSuccessfully promoted (.+) to party leader!");
    private static final Pattern PARTY_SELF_LEAVE_MESSAGE_PATTERN =
            Pattern.compile("§eYou have been removed from the party\\.");
    private static final Pattern PARTY_SELF_ALREADY_LEFT_MESSAGE_PATTERN =
            Pattern.compile("§eYou must be in a party to leave\\.");
    private static final Pattern PARTY_SELF_JOIN_MESSAGE_PATTERN =
            Pattern.compile("§eYou have successfully joined the party\\.");
    private static final Pattern PARTY_SELF_DISBAND =
            Pattern.compile("§eYour party has been disbanded since you were the only member remaining\\.");
    private static final Pattern PARTY_SELF_PROMOTED_MESSAGE_PATTERN =
            Pattern.compile("§eYou are now the leader of this party! Type /party for a list of commands\\.");
    private static final Pattern PARTY_CREATE = Pattern.compile("§eYou have successfully created a party\\.");
    private static final Pattern PARTY_DISBAND = Pattern.compile("§eYour party has been disbanded\\.");

    private boolean expectingPartyMessage = false;

    private HashSet<String> partyMembers = new HashSet<>();
    private String partyLeader = null;
    private boolean isPartying;

    public PartyModel(WorldStateModel worldStateModel) {
        super(List.of(worldStateModel));
        resetRelations();
    }

    @SubscribeEvent
    public void onAuth(HadesEvent.Authenticated event) {
        if (!Models.WorldState.onWorld()) return;

        requestPartyListUpdate();
    }

    @SubscribeEvent
    public void onWorldStateChange(WorldStateEvent event) {
        if (event.getNewState() == WorldState.WORLD) {
            requestPartyListUpdate();
        } else {
            resetRelations();
        }
    }

    @SubscribeEvent
    public void onChatReceived(ChatMessageReceivedEvent event) {
        if (event.getMessageType() != MessageType.FOREGROUND) return;

        String coded = event.getOriginalCodedMessage();

        if (tryParsePartyMessages(coded)) {
            return;
        }

        if (expectingPartyMessage) {
            if (tryParseNoPartyMessage(coded) || tryParsePartyList(coded)) {
                event.setCanceled(true);
                expectingPartyMessage = false;
                return;
            }
        }
    }

    private boolean tryParsePartyMessages(String coded) {
        if (PARTY_CREATE.matcher(coded).matches()) {
            WynntilsMod.info("Player created a new party.");

            isPartying = true;
            partyLeader = McUtils.player().getName().getString();
            partyMembers = new HashSet<>(Set.of(partyLeader));
            WynntilsMod.postEvent(
                    new RelationsUpdateEvent.PartyList(partyMembers, RelationsUpdateEvent.ChangeType.RELOAD));
            return true;
        }

        if (PARTY_DISBAND.matcher(coded).matches()
                || PARTY_SELF_LEAVE_MESSAGE_PATTERN.matcher(coded).matches()
                || PARTY_SELF_ALREADY_LEFT_MESSAGE_PATTERN.matcher(coded).matches()
                || PARTY_SELF_DISBAND.matcher(coded).matches()) {
            WynntilsMod.info("Player left the party.");

            isPartying = false;
            partyMembers = new HashSet<>();
            partyLeader = null;
            WynntilsMod.postEvent(
                    new RelationsUpdateEvent.PartyList(partyMembers, RelationsUpdateEvent.ChangeType.RELOAD));
            return true;
        }

        if (PARTY_SELF_JOIN_MESSAGE_PATTERN.matcher(coded).matches()) {
            WynntilsMod.info("Player joined a party.");

            isPartying = true;
            requestPartyListUpdate();
            return true;
        }

        Matcher matcher = PARTY_OTHER_JOIN_MESSAGE_PATTERN.matcher(coded);
        if (matcher.matches()) {
            String player = matcher.group(1);

            WynntilsMod.info("Player's party has a new member: " + player);

            isPartying = true;
            partyMembers.add(player);
            WynntilsMod.postEvent(
                    new RelationsUpdateEvent.PartyList(Set.of(player), RelationsUpdateEvent.ChangeType.ADD));
            return true;
        }

        matcher = PARTY_OTHER_JOIN_SWITCH_MESSAGE_PATTERN.matcher(coded);
        if (matcher.matches()) {
            String player = matcher.group(1);

            WynntilsMod.info("Player's party has a new member #2: " + player);

            isPartying = true;
            partyMembers.add(player);
            WynntilsMod.postEvent(
                    new RelationsUpdateEvent.PartyList(Set.of(player), RelationsUpdateEvent.ChangeType.ADD));
            return true;
        }

        matcher = PARTY_OTHER_LEAVE_MESSAGE_PATTERN.matcher(coded);
        if (matcher.matches()) {
            String player = matcher.group(1);

            WynntilsMod.info("Player's party has been left by an other player: " + player);

            isPartying = true;
            partyMembers.remove(player);
            WynntilsMod.postEvent(
                    new RelationsUpdateEvent.PartyList(Set.of(player), RelationsUpdateEvent.ChangeType.REMOVE));
            return true;
        }

        matcher = PARTY_OTHER_PROMOTED.matcher(coded);
        if (matcher.matches()) {
            String player = matcher.group(1);

            WynntilsMod.info("Player's party has a new leader: " + player);

            isPartying = true;
            partyLeader = player;
            return true;
        }

        matcher = PARTY_SELF_PROMOTED_MESSAGE_PATTERN.matcher(coded);
        if (matcher.matches()) {
            WynntilsMod.info("Player has been promoted to party leader.");

            isPartying = true;
            partyLeader = McUtils.player().getName().getString();
            return true;
        }

        return false;
    }

    private boolean tryParseNoPartyMessage(String coded) {
        if (PARTY_NO_LIST_MESSAGE_PATTERN.matcher(coded).matches()) {
            isPartying = false;
            WynntilsMod.info("Player is not in a party.");
            return true;
        }

        return false;
    }

    private boolean tryParsePartyList(String coded) {
        Matcher matcher = PARTY_LIST_MESSAGE_PATTERN.matcher(coded);
        if (!matcher.matches()) return false;

        String[] partyList = matcher.group(1).split(", ");

        for (String member : partyList) {
            Matcher m = PARTY_LIST_LEADER_PATTERN.matcher(member);
            if (m.matches()) {
                partyLeader = m.group(1);
            }

            partyMembers.add(ComponentUtils.stripFormatting(member));
        }

        isPartying = true;
        WynntilsMod.postEvent(new RelationsUpdateEvent.PartyList(partyMembers, RelationsUpdateEvent.ChangeType.RELOAD));

        WynntilsMod.info("Successfully updated party list, user has " + partyList.length + " party members.");
        return true;
    }

    private void resetRelations() {
        partyMembers = new HashSet<>();
        partyLeader = null;
        isPartying = false;

        WynntilsMod.postEvent(new RelationsUpdateEvent.PartyList(partyMembers, RelationsUpdateEvent.ChangeType.RELOAD));
    }

    public void requestPartyListUpdate() {
        if (McUtils.player() == null) return;

        expectingPartyMessage = true;
        McUtils.sendCommand("party list");
        WynntilsMod.info("Requested party list from Wynncraft.");
    }

    public Set<String> getPartyMembers() {
        return partyMembers;
    }

    public String getPartyLeader() {
        return partyLeader;
    }

    public boolean isPartying() {
        return isPartying;
    }

    public void kickFromParty(String player) {
        McUtils.sendCommand("party kick " + player);
    }

    public void promoteToLeader(String player) {
        McUtils.sendCommand("party promote " + player);
    }

    /**
     * Invites a player to the party. Creates a party if the player is not in one.
     * @param player The player to invite to the party
     */
    public void inviteToParty(String player) {
        if (!isPartying()) {
            createParty();
        }
        McUtils.sendCommand("party invite " + player);
    }

    public void leaveParty() {
        McUtils.sendCommand("party leave");
    }

    public void disbandParty() {
        McUtils.sendCommand("party disband");
    }

    public void createParty() {
        McUtils.sendCommand("party create");
    }

    @SubscribeEvent
    public void onPartyUpdate(RelationsUpdateEvent.PartyList e) {
        if (McUtils.mc().screen instanceof PartyManagementScreen partyManagementScreen) {
            partyManagementScreen.reloadMembersWidgets();
            partyManagementScreen
                    .reloadSuggestedPlayersWidgets(); // Reload because we don't want to suggest party members
        }
    }
}
