package com.wynntils.screens.gearviewer.widgets;

import com.wynntils.core.components.Handlers;
import com.wynntils.core.components.Models;
import net.minecraft.network.chat.Component;

import java.util.List;

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
        Handlers.Command.queueCommand("friend " + (Models.Friends.isFriend(playerName) ? "remove " : "add ") + playerName);
    }

    public void updateIcon() {
        boolean isFriend = Models.Friends.isFriend(playerName);
        this.setMessage(Component.literal(isFriend ? "-F" : "+F"));
        this.tooltipText = List.of(Component.translatable("screens.wynntils.gearViewer." + (isFriend ? "removeFriend" : "addFriend")));
    }
}
