/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.sockets;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.webapi.WebManager;
import com.wynntils.features.user.SocketFeature;
import com.wynntils.hades.objects.HadesConnection;
import com.wynntils.hades.protocol.interfaces.adapters.IHadesClientAdapter;
import com.wynntils.hades.protocol.packets.client.HCPacketAuthenticate;
import com.wynntils.hades.protocol.packets.server.HSPacketAuthenticationResponse;
import com.wynntils.hades.protocol.packets.server.HSPacketClearMutual;
import com.wynntils.hades.protocol.packets.server.HSPacketDisconnect;
import com.wynntils.hades.protocol.packets.server.HSPacketDiscordLobbyServer;
import com.wynntils.hades.protocol.packets.server.HSPacketUpdateMutual;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.sockets.model.HadesUserModel;
import com.wynntils.sockets.objects.HadesUser;
import com.wynntils.wynn.model.WorldStateManager;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

public class SocketClientHandler implements IHadesClientAdapter {
    private final HadesConnection hadesConnection;

    public SocketClientHandler(HadesConnection hadesConnection) {
        this.hadesConnection = hadesConnection;
    }

    @Override
    public void onConnect() {
        if (WebManager.getAccount().isEmpty()) {
            hadesConnection.disconnect();

            if (WorldStateManager.onServer()) {
                McUtils.sendMessageToClient(
                        new TextComponent("Could not connect to HadesServer because you are not connected to Athena.")
                                .withStyle(ChatFormatting.RED));
            }

            throw new IllegalStateException("Tried to auth to HadesServer when WebManager#getAccount is empty.");
        }

        //        hadesConnection.sendPacket(new HCPacketAuthenticate(WebManager.getAccount().get().getToken()));

        if (WynntilsMod.isDevelopmentEnvironment()) {
            hadesConnection.sendPacket(new HCPacketAuthenticate("84dbc8e5-aa32-4976-ae19-062ba4443b1d"));
        } else {
            hadesConnection.sendPacket(new HCPacketAuthenticate("84dbc8e5-bb32-4976-ae19-062ba4443b1d"));
        }
    }

    @Override
    public void onDisconnect() {
        if (WorldStateManager.onServer()) {
            McUtils.sendMessageToClient(
                    new TextComponent("Disconnected from HadesServer.").withStyle(ChatFormatting.RED));
        }
        WynntilsMod.info("Disconnected from HadesServer.");

        HadesUserModel.getHadesUserMap().clear();
    }

    @Override
    public void handleAuthenticationResponse(HSPacketAuthenticationResponse packet) {
        Component userComponent = TextComponent.EMPTY;

        switch (packet.getResponse()) {
            case SUCCESS -> {
                WynntilsMod.info("Successfully connected to HadesServer: " + packet.getMessage());
                userComponent =
                        new TextComponent("Successfully connected to HadesServer").withStyle(ChatFormatting.RED);
            }
            case INVALID_TOKEN -> {
                WynntilsMod.error("Got invalid token when trying to connect to HadesServer: " + packet.getMessage());
                userComponent = new TextComponent("Got invalid token when connecting HadesServer")
                        .withStyle(ChatFormatting.RED);
            }
            case ERROR -> {
                WynntilsMod.error("Got an error trying to connect to HadesServer: " + packet.getMessage());
                userComponent =
                        new TextComponent("Got error when connecting HadesServer").withStyle(ChatFormatting.RED);
            }
        }

        if (WorldStateManager.onServer()) {
            McUtils.sendMessageToClient(userComponent);
        }
    }

    @Override
    public void handleUpdateMutual(HSPacketUpdateMutual packet) {
        if (!SocketFeature.INSTANCE.getOtherPlayerInfo) return;

        Optional<HadesUser> userOptional = HadesUserModel.getUser(packet.getUser());
        if (userOptional.isPresent()) {
            userOptional.get().updateFromPacket(packet);
        } else {
            HadesUserModel.putUser(packet.getUser(), new HadesUser(packet));
        }
    }

    @Override
    public void handleDiscordLobbyServer(HSPacketDiscordLobbyServer packet) {
        // noop for now
    }

    @Override
    public void handleClearMutual(HSPacketClearMutual packet) {
        HadesUserModel.removeUser(packet.getUser());
    }

    @Override
    public void handleDisconnect(HSPacketDisconnect packet) {
        WynntilsMod.info("Disconnected from HadesServer. Reason: " + packet.getReason());

        if (WorldStateManager.onServer()) {
            McUtils.sendMessageToClient(new TextComponent("[Wynntils/Artemis] Disconnected from HadesServer.")
                    .withStyle(ChatFormatting.YELLOW));
        }

        HadesUserModel.getHadesUserMap().clear();
    }
}
