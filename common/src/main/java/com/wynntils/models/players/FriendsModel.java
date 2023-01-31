package com.wynntils.models.players;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.handlers.chat.MessageType;
import com.wynntils.handlers.chat.event.ChatMessageReceivedEvent;
import com.wynntils.models.players.event.FriendConnectionEvent;
import com.wynntils.models.players.event.RelationsUpdateEvent;
import com.wynntils.models.players.hades.event.HadesEvent;
import com.wynntils.models.worlds.WorldStateModel;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.models.worlds.type.WorldState;
import com.wynntils.utils.mc.ComponentUtils;
import com.wynntils.utils.mc.McUtils;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class FriendsModel extends Model {
    private static final Pattern FRIEND_LIST_MESSAGE_PATTERN = Pattern.compile(".+'s friends \\(.+\\): (.*)");
    private static final Pattern FRIEND_NO_LIST_MESSAGE_PATTERN_1 = Pattern.compile("§eWe couldn't find any friends\\.");
    private static final Pattern FRIEND_NO_LIST_MESSAGE_PATTERN_2 =
            Pattern.compile("§eTry typing §r§6/friend add Username§r§e!");
    private static final Pattern FRIEND_REMOVE_MESSAGE_PATTERN =
            Pattern.compile("§e(.+) has been removed from your friends!");
    private static final Pattern FRIEND_ADD_MESSAGE_PATTERN = Pattern.compile("§e(.+) has been added to your friends!");

    private static final Pattern JOIN_PATTERN = Pattern.compile(
            "(?:§a|§r§7)(?:§o)?(.+)§r(?:§2|§8(?:§o)?) has logged into server §r(?:§a|§7(?:§o)?)(?<server>.+)§r(?:§2|§8(?:§o)?) as (?:§r§a|§r§7(?:§o)?)an? (?<class>.+)");
    private static final Pattern LEAVE_PATTERN = Pattern.compile("(?:§a|§r§7)(.+) left the game\\.");

    private boolean expectingFriendMessage = false;

    private Set<String> friends;

    public FriendsModel(WorldStateModel worldStateModel) {
        super(List.of(worldStateModel));
        resetRelations();
    }

    @SubscribeEvent
    public void onAuth(HadesEvent.Authenticated event) {
        if (!Models.WorldState.onWorld()) return;

        requestFriendListUpdate();
    }

    @SubscribeEvent
    public void onWorldStateChange(WorldStateEvent event) {
        if (event.getNewState() == WorldState.WORLD) {
            requestFriendListUpdate();
        } else {
            resetRelations();
        }
    }

    @SubscribeEvent
    public void onChatReceived(ChatMessageReceivedEvent event) {
        if (event.getMessageType() != MessageType.FOREGROUND) return;

        String coded = event.getOriginalCodedMessage();
        String unformatted = ComponentUtils.stripFormatting(coded);

        Matcher joinMatcher = JOIN_PATTERN.matcher(coded);
        if (joinMatcher.matches()) {
            WynntilsMod.postEvent(new FriendConnectionEvent.Join(joinMatcher.group(1)));
        } else {
            Matcher leaveMatcher = LEAVE_PATTERN.matcher(coded);
            if (leaveMatcher.matches()) {
                WynntilsMod.postEvent(new FriendConnectionEvent.Leave(leaveMatcher.group(1)));
            }
        }

        if (tryParseFriendMessages(coded)) {
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
    }

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

    private void resetRelations() {
        friends = new HashSet<>();

        WynntilsMod.postEvent(new RelationsUpdateEvent.FriendList(friends, RelationsUpdateEvent.ChangeType.RELOAD));
    }

    public void requestFriendListUpdate() {
        if (McUtils.player() == null) return;

        expectingFriendMessage = true;
        McUtils.sendCommand("friend list");
        WynntilsMod.info("Requested friend list from Wynncraft.");
    }

    public Set<String> getFriends() {
        return friends;
    }
}
