package com.wynntils.screens.gearviewer.widgets;

import com.wynntils.core.components.Handlers;
import net.minecraft.network.chat.Component;

public class PartyButton extends PlayerInteractionButton {
    private final String playerName;

    public PartyButton(int x, int y, String playerName) {
        super(x, y, Component.translatable("screens.wynntils.gearViewer.inviteParty"), Component.literal("P"));
        this.playerName = playerName;
    }

    @Override
    public void onPress() {
        super.onPress();
        Handlers.Command.queueCommand("party " + playerName);
    }
}
