/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.sockets;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.managers.Managers;
import com.wynntils.features.user.HadesFeature;
import com.wynntils.hades.objects.HadesConnection;
import com.wynntils.hades.protocol.interfaces.adapters.IHadesClientAdapter;
import com.wynntils.hades.protocol.packets.client.HCPacketAuthenticate;
import com.wynntils.hades.protocol.packets.server.HSPacketAuthenticationResponse;
import com.wynntils.hades.protocol.packets.server.HSPacketClearMutual;
import com.wynntils.hades.protocol.packets.server.HSPacketDisconnect;
import com.wynntils.hades.protocol.packets.server.HSPacketDiscordLobbyServer;
import com.wynntils.hades.protocol.packets.server.HSPacketPong;
import com.wynntils.hades.protocol.packets.server.HSPacketUpdateMutual;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.sockets.events.SocketEvent;
import com.wynntils.sockets.model.HadesUserModel;
import com.wynntils.sockets.objects.HadesUser;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

public class HadesClientHandler implements IHadesClientAdapter {
    private final HadesConnection hadesConnection;

    public HadesClientHandler(HadesConnection hadesConnection) {
        this.hadesConnection = hadesConnection;
    }

    @Override
    public void onConnect() {
        if (!Managers.WYNNTILS_ACCOUNT.isLoggedIn()) {
            hadesConnection.disconnect();

            if (Managers.WORLD_STATE.onServer()) {
                McUtils.sendMessageToClient(
                        new TextComponent("Could not connect to HadesServer because you are not logged in on Athena.")
                                .withStyle(ChatFormatting.RED));
            }

            throw new IllegalStateException("Tried to auth to HadesServer without being logged in on Athena.");
        }

        hadesConnection.sendPacketAndFlush(new HCPacketAuthenticate(Managers.WYNNTILS_ACCOUNT.getToken()));
    }

    @Override
    public void onDisconnect() {
        WynntilsMod.postEvent(new SocketEvent.Disconnected());

        if (Managers.WORLD_STATE.onServer()) {
            McUtils.sendMessageToClient(
                    new TextComponent("Disconnected from HadesServer").withStyle(ChatFormatting.RED));
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
                        new TextComponent("Successfully connected to HadesServer").withStyle(ChatFormatting.GREEN);
                WynntilsMod.postEvent(new SocketEvent.Authenticated());
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

        if (Managers.WORLD_STATE.onServer()) {
            McUtils.sendMessageToClient(userComponent);
        }
    }

    @Override
    public void handlePing(HSPacketPong packet) {
        // noop at the moment
        // todo eventually calculate ping
    }

    @Override
    public void handleUpdateMutual(HSPacketUpdateMutual packet) {
        if (!HadesFeature.INSTANCE.getOtherPlayerInfo) return;

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

        if (Managers.WORLD_STATE.onServer()) {
            McUtils.sendMessageToClient(new TextComponent("[Wynntils/Artemis] Disconnected from HadesServer.")
                    .withStyle(ChatFormatting.YELLOW));
        }

        HadesUserModel.getHadesUserMap().clear();
    }
}
