/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.players;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.handlers.chat.MessageType;
import com.wynntils.handlers.chat.event.ChatMessageReceivedEvent;
import com.wynntils.mc.event.PlayerJoinedWorldEvent;
import com.wynntils.models.players.event.RelationsUpdateEvent;
import com.wynntils.models.players.hades.event.HadesEvent;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.models.worlds.type.WorldState;
import com.wynntils.utils.mc.ComponentUtils;
import com.wynntils.utils.mc.McUtils;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * This model handles the player's in-game relations, like friends, party info, guild info.
 */
public final class PlayerRelationsModel extends Model {
    private static final Pattern FRIEND_LIST_MESSAGE_PATTERN = Pattern.compile(".+'s friends \\(.+\\): (.*)");
    private static final Pattern FRIEND_NO_LIST_MESSAGE_PATTERN_1 = Pattern.compile("§eWe couldn't find any friends\\.");
    private static final Pattern FRIEND_NO_LIST_MESSAGE_PATTERN_2 =
            Pattern.compile("§eTry typing §r§6/friend add Username§r§e!");
    private static final Pattern FRIEND_REMOVE_MESSAGE_PATTERN =
            Pattern.compile("§e(.+) has been removed from your friends!");
    private static final Pattern FRIEND_ADD_MESSAGE_PATTERN = Pattern.compile("§e(.+) has been added to your friends!");

    private static final Pattern PARTY_LIST_MESSAGE_PATTERN = Pattern.compile("Party members: (.*)");
    private static final Pattern PARTY_NO_LIST_MESSAGE_PATTERN = Pattern.compile("§eYou must be in a party to list\\.");
    private static final Pattern PARTY_OTHER_LEAVE_MESSAGE_PATTERN = Pattern.compile("§e(.+) has left the party\\.");
    private static final Pattern PARTY_OTHER_JOIN_MESSAGE_PATTERN = Pattern.compile("§e(.+) has joined the party\\.");
    private static final Pattern PARTY_OTHER_JOIN_SWITCH_MESSAGE_PATTERN =
            Pattern.compile("§eSay hello to (.+) which just joined your party!");
    private static final Pattern PARTY_SELF_LEAVE_MESSAGE_PATTERN =
            Pattern.compile("§eYou have been removed from the party\\.");
    private static final Pattern PARTY_SELF_ALREADY_LEFT_MESSAGE_PATTERN =
            Pattern.compile("§eYou must be in a party to leave\\.");
    private static final Pattern PARTY_SELF_JOIN_MESSAGE_PATTERN =
            Pattern.compile("§eYou have successfully joined the party\\.");
    private static final Pattern PARTY_CREATE = Pattern.compile("§eYou have successfully created a party\\.");
    private static final Pattern PARTY_DISBAND = Pattern.compile("§eYour party has been disbanded\\.");
    private static final Pattern PARTY_SINGLE_DISBAND = Pattern.compile("§eYour party has been disbanded since you were the only member remaining\\.");

    private boolean expectingFriendMessage = false;
    private boolean expectingPartyMessage = false;

    private Set<String> friends;
    private Set<String> partyMembers;
    private Set<String> worldPlayers;
    private boolean isPartying = false;

    public PlayerRelationsModel() {
        resetRelations();
    }

    @SubscribeEvent
    public void onAuth(HadesEvent.Authenticated event) {
        if (!Models.WorldState.onWorld()) return;

        requestFriendListUpdate();
        requestPartyListUpdate();
    }

    @SubscribeEvent
    public void onWorldStateChange(WorldStateEvent event) {
        if (event.getNewState() == WorldState.WORLD) {
            requestFriendListUpdate();
            requestPartyListUpdate();
        } else {
            resetRelations();
        }
    }

    @SubscribeEvent
    public void onChatReceived(ChatMessageReceivedEvent event) {
        if (event.getMessageType() != MessageType.FOREGROUND) return;

        String coded = event.getOriginalCodedMessage();
        String unformatted = ComponentUtils.stripFormatting(coded);

        if (tryParseFriendMessages(coded)) {
            return;
        }

        if (tryParsePartyMessages(coded)) {
            return;
        }

        if (expectingFriendMessage) {
            if (tryParseFriendList(unformatted) || tryParseNoFriendList(coded)) {
                event.setCanceled(true);
                expectingFriendMessage = false;
                return;
            }

            // Skip first message of two, but still expect more messages
            if (FRIEND_NO_LIST_MESSAGE_PATTERN_1.matcher(coded).matches()) {
                event.setCanceled(true);
                return;
            }
        }

        if (expectingPartyMessage) {
            if (tryParseNoPartyMessage(coded) || tryParsePartyList(unformatted)) {
                event.setCanceled(true);
                expectingPartyMessage = false;
                return;
            }
        }
    }

    // region Party List Parsing

