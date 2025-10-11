/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.players;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Handlers;
import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.chat.event.ChatMessageEvent;
import com.wynntils.handlers.chat.type.MessageType;
import com.wynntils.handlers.scoreboard.ScoreboardPart;
import com.wynntils.mc.event.SetPlayerTeamEvent;
import com.wynntils.models.players.event.HadesRelationsUpdateEvent;
import com.wynntils.models.players.event.PartyEvent;
import com.wynntils.models.players.scoreboard.PartyScoreboardPart;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.models.worlds.type.WorldState;
import com.wynntils.services.hades.event.HadesEvent;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.mc.StyledTextUtils;
import com.wynntils.utils.type.Pair;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import net.neoforged.bus.api.SubscribeEvent;

/**
 * This model handles the player's party relations.
 */
public final class PartyModel extends Model {
    // \uE005\uE002 is for the first line
    // \uE001  is for the other lines
    private static final String PARTY_PREFIX_REGEX = "§e(?:\uE005\uE002|\uE001) ";

    // region Party Regexes
    /*
    Regexes are now named without any specific format. They are grouped by behaviour.
    Eg. The player getting kicked and leaving the party are grouped together.
    Player typically refers to the active user.
    Other typically refers to some different player in or out of the party.
    */
    // Test in PartyModel_PARTY_LIST_ALL
    private static final Pattern PARTY_LIST_ALL = Pattern.compile(PARTY_PREFIX_REGEX + "Party members: (.*)");
    private static final Pattern PARTY_LIST_LEADER = Pattern.compile("§b([a-zA-Z0-9_]+)");

    // General purpose message for all party cmds executed when not in a party
    private static final Pattern PARTY_COMMAND_FAILED =
            Pattern.compile(PARTY_PREFIX_REGEX + "You must be in a party to use this\\.");

    // Some messages have no periods. Add them back if/when Wynn does.
    // Player is no longer in a party
    private static final Pattern PARTY_PLAYER_LEFT =
            Pattern.compile(PARTY_PREFIX_REGEX + "You have left your current party");
    private static final Pattern PARTY_PLAYER_KICKED =
            Pattern.compile(PARTY_PREFIX_REGEX + "You have been kicked from your party");
    private static final Pattern PARTY_PLAYER_DISBANDED =
            Pattern.compile(PARTY_PREFIX_REGEX + "Your party has been disbanded");

    // Player has joined some party
    private static final Pattern PARTY_PLAYER_CREATED =
            Pattern.compile(PARTY_PREFIX_REGEX + "You have successfully created a party\\.");
    // This part is iffy, there is no longer any successfully joined message.
    // Instead, the other player has joined message is used when the player joins any new party.
    // So effectively other than party creations, anyone joining will trigger this message.
    // We also will have no idea of the party state when we ourselves join and get this message (so req party list)
    private static final Pattern PARTY_SOMEONE_JOINED =
            Pattern.compile(PARTY_PREFIX_REGEX + "(.+) has joined your party, say hello!");

    // Other player is no longer in the party
    private static final Pattern PARTY_OTHER_LEFT = Pattern.compile(PARTY_PREFIX_REGEX + "(.+) has left the party!");
    private static final Pattern PARTY_OTHER_KICKED =
            Pattern.compile(PARTY_PREFIX_REGEX + "(.+) has been kicked from the party!");

    // New party leader
    private static final Pattern PARTY_NEW_LEADER =
            Pattern.compile(PARTY_PREFIX_REGEX + "(?:§c)?(.+)§e is now the Party Leader!.*");

    // Temporary party event over, previous party restored
    // This actually means nothing of value to us so just re-request
    private static final Pattern PARTY_RESTORED_SELF =
            Pattern.compile(PARTY_PREFIX_REGEX + "Your previous party was restored");

    private static final Pattern PARTY_INVITED =
            Pattern.compile("(?:" + PARTY_PREFIX_REGEX + "|\\s+§e)You have been invited to join (.+)'s? party!\\s*");
    // endregion

    private static final ScoreboardPart PARTY_SCOREBOARD_PART = new PartyScoreboardPart();

    public static final int MAX_PARTY_MEMBER_COUNT = 10;

    private boolean expectingPartyMessage = false; // Whether the client is expecting a response from "/party list"
    private long lastPartyRequest = 0; // The last time the client requested party data

