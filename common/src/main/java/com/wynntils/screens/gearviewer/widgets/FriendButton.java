package com.wynntils.screens.gearviewer.widgets;

import com.wynntils.core.components.Handlers;
import com.wynntils.models.players.event.FriendsEvent;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;

public class FriendButton extends PlayerInteractionButton {
    private final String playerName;
    private boolean isAlreadyFriend = false;

    public FriendButton(int x, int y, String playerName) {
        super(x, y, Component.translatable("screens.wynntils.gearViewer.addFriend"), Component.literal("F"));
        this.playerName = playerName;
    }

    @Override
    public void onPress() {
        super.onPress();
        Handlers.Command.queueCommand("friend " + (isAlreadyFriend ? "remove" : "add") + " " + playerName);
    }

    @SubscribeEvent
    public void onFriendAdd(FriendsEvent.Added e) {
        isAlreadyFriend = e.getPlayerName().equals(playerName);
    }

    @SubscribeEvent
    public void onFriendRemove(FriendsEvent.Removed e) {
        if (e.getPlayerName().equals(playerName)) {
            isAlreadyFriend = false;
        }
    }
}
