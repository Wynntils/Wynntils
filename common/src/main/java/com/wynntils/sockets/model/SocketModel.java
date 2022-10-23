/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.sockets.model;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.managers.Model;
import com.wynntils.core.webapi.WebManager;
import com.wynntils.features.user.SocketFeature;
import com.wynntils.hades.objects.HadesConnection;
import com.wynntils.hades.protocol.builders.HadesNetworkBuilder;
import com.wynntils.hades.protocol.enums.PacketAction;
import com.wynntils.hades.protocol.enums.PacketDirection;
import com.wynntils.hades.protocol.enums.SocialType;
import com.wynntils.hades.protocol.packets.client.HCPacketSocialUpdate;
import com.wynntils.hades.protocol.packets.client.HCPacketUpdateStatus;
import com.wynntils.hades.protocol.packets.client.HCPacketUpdateWorld;
import com.wynntils.mc.event.ClientTickEvent;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.sockets.SocketClientHandler;
import com.wynntils.sockets.objects.PlayerStatus;
import com.wynntils.wynn.event.CharacterUpdateEvent;
import com.wynntils.wynn.event.RelationsUpdateEvent;
import com.wynntils.wynn.event.WorldStateEvent;
import com.wynntils.wynn.model.ActionBarModel;
import com.wynntils.wynn.model.CharacterManager;
import com.wynntils.wynn.model.WorldStateManager;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class SocketModel extends Model {
    private static final int TICKS_PER_UPDATE = 5;

    private static HadesConnection hadesConnection;
    private static int tickCountUntilUpdate = 0;
    private static PlayerStatus lastSentStatus = null;

    public static void init() {
        tryCreateConnection();
    }

    public static void disable() {
        tryDisconnect();
    }

    private static void tryCreateConnection() {
        if (WebManager.getAccount().isEmpty()) {
            WynntilsMod.error("Cannot connect to HadesServer when WebManager does not have account.");
            return;
        }

        try {
            hadesConnection = new HadesNetworkBuilder()
                    .setAddress(InetAddress.getByName("io.wynntils.com"), 9000)
                    .setDirection(PacketDirection.SERVER)
                    .setCompressionThreshold(256)
                    .setHandlerFactory(SocketClientHandler::new)
                    .buildClient();

            tickCountUntilUpdate = 0;
            lastSentStatus = null;
        } catch (UnknownHostException e) {
            WynntilsMod.error("Could not resolve Hades host address.", e);
        }
    }

    private static void tryDisconnect() {
        if (hadesConnection != null && hadesConnection.isOpen()) {
            hadesConnection.disconnect();
        }
    }

    @SubscribeEvent
    public static void onFriendListUpdate(RelationsUpdateEvent.FriendList event) {
        if (!SocketFeature.INSTANCE.shareWithFriends || !isSocketOpen()) return;

        hadesConnection.sendPacket(new HCPacketSocialUpdate(
                event.getChangedPlayers().stream().toList(),
                event.getChangeType().getPacketAction(),
                SocialType.FRIEND));
    }

    @SubscribeEvent
    public static void onPartyListUpdate(RelationsUpdateEvent.PartyList event) {
        if (!SocketFeature.INSTANCE.shareWithParty || !isSocketOpen()) return;

        hadesConnection.sendPacket(new HCPacketSocialUpdate(
                event.getChangedPlayers().stream().toList(),
                event.getChangeType().getPacketAction(),
                SocialType.PARTY));
    }

    @SubscribeEvent
    public static void onWorldStateChange(WorldStateEvent event) {
        if (!isSocketOpen()) return;

        hadesConnection.sendPacket(new HCPacketUpdateWorld(
                event.getWorldName(), CharacterManager.getCharacterInfo().getId()));
    }

    @SubscribeEvent
    public static void onClassChange(CharacterUpdateEvent event) {
        if (!isSocketOpen()) return;

        hadesConnection.sendPacket(new HCPacketUpdateWorld(
                WorldStateManager.getCurrentWorldName(),
                CharacterManager.getCharacterInfo().getId()));
    }

    @SubscribeEvent
    public static void onTick(ClientTickEvent.End event) {
        if (!isSocketOpen()) return;
        if (!WorldStateManager.onWorld()) return;
        if (!SocketFeature.INSTANCE.shareWithParty
                && !SocketFeature.INSTANCE.shareWithGuild
                && !SocketFeature.INSTANCE.shareWithFriends) return;

        tickCountUntilUpdate--;

        if (tickCountUntilUpdate <= 0) {
            LocalPlayer player = McUtils.player();

            float pX = (float) player.getX();
            float pY = (float) player.getY();
            float pZ = (float) player.getZ();

            if (lastSentStatus != null
                    && lastSentStatus.equals(
                            pX,
                            pY,
                            pZ,
                            ActionBarModel.getCurrentHealth(),
                            ActionBarModel.getMaxHealth(),
                            ActionBarModel.getCurrentMana(),
                            ActionBarModel.getMaxMana())) {
                tickCountUntilUpdate = 1;
                return;
            }

            tickCountUntilUpdate = TICKS_PER_UPDATE;

            lastSentStatus = new PlayerStatus(
                    pX,
                    pY,
                    pZ,
                    ActionBarModel.getCurrentHealth(),
                    ActionBarModel.getMaxHealth(),
                    ActionBarModel.getCurrentMana(),
                    ActionBarModel.getMaxMana());

            hadesConnection.sendPacket(new HCPacketUpdateStatus(
                    lastSentStatus.x(),
                    lastSentStatus.y(),
                    lastSentStatus.z(),
                    lastSentStatus.health(),
                    lastSentStatus.maxHealth(),
                    lastSentStatus.mana(),
                    lastSentStatus.maxMana()));
        }
    }

    public static void resetSocialType(SocialType socialType) {
        if (!isSocketOpen()) return;

        hadesConnection.sendPacket(new HCPacketSocialUpdate(List.of(), PacketAction.RESET, socialType));
    }

    private static boolean isSocketOpen() {
        return hadesConnection != null && hadesConnection.isOpen();
    }
}