    private boolean inParty; // Whether the player is in a party
    private String partyLeader = null; // The name of the party leader
    private List<String> partyMembers = new ArrayList<>(); // A set of Strings representing all party members
    private Set<String> offlineMembers =
            new HashSet<>(); // A set of Strings representing all offline (disconnected) party members

    public PartyModel() {
        super(List.of());

        resetData();
        Handlers.Scoreboard.addPart(PARTY_SCOREBOARD_PART);
    }

    @SubscribeEvent
    public void onAuth(HadesEvent.Authenticated event) {
        if (!Models.WorldState.onWorld()) return;

        requestData();
    }

    @SubscribeEvent
    public void onWorldStateChange(WorldStateEvent event) {
        if (event.getNewState() == WorldState.WORLD) {
            requestData();
        } else {
            resetData();
        }
    }

    @SubscribeEvent
    public void onChatReceived(ChatMessageEvent.Match event) {
        if (event.getMessageType() != MessageType.FOREGROUND) return;

        StyledText chatMessage = StyledTextUtils.unwrap(event.getMessage()).stripAlignment();

        if (tryParsePartyMessages(chatMessage)) return;

        if (expectingPartyMessage) {
            if (tryParseNoPartyMessage(chatMessage) || tryParsePartyList(chatMessage)) {
                event.cancelChat();
                expectingPartyMessage = false;
                return;
            }
        }
    }

    private boolean tryParsePartyMessages(StyledText styledText) {
        if (styledText.matches(PARTY_PLAYER_CREATED)) {
            WynntilsMod.info("Player created a new party.");

            inParty = true;
            partyLeader = McUtils.playerName();
            partyMembers = new ArrayList<>(List.of(partyLeader));
            WynntilsMod.postEvent(new HadesRelationsUpdateEvent.PartyList(
                    Set.copyOf(partyMembers), HadesRelationsUpdateEvent.ChangeType.RELOAD));
            WynntilsMod.postEvent(new PartyEvent.Listed());
            return true;
        }

        if (styledText.matches(PARTY_PLAYER_LEFT)
                || styledText.matches(PARTY_PLAYER_DISBANDED)
                || styledText.matches(PARTY_PLAYER_KICKED)) {
            WynntilsMod.info("Player is no longer in a party.");

            resetData(); // (!) resetData() already posts events for both HadesRelationsUpdateEvent and PartyEvent
            return true;
        }

        Matcher matcher = styledText.getMatcher(PARTY_SOMEONE_JOINED);
        if (matcher.matches()) {
            Pair<String, String> possibleNameAndNick = StyledTextUtils.extractNameAndNick(styledText);
            String player;
            if (possibleNameAndNick != null) {
                player = possibleNameAndNick.a();
            } else {
                player = matcher.group(1);
            }

            // If this is us, then we joined a new party and have no idea about party state.
            if (player.equals(McUtils.playerName())) {
                WynntilsMod.info("Player joined a new party, requesting party list.");
                requestData();
            } else {
                WynntilsMod.info("Player's party has a new member: " + player);
                partyMembers.add(player);
                WynntilsMod.postEvent(new HadesRelationsUpdateEvent.PartyList(
                        Set.of(player), HadesRelationsUpdateEvent.ChangeType.ADD));
                WynntilsMod.postEvent(new PartyEvent.OtherJoined(player));
            }

            return true;
        }

        matcher = styledText.getMatcher(PARTY_OTHER_LEFT);
        if (matcher.matches()) {
            Pair<String, String> possibleNameAndNick = StyledTextUtils.extractNameAndNick(styledText);
            String player;
            if (possibleNameAndNick != null) {
                player = possibleNameAndNick.a();
            } else {
                player = matcher.group(1);
            }

            WynntilsMod.info("Other player left player's party: " + player);

            partyMembers.remove(player);
            WynntilsMod.postEvent(new HadesRelationsUpdateEvent.PartyList(
                    Set.of(player), HadesRelationsUpdateEvent.ChangeType.REMOVE));
            WynntilsMod.postEvent(new PartyEvent.OtherLeft(player));
            return true;
        }

        matcher = styledText.getMatcher(PARTY_OTHER_KICKED);
        if (matcher.matches()) {
            Pair<String, String> possibleNameAndNick = StyledTextUtils.extractNameAndNick(styledText);
            String player;
            if (possibleNameAndNick != null) {
                player = possibleNameAndNick.a();
            } else {
                player = matcher.group(1);
            }

            WynntilsMod.info("Other player was kicked from player's party: " + player);

            partyMembers.remove(player);
            WynntilsMod.postEvent(new HadesRelationsUpdateEvent.PartyList(
                    Set.of(player), HadesRelationsUpdateEvent.ChangeType.REMOVE));
            WynntilsMod.postEvent(new PartyEvent.OtherLeft(player));
            return true;
        }

        matcher = styledText.getMatcher(PARTY_NEW_LEADER);
        if (matcher.matches()) {
            Pair<String, String> possibleNameAndNick = StyledTextUtils.extractNameAndNick(styledText);
            String player;
            if (possibleNameAndNick != null) {
                player = possibleNameAndNick.a();
            } else {
                player = matcher.group(1);
            }

            WynntilsMod.info("Player's party has a new leader: " + player);

            // Prevent race conditions for methods that manually request leader immediately on event
            String oldLeader = partyLeader;
            partyLeader = player;
            WynntilsMod.postEvent(new PartyEvent.Promoted(oldLeader, partyLeader));
            return true;
        }

        matcher = styledText.getMatcher(PARTY_INVITED);
        if (matcher.matches()) {
            Pair<String, String> possibleNameAndNick = StyledTextUtils.extractNameAndNick(styledText);
            String inviter;
            if (possibleNameAndNick != null) {
                inviter = possibleNameAndNick.a();
            } else {
                inviter = matcher.group(1);
            }
            WynntilsMod.info("Player has been invited to party by " + inviter);

            WynntilsMod.postEvent(new PartyEvent.Invited(inviter));
            return true;
        }

        matcher = styledText.getMatcher(PARTY_RESTORED_SELF);
        if (matcher.matches()) {
            // We have no idea what the previous party was, so we have to request the party list.
            WynntilsMod.info("Player's previous party was restored, requesting party list.");

            requestData();
            return true;
        }

        return false;
    }