    private boolean tryParsePartyMessages(String coded) {
        if (PARTY_CREATE.matcher(coded).matches()) {
            WynntilsMod.info("Player created a new party.");

            isPartying = true;
            partyMembers = Set.of();
            WynntilsMod.postEvent(
                    new RelationsUpdateEvent.PartyList(partyMembers, RelationsUpdateEvent.ChangeType.RELOAD));
            return true;
        }

        if (PARTY_DISBAND.matcher(coded).matches()
                || PARTY_SELF_LEAVE_MESSAGE_PATTERN.matcher(coded).matches() || PARTY_SELF_ALREADY_LEFT_MESSAGE_PATTERN.matcher(coded).matches() || PARTY_SINGLE_DISBAND.matcher(coded).matches()) {
            WynntilsMod.info("Player left the party.");

            isPartying = false;
            partyMembers = Set.of();
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

    private boolean tryParsePartyList(String unformatted) {
        Matcher matcher = PARTY_LIST_MESSAGE_PATTERN.matcher(unformatted);
        if (!matcher.matches()) return false;

        String[] partyList = matcher.group(1).split(", ");

        partyMembers = Arrays.stream(partyList).collect(Collectors.toSet());
        WynntilsMod.postEvent(new RelationsUpdateEvent.PartyList(partyMembers, RelationsUpdateEvent.ChangeType.RELOAD));

        WynntilsMod.info("Successfully updated party list, user has " + partyList.length + " party members.");
        return true;
    }

    // endregion

    // region Friend List Parsing

    private boolean tryParseNoFriendList(String coded) {
        if (FRIEND_NO_LIST_MESSAGE_PATTERN_2.matcher(coded).matches()) {
            WynntilsMod.info("Player has no friends!");
            return true;
        }

        return false;
    }

    private boolean tryParseFriendMessages(String coded) {
        Matcher matcher = FRIEND_REMOVE_MESSAGE_PATTERN.matcher(coded);
        if (matcher.matches()) {
            String player = matcher.group(1);

            WynntilsMod.info("Player has removed friend: " + player);

            friends.remove(player);
            WynntilsMod.postEvent(
                    new RelationsUpdateEvent.FriendList(Set.of(player), RelationsUpdateEvent.ChangeType.REMOVE));
            return true;
        }

        matcher = FRIEND_ADD_MESSAGE_PATTERN.matcher(coded);
        if (matcher.matches()) {
            String player = matcher.group(1);

            WynntilsMod.info("Player has added friend: " + player);

            friends.add(player);
            WynntilsMod.postEvent(
                    new RelationsUpdateEvent.FriendList(Set.of(player), RelationsUpdateEvent.ChangeType.ADD));
            return true;
        }

        return false;
    }

    private boolean tryParseFriendList(String unformatted) {
        Matcher matcher = FRIEND_LIST_MESSAGE_PATTERN.matcher(unformatted);
        if (!matcher.matches()) return false;

        String[] friendList = matcher.group(1).split(", ");

        friends = Arrays.stream(friendList).collect(Collectors.toSet());
        WynntilsMod.postEvent(new RelationsUpdateEvent.FriendList(friends, RelationsUpdateEvent.ChangeType.RELOAD));

        WynntilsMod.info("Successfully updated friend list, user has " + friendList.length + " friends.");
        return true;
    }

    // endregion

    private void resetRelations() {
        friends = new HashSet<>();
        partyMembers = new HashSet<>();
        worldPlayers = new HashSet<>();

        WynntilsMod.postEvent(new RelationsUpdateEvent.FriendList(friends, RelationsUpdateEvent.ChangeType.RELOAD));
        WynntilsMod.postEvent(new RelationsUpdateEvent.PartyList(partyMembers, RelationsUpdateEvent.ChangeType.RELOAD));
        WynntilsMod.postEvent(new RelationsUpdateEvent.UserList(worldPlayers, RelationsUpdateEvent.ChangeType.RELOAD));
    }

    public void requestFriendListUpdate() {
        if (McUtils.player() == null) return;

        expectingFriendMessage = true;
        McUtils.sendCommand("friend list");
        WynntilsMod.info("Requested friend list from Wynncraft.");
    }

    public void requestPartyListUpdate() {
        if (McUtils.player() == null) return;

        expectingPartyMessage = true;
        McUtils.sendCommand("party list");
        WynntilsMod.info("Requested party list from Wynncraft.");
    }

    public void updateWorldPlayers() {
        if (McUtils.player() == null) return;

        worldPlayers = new HashSet<>(McUtils.mc().level.getScoreboard().getTeamNames());
    }

    public Set<String> getFriends() {
        return friends;
    }

    public Set<String> getPartyMembers() {
        return partyMembers;
    }

    public Set<String> getWorldPlayers() {
        return worldPlayers;
    }

    public boolean isPartying() {
        return isPartying;
    }
}
