/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.RootCommandNode;
import com.wynntils.mc.EventFactory;
import com.wynntils.mc.event.ChatPacketReceivedEvent;
import com.wynntils.mc.event.ChatSentEvent;
import com.wynntils.mc.event.CommandsPacketEvent;
import com.wynntils.mc.utils.McUtils;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.ClientRegistryLayer;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.LastSeenMessagesTracker;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.MessageSignatureCache;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.chat.RemoteChatSession;
import net.minecraft.network.chat.SignedMessageBody;
import net.minecraft.network.chat.SignedMessageChain;
import net.minecraft.network.chat.SignedMessageLink;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddPlayerPacket;
import net.minecraft.network.protocol.game.ClientboundCommandsPacket;
import net.minecraft.network.protocol.game.ClientboundContainerClosePacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerChatPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundResourcePackPacket;
import net.minecraft.network.protocol.game.ClientboundSetDefaultSpawnPositionPacket;
import net.minecraft.network.protocol.game.ClientboundSetExperiencePacket;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket;
import net.minecraft.network.protocol.game.ClientboundSetScorePacket;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.network.protocol.game.ClientboundTabListPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateAdvancementsPacket;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
import net.minecraft.network.protocol.game.ServerboundResourcePackPacket;
import net.minecraft.util.Crypt;
import net.minecraft.world.flag.FeatureFlagSet;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {
    @Shadow
    public abstract PlayerInfo getPlayerInfo(UUID uniqueId);

    @Shadow
    protected abstract void send(ServerboundResourcePackPacket.Action action);

    @Shadow
    public abstract void send(Packet<?> packet);

    @Shadow
    private CommandDispatcher<SharedSuggestionProvider> commands;

    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    public abstract FeatureFlagSet enabledFeatures();

    @Shadow
    private LayeredRegistryAccess<ClientRegistryLayer> registryAccess;

    @Shadow
    private MessageSignatureCache messageSignatureCache;

    @Shadow
    private SignedMessageChain.Encoder signedMessageEncoder;

    @Shadow
    private LastSeenMessagesTracker lastSeenMessages;

    private static boolean isRenderThread() {
        return McUtils.mc().isSameThread();
    }

    @Inject(method = "sendChat", at = @At("HEAD"), cancellable = true)
    private void onChatPre(String string, CallbackInfo ci) {
        ChatSentEvent result = EventFactory.onChatSent(string);
        if (result.isCanceled()) {
            ci.cancel();
        }

        if (!Objects.equals(string, result.getMessage())) {
            // Note: This code is taken from the MC source code. I don't think there is a sane way of not doing this,
            // unfortunately (in 1.19.2, there was, not in 1.19.3).
            //       For anyone concerned, this does not mess with NoChatReport at the time of writing.

            Instant instant = Instant.now();
            long l = Crypt.SaltSupplier.getLong();
            LastSeenMessagesTracker.Update update = this.lastSeenMessages.generateAndApplyUpdate();
            MessageSignature messageSignature =
                    this.signedMessageEncoder.pack(new SignedMessageBody(string, instant, l, update.lastSeen()));
            this.send(new ServerboundChatPacket(string, instant, l, messageSignature, update.update()));

            ci.cancel();
        }
    }

    @Inject(method = "sendCommand", at = @At("HEAD"), cancellable = true)
    private void onSignedCommandPre(String string, CallbackInfo ci) {
        if (EventFactory.onCommandSent(string, true).isCanceled()) {
            ci.cancel();
        }
    }

    @Inject(method = "sendUnsignedCommand", at = @At("HEAD"), cancellable = true)
    private void onUnsignedCommandPre(String command, CallbackInfoReturnable<Boolean> cir) {
        if (EventFactory.onCommandSent(command, false).isCanceled()) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }

    @Inject(
            method = "handleCommands(Lnet/minecraft/network/protocol/game/ClientboundCommandsPacket;)V",
            at = @At("HEAD"))
    private void handleCommandsPre(ClientboundCommandsPacket packet, CallbackInfo ci) {
        if (!isRenderThread()) return;
        RootCommandNode<SharedSuggestionProvider> root = packet.getRoot(
                CommandBuildContext.simple(this.registryAccess.compositeAccess(), this.enabledFeatures()));
        CommandsPacketEvent event = EventFactory.onCommandsPacket(root);
        if (event.getRoot() != root) {
            // We modified command root, so inject it
            this.commands = new CommandDispatcher<>(event.getRoot());
        }
    }

    @Inject(
            method = "handlePlayerInfo(Lnet/minecraft/network/protocol/game/ClientboundPlayerInfoPacket;)V",
            at = @At("RETURN"))
    private void handlePlayerInfoPost(ClientboundPlayerInfoPacket packet, CallbackInfo ci) {
        if (!isRenderThread()) return;
        EventFactory.onPlayerInfoPacket(packet);
    }

    @Inject(
            method = "handleTabListCustomisation(Lnet/minecraft/network/protocol/game/ClientboundTabListPacket;)V",
            at = @At("RETURN"))
    private void handleTabListCustomisationPost(ClientboundTabListPacket packet, CallbackInfo ci) {
        if (!isRenderThread()) return;
        EventFactory.onTabListCustomisation(packet);
    }

    @Inject(
            method = "handleResourcePack(Lnet/minecraft/network/protocol/game/ClientboundResourcePackPacket;)V",
            at = @At("HEAD"),
            cancellable = true)
    private void handleResourcePackPre(ClientboundResourcePackPacket packet, CallbackInfo ci) {
        if (EventFactory.onResourcePack(packet).isCanceled()) {
            this.send(ServerboundResourcePackPacket.Action.SUCCESSFULLY_LOADED);
            ci.cancel();
        }
    }

    @Inject(
            method = "handleMovePlayer(Lnet/minecraft/network/protocol/game/ClientboundPlayerPositionPacket;)V",
            at = @At("RETURN"))
    private void handleMovePlayerPost(ClientboundPlayerPositionPacket packet, CallbackInfo ci) {
        if (!isRenderThread()) return;
        EventFactory.onPlayerMove(packet);
    }

    @Inject(
            method = "handleOpenScreen(Lnet/minecraft/network/protocol/game/ClientboundOpenScreenPacket;)V",
            at = @At("HEAD"),
            cancellable = true)
    private void handleOpenScreenPre(ClientboundOpenScreenPacket packet, CallbackInfo ci) {
        if (!isRenderThread()) return;
        if (EventFactory.onOpenScreen(packet).isCanceled()) {
            ci.cancel();
        }
    }

    @Inject(
            method = "handleContainerClose(Lnet/minecraft/network/protocol/game/ClientboundContainerClosePacket;)V",
            at = @At("HEAD"),
            cancellable = true)
    private void handleContainerClosePre(ClientboundContainerClosePacket packet, CallbackInfo ci) {
        if (!isRenderThread()) return;
        if (EventFactory.onClientboundContainerClosePacket(packet.getContainerId())
                .isCanceled()) {
            ci.cancel();
        }
    }

    @Inject(
            method =
                    "handleContainerContent(Lnet/minecraft/network/protocol/game/ClientboundContainerSetContentPacket;)V",
            at = @At("HEAD"),
            cancellable = true)
    private void handleContainerContentPre(ClientboundContainerSetContentPacket packet, CallbackInfo ci) {
        if (!isRenderThread()) return;
        if (EventFactory.onContainerSetContentPre(packet).isCanceled()) {
            ci.cancel();
        }
    }

    @Inject(
            method =
                    "handleContainerContent(Lnet/minecraft/network/protocol/game/ClientboundContainerSetContentPacket;)V",
            at = @At("RETURN"))
    private void handleContainerContentPost(ClientboundContainerSetContentPacket packet, CallbackInfo ci) {
        if (!isRenderThread()) return;
        EventFactory.onContainerSetContentPost(packet);
    }

    @Inject(method = "handleContainerSetSlot", at = @At("HEAD"))
    private void handleContainerSetSlot(ClientboundContainerSetSlotPacket packet, CallbackInfo ci) {
        if (!isRenderThread()) return;
        EventFactory.onContainerSetSlot(packet);
    }

    @Inject(
            method = "handleSetPlayerTeamPacket(Lnet/minecraft/network/protocol/game/ClientboundSetPlayerTeamPacket;)V",
            at = @At("HEAD"),
            cancellable = true)
    private void handleSetPlayerTeamPacketPre(ClientboundSetPlayerTeamPacket packet, CallbackInfo ci) {
        if (!isRenderThread()) return;
        if (EventFactory.onSetPlayerTeam(packet).isCanceled()) {
            ci.cancel();
        }
    }

    @Inject(method = "handleSetExperience", at = @At("RETURN"))
    private void handleSetExperiencePost(ClientboundSetExperiencePacket packet, CallbackInfo ci) {
        if (!isRenderThread()) return;
        EventFactory.onSetXp(packet);
    }

    @Inject(
            method =
                    "handleSetEntityPassengersPacket(Lnet/minecraft/network/protocol/game/ClientboundSetPassengersPacket;)V",
            at = @At("HEAD"),
            cancellable = true)
    private void handleSetEntityPassengersPacketPre(ClientboundSetPassengersPacket packet, CallbackInfo ci) {
        if (!isRenderThread()) return;
        if (EventFactory.onSetEntityPassengers(packet).isCanceled()) {
            ci.cancel();
        }
    }

    @Inject(method = "handleSetSpawn", at = @At("HEAD"), cancellable = true)
    private void handleSetSpawnPre(ClientboundSetDefaultSpawnPositionPacket packet, CallbackInfo ci) {
        if (!isRenderThread()) return;
        if (EventFactory.onSetSpawn(packet.getPos()).isCanceled()) {
            ci.cancel();
        }
    }

    @Inject(
            method = "setTitleText(Lnet/minecraft/network/protocol/game/ClientboundSetTitleTextPacket;)V",
            at = @At("HEAD"),
            cancellable = true)
    private void setTitleTextPre(ClientboundSetTitleTextPacket packet, CallbackInfo ci) {
        if (!isRenderThread()) return;
        if (EventFactory.onTitleSetText(packet).isCanceled()) {
            ci.cancel();
        }
    }

    @Inject(
            method = "setSubtitleText(Lnet/minecraft/network/protocol/game/ClientboundSetSubtitleTextPacket;)V",
            at = @At("HEAD"),
            cancellable = true)
    private void setSubtitleTextPre(ClientboundSetSubtitleTextPacket packet, CallbackInfo ci) {
        if (!isRenderThread()) return;
        if (EventFactory.onSubtitleSetText(packet).isCanceled()) {
            ci.cancel();
        }
    }

    @Inject(
            method = "handlePlayerChat",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/multiplayer/chat/ChatListener;handlePlayerChatMessage(Lnet/minecraft/network/chat/PlayerChatMessage;Lcom/mojang/authlib/GameProfile;Lnet/minecraft/network/chat/ChatType$Bound;)V"),
            cancellable = true)
    private void handlePlayerChat(ClientboundPlayerChatPacket packet, CallbackInfo ci) {
        if (!isRenderThread()) return;
        ChatPacketReceivedEvent result =
                EventFactory.onChatReceived(com.wynntils.mc.objects.ChatType.CHAT, packet.unsignedContent());
        if (result.isCanceled()) {
            ci.cancel();
            return;
        }

        if (!result.getMessage().equals(packet.unsignedContent())) {
            // Because of the injection point, we know these optionals are present
            SignedMessageBody signedMessageBody =
                    packet.body().unpack(this.messageSignatureCache).get();
            ChatType.Bound bound = packet.chatType()
                    .resolve(this.registryAccess.compositeAccess())
                    .get();

            UUID uuid = packet.sender();
            PlayerInfo playerInfo = this.getPlayerInfo(uuid);
            RemoteChatSession remoteChatSession = playerInfo.getChatSession();

            SignedMessageLink signedMessageLink;
            if (remoteChatSession != null) {
                signedMessageLink = new SignedMessageLink(packet.index(), uuid, remoteChatSession.sessionId());
            } else {
                signedMessageLink = SignedMessageLink.unsigned(uuid);
            }

            PlayerChatMessage playerChatMessage = new PlayerChatMessage(
                    signedMessageLink,
                    packet.signature(),
                    signedMessageBody,
                    packet.unsignedContent(),
                    packet.filterMask());

            this.minecraft.getChatListener().handlePlayerChatMessage(playerChatMessage, playerInfo.getProfile(), bound);
            this.messageSignatureCache.push(playerChatMessage);

            ci.cancel();
        }
    }

    @Inject(
            method = "handleSystemChat",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/multiplayer/chat/ChatListener;handleSystemMessage(Lnet/minecraft/network/chat/Component;Z)V"),
            cancellable = true)
    private void handleSystemChat(ClientboundSystemChatPacket packet, CallbackInfo ci) {
        if (!isRenderThread()) return;
        ChatPacketReceivedEvent result = EventFactory.onChatReceived(
                packet.overlay() ? com.wynntils.mc.objects.ChatType.GAME_INFO : com.wynntils.mc.objects.ChatType.SYSTEM,
                packet.content());
        if (result.isCanceled()) {
            ci.cancel();
            return;
        }

        if (!result.getMessage().equals(packet.content())) {

            this.minecraft.getChatListener().handleSystemMessage(result.getMessage(), packet.overlay());
            ci.cancel();
        }
    }

    @Inject(method = "handleSetScore", at = @At("HEAD"), cancellable = true)
    private void handleSetScore(ClientboundSetScorePacket packet, CallbackInfo ci) {
        if (!isRenderThread()) return;
        if (EventFactory.onSetScore(packet).isCanceled()) {
            ci.cancel();
        }
    }

    @Inject(method = "onDisconnect(Lnet/minecraft/network/chat/Component;)V", at = @At("HEAD"))
    private void onDisconnectPre(Component reason, CallbackInfo ci) {
        // Unexpected disconnect
        EventFactory.onDisconnect();
    }

    @Inject(method = "handleAddPlayer", at = @At("HEAD"))
    private void handleAddPlayer(ClientboundAddPlayerPacket packet, CallbackInfo ci) {
        if (!isRenderThread()) return;
        EventFactory.onPlayerJoinedWorld(packet, this.getPlayerInfo(packet.getPlayerId()));
    }

    @Inject(method = "handleUpdateAdvancementsPacket", at = @At("RETURN"))
    private void handleUpdateAdvancementsPacket(ClientboundUpdateAdvancementsPacket packet, CallbackInfo ci) {
        if (!isRenderThread()) return;
        EventFactory.onUpdateAdvancements(packet);
    }
}
