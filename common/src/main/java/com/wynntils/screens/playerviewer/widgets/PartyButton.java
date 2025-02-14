/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.playerviewer.widgets;

import com.wynntils.core.components.Handlers;
import com.wynntils.core.components.Models;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.Texture;
import java.util.List;
import net.minecraft.network.chat.Component;

public class PartyButton extends PlayerInteractionButton {
    private final String playerName;

    public PartyButton(int x, int y, String playerName) {
        super(x, y);
        this.playerName = playerName; // Must be run before updateIcon to ensure name is set
        updateIcon();
    }

    @Override
    public void onPress() {
        super.onPress();
        Handlers.Command.queueCommand(
                "party " + (Models.Party.getPartyMembers().contains(playerName) ? "kick " : "") + playerName);
    }

    public void updateIcon() {
        // only allow button press if we can perform the action
        this.active = !Models.Party.isInParty() || Models.Party.isPartyLeader(McUtils.playerName());
        boolean isPartyMember = Models.Party.getPartyMembers().contains(playerName);
        this.icon = isPartyMember ? Texture.PARTY_KICK_ICON : Texture.PARTY_INVITE_ICON;
        this.tooltipText = List.of(Component.translatable(
                "screens.wynntils.playerViewer." + (isPartyMember ? "kickParty" : "inviteParty")));
    }
}
