/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.managers.Model;
import com.wynntils.mc.event.ChatPacketReceivedEvent;
import com.wynntils.mc.utils.ComponentUtils;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.wynn.event.FriendListUpdateEvent;
import com.wynntils.wynn.event.WorldStateEvent;
import java.util.Arrays;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import net.minecraft.network.chat.ChatType;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * This model handles the player's in-game relations, like friends, party info, guild info.
 */
public class PlayerRelationsModel extends Model {
    private static final Pattern FRIEND_LIST_MESSAGE_PATTERN = Pattern.compile(".+'s friends \\(.+\\): (.*)");
    private static final Pattern FRIEND_REMOVE_MESSAGE_PATTERN =
            Pattern.compile("§e(.+) has been removed from your friends!");
    private static final Pattern FRIEND_ADD_MESSAGE_PATTERN = Pattern.compile("§e(.+) has been added to your friends!");

    private static boolean expectingFriendMessage = false;

    private static Set<String> friends;

    public static void init() {
        friends = Set.of();

        if (WorldStateManager.onServer()) {
            tryGetFriendList();
        }
    }

    public static void disable() {
        friends = Set.of();
    }

    @SubscribeEvent
    public static void onChatReceived(ChatPacketReceivedEvent e) {
        if (e.getType() != ChatType.SYSTEM) return;

        if (expectingFriendMessage) {
            String unformatted = ComponentUtils.getUnformatted(e.getMessage());
            Matcher matcher = FRIEND_LIST_MESSAGE_PATTERN.matcher(unformatted);
            if (!matcher.matches()) return;
            e.setCanceled(true);

            String[] friendList = matcher.group(1).split(", ");

            friends = Arrays.stream(friendList).collect(Collectors.toSet());
            expectingFriendMessage = false;
            WynntilsMod.postEvent(new FriendListUpdateEvent(friends, FriendListUpdateEvent.ChangeType.RELOAD));

            WynntilsMod.info("Successfully updated friend list, user has " + friendList.length + " friends.");
            return;
        }

        String coded = ComponentUtils.getCoded(e.getMessage());
        Matcher matcher = FRIEND_REMOVE_MESSAGE_PATTERN.matcher(coded);
        if (matcher.matches()) {
            String player = matcher.group(1);
            friends.remove(player);
            WynntilsMod.postEvent(new FriendListUpdateEvent(Set.of(player), FriendListUpdateEvent.ChangeType.REMOVE));
            return;
        }

        matcher = FRIEND_ADD_MESSAGE_PATTERN.matcher(coded);
        if (matcher.matches()) {
            String player = matcher.group(1);
            friends.add(player);
            WynntilsMod.postEvent(new FriendListUpdateEvent(Set.of(player), FriendListUpdateEvent.ChangeType.ADD));
            return;
        }
    }

    @SubscribeEvent
    public static void onWorldStateChange(WorldStateEvent event) {
        if (event.getNewState() == WorldStateManager.State.WORLD) {
            tryGetFriendList();
        }
    }

    private static void tryGetFriendList() {
        expectingFriendMessage = true;
        McUtils.player().chat("/friend list");
    }

    public static Set<String> getFriends() {
        return friends;
    }
}