    private boolean tryParseNoPartyMessage(StyledText styledText) {
        if (styledText.matches(PARTY_COMMAND_FAILED)) {
            resetData();
            WynntilsMod.info("Player is not in a party.");
            return true;
        }

        return false;
    }

    private boolean tryParsePartyList(StyledText styledText) {
        Matcher matcher = styledText.getMatcher(PARTY_LIST_ALL);
        if (!matcher.matches()) return false;

        String[] partyList = StyledText.fromString(matcher.group(1))
                .getStringWithoutFormatting()
                .split("(?:,(?: and)? )");
        List<String> newPartyMembers = new ArrayList<>();
        Collections.addAll(newPartyMembers, partyList);

        // Attempt to look for party leader with pattern.
        // If fail, assume we are leader (no special color will appear in list)
        Matcher leaderMatcher = styledText.getMatcher(PARTY_LIST_LEADER);
        String oldLeader = partyLeader;
        partyLeader = leaderMatcher.find() ? leaderMatcher.group(1) : McUtils.playerName();
        WynntilsMod.postEvent(new PartyEvent.Promoted(oldLeader, partyLeader));
        WynntilsMod.info("Successfully updated party leader, current leader is " + partyLeader + ".");

        // Sort the party members by the order they appear in the old party list, to preserve the order
        partyMembers = newPartyMembers.stream()
                .sorted(Comparator.comparing(element -> partyMembers.indexOf(element)))
                .collect(Collectors.toList());

        inParty = true;
        WynntilsMod.postEvent(new HadesRelationsUpdateEvent.PartyList(
                Set.copyOf(partyMembers), HadesRelationsUpdateEvent.ChangeType.RELOAD));
        WynntilsMod.postEvent(new PartyEvent.Listed());
        WynntilsMod.info("Successfully updated party list, user has " + partyList.length + " party members.");

        Collection<String> teamNames = McUtils.mc().level.getScoreboard().getTeamNames();
        offlineMembers = partyMembers.stream()
                .filter(member -> !teamNames.contains(member))
                .collect(Collectors.toSet());
        WynntilsMod.info("Successfully updated offline members, user's party has " + offlineMembers.size()
                + " offline members.");

        return true;
    }

