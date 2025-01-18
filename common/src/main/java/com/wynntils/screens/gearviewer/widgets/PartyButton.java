package com.wynntils.screens.gearviewer.widgets;

import com.wynntils.core.components.Handlers;
import com.wynntils.core.components.Models;
import com.wynntils.models.players.event.PartyEvent;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;

import java.util.List;

public class PartyButton extends PlayerInteractionButton {
    private final String playerName;

    public PartyButton(int x, int y, String playerName) {
        super(x, y);
        updateIcon();
        this.playerName = playerName;
    }

    @Override
    public void onPress() {
        super.onPress();
        Handlers.Command.queueCommand("party " + (Models.Party.getPartyMembers().contains(playerName) ? "kick " : "") + playerName);
    }

    public void updateIcon() {
        boolean isParty = Models.Party.getPartyMembers().contains(playerName);
        this.setMessage(Component.literal(isParty ? "-P" : "+P"));
        this.tooltipText = List.of(Component.translatable("screens.wynntils.gearViewer." + (isParty ? "kickParty" : "inviteParty")));
    }
}
