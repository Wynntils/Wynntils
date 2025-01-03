package com.wynntils.screens.gearviewer.widgets;

import com.wynntils.core.components.Handlers;
import net.minecraft.network.chat.Component;

public class AddFriendButton extends PlayerInteractionButton {
    private final String playerName;

    public AddFriendButton(int x, int y, String playerName) {
        super(x, y, Component.translatable("screens.wynntils.gearViewer.addFriend"), Component.literal("F"));
        this.playerName = playerName;
    }

    @Override
    public void onPress() {
        super.onPress();
        Handlers.Command.queueCommand("friend add " + playerName);
    }
}
