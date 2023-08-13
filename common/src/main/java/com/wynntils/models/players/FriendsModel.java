/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.players;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.chat.event.ChatMessageReceivedEvent;
import com.wynntils.handlers.chat.type.MessageType;
import com.wynntils.models.players.event.FriendsEvent;
import com.wynntils.models.players.event.HadesRelationsUpdateEvent;
import com.wynntils.models.worlds.WorldStateModel;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.models.worlds.type.WorldState;
import com.wynntils.services.hades.event.HadesEvent;
import com.wynntils.utils.mc.McUtils;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class FriendsModel extends Model {
    // region Friend Regexes
    /*
    Regexes should be named with this format:
    FRIEND_ACTION_(DETAIL)
    where:
    FRIEND should be the first word
    ACTION should be something like ADD, LIST, etc.
    DETAIL (optional) should be a descriptor if necessary
     */
    private static final Pattern FRIEND_LIST = Pattern.compile(".+'s? friends \\(.+\\): (.*)");
    private static final Pattern FRIEND_LIST_FAIL_1 = Pattern.compile("§eWe couldn't find any friends\\.");
    private static final Pattern FRIEND_LIST_FAIL_2 = Pattern.compile("§eTry typing §6/friend add Username§e!");
    private static final Pattern FRIEND_REMOVE_MESSAGE_PATTERN =
            Pattern.compile("§e(.+) has been removed from your friends!");
    private static final Pattern FRIEND_ADD_MESSAGE_PATTERN = Pattern.compile("§e(.+) has been added to your friends!");

    // https://regexr.com/7ihc9
    private static final Pattern ONLINE_FRIENDS_HEADER = Pattern.compile("§2Online §aFriends:");
    // https://regexr.com/7ihcc
    private static final Pattern ONLINE_FRIEND = Pattern.compile("§2 - §a(\\w{1,16})§2 \\[Server: §aWC(\\d{1,3})§2]");

    // https://regexr.com/7ihci
    private static final Pattern JOIN_PATTERN = Pattern.compile(
            "§a(?<username>\\w{1,16})§2 has logged into server §aWC(?<server>\\d{1,3})§2 as §aan? (?<class>[A-Z][a-z]+)");
    // https://regexr.com/7ihcl
    private static final Pattern LEAVE_PATTERN = Pattern.compile("§a(?<username>\\w{1,16}) left the game\\.");
    // endregion

    private static final int REQUEST_RATELIMIT = 250;

    private boolean expectingFriendMessage = false;
    private long lastFriendRequest = 0;

    private boolean expectingOnlineList = false;
    private boolean processingOnlineList = false;
    private long lastOnlineRequest = 0;

    private Set<String> friends;
    private Map<String, Integer> onlineFriends = new HashMap<>(); // <username, server>

    public FriendsModel(WorldStateModel worldStateModel) {
        super(List.of(worldStateModel));
        resetData();
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

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onChatReceived(ChatMessageReceivedEvent event) {
        if (event.getMessageType() != MessageType.FOREGROUND) return;

        StyledText styledText = event.getOriginalStyledText();
        String unformatted = styledText.getStringWithoutFormatting();

        Matcher joinMatcher = styledText.getMatcher(JOIN_PATTERN);
        if (joinMatcher.matches()) {
            onlineFriends.put(joinMatcher.group("username"), Integer.parseInt(joinMatcher.group("server")));
            WynntilsMod.postEvent(new FriendsEvent.Joined(joinMatcher.group("username")));
            return;
        }
        Matcher leaveMatcher = styledText.getMatcher(LEAVE_PATTERN);
        if (leaveMatcher.matches()) {
            onlineFriends.remove(leaveMatcher.group("username"));
            WynntilsMod.postEvent(new FriendsEvent.Left(leaveMatcher.group("username")));
            return;
        }

        if (tryParseFriendMessages(styledText)) return;

        if (expectingFriendMessage) {
            if (tryParseFriendList(unformatted) || tryParseNoFriendList(styledText)) {
                event.setCanceled(true);
                expectingFriendMessage = false;
                return;
            }

            // Skip first message of two, but still expect more messages
            if (styledText.getMatcher(FRIEND_LIST_FAIL_1).matches()) {
                event.setCanceled(true);
                return;
            }
        }

        if (expectingOnlineList && styledText.getMatcher(ONLINE_FRIENDS_HEADER).matches()) {
            // List of online friends is sent in multiple messages
            // When we detect the first message indicating the start of the friends list, we set a flag
            processingOnlineList = true;
            expectingOnlineList = false;
            onlineFriends.clear();
            event.setCanceled(true);
            return;
        }
        if (processingOnlineList) {
            // If this flag is set, the next messages should be the list of online friends
            // But as soon as the matcher fails, we know we've reached the end of the list
            Matcher onlineFriendMatcher = styledText.getMatcher(ONLINE_FRIEND);
            if (onlineFriendMatcher.matches()) {
                onlineFriends.put(onlineFriendMatcher.group(1), Integer.parseInt(onlineFriendMatcher.group(2)));
                WynntilsMod.info(
                        "Friend " + onlineFriendMatcher.group(1) + " is online on WC" + onlineFriendMatcher.group(2));
                event.setCanceled(true);
                return;
            } else {
                WynntilsMod.postEvent(new FriendsEvent.OnlineListed());
                processingOnlineList = false;
            }
        }
    }

    private boolean tryParseNoFriendList(StyledText styledText) {
        if (styledText.getMatcher(FRIEND_LIST_FAIL_2).matches()) {
            WynntilsMod.info("Friend list is empty.");
            return true;
        }

        return false;
    }

    private boolean tryParseFriendMessages(StyledText styledText) {
        Matcher matcher = styledText.getMatcher(FRIEND_REMOVE_MESSAGE_PATTERN);
        if (matcher.matches()) {
            String player = matcher.group(1);

            WynntilsMod.info("Player has removed friend: " + player);

            friends.remove(player);
            WynntilsMod.postEvent(new HadesRelationsUpdateEvent.FriendList(
                    Set.of(player), HadesRelationsUpdateEvent.ChangeType.REMOVE));
            WynntilsMod.postEvent(new FriendsEvent.Removed(player));
            return true;
        }

        matcher = styledText.getMatcher(FRIEND_ADD_MESSAGE_PATTERN);
        if (matcher.matches()) {
            String player = matcher.group(1);

            WynntilsMod.info("Player has added friend: " + player);

            friends.add(player);
            WynntilsMod.postEvent(
                    new HadesRelationsUpdateEvent.FriendList(Set.of(player), HadesRelationsUpdateEvent.ChangeType.ADD));
            WynntilsMod.postEvent(new FriendsEvent.Added(player));
            return true;
        }

        return false;
    }

    private boolean tryParseFriendList(String unformatted) {
        Matcher matcher = FRIEND_LIST.matcher(unformatted);
        if (!matcher.matches()) return false;

        String[] friendList = matcher.group(1).split(", ");

        friends = Arrays.stream(friendList).collect(Collectors.toSet());
        WynntilsMod.postEvent(
                new HadesRelationsUpdateEvent.FriendList(friends, HadesRelationsUpdateEvent.ChangeType.RELOAD));
        WynntilsMod.postEvent(new FriendsEvent.Listed());

        WynntilsMod.info("Successfully updated friend list, user has " + friendList.length + " friends.");
        return true;
    }

    private void resetData() {
        friends = new HashSet<>();
        onlineFriends = new HashMap<>();

        WynntilsMod.postEvent(
                new HadesRelationsUpdateEvent.FriendList(friends, HadesRelationsUpdateEvent.ChangeType.RELOAD));
    }

    /**
     * Sends "/friend list" to Wynncraft and waits for the response.
     * (!) Skips if the last request was less than 250ms ago.
     * When the response is received, friends will be updated.
     */
    public void requestData() {
        if (McUtils.player() == null) return;

        if (System.currentTimeMillis() - lastFriendRequest > REQUEST_RATELIMIT) {
            expectingFriendMessage = true;
            lastFriendRequest = System.currentTimeMillis();
            McUtils.sendCommand("friend list");
            WynntilsMod.info("Requested friend list from Wynncraft.");
        } else {
            WynntilsMod.info("Skipping friend list request because it was requested very recently.");
        }

        if (System.currentTimeMillis() - lastOnlineRequest > REQUEST_RATELIMIT) {
            expectingOnlineList = true;
            lastOnlineRequest = System.currentTimeMillis();
            McUtils.sendCommand("friend online");
            WynntilsMod.info("Requested online friend list from Wynncraft.");
        } else {
            WynntilsMod.info("Skipping online friend list request because it was requested very recently.");
        }
    }

    public boolean isFriend(String playerName) {
        return friends.contains(playerName);
    }

    public Set<String> getFriends() {
        return Collections.unmodifiableSet(friends);
    }

    public Map<String, Integer> getOnlineFriends() {
        return Collections.unmodifiableMap(onlineFriends);
    }
}
