package com.wynntils.screens.gearviewer.widgets;

import com.wynntils.core.components.Handlers;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;

public class FriendButton extends PlayerInteractionButton {
    private final String playerName;

    public FriendButton(int x, int y, String playerName) {
        super(x, y, Component.translatable("screens.wynntils.gearViewer.addFriend"), Component.literal("F"));
        this.playerName = playerName;
    }

    @Override
    public void onPress() {
        super.onPress();
        Handlers.Command.queueCommand("friend add " + playerName);
    }

    @SubscribeEvent
    public void onFriendAdd(String playerName) {
        Handlers.Command.queueCommand("friend add " + playerName);
    }
}
