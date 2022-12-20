/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.sockets.model;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.managers.Managers;
import com.wynntils.core.managers.Model;
import com.wynntils.core.managers.Models;
import com.wynntils.features.user.HadesFeature;
import com.wynntils.hades.objects.HadesConnection;
import com.wynntils.hades.protocol.builders.HadesNetworkBuilder;
import com.wynntils.hades.protocol.enums.PacketAction;
import com.wynntils.hades.protocol.enums.PacketDirection;
import com.wynntils.hades.protocol.enums.SocialType;
import com.wynntils.hades.protocol.packets.client.HCPacketPing;
import com.wynntils.hades.protocol.packets.client.HCPacketSocialUpdate;
import com.wynntils.hades.protocol.packets.client.HCPacketUpdateStatus;
import com.wynntils.hades.protocol.packets.client.HCPacketUpdateWorld;
import com.wynntils.mc.event.ClientTickEvent;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.sockets.HadesClientHandler;
import com.wynntils.sockets.events.SocketEvent;
import com.wynntils.sockets.objects.PlayerStatus;
import com.wynntils.wynn.event.AthenaLoginEvent;
import com.wynntils.wynn.event.CharacterUpdateEvent;
import com.wynntils.wynn.event.RelationsUpdateEvent;
import com.wynntils.wynn.event.WorldStateEvent;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class HadesModel extends Model {
    private static final int TICKS_PER_UPDATE = 5;
    private static final int MS_PER_PING = 1000;

    private HadesConnection hadesConnection;
    private int tickCountUntilUpdate = 0;
    private PlayerStatus lastSentStatus;
    private ScheduledExecutorService pingScheduler;

    @Override
    public void init() {
        if (Managers.WynntilsAccount.isLoggedIn()) {
            tryCreateConnection();
        }
    }

    @Override
    public void disable() {
        tryDisconnect();
    }

    @SubscribeEvent
    public void onAthenaLoginEvent(AthenaLoginEvent event) {
        // Try to log in to Hades, if we're not already connected
        if (hadesConnection == null || !hadesConnection.isOpen()) {
            tryCreateConnection();
        }
    }

    private void tryCreateConnection() {
        try {
            hadesConnection = new HadesNetworkBuilder()
                    .setAddress(InetAddress.getByName("io.wynntils.com"), 9000)
                    .setDirection(PacketDirection.SERVER)
                    .setCompressionThreshold(256)
                    .setHandlerFactory(HadesClientHandler::new)
                    .buildClient();

            tickCountUntilUpdate = 0;
            lastSentStatus = null;
        } catch (UnknownHostException e) {
            WynntilsMod.error("Could not resolve Hades host address.", e);
        }
    }

    private void tryDisconnect() {
        if (hadesConnection != null && hadesConnection.isOpen()) {
            hadesConnection.disconnect();
        }
    }

    @SubscribeEvent
    public void onAuth(SocketEvent.Authenticated event) {
        pingScheduler = Executors.newSingleThreadScheduledExecutor();
        pingScheduler.scheduleAtFixedRate(this::sendPing, 0, MS_PER_PING, TimeUnit.MILLISECONDS);
    }

    @SubscribeEvent
    public void onDisconnect(SocketEvent.Disconnected event) {
        pingScheduler.shutdown();
    }

    private void sendPing() {
        if (!isSocketOpen()) return;

        hadesConnection.sendPacketAndFlush(new HCPacketPing(System.currentTimeMillis()));
    }

    @SubscribeEvent
    public void onFriendListUpdate(RelationsUpdateEvent.FriendList event) {
        if (!HadesFeature.INSTANCE.shareWithFriends || !isSocketOpen()) return;

        hadesConnection.sendPacket(new HCPacketSocialUpdate(
                event.getChangedPlayers().stream().toList(),
                event.getChangeType().getPacketAction(),
                SocialType.FRIEND));
    }

    @SubscribeEvent
    public void onPartyListUpdate(RelationsUpdateEvent.PartyList event) {
        if (!HadesFeature.INSTANCE.shareWithParty || !isSocketOpen()) return;

        hadesConnection.sendPacket(new HCPacketSocialUpdate(
                event.getChangedPlayers().stream().toList(),
                event.getChangeType().getPacketAction(),
                SocialType.PARTY));
    }

    @SubscribeEvent
    public void onWorldStateChange(WorldStateEvent event) {
        tryResendWorldData();
    }

    @SubscribeEvent
    public void onClassChange(CharacterUpdateEvent event) {
        tryResendWorldData();
    }

    @SubscribeEvent
    public void onTick(ClientTickEvent.End event) {
        if (!isSocketOpen()) return;
        if (!Managers.WorldState.onWorld() || McUtils.player().hasEffect(MobEffects.NIGHT_VISION)) return;
        if (!HadesFeature.INSTANCE.shareWithParty
                && !HadesFeature.INSTANCE.shareWithGuild
                && !HadesFeature.INSTANCE.shareWithFriends) return;

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
                            Models.ActionBar.getCurrentHealth(),
                            Models.ActionBar.getMaxHealth(),
                            Models.ActionBar.getCurrentMana(),
                            Models.ActionBar.getMaxMana())) {
                tickCountUntilUpdate = 1;
                return;
            }

            tickCountUntilUpdate = TICKS_PER_UPDATE;

            lastSentStatus = new PlayerStatus(
                    pX,
                    pY,
                    pZ,
                    Models.ActionBar.getCurrentHealth(),
                    Models.ActionBar.getMaxHealth(),
                    Models.ActionBar.getCurrentMana(),
                    Models.ActionBar.getMaxMana());

            hadesConnection.sendPacketAndFlush(new HCPacketUpdateStatus(
                    lastSentStatus.x(),
                    lastSentStatus.y(),
                    lastSentStatus.z(),
                    lastSentStatus.health(),
                    lastSentStatus.maxHealth(),
                    lastSentStatus.mana(),
                    lastSentStatus.maxMana()));
        }
    }

    public void tryResendWorldData() {
        if (!isSocketOpen()) return;

        hadesConnection.sendPacket(new HCPacketUpdateWorld(
                Managers.WorldState.getCurrentWorldName(),
                Managers.Character.getCharacterInfo().getId()));
    }

    public void resetSocialType(SocialType socialType) {
        if (!isSocketOpen()) return;

        hadesConnection.sendPacketAndFlush(new HCPacketSocialUpdate(List.of(), PacketAction.RESET, socialType));
    }

    private boolean isSocketOpen() {
        return hadesConnection != null && hadesConnection.isOpen();
    }
}
