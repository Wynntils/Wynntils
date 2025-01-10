package com.wynntils.screens.gearviewer.widgets;

import com.wynntils.core.components.Handlers;
import com.wynntils.models.players.event.PartyEvent;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;

public class PartyButton extends PlayerInteractionButton {
    private final String playerName;
    private boolean isAlreadyParty = false;

    public PartyButton(int x, int y, String playerName) {
        super(x, y, Component.translatable("screens.wynntils.gearViewer.inviteParty"), Component.literal("P"));
        this.playerName = playerName;
    }

    @Override
    public void onPress() {
        super.onPress();
        Handlers.Command.queueCommand("party " + (isAlreadyParty ? "kick " : "") + playerName);
    }

    @SubscribeEvent
    public void onPartyInvite(PartyEvent.Invited e) {
        isAlreadyParty = e.getPlayerName().equals(playerName);
    }

    @SubscribeEvent
    public void onPartyKick(PartyEvent.OtherLeft e) {
        if (e.getPlayerName().equals(playerName)) {
            isAlreadyParty = false;
        }
    }
}
