/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.playerviewer.widgets;

import com.wynntils.core.components.Handlers;
import com.wynntils.core.components.Models;
import com.wynntils.utils.render.Texture;
import java.util.List;
import net.minecraft.network.chat.Component;

public class FriendButton extends PlayerInteractionButton {
    private final String playerName;

    public FriendButton(int x, int y, String playerName) {
        super(x, y);
        this.playerName = playerName; // Must be run before updateIcon to ensure name is set
        updateIcon();
    }

    @Override
    public void onPress() {
        super.onPress();
        Handlers.Command.queueCommand(
                "friend " + (Models.Friends.isFriend(playerName) ? "remove " : "add ") + playerName);
    }

    public void updateIcon() {
        boolean isFriend = Models.Friends.isFriend(playerName);
        this.icon = isFriend ? Texture.FRIEND_REMOVE_ICON : Texture.FRIEND_ADD_ICON;
        this.tooltipText = List.of(
                Component.translatable("screens.wynntils.playerViewer." + (isFriend ? "removeFriend" : "addFriend")));
    }
}
