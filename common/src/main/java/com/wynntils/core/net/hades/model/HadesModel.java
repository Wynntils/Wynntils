/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.net.hades.model;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.core.net.hades.HadesClientHandler;
import com.wynntils.core.net.hades.event.HadesEvent;
import com.wynntils.core.net.hades.objects.PlayerStatus;
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
import com.wynntils.wynn.event.CharacterUpdateEvent;
import com.wynntils.wynn.event.RelationsUpdateEvent;
import com.wynntils.wynn.event.WorldStateEvent;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import net.minecraft.ChatFormatting;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
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

        Managers.WynntilsAccount.onLoginRun(this::onLogin);
    }

    @Override
    public void disable() {
        tryDisconnect();

        Managers.WynntilsAccount.removeOnLogin(this::onLogin);
    }

    public void onLogin() {
        // Try to log in to Hades, if we're not already connected
        if (!isConnected()) {
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

    public void tryDisconnect() {
        if (hadesConnection != null && hadesConnection.isOpen()) {
            hadesConnection.disconnect();
        }
    }

    @SubscribeEvent
    public void onAuth(HadesEvent.Authenticated event) {
        pingScheduler = Executors.newSingleThreadScheduledExecutor();
        pingScheduler.scheduleAtFixedRate(this::sendPing, 0, MS_PER_PING, TimeUnit.MILLISECONDS);
    }

    @SubscribeEvent
    public void onDisconnect(HadesEvent.Disconnected event) {
        pingScheduler.shutdown();
    }

    private void sendPing() {
        if (!isConnected()) return;

        hadesConnection.sendPacketAndFlush(new HCPacketPing(System.currentTimeMillis()));
    }

    @SubscribeEvent
    public void onFriendListUpdate(RelationsUpdateEvent.FriendList event) {
        if (!HadesFeature.INSTANCE.shareWithFriends || !isConnected()) return;

        hadesConnection.sendPacket(new HCPacketSocialUpdate(
                event.getChangedPlayers().stream().toList(),
                event.getChangeType().getPacketAction(),
                SocialType.FRIEND));
    }

    @SubscribeEvent
    public void onPartyListUpdate(RelationsUpdateEvent.PartyList event) {
        if (!HadesFeature.INSTANCE.shareWithParty || !isConnected()) return;

        hadesConnection.sendPacket(new HCPacketSocialUpdate(
                event.getChangedPlayers().stream().toList(),
                event.getChangeType().getPacketAction(),
                SocialType.PARTY));
    }

    @SubscribeEvent
    public void onWorldStateChange(WorldStateEvent event) {
        if (event.isFirstJoinWorld()) {
            if (!isConnected()) {
                // FIXME: Use the proper reload command here, once they are reworked
                MutableComponent failed = Component.literal("Welps! Trying to connect to Hades failed.")
                        .withStyle(ChatFormatting.GREEN);
                failed.append(Component.literal("/wynntils reload")
                        .withStyle(Style.EMPTY
                                .withColor(ChatFormatting.AQUA)
                                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/wynntils reload"))));

                McUtils.sendMessageToClient(failed);

                return;
            }
        }

        tryResendWorldData();
    }

    @SubscribeEvent
    public void onClassChange(CharacterUpdateEvent event) {
        tryResendWorldData();
    }

    @SubscribeEvent
    public void onTick(ClientTickEvent.End event) {
        if (!isConnected()) return;
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
        if (!isConnected()) return;

        hadesConnection.sendPacket(
                new HCPacketUpdateWorld(Managers.WorldState.getCurrentWorldName(), Managers.Character.getId()));
    }

    public void resetSocialType(SocialType socialType) {
        if (!isConnected()) return;

        hadesConnection.sendPacketAndFlush(new HCPacketSocialUpdate(List.of(), PacketAction.RESET, socialType));
    }

    private boolean isConnected() {
        return hadesConnection != null && hadesConnection.isOpen();
    }
}