    /**
     * Resets all party data to a state where the player is not in a party.
     * Posts events for both PartyEvent and HadesRelationsUpdateEvent.
     */
    private void resetData() {
        partyMembers = new ArrayList<>();
        partyLeader = null;
        inParty = false;
        offlineMembers = new HashSet<>();

        WynntilsMod.postEvent(new HadesRelationsUpdateEvent.PartyList(
                Set.copyOf(partyMembers), HadesRelationsUpdateEvent.ChangeType.RELOAD));
        WynntilsMod.postEvent(new PartyEvent.Listed());
    }

    /**
     * Sends "/party list" to Wynncraft and waits for the response.
     * (!) Skips if the last request was less than 250ms ago.
     * When the response is received, partyMembers and partyLeader will be updated.
     * After that, the offlineMembers list will be updated from scoreboard data.
     * Hades relations will be updated and PartyEvent.Listed will be posted.
     */
    public void requestData() {
        if (McUtils.player() == null) return;

        if (System.currentTimeMillis() - lastPartyRequest < 250) {
            WynntilsMod.info("Skipping party list request because it was requested less than 250ms ago.");
            return;
        }

        expectingPartyMessage = true;
        lastPartyRequest = System.currentTimeMillis();
        Handlers.Command.queueCommand("party list");
    }

    public void increasePlayerPriority(String playerName) {
        int index = partyMembers.indexOf(playerName);

        if (index == -1) return;

        partyMembers.add(Math.max(0, index - 1), partyMembers.remove(index));
        WynntilsMod.postEvent(new PartyEvent.PriorityChanged(playerName, index - 1));
    }

    public void decreasePlayerPriority(String playerName) {
        int index = partyMembers.indexOf(playerName);

        if (index == -1) return;

        partyMembers.add(Math.min(partyMembers.size() - 1, index + 1), partyMembers.remove(index));
        WynntilsMod.postEvent(new PartyEvent.PriorityChanged(playerName, index + 1));
    }

    public boolean isInParty() {
        return inParty;
    }

    public boolean isPartyLeader(String userName) {
        return userName.equals(partyLeader);
    }

    public Optional<String> getPartyLeader() {
        return Optional.ofNullable(partyLeader);
    }

    public List<String> getPartyMembers() {
        return partyMembers;
    }

    public Set<String> getOfflineMembers() {
        return offlineMembers;
    }

    @SubscribeEvent
    public void onSetTeam(SetPlayerTeamEvent e) {
        if (!inParty) return;

        if (e.getMethod() == 0) { // ADD, so player joined the server
            offlineMembers.remove(e.getTeamName());
            WynntilsMod.postEvent(new PartyEvent.OtherReconnected(e.getTeamName()));
        } else if (e.getMethod() == 1) { // REMOVE, so player left the server
            if (partyMembers.contains(e.getTeamName())) {
                offlineMembers.add(e.getTeamName());
                WynntilsMod.postEvent(new PartyEvent.OtherDisconnected(e.getTeamName()));
            }
        }
    }

    // region Party Commands
    /**
     * Kicks a player from the party.
     */
    public void partyKick(String player) {
        Handlers.Command.queueCommand("party kick " + player);
    }

    /**
     * Promotes a player to party leader.
     */
    public void partyPromote(String player) {
        Handlers.Command.queueCommand("party promote " + player);
    }

    /**
     * Invites a player to the party. Creates a party if the player is not in one.
     */
    public void partyInvite(String player) {
        if (!inParty) partyCreate();
        Handlers.Command.queueCommand("party invite " + player);
    }

    /**
     * Leaves the party.
     */
    public void partyLeave() {
        Handlers.Command.queueCommand("party leave");
    }

    /**
     * Disbands the party.
     */
    public void partyDisband() {
        Handlers.Command.queueCommand("party disband");
    }

    /**
     * Creates a party.
     */
    public void partyCreate() {
        Handlers.Command.queueCommand("party create");
    }

    /**
     * Join another players party
     */
    public void partyJoin(String playerName) {
        Handlers.Command.queueCommand("party join " + playerName);
    }

    /**
     * Kicks everyone in offlineMembers.
     * Consider running {@link #requestData()} before this.
     */
    public void partyKickOffline() {
        Set<String> offlineMembersCopy = new HashSet<>(offlineMembers);
        offlineMembersCopy.forEach(this::partyKick);
    }
    // endregion
}
